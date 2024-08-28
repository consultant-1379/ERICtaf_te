package com.ericsson.cifwk.taf.executor;

import com.cloudbees.plugins.flow.FlowRun;
import com.ericsson.cifwk.taf.executor.abort.AbortedJobListener;
import com.ericsson.cifwk.taf.executor.api.ArmInfo;
import com.ericsson.cifwk.taf.executor.api.ArtifactInfo;
import com.ericsson.cifwk.taf.executor.api.Host;
import com.ericsson.cifwk.taf.executor.api.ScheduleRequest;
import com.ericsson.cifwk.taf.executor.api.TafTeAbortBuildResponse;
import com.ericsson.cifwk.taf.executor.api.TafTeBuildDetails;
import com.ericsson.cifwk.taf.executor.api.TafTeBuildTriggerResponse;
import com.ericsson.cifwk.taf.executor.api.TafTeJenkinsJob;
import com.ericsson.cifwk.taf.executor.api.TafTeJenkinsJobImpl;
import com.ericsson.cifwk.taf.executor.api.TriggeringTask;
import com.ericsson.cifwk.taf.executor.api.schedule.model.Schedule;
import com.ericsson.cifwk.taf.executor.eiffel.EiffelMessageBus;
import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;
import com.ericsson.cifwk.taf.executor.schedule.parser.ScheduleValidator;
import com.ericsson.cifwk.taf.executor.utils.GlobalTeSettingsProvider;
import com.ericsson.cifwk.taf.executor.utils.JenkinsUtils;
import com.ericsson.cifwk.taf.executor.utils.MessageBusUtils;
import com.ericsson.cifwk.taf.executor.utils.TriggeringEventBuilder;
import com.ericsson.duraci.datawrappers.EventId;
import com.ericsson.duraci.datawrappers.ExecutionId;
import com.ericsson.duraci.datawrappers.MessageBus;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelBaselineDefinedEvent;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hudson.Extension;
import hudson.model.Build;
import hudson.model.Executor;
import hudson.model.PageDecorator;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Project;
import hudson.model.Queue;
import hudson.model.Result;
import hudson.model.Run;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;

@Extension
public class TeRestService extends PageDecorator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeRestService.class);

    private static final int JOB_TRIGGER_TIMEOUT_MILLIS = 10000;
    private static final long ABORT_TIMEOUT = 10000;
    private static final String JOB_EXECUTION_ID_PARAM_NAME = "jobExecutionId";
    private static final String LINE_SEPARATOR = System.lineSeparator();

    public static final String NAME = Configurations.PLUGIN_NAME + ": REST Schedule Trigger";

    private Gson gson = new GsonBuilder().create();


    //accessible via jenkins_url/jenkins/descriptorByName/com.ericsson.cifwk.taf.executor.TeRestService/scheduleSchema
    public HttpResponse doScheduleSchema(final StaplerRequest req, final StaplerResponse rsp) throws IOException {
        return (req1, rsp1, node) -> {
            rsp1.setContentType("application/xml;charset=UTF-8");
            Writer writer = rsp1.getCompressedWriter(req1);
            URL schemaResource = TeRestService.class.getClassLoader().getResource(ScheduleValidator.DEFAULT_SCHEMA_LOCATION);
            writer.write(Resources.toString(schemaResource, StandardCharsets.UTF_8));
            writer.close();
        };
    }

    //accessible via jenkins_url/jenkins/descriptorByName/com.ericsson.cifwk.taf.executor.TeRestService/trigger
    public HttpResponse doTrigger(final StaplerRequest req, final StaplerResponse rsp) throws IOException {
        return new TeRestServiceHttpResponse(req, rsp) {
            @Override
            protected Object createResponse() {
                try {
                    LOGGER.info("Triggering task received via REST");
                    String requestBody = getRequestBody(req);
                    TriggeringTask task = deserializeTriggeringTask(requestBody);
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Triggering task '" + requestBody + "', deserialized as " + task);
                    }
                    try {
                        return processTriggerTask(task);
                    } catch (Exception e) {
                        String errorTxt = String.format("Error during build triggering: %s", e.getMessage());
                        LOGGER.error(errorTxt, e);
                        return new TafTeBuildTriggerResponse(errorTxt);
                    }
                } catch (IOException e) {
                    String errorTxt = String.format("Error during request processing: %s", e.getMessage());
                    LOGGER.error(errorTxt, e);
                    return new TafTeBuildTriggerResponse(errorTxt);
                }
            }
        };
    }

    //accessible via jenkins_url/jenkins/descriptorByName/com.ericsson.cifwk.taf.executor.TeRestService/abort
    public HttpResponse doAbort(final StaplerRequest req, final StaplerResponse rsp) throws IOException {
        return new TeRestServiceHttpResponse(req, rsp) {
            @Override
            protected Object createResponse() {
                String jobExecutionIdStr;
                try {
                    jobExecutionIdStr = getRequestBody(req);
                } catch (IOException e) {
                    String errorTxt = String.format("Error during request to abort job %s", e.getMessage());
                    LOGGER.error(errorTxt, e);
                    return new TafTeBuildTriggerResponse(errorTxt);
                }
                ExecutionId executionId = new ExecutionId(jobExecutionIdStr);

                StringBuilder response = new StringBuilder("Aborting Spawned Jobs Result: ");
                Jenkins jenkins = getJenkinsInstance();
                AbortedJobListener abortedJobListener = getAbortedJobListener(jenkins);
                TafScheduleBuild scheduleBuild = findSchedulerJob(jenkins, executionId);

                if (scheduleBuild == null || scheduleBuild.getExecutor() == null) {
                    return new TafTeAbortBuildResponse("Test execution run has finished or has not run. Abort invalid");
                }
                abortScheduleBuild(executionId, abortedJobListener, scheduleBuild);

                cancelQueueItems(executionId, jenkins);

                abortExecutionJobs(executionId, jenkins);

                FlowRun flowRun = getFlowRun(executionId, jenkins);
                if (flowRun != null) {
                    abortFlowRun(flowRun);

                    long timeout = System.currentTimeMillis() + ABORT_TIMEOUT;

                    while (flowRun.getExecutor() != null && System.currentTimeMillis() < timeout) {
                        try {
                            Thread.sleep(1000);
                            //needs to be called multiple times in case where there are no execution nodes connected to TE master
                            abortFlowRun(flowRun);
                        } catch (InterruptedException e) {
                        }
                    }

                    if (flowRun.getExecutor() == null) {
                        response.append(LINE_SEPARATOR);
                        response.append("All Spawned Jobs are aborted");
                        cancelQueueItems(executionId, jenkins);
                    } else {
                        response.append(LINE_SEPARATOR);
                        response.append("All spawned jobs were not successfully aborted");
                    }
                }
                LOGGER.info(response.toString());
                abortedJobListener.remove(executionId);
                return new TafTeAbortBuildResponse(response.toString());
            }
        };
    }

    @VisibleForTesting
    AbortedJobListener getAbortedJobListener(Jenkins jenkins) {
        return jenkins.getExtensionList(AbortedJobListener.class).get(0);
    }

    @VisibleForTesting
    FlowRun getFlowRun(ExecutionId executionId, Jenkins jenkins) {
        return JenkinsUtils.getFlowRun(jenkins, executionId);
    }

    @VisibleForTesting
    void abortScheduleBuild(ExecutionId executionId, AbortedJobListener abortedJobListener, TafScheduleBuild scheduleBuild) {
        //Adding Execution Id to list in abortJobListener to stop any jobs with execution id from starting
        abortedJobListener.setExecutionAsAborted(executionId);
        scheduleBuild.abort();
    }

    @VisibleForTesting
    void abortFlowRun(FlowRun flowRun) {
        if (flowRun != null) {
            Executor executor = flowRun.getExecutor();
            if (executor != null) {
                executor.interrupt();
            }
        }
    }

    @VisibleForTesting
    void abortExecutionJobs(ExecutionId executionId, Jenkins jenkins) {
        TafExecutionProject executionProject = getProjectOfType(jenkins, TafExecutionProject.class);
        if (executionProject != null) {
            List<TafExecutionBuild> executionBuilds = getTafExecutionBuilds(executionId, executionProject);
            for (TafExecutionBuild job : executionBuilds) {
                job.abort();
            }
        }
    }

    @VisibleForTesting
    List<TafExecutionBuild> getTafExecutionBuilds(ExecutionId executionId, TafExecutionProject executionProject) {
        return JenkinsUtils.findExecutorJobs(executionProject, executionId);
    }

    private void cancelQueueItems(ExecutionId executionId, Jenkins jenkins) {
        Queue queue = jenkins.getQueue();
        if (queue != null) {
            Queue.Item[] queueItems = queue.getItems();
            for (Queue.Item queueItem : queueItems) {
                Optional<ExecutionId> queueItemExecutionId = JenkinsUtils.findExecutionId(queueItem.getActions(ParametersAction.class));
                if (queueItemExecutionId.isPresent() && queueItemExecutionId.get().equals(executionId)) {
                    queue.cancel(queueItem);
                }
            }
        }
    }

    //accessible via jenkins_url/jenkins/descriptorByName/com.ericsson.cifwk.taf.executor.TeRestService/getSpawnedJobsDetails
    @SuppressWarnings("unused")
    public HttpResponse doGetSpawnedJobsDetails(final StaplerRequest req, StaplerResponse rsp) throws IOException {
        return new TeRestServiceHttpResponse(req, rsp) {
            @Override
            protected Object createResponse() {
                List<TafTeJenkinsJob> spawnedJobs = new ArrayList<>();

                String jobExecutionIdStr = req.getParameter(JOB_EXECUTION_ID_PARAM_NAME);
                ExecutionId executionId = new ExecutionId(jobExecutionIdStr);

                TafScheduleBuild schedulerBuild;
                try {
                    Jenkins jenkins = getJenkinsInstance();
                    schedulerBuild = findSchedulerJob(jenkins, executionId);
                    TafTeJenkinsJob scheduleJob = getScheduleJobInfo(schedulerBuild);
                    if (scheduleJob != null) {
                        spawnedJobs.add(scheduleJob);
                    }
                    TafTeJenkinsJob flowJob = getFlowJob(executionId, jenkins);
                    if (flowJob != null) {
                        spawnedJobs.add(flowJob);
                    }

                    List<TafTeJenkinsJob> executionJobs = getExecutionJobs(executionId, jenkins);
                    if (!executionJobs.isEmpty()) {
                        spawnedJobs.addAll(executionJobs);
                    }
                } catch (Exception e) {
                    String errorTxt = "Failed to get TE build details";
                    LOGGER.error(errorTxt, e);
                    return new TafTeBuildDetails(errorTxt + ": " + e.getMessage());
                }

                return new TafTeBuildDetails(spawnedJobs, getAllureLogUrl(schedulerBuild), jobExecutionIdStr, getSchedule(schedulerBuild));
            }
        };
    }

    private TafTeJenkinsJob getScheduleJobInfo(TafScheduleBuild schedulerBuild) {
        if (schedulerBuild != null) {
            TafTeJenkinsJobImpl schedulerJobInfo = new TafTeJenkinsJobImpl();
            schedulerJobInfo.setType(TafTeJenkinsJob.Type.SCHEDULER);
            populateCommonJobDetails(schedulerBuild, schedulerJobInfo);
            return schedulerJobInfo;
        }
        return null;
    }

    private TafTeJenkinsJob getFlowJob(ExecutionId executionId, Jenkins jenkins) {
        FlowRun flowRun = getFlowRun(executionId, jenkins);
        if (flowRun != null) {
            TafTeJenkinsJobImpl flowRunInfo = new TafTeJenkinsJobImpl();
            flowRunInfo.setType(TafTeJenkinsJob.Type.FLOW);
            populateCommonJobDetails(flowRun, flowRunInfo);
            return flowRunInfo;
        }
        return null;
    }

    private List<TafTeJenkinsJob> getExecutionJobs(ExecutionId executionId, Jenkins jenkins) throws IOException, javax.servlet.ServletException {
        List<TafTeJenkinsJob> executionJobs = new ArrayList<>();
        List<TafExecutionProject> executionProjects = getProjectsOfType(jenkins, TafExecutionProject.class);
        executionProjects
                .forEach(executionProject -> {
                    List<TafExecutionBuild> executionBuilds = getTafExecutionBuilds(executionId, executionProject);
                    for (TafExecutionBuild job : executionBuilds) {
                        TafTeJenkinsJobImpl jobInfo = new TafTeJenkinsJobImpl();
                        jobInfo.setType(TafTeJenkinsJob.Type.EXECUTOR);
                        populateCommonJobDetails(job, jobInfo);
                        jobInfo.setItemName(getItemName(job));
                        executionJobs.add(jobInfo);
                    }});

        return executionJobs;
    }

    @VisibleForTesting
    String getItemName(TafExecutionBuild job) {
        String itemName = getBuildParameter(job, BuildParameterNames.STEP_NAME);
        if (StringUtils.isBlank(itemName)) {
            String description = job.getDescription();
            itemName = StringUtils.isNotBlank(description) ? description : job.getFullDisplayName();
        }
        return itemName;
    }

    @VisibleForTesting
    String getAllureLogUrl(TafScheduleBuild build) {
        return getBuildParameter(build, BuildParameterNames.ALLURE_LOG_URL);
    }

    @VisibleForTesting
    String getBuildParameter(Build build, String paramName) {
        return JenkinsUtils.getBuildParameter(build, paramName);
    }

    @VisibleForTesting
    Schedule getSchedule(TafScheduleBuild build) {
        return build.getSchedule();
    }

    @SuppressWarnings({"deprecation"})
    void populateCommonJobDetails(Run run, TafTeJenkinsJobImpl jobInfo) {
        jobInfo.setRunStatus(getRunStatus(run));
        jobInfo.setName(run.getFullDisplayName());
        jobInfo.setNumber(run.getNumber());
        String absoluteUrl = run.getAbsoluteUrl();
        jobInfo.setUrl(absoluteUrl);
        jobInfo.setFullLogUrl(getFullLogUrl(absoluteUrl));
        populateJobResult(run, jobInfo);
    }

    @VisibleForTesting
    void populateJobResult(Run run, TafTeJenkinsJobImpl jobInfo) {
        Result result = run.getResult();
        if (run.isBuilding() || result == null) {
            jobInfo.setResult(null);
            return;
        }
        if (Result.SUCCESS.equals(result)) {
            jobInfo.setResult(TafTeJenkinsJob.Result.SUCCESS);
        } else if (Result.ABORTED.equals(result)) {
            jobInfo.setResult(TafTeJenkinsJob.Result.ABORTED);
        } else {
            jobInfo.setResult(TafTeJenkinsJob.Result.FAILURE);
        }
    }

    private TafTeJenkinsJob.RunStatus getRunStatus(Run run) {
        return run.isBuilding() ? TafTeJenkinsJob.RunStatus.BUILDING : TafTeJenkinsJob.RunStatus.COMPLETE;
    }

    private String getFullLogUrl(String jobUrl) {
        return jobUrl + "logText/progressiveText";
    }

    EventId sendTriggeringEvent(MessageBus reportMbDefinition, String reportMbDomainId, TriggeringTask task) {
        EiffelBaselineDefinedEvent event = new TriggeringEventBuilder().build(task);
        LOGGER.info("Report the triggering event {} to MB: {}", event, reportMbDefinition);
        EiffelMessageBus messageBus = MessageBusUtils.initializeAndConnect(
                reportMbDomainId,
                reportMbDefinition.getHostName(),
                reportMbDefinition.getExchangeName());
        EventId triggerEventId;
        try {
            triggerEventId = messageBus.sendStart(event, new ExecutionId());
        } finally {
            messageBus.disconnect();
        }
        return triggerEventId;
    }

    String getRequestBody(StaplerRequest req) throws IOException {
        StringBuilder buffer = new StringBuilder();

        String line;
        try (BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
        }
        return buffer.toString();
    }

    TriggeringTask deserializeTriggeringTask(String requestBody) {
        return gson.fromJson(requestBody, TriggeringTask.class);
    }

    TafTeBuildTriggerResponse processTriggerTask(TriggeringTask task) {
        LOGGER.info("Scheduling the build for task " + task);

        Set<ArtifactInfo> testWares = task.getTestWare();
        Collection<ScheduleRequest> schedules = task.getSchedules();

        Collection<Host> slaveHosts = task.getSlaveHosts();

        Jenkins jenkins = getJenkinsInstance();
        TafScheduleProject project = getProjectOfType(jenkins, TafScheduleProject.class);
        Preconditions.checkArgument(project != null, "Cannot find TAF Scheduler project instance");

        GlobalTeSettings globalTeSettings = getGlobalTeSettings();
        MessageBus messageBus = globalTeSettings.getMessageBus();
        String reportMbDomainId = globalTeSettings.getReportMbDomainId();

        EventId triggeringEventId;
        // Port 0 is used for test purposes
        if (messageBus.getPort() != 0) {
            try {
                triggeringEventId = sendTriggeringEvent(messageBus, reportMbDomainId, task);
            } catch (Exception e) {
                LOGGER.error("Failed to send triggering event", e);
                triggeringEventId = new EventId();
            }
        } else {
            triggeringEventId = new EventId();
            LOGGER.info("MB port 0 is set, therefore no MB will be used");
        }

        ArmInfo arm = task.getArmInfo();
        String sutResource = task.getSutResource();

        String minTafVersion = task.getMinTafVersion();

        String userDefinedGAVs = task.getUserDefinedGAVs();

        ExecutionId executionId = createExecutionId();

        Properties commonTestProperties = getCommonTestProperties(task);

        for (ScheduleRequest schedule : schedules) {
            // Create the set of test properties from global ones, appended (or overridden) by schedule ones
            ParametersAction parametersAction = new TafScheduleParametersActionBuilder(project)
                    .withTestTriggerDetails(gson.toJson(task.getTestTriggerDetails()))
                    .withSchedule(schedule)
                    .withTestWare(testWares)
                    .withRepositoryUrl(arm)
                    .withSutResource(sutResource)
                    .withCommonTestProperties(mergeTestProperties(commonTestProperties, schedule.getTestProperties()))
                    .withSlaveHosts(slaveHosts)
                    .withJobTriggeringEventId(triggeringEventId)
                    .withExecutionId(executionId)
                    .withMinTafVersion(minTafVersion)
                    .withUserDefinedGAVs(userDefinedGAVs)
                    .withMiscProperties(task.getMiscProperties())
                    .withJobType(task.getJobType())
                    .withTafSchedulerAddress(task.getTafSchedulerAddress())
                    .withEnableLdap(task.getEnableLdap())
                    .withTeUsername(task.getTeUsername())
                    .withTePassword(task.getTePassword())
                    .build();
            try {
                LOGGER.debug("{} scheduling job for: {} with parametersAction: {}",
                        NAME, project.getName(), JenkinsUtils.toString(parametersAction));
                project.scheduleBuild2(0, new Cause(parametersAction.getParameters()), parametersAction);
                LOGGER.debug("{} scheduled job for {}", NAME, project.getName());
            } catch (Exception e) {
                String msg = String.format("%s failed to schedule build for: %s with parametersAction: %s throws exception: %s",
                        NAME, project.getName(), JenkinsUtils.toString(parametersAction), e.getMessage());
                LOGGER.error(msg, e);
                return new TafTeBuildTriggerResponse(msg);
            }
        }

        LOGGER.info("Job execution ID is {}", executionId);

        LOGGER.info("Waiting until job {} kicks off", executionId);

        // Wait until the job kicks off
        TafScheduleBuild build = waitForSchedulerJob(project, executionId);
        Result result = build.getResult();
        if (Result.FAILURE.equals(result) || Result.ABORTED.equals(result)) {
            return new TafTeBuildTriggerResponse(triggeringEventId.toString(), executionId.toString(),
                    getJobUrl(jenkins, build),
                    TafTeBuildTriggerResponse.Status.FAILURE, "Build failed to get scheduled - see the logs for details");
        }
        LOGGER.info("Scheduler job has started");

        return new TafTeBuildTriggerResponse(triggeringEventId.toString(), executionId.toString(),
                getJobUrl(jenkins, build));
    }

    @VisibleForTesting
    GlobalTeSettings getGlobalTeSettings() {
        return GlobalTeSettingsProvider.getInstance().provide();
    }

    @VisibleForTesting
    Properties mergeTestProperties(Properties commonTestProperties, Properties scheduleTestProperties) {
        Properties testProperties = new Properties();
        testProperties.putAll(commonTestProperties);
        testProperties.putAll(scheduleTestProperties);
        return testProperties;
    }

    private String getJobUrl(Jenkins jenkins, TafScheduleBuild build) {
        return jenkins.getRootUrl() + build.getUrl();
    }

    @VisibleForTesting
    ExecutionId createExecutionId() {
        return new ExecutionId();
    }

    @VisibleForTesting
    Jenkins getJenkinsInstance() {
        return JenkinsUtils.getJenkinsInstance();
    }

    @VisibleForTesting
    Properties getCommonTestProperties(TriggeringTask task) {
        Properties result = new Properties();
        if (task.getGlobalTestProperties() != null) {
            result.putAll(task.getGlobalTestProperties());
        }
        return result;
    }

    @VisibleForTesting
    <T extends Project> T getProjectOfType(Jenkins jenkins, Class<T> clazz) {
        return JenkinsUtils.getProjectOfType(jenkins, clazz);
    }

    @VisibleForTesting
    <T extends Project> List<T> getProjectsOfType(Jenkins jenkins, Class<T> clazz) {
        return JenkinsUtils.getProjectsOfType(jenkins, clazz);
    }

    @VisibleForTesting
    TafScheduleBuild waitForSchedulerJob(TafScheduleProject tafScheduleProject, ExecutionId executionId) {
        return JenkinsUtils.waitForSchedulerJob(tafScheduleProject, executionId, JOB_TRIGGER_TIMEOUT_MILLIS / 1000);
    }

    @VisibleForTesting
    TafScheduleBuild findSchedulerJob(Jenkins jenkins, ExecutionId executionId) {
        return JenkinsUtils.getSchedulerJob(jenkins, executionId);
    }

    public static class Cause extends hudson.model.Cause {

        private final List<ParameterValue> parameters;

        public Cause(List<ParameterValue> parameters) {
            this.parameters = parameters;
        }

        public List<ParameterValue> getParameters() {
            return parameters;
        }

        @Override
        public String getShortDescription() {
            return "TeRestTrigger.Cause";
        }
    }

}
