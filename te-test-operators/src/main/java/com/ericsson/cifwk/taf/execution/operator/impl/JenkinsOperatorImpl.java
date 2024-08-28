package com.ericsson.cifwk.taf.execution.operator.impl;

import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.execution.TestConstants;
import com.ericsson.cifwk.taf.execution.operator.JenkinsOperator;
import com.ericsson.cifwk.taf.execution.operator.model.jenkins.Build;
import com.ericsson.cifwk.taf.execution.operator.model.jenkins.Jenkins;
import com.ericsson.cifwk.taf.execution.operator.model.jenkins.Job;
import com.ericsson.cifwk.taf.execution.operator.model.jenkins.Plugin;
import com.ericsson.cifwk.taf.execution.operator.model.jenkins.SchedulerJobConfig;
import com.ericsson.cifwk.taf.execution.operator.model.trigger.TriggerPluginDescriptor;
import com.ericsson.cifwk.taf.execution.utils.FileHelper;
import com.ericsson.cifwk.taf.executor.TimeoutException;
import com.ericsson.cifwk.taf.executor.utils.TimeLimitedTask;
import com.ericsson.cifwk.taf.executor.utils.TimeLimitedWorker;
import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.RequestBuilder;
import com.ericsson.cifwk.taf.tools.http.constants.ContentType;
import com.ericsson.cifwk.taf.tools.http.constants.HttpStatus;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
@Operator
public class JenkinsOperatorImpl implements JenkinsOperator {

    private static final Logger LOGGER = Logger.getLogger(JenkinsOperatorImpl.class);

    /**
     * Sometimes FEM Jenkins randomly replies with 404 when trying to get the details of the existing job
     * This can be caused by security issues (see https://issues.jenkins-ci.org/browse/JENKINS-21005)
     * or the job can be stuck in Jenkins waiting for available executor - then job API calls also return 404.
     */
    private static final int TIME_TO_WAIT_AFTER_404_IN_MILLIS = 5000;
    private static final int DEFAULT_OVERALL_MAX_TIME_TO_GET_OVER_404_IN_SECONDS = 120;
    private static final String JENKINS_API_ROOT = "/jenkins/";
    private static final String JENKINS_API_UPLOAD_PLUGIN = JENKINS_API_ROOT + "pluginManager/uploadPlugin";
    private static final String JENKINS_API_RESTART = JENKINS_API_ROOT + "restart";
    private static final int RESTART_TIMEOUT_IN_SECONDS = 15;

    private transient JenkinsHttpRequestHelper requestHelper;


    @Override
    public Jenkins jenkins(Host jenkinsHost) {
        LOGGER.info("Retrieving jenkins info.");
        Optional<String> body = getRequestTo(jenkinsHost, "/jenkins/api/json");
        return fromJson(body.get(), Jenkins.class);
    }

    @Override
    public Optional<SchedulerJobConfig> getMainJobConfig(Host jenkinsHost) throws Exception {
        String configXmlUri = String.format("/jenkins/job/%s/config.xml", TestConstants.SCHEDULER_JOB_NAME);
        Optional<String> optionalConfigXml = getRequestTo(jenkinsHost, configXmlUri);
        if (!optionalConfigXml.isPresent()) {
            return Optional.absent();
        }
        Persister serializer = new Persister();
        SchedulerJobConfig jobConfig = serializer.read(SchedulerJobConfig.class, optionalConfigXml.get());
        return Optional.of(jobConfig);
    }

    @Override
    public Optional<Job> getJob(String jobName, Host jenkinsHost) {
        LOGGER.info("Retrieving jenkins job: " + jobName);
        Optional<String> body = getRequestTo(jenkinsHost, "/jenkins/job/" + jobName + "/api/json");
        if (!body.isPresent()) {
            return Optional.absent();
        }
        Job job = fromJson(body.get(), Job.class);
        return Optional.of(job);
    }

    @Override
    public void createJob(String name, String projectDefinitionTemplateLocation, final Map<Object, Object> projectProperties, final Host jenkinsHost) {
        LOGGER.info("Creating job: " + name);
        Preconditions.checkState(!Strings.isNullOrEmpty(name));

        final String projectDefinition = preProcessAsTemplate(projectDefinitionTemplateLocation, projectProperties);

        RequestBuilder requestBuilder = getRequestBuilder(jenkinsHost);
        HttpResponse response = requestBuilder.body(projectDefinition)
                .contentType(ContentType.APPLICATION_XML)
                .post("/jenkins/createItem?name=" + name);
        checkResponse(response, HttpStatus.OK);
    }

    String preProcessAsTemplate(String projectDefinitionTemplateLocation, Map<Object, Object> projectProperties) {
        try {
            Configuration cfg = new Configuration();
            cfg.setClassForTemplateLoading(this.getClass(), "/");
            cfg.setDefaultEncoding("UTF-8");
            Template template = cfg.getTemplate(projectDefinitionTemplateLocation);
            StringWriter stringWriter = new StringWriter();
            template.process(projectProperties, stringWriter);
            return stringWriter.toString();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void deleteJob(final String name, Host jenkinsHost) {
        LOGGER.info("Deleting job: " + name);
        RequestBuilder requestBuilder = getRequestBuilder(jenkinsHost);
        HttpResponse response = requestBuilder.post("/jenkins/job/" + name + "/doDelete");
        checkResponse(response, HttpStatus.MOVED_TEMPORARILY);
    }

    @Override
    public Build getBuild(String jobName, Host jenkinsHost, int buildNumber) {
        return findBuild(jobName, jenkinsHost, String.valueOf(buildNumber), DEFAULT_OVERALL_MAX_TIME_TO_GET_OVER_404_IN_SECONDS);
    }

    @Override
    public Build lastBuild(String jobName, Host jenkinsHost) {
        return lastBuild(jobName, jenkinsHost, DEFAULT_OVERALL_MAX_TIME_TO_GET_OVER_404_IN_SECONDS);
    }

    @Override
    public Build lastBuild(String jobName, Host jenkinsHost, int maxSecondsToTolerate404) {
        return findBuild(jobName, jenkinsHost, "lastBuild", maxSecondsToTolerate404);
    }

    protected Build findBuild(String jobName, Host jenkinsHost, String buildNumberOrKeyword, int maxSecondsToTolerate404) {
        String body;
        try {
            String jobNameEncoded = URLEncoder.encode(jobName, "UTF-8");
            final RequestBuilder requestBuilder = getRequestBuilder(jenkinsHost);
            String jobNamePreparedForApi = jobNameEncoded.replaceAll("\\+", "%20");
            final String jobUri = String.format("/jenkins/job/%s/%s/api/json", jobNamePreparedForApi, buildNumberOrKeyword);
            LOGGER.debug(String.format("Getting the build '%s' of the job '%s' from '%s' (host '%s')",
                    buildNumberOrKeyword, jobNamePreparedForApi, jobUri, jenkinsHost.getIp()));
            String totalFailureMsg = String.format("Failed to get over 404 error for requests to '%s' on host %s(%s). " +
                            "Perhaps stuck in Jenkins waiting for available executor?",
                    jobUri, jenkinsHost, jenkinsHost.getIp());
            HttpResponse response = requestWithToleranceToErrorCodes(new Supplier<HttpResponse>() {
                @Override
                public HttpResponse get() {
                    return requestBuilder.get(jobUri);
                }
            }, "get the last job build", totalFailureMsg, maxSecondsToTolerate404, TIME_TO_WAIT_AFTER_404_IN_MILLIS, HttpStatus.NOT_FOUND);
            checkResponse(response, HttpStatus.OK);
            body = response.getBody();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        return fromJson(body, Build.class);
    }

    @Override
    public HttpResponse requestWithToleranceToErrorCodes(final Supplier<HttpResponse> responseSupplier,
                                                         final String operationDescription, String totalFailureMsg,
                                                         int maxSecondsToTolerate, int timeToWaitBeforeNewAttemptInMillis,
                                                         HttpStatus... httpStatusesToAvoid) {
        final Set<HttpStatus> badHttpStatuses = Sets.newHashSet(httpStatusesToAvoid);
        final AtomicReference<HttpStatus> lastHttpStatus = new AtomicReference<>();
        try {
            return TimeLimitedTask.performUntilTimeout(new TimeLimitedWorker<HttpResponse>() {
                @Override
                public Optional<HttpResponse> doWork() {
                    HttpResponse result = responseSupplier.get();
                    HttpStatus responseCode = result.getResponseCode();
                    lastHttpStatus.set(responseCode);
                    boolean isBad = badHttpStatuses.contains(responseCode);
                    if (isBad && !StringUtils.isBlank(operationDescription)) {
                        LOGGER.warn(String.format("Jenkins replied with %d code when trying to '%s', retrying...",
                                responseCode.getCode(), operationDescription));
                    }
                    return isBad ? Optional.<HttpResponse>absent() : Optional.of(result);
                }
            }, maxSecondsToTolerate, timeToWaitBeforeNewAttemptInMillis);
        } catch (TimeoutException e) { // NOSONAR
            if (!StringUtils.isBlank(totalFailureMsg)) {
                LOGGER.error(totalFailureMsg);
            }
            throw new IllegalStateException(lastHttpStatus.get().toString());
        }
    }

    @Override
    public void stopBuild(Build build, Host jenkinsHost) {
        final RequestBuilder requestBuilder = getRequestBuilder(jenkinsHost);
        final String buildUrl = build.getUrl();
        // HTTPtool currently supports only relative URLs
        HttpResponse response = requestWithToleranceToErrorCodes(new Supplier<HttpResponse>() {
                                                                     @Override
                                                                     public HttpResponse get() {
                                                                         return requestBuilder.post(extractRequestUri(buildUrl) + "stop");
                                                                     }
                                                                 }, "stop the build", null,
                DEFAULT_OVERALL_MAX_TIME_TO_GET_OVER_404_IN_SECONDS, TIME_TO_WAIT_AFTER_404_IN_MILLIS,
                HttpStatus.NOT_FOUND);
        checkResponse(response, HttpStatus.MOVED_TEMPORARILY);
    }

    @Override
    public String extractRequestUri(String buildUrl) {
        Preconditions.checkArgument(buildUrl != null);
        URL url;
        try {
            url = new URL(buildUrl);
        } catch (MalformedURLException e) {
            throw Throwables.propagate(e);
        }
        return url.getPath();
    }

    @Override
    public String getBuildConsoleLogText(Build build, Host jenkinsHost) {
        String fullConsoleTextLogUrl = build.getFullConsoleTextLogUrl();
        final String consoleLogRequestUri = extractRequestUri(fullConsoleTextLogUrl);
        final RequestBuilder requestBuilder = getRequestBuilder(jenkinsHost);
        HttpResponse buildLogResponse = requestWithToleranceToErrorCodes(new Supplier<HttpResponse>() {
            @Override
            public HttpResponse get() {
                return requestBuilder.get(consoleLogRequestUri);
            }
        }, "get console log", null, DEFAULT_OVERALL_MAX_TIME_TO_GET_OVER_404_IN_SECONDS, 5000, HttpStatus.NOT_FOUND);
        return buildLogResponse.getBody();
    }

    private <T> T fromJson(String body, Class<T> type) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });
        Gson gson = builder.create();
        try {
            return gson.fromJson(body, type);
        } catch (RuntimeException e) {
            LOGGER.error("Failed to deserialize Jenkins response '" + body + "'", e);
            throw e;
        }
    }

    @VisibleForTesting
    public void checkResponse(HttpResponse response, HttpStatus status) {
        HttpStatus code = response.getResponseCode();
        if (code != status) {
            throw new IllegalArgumentException(code.name());
        }
    }

    //TODO: keep request helpers in Host => requestHelper Map, otherwise it won't work correctly with N hosts used with the same operator
    private JenkinsHttpRequestHelper getRequestHelper(Host host) {
        if (requestHelper == null) {
            requestHelper = new JenkinsHttpRequestHelper(host);
        }
        return requestHelper;
    }

    @VisibleForTesting
    public RequestBuilder getRequestBuilder(Host jenkinsHost) {
        return getRequestHelper(jenkinsHost).getRequestBuilder();
    }

    private Optional<String> getRequestTo(Host jenkinsHost, String uri) {
        RequestBuilder requestBuilder = getRequestBuilder(jenkinsHost);
        HttpResponse response = requestBuilder.get(uri);
        if (!HttpStatus.OK.equals(response.getResponseCode())) {
            return Optional.absent();
        }
        return Optional.of(response.getBody());
    }

    public Plugin deployPlugin(Host teMasterHost, TriggerPluginDescriptor pluginDescriptor) {
        return deployPlugin(teMasterHost, pluginDescriptor, 300);
    }

    public Plugin deployPlugin(Host teMasterHost, TriggerPluginDescriptor pluginDescriptor, int restartTimeoutInSec) {
        uploadPlugin(teMasterHost, pluginDescriptor);
        restart(teMasterHost);
        waitUntilJenkinsUpAndRunning(teMasterHost, restartTimeoutInSec);
        return checkPluginVersion(teMasterHost, pluginDescriptor.getPluginName());
    }

    public void uploadPlugin(Host jenkinsHost, TriggerPluginDescriptor pluginDescriptor) {
        Path path = FileHelper.getPath(pluginDescriptor.getHpiPath());
        final File hpiFile = path.toAbsolutePath().normalize().toFile();
        if (!hpiFile.exists()) {
            throw new RuntimeException("Jenkins Plugin(HPI) not found in path: " + hpiFile.getPath());
        }
        RequestBuilder requestBuilder = getRequestHelper(jenkinsHost).getRequestBuilder();
        HttpResponse response = requestBuilder
                .contentType(ContentType.MULTIPART_FORM_DATA)
                .file(hpiFile.getName(), hpiFile)
                .post(JENKINS_API_UPLOAD_PLUGIN);
        if (HttpStatus.SERVICE_UNAVAILABLE.equals(response.getResponseCode())) {
            // Some Jenkins versions restart right away after plugin upload
            LOGGER.info(String.format("Jenkins unavailable after plugin installation - waiting max %d seconds for it to return",
                    RESTART_TIMEOUT_IN_SECONDS));
            waitUntilJenkinsUpAndRunning(jenkinsHost, RESTART_TIMEOUT_IN_SECONDS);
        } else {
            if (!isSuccess(response)) {
                throw new RuntimeException(String.format("Failed to upload plugin HPI. Response:%s%n%s",
                        response.getResponseCode(), response.getBody()));
            }
        }
        LOGGER.info("Plugin uploaded successfully");
    }

    void restart(Host jenkinsHost) {
        HttpResponse response = getRequestHelper(jenkinsHost).getRequestBuilder().post(JENKINS_API_RESTART);
        failOnNegativeResponse(response,
                String.format("Failed to restart Jenkins. Response:%s%n%s", response.getResponseCode(), response.getBody()));
    }

    void waitUntilJenkinsUpAndRunning(Host jenkinsHost, int timeoutInSeconds) {
        int millis = timeoutInSeconds * 1000;
        long start = System.currentTimeMillis();
        while (true) {
            try {
                HttpResponse response = getRequestHelper(jenkinsHost).getRequestBuilder().get(JENKINS_API_ROOT);
                if (isSuccess(response)) {
                    return;
                }
                if (System.currentTimeMillis() - start >= millis) {
                    throw new TimeoutException("Restart Timeout error, Jenkins unavailable after " + timeoutInSeconds + " sec");
                }
                sleep(1000);
            } catch (TimeoutException e) {
                throw Throwables.propagate(e);
            } catch (Exception ignore) { //NOSONAR
                // IGNORE
            }
        }
    }

    Plugin checkPluginVersion(Host jenkinsHost, String pluginName) {
        JenkinsHttpRequestHelper requestHelper = getRequestHelper(jenkinsHost);
        RequestBuilder requestBuilder = requestHelper.getRequestBuilder();
        HttpResponse response = requestBuilder.get("/jenkins/pluginManager/plugin/" + pluginName + "/api/json");
        failOnNegativeResponse(response, String.format("Failed to upload plugin HPI. Response:%s%n%s", response.getResponseCode(), response.getBody()));
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        return gson.fromJson(response.getBody(), Plugin.class);
    }

    private boolean isSuccess(HttpResponse response) {
        int code = response.getResponseCode().getCode();
        return isSuccess(code);
    }

    private boolean isSuccess(int responseCode) {
        return responseCode == 200 || responseCode == 302;
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw Throwables.propagate(e);
        }
    }

    private void failOnNegativeResponse(HttpResponse response, String message) {
        if (!isSuccess(response)) {
            throw new RuntimeException(message);
        }
    }
}
