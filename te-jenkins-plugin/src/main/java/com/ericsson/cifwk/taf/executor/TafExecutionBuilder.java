package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.cluster.TestPomViewBadge;
import com.ericsson.cifwk.taf.executor.eiffel.EiffelMessageBus;
import com.ericsson.cifwk.taf.executor.model.ExecutorBuildParameters;
import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;
import com.ericsson.cifwk.taf.executor.model.ManualTestsBuildParameters;
import com.ericsson.cifwk.taf.executor.model.ScheduleBuildParameters;
import com.ericsson.cifwk.taf.executor.model.TeBuildMainParameters;
import com.ericsson.cifwk.taf.executor.utils.MessageBusUtils;
import com.ericsson.duraci.datawrappers.EventId;
import com.ericsson.duraci.datawrappers.ExecutionId;
import com.ericsson.duraci.datawrappers.LogReference;
import com.ericsson.duraci.datawrappers.ResultCode;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelTestSuiteFinishedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelTestSuiteStartedEvent;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.CauseAction;
import hudson.model.ParametersAction;
import hudson.remoting.RequestAbortedException;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Build step, which connects to TAF agents and executes tests remotely.
 */
public class TafExecutionBuilder extends AbstractTestExecutionBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(TafExecutionBuilder.class);
    private static final String BUILDER_NAME = Configurations.PLUGIN_NAME + " : Test Invocation";
    private static final int RETRY_QUIET_PERIOD = 30;

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        PrintStream logger = listener.getLogger();

        ManualTestsBuildParameters manualTestParameters = getCurrentBuildParameters(build, ManualTestsBuildParameters.class);
        if (manualTestParameters.defined()) {
            return true;
        }

        ExecutorBuildParameters currentExecutionParams = getCurrentBuildParameters(build, ExecutorBuildParameters.class);
        build.setDescription("Suite(-s) " + currentExecutionParams.getTafSuites());
        logger.println("Execution parameters retrieved: " + currentExecutionParams.getAllParameters());
        String executionId = currentExecutionParams.getExecutionId();
        if (!isTePipelineOk(executionId, logger)) {
            return false;
        }

        TeBuildMainParameters mainParameters = getMainParameters(executionId);
        GlobalTeSettings globalTeSettings = mainParameters.getGlobalTeSettings();
        ScheduleBuildParameters mainBuildParameters = mainParameters.getScheduleBuildParameters();

        logBuildParameters(build.getBuildVariables(), logger);

        String[] logAndScriptUrls = getLogAndScriptUrls(build, logger);
        String logUrl = logAndScriptUrls[0];
        String scriptUrl = logAndScriptUrls[1];
        Map<String, LogReference> logReferences = new HashMap<>();
        logReferences.put("default_log", new LogReference(logUrl));

        EiffelMessageBus messageBus = getEiffelMessageBus(globalTeSettings);
        String eiffelJobExecutionIdStr = mainBuildParameters.getExecutionId();
        ExecutionId eiffelJobExecutionId = new ExecutionId(eiffelJobExecutionIdStr);
        String eiffelJobStartedEventIdStr = mainBuildParameters.getEiffelJobStartedEventId();
        String enableLdap = mainBuildParameters.getEnableLdap();
        String teUsername = mainBuildParameters.getTeUsername();
        String tePassword = mainBuildParameters.getTePassword();
        messageBus.pushAsParent(new EventId(eiffelJobStartedEventIdStr), eiffelJobExecutionId);
        String eiffelTestExecutionIdStr = mainBuildParameters.getEiffelTestExecutionId();
        ExecutionId testExecutionId = new ExecutionId(eiffelTestExecutionIdStr);

        try {
            String scheduleItemExecutionIdStr = currentExecutionParams.getEiffelScheduleItemExecutionId();
            ExecutionId scheduleItemExecutionId = new ExecutionId(scheduleItemExecutionIdStr);

            String eiffelScheduleStartedExecutionIdStr = mainBuildParameters.getEiffelScheduleStartedExecutionId();
            String eiffelScheduleStartedEventIdStr = mainBuildParameters.getEiffelScheduleStartedEventId();
            EiffelTestSuiteStartedEvent scheduleItemStartedEvent = EiffelTestSuiteStartedEvent.Factory.create(
                    new ExecutionId(eiffelScheduleStartedExecutionIdStr),
                    "scheduleItem",
                    currentExecutionParams.getTafSuites(),
                    scheduleItemExecutionId);
            scheduleItemStartedEvent.setTestExecutionId(testExecutionId);
            EventId scheduleItemStartedEventId = messageBus.sendStart(scheduleItemStartedEvent, scheduleItemExecutionId,
                    new EventId(eiffelScheduleStartedEventIdStr));

            String workspaceUri = getWorkspaceUri(build);
            TestExecution execution = TestExecution.builder()
                    .from(globalTeSettings, mainBuildParameters, currentExecutionParams)
                    .withJenkinsWorkspace(workspaceUri)
                    .withParentEventId(scheduleItemStartedEventId.toString())
                    .withLog(logUrl)
                    .withScriptUrl(scriptUrl)
                    .withEnableLdap(enableLdap)
                    .withTeUsername(teUsername)
                    .withTePassword(tePassword)
                    .withSkipTests(getSkipTestValue(mainBuildParameters.getCommonTestProperties()))
                    .build();

            TestExecutionResult testExecutionResult = executeOnRemoteSlave(launcher, logger, execution);
            addLinkToTestPom(build, testExecutionResult);
            TestResult.Status status = testExecutionResult.getTestResultStatus();
            ResultCode resultCode = EiffelMessageBus.executorResultCode(status);
            sendFinish(messageBus, resultCode, logReferences, testExecutionId);
            return status == TestResult.Status.SUCCESS;
        } catch (RequestAbortedException e) {
            logger.println(ExceptionUtils.getStackTrace(e));
            sendFinish(messageBus, ResultCode.ABORTED, logReferences, testExecutionId);
            retryBuild(build, e);
            return false;
        } catch (Exception e) {
            logger.println(ExceptionUtils.getStackTrace(e));
            sendFinish(messageBus, ResultCode.FAILURE, logReferences, testExecutionId);
            throw Throwables.propagate(e);
        } finally {
            messageBus.disconnect();
        }
    }

    @VisibleForTesting
    String getSkipTestValue (String testExecutionAdditionalParameters){
        String [] lines = testExecutionAdditionalParameters.split(System.getProperty("line.separator"));
        String result = "false";
        for (String line : lines) {
            if (line.contains("skipTests") && line.contains("=")){
                String [] temp = line.split("=");
                if (temp.length == 2 && temp[1].equals("true")){
                    result = "true";
                }
            }
        }
        return result;
    }

    private void addLinkToTestPom(AbstractBuild<?, ?> build, TestExecutionResult testExecutionResult) {
        String testPomLocation = testExecutionResult.getTestPomLocation();
        if (isNotBlank(testPomLocation)) {
            build.addAction(new TestPomViewBadge(build, testPomLocation));
        }
    }

    @VisibleForTesting
    String[] getLogAndScriptUrls(AbstractBuild<?, ?> build, PrintStream logger) {
        String rootUrl = getJenkinsInstance().getRootUrl();
        String logUrl = "";
        String scriptUrl = "";
        if (rootUrl != null) {
            logUrl = rootUrl + build.getUrl() + "consoleFull";
            scriptUrl = rootUrl + build.getProject().getUrl() + "script";
        } else {
            logger.println("WARNING: Jenkins root URL is not configured!");
        }
        return new String[]{logUrl, scriptUrl};
    }

    @VisibleForTesting
    EiffelMessageBus getEiffelMessageBus(GlobalTeSettings globalTeSettings) {
        return MessageBusUtils.initializeAndConnect(globalTeSettings);
    }

    @VisibleForTesting
    void sendFinish(EiffelMessageBus messageBus, ResultCode resultCode, Map<String, LogReference> logReferences, ExecutionId testExecutionId) {
        ExecutionId scheduleItemExecutionId = messageBus.getSentParent().getExecutionId();
        EiffelTestSuiteFinishedEvent suiteFinishedEvent = EiffelTestSuiteFinishedEvent.Factory.create(resultCode, logReferences, scheduleItemExecutionId);
        suiteFinishedEvent.setTestExecutionId(testExecutionId);
        messageBus.sendFinish(suiteFinishedEvent);
    }

    private void retryBuild(AbstractBuild<?, ?> build, RequestAbortedException e) {
        LOGGER.error("Retrying build because node thrown: ", e.getCause());
        ParametersAction params = build.getAction(ParametersAction.class);
        CauseAction causeAction = new CauseAction(build.getAction(CauseAction.class));
        BuildCause buildCause = new BuildCause("Retry Action");
        RetryAction retryAction = new RetryAction();
        build.getProject().scheduleBuild(RETRY_QUIET_PERIOD, buildCause, params, retryAction, causeAction);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) getJenkinsInstance().getDescriptorOrDie(getClass());
    }

    @Extension(ordinal = 110)
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public String getDisplayName() {
            return BUILDER_NAME;
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return TafExecutionProject.class.isAssignableFrom(jobType);
        }

    }

}
