package com.ericsson.cifwk.taf.executor;

import com.cloudbees.plugins.flow.FlowRun;
import com.ericsson.cifwk.taf.executor.api.schedule.model.Schedule;
import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;
import com.ericsson.cifwk.taf.executor.model.ScheduleBuildParameters;
import com.ericsson.cifwk.taf.executor.schedule.FlowExecutor;
import com.ericsson.cifwk.taf.executor.utils.GlobalTeSettingsProvider;
import com.ericsson.cifwk.taf.executor.utils.HttpResponseBuilder;
import com.ericsson.cifwk.taf.executor.utils.JenkinsUtils;
import com.google.common.annotations.VisibleForTesting;
import hudson.model.Build;
import hudson.model.CauseAction;
import hudson.model.Executor;
import hudson.model.Failure;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.StringParameterValue;
import jenkins.model.Jenkins;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

public class TafScheduleBuild extends Build<TafScheduleProject, TafScheduleBuild> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TafScheduleBuild.class);

    private String executionId;
    private Schedule schedule;

    public TafScheduleBuild(TafScheduleProject project) throws IOException {
        super(project);
    }

    public TafScheduleBuild(TafScheduleProject project, File buildDir) throws IOException {
        super(project, buildDir);
    }

    @Override
    public void run() {
        Map<String, String> variables = this.getBuildVariables();
        this.executionId = variables.get(BuildParameterNames.EIFFEL_JOB_EXECUTION_ID);
        if (StringUtils.isBlank(this.executionId)) {
            this.executionId = UUID.randomUUID().toString();
            addToParameters(BuildParameterNames.EIFFEL_JOB_EXECUTION_ID, executionId);
        }

        addExecutionParameters();

        this.addAction(new CauseAction(new TafScheduleCause()));
        this.execute(new TafScheduleBuildExecution());
    }

    private void addExecutionParameters() {
        addToParameters(BuildParameterNames.CONFIG_URL, getConfigUrl());

        GlobalTeSettings globalTeSettings = GlobalTeSettingsProvider.getInstance().provide();
        String reportsHost = globalTeSettings.getReportsHost();
        if (StringUtils.isNotBlank(reportsHost)) {
            addToParameters(BuildParameterNames.ALLURE_LOG_URL, MessageFormat.format("{0}/{1}/", reportsHost, executionId));
        }
        String localReportsStorage = globalTeSettings.getLocalReportsStorage();
        if (StringUtils.isNotEmpty(localReportsStorage)) {
            addToParameters(BuildParameterNames.ALLURE_LOG_DIR,  MessageFormat.format("{0}/{1}/", localReportsStorage, executionId));
        }
    }

    public String getConfigUrl() {
        return getJenkinsRootUrl() + this.getUrl() + "config";
    }

    private static String getJenkinsRootUrl() {
        Jenkins jenkins = JenkinsUtils.getJenkinsInstance();
        String rootUrl = jenkins.getRootUrl();
        if (rootUrl == null) {
            // Dirty hack to allow the local running of the plugin via 'mvn hpi:run'
            return "http://localhost:8091/jenkins/";
        }
        return rootUrl;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public String resolveSchedule(String repositoryUrl, String scheduleArtifact, String scheduleName) {
        return project.getArtifactHelper().resolveArtefact(repositoryUrl, scheduleArtifact, scheduleName);
    }

    public String createFlowJob(String dsl) throws IOException {
        ScheduleBuildParameters params = getBuildParameters();
        return project.getFlowExecutor().createProject(dsl, params);
    }

    public Future<FlowRun> scheduleFlowJob(String flowJobName) {
        ParametersAction parametersAction =
                new ParametersAction(new StringParameterValue(BuildParameterNames.EIFFEL_JOB_EXECUTION_ID, executionId));
        FlowExecutor flowExecutor = project.getFlowExecutor();
        return flowExecutor.scheduleBuild(flowJobName, parametersAction);
    }

    public void addToParameters(ParameterValue parameterValue) {
        ParametersAction actions = this.getAction(ParametersAction.class);
        actions = actions.merge(new ParametersAction(parameterValue));
        this.replaceAction(actions);
    }

    public void addToParameters(String name, String value) {
        addToParameters(new StringParameterValue(name, value));
    }

    public void addToParameters(Map<String, String> params) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            addToParameters(new StringParameterValue(entry.getKey(), entry.getValue()));
        }
    }

    public <T extends ParameterValue> void addToParameters(String name, String value, Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getConstructor(String.class, String.class);
            T paramValue = constructor.newInstance(name, value);
            addToParameters(paramValue);
        } catch (NoSuchMethodException e) {
            throw new UnsupportedOperationException("Unexpected parameter type: " + clazz, e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize parameter of type " + clazz, e);
        }
    }

    protected class TafScheduleBuildExecution extends BuildExecution {
    }

    /**
     * Jenkins call this method by web url: /jenkins/job/{build_name}/{build_number}/config?type={hosts|properties}
     */
    public HttpResponse doConfig(StaplerRequest req, StaplerResponse rsp, @QueryParameter String type) throws IOException {
        ScheduleBuildParameters params = getBuildParameters();
        if ("hosts".equalsIgnoreCase(type)) {
            return HttpResponseBuilder.json(params.getSutResource());
        } else if ("properties".equalsIgnoreCase(type)) {
            return HttpResponseBuilder.properties(params.getCommonTestProperties());
        }
        return new Failure("Parameters error");
    }

    @VisibleForTesting
    ScheduleBuildParameters getBuildParameters() {
        return JenkinsUtils.getBuildParameters(this, ScheduleBuildParameters.class);
    }

    /**
     * Jenkins call this method by web url: /jenkins/{build_name}/{build_number}/resolveSchedule?type={host|properties}
     */
    public HttpResponse doResolveSchedule(StaplerRequest req, StaplerResponse rsp,
                                          final @QueryParameter("REPOSITORY_URL") String repositoryUrl,
                                          final @QueryParameter("SCHEDULE_ARTIFACT") String scheduleArtifact,
                                          final @QueryParameter("SCHEDULE_NAME") String scheduleName) throws IOException {
        return new HttpResponse() {
            @Override
            public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                rsp.setContentType("test/xml");
                Writer w = rsp.getCompressedWriter(req);
                try {
                    String scheduleXml = resolveSchedule(repositoryUrl, scheduleArtifact, scheduleName);
                    w.write(scheduleXml);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    rsp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    w.write(e.getMessage());
                } finally {
                    try {
                        w.close();
                    } catch (Exception ignore) { // NOSONAR
                        // IGNORE
                    }
                }
            }
        };
    }

    public void abort() {
        Executor executor = this.getExecutor();
        if (executor != null) {
            executor.interrupt();
        }
    }

    /**
     * Display build information on build page.
     * see: com/ericsson/cifwk/taf/executor/TafScheduleBuild/TafScheduleCause/description.jelly
     */
    public static class TafScheduleCause extends hudson.model.Cause {
        @Override
        public String getShortDescription() {
            return "TafScheduleBuild.Cause";
        }
    }

}
