package com.ericsson.cifwk.taf.executor.api;

import com.ericsson.cifwk.taf.executor.api.healthcheck.HealthCheck;
import com.ericsson.cifwk.taf.executor.api.healthcheck.HealthCheckState;
import com.ericsson.cifwk.taf.executor.api.schedule.ScheduleChildDeserializer;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleChild;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * TAF TE REST service client implementation
 */
class TeRestServiceClientImpl implements TeRestServiceClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeRestServiceClientImpl.class);

    private static final String TRIGGER_URL_PATTERN = "http://%1$s:%2$d/jenkins/descriptorByName/" +
            "com.ericsson.cifwk.taf.executor.TeRestService/trigger";
    private static final String ABORT_URL_PATTERN = "http://%1$s:%2$d/jenkins/descriptorByName/" +
            "com.ericsson.cifwk.taf.executor.TeRestService/abort";
    private static final String SPAWNED_JOBS_INFO_URL_PATTERN = "http://%1$s:%2$d/jenkins/descriptorByName/" +
            "com.ericsson.cifwk.taf.executor.TeRestService/getSpawnedJobsDetails?jobExecutionId=%3$s";
    private static final String TE_HEALTHCHECK_URL_PATTERN = "http://%s:%s/jenkins/descriptorByName/" +
            "com.ericsson.cifwk.taf.executor.healthcheck.HealthCheck/healthCheck";

    private static final int QUERY_TIMEOUT_IN_MILLIS = 180000;

    private String teHostAddress;
    private int teHostPort;
    private final Gson gson;

    @VisibleForTesting
    HttpClient httpClient;

    public TeRestServiceClientImpl(String teHostAddress, int teHostPort) {
        this();

        Preconditions.checkArgument(StringUtils.isNotBlank(teHostAddress), "Host address cannot be empty");
        Preconditions.checkArgument(teHostPort != 0, "Host port is undefined");

        this.teHostAddress = teHostAddress;
        this.teHostPort = teHostPort;
        this.httpClient = getHttpClient();
    }

    @VisibleForTesting
    TeRestServiceClientImpl() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(TafTeJenkinsJob.class, new TafTeJenkinsJobDeserializer())
                .registerTypeAdapter(ScheduleChild.class, new ScheduleChildDeserializer())
                .create();
    }

    @Override
    public TafTeBuildTriggerResponse triggerBuild(TriggeringTask triggeringTask) {
        String taskStr = gson.toJson(triggeringTask);
        ResponseAndCode response = requestPost(buildTriggerUrl(), taskStr);
        if (response.isOk()) {
            return deserializeTriggerResponse(response.responseText);
        } else {
            return new TafTeBuildTriggerResponse(response.responseText);
        }
    }

    @Override
    public TafTeAbortBuildResponse abortBuild(String jobExecutionId) {
        LOGGER.info("Sending signal to abort TE job in vApp with Execution Id " + jobExecutionId);
        ResponseAndCode response = requestPost(buildAbortUrl(), jobExecutionId);
        if (response.isOk()) {
            return deserializeAbortResponse(response.responseText);
        } else {
            return new TafTeAbortBuildResponse(response.responseText);
        }
    }

    @VisibleForTesting
    TafTeBuildTriggerResponse deserializeTriggerResponse(String responseStr) {
        try {
            return gson.fromJson(responseStr, TafTeBuildTriggerResponse.class);
        } catch (Exception e) {
            LOGGER.error("Failed to deserialize build trigger responseText '" + responseStr + "'", e);
            throw Throwables.propagate(e);
        }
    }

    @VisibleForTesting
    TafTeAbortBuildResponse deserializeAbortResponse(String responseStr) {
        try {
            return gson.fromJson(responseStr, TafTeAbortBuildResponse.class);
        } catch (Exception e) {
            LOGGER.error("Failed to deserialize build abort responseText '" + responseStr + "'", e);
            throw Throwables.propagate(e);
        }
    }

    @Override
    public TafTeBuildDetails getBuildDetails(String primaryJobExecutionId) {
        ResponseAndCode response = requestGet(buildInfoUrl(primaryJobExecutionId));
        if (response.isOk()) {
            return deserializeTafTeBuildDetails(response.responseText);
        } else {
            return new TafTeBuildDetails(response.responseText);
        }
    }

    @Override
    public HealthCheckState getTeHealthCheck() {
        ResponseAndCode response = requestGet(buildHealthCheckUrl());
        List<HealthCheck> healthChecks;
        if (response.isOk()) {
            healthChecks = deserializeTeHealthCheckDetails(response.responseText);
        } else {
            healthChecks = Collections.emptyList();
        }
        return new HealthCheckState(healthChecks);
    }

    @Override
    public String getHostAddress() {
        return teHostAddress;
    }

    @Override
    public int getHostPort() {
        return teHostPort;
    }

    @Override
    public final HttpClient getHttpClient() {
        return getHttpClient(QUERY_TIMEOUT_IN_MILLIS);
    }

    @VisibleForTesting
    TafTeBuildDetails deserializeTafTeBuildDetails(String responseStr) {
        try {
            return gson.fromJson(responseStr, TafTeBuildDetails.class);
        } catch (Exception e) {
            LOGGER.error("Failed to deserialize build details responseText '" + responseStr + "'", e);
            throw Throwables.propagate(e);
        }
    }

    @VisibleForTesting
    List<HealthCheck> deserializeTeHealthCheckDetails(String responseStr) {
        try {
            return gson.fromJson(responseStr, new TypeToken<List<HealthCheck>>() {
            }.getType());
        } catch (Exception e) {
            LOGGER.error("Failed to deserialize health check details responseText '" + responseStr + "'", e);
            throw Throwables.propagate(e);
        }
    }

    String buildInfoUrl(String primaryJobExecutionId) {
        return String.format(SPAWNED_JOBS_INFO_URL_PATTERN, teHostAddress, teHostPort, primaryJobExecutionId);
    }

    String buildTriggerUrl() {
        return String.format(TRIGGER_URL_PATTERN, teHostAddress, teHostPort);
    }

    String buildAbortUrl() {
        return String.format(ABORT_URL_PATTERN, teHostAddress, teHostPort);
    }

    String buildHealthCheckUrl() {
        return String.format(TE_HEALTHCHECK_URL_PATTERN, teHostAddress, teHostPort);
    }

    private ResponseAndCode requestPost(String url, String payload) {
        HttpPost request = new HttpPost(url);
        request.setEntity(new StringEntity(payload, "UTF-8"));

        return executeRequest(httpClient, request);
    }

    private CloseableHttpClient getHttpClient(int timeoutInMillis) {
        CloseableHttpClient httpClient;
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(timeoutInMillis)
                .setConnectionRequestTimeout(timeoutInMillis)
                .setSocketTimeout(timeoutInMillis).build();
        httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig).build();
        return httpClient;
    }

    private ResponseAndCode requestGet(String url) {
        HttpGet request = new HttpGet(url);
        return executeRequest(httpClient, request);
    }

    private ResponseAndCode executeRequest(HttpClient client, HttpUriRequest request) {
        try {
            ResponseAndCode result = new ResponseAndCode();
            HttpResponse httpResponse = client.execute(request);
            byte[] response = EntityUtils.toByteArray(httpResponse.getEntity());
            result.code = httpResponse.getStatusLine().getStatusCode();
            result.responseText = new String(response);
            return result;
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private class TafTeJenkinsJobDeserializer implements JsonDeserializer<TafTeJenkinsJob> {
        @Override
        public TafTeJenkinsJob deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return context.deserialize(jsonObject, TafTeJenkinsJobImpl.class);
        }
    }

    private class ResponseAndCode {
        String responseText;
        int code;

        public boolean isOk() {
            return code == 200;
        }
    }

}
