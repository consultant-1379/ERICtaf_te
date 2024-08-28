package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;
import com.ericsson.cifwk.taf.executor.model.ManualTestsBuildParameters;
import com.ericsson.cifwk.taf.executor.model.ScheduleBuildParameters;
import com.ericsson.cifwk.taf.executor.model.TeBuildMainParameters;
import com.google.common.base.Throwables;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Build step for optional manual tests execution
 */
public class TafManualTestExecutionBuilder extends AbstractTestExecutionBuilder {

    private static final String BUILDER_NAME = Configurations.PLUGIN_NAME + " : Manual Test Running";

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        PrintStream logger = listener.getLogger();
        // Check if we need to run manual tests now
        ManualTestsBuildParameters currentExecutionParams = getCurrentBuildParameters(build, ManualTestsBuildParameters.class);
        if (!currentExecutionParams.defined()) {
            return true;
        }
        build.setDescription("Manual tests: campaigns " + currentExecutionParams.getManualTestCampaignIdsAsCsv());
        logger.println("Execution parameters retrieved: " + currentExecutionParams.getAllParameters());
        String executionId = currentExecutionParams.getExecutionId();
        if (!isTePipelineOk(executionId, logger)) {
            return false;
        }
        TeBuildMainParameters globalSettings = getMainParameters(executionId);
        GlobalTeSettings globalTeSettings = globalSettings.getGlobalTeSettings();
        ScheduleBuildParameters mainBuildParameters = globalSettings.getScheduleBuildParameters();

        logBuildParameters(build.getBuildVariables(), logger);

        try {
            String workspaceUri = getWorkspaceUri(build);
            TestExecution execution = TestExecution.builder()
                    .from(globalTeSettings, mainBuildParameters, currentExecutionParams)
                    .withJenkinsWorkspace(workspaceUri)
                    .build();

            TestExecutionResult testExecutionResult = executeOnRemoteSlave(launcher, logger, execution);
            return testExecutionResult.getTestResultStatus() == TestResult.Status.SUCCESS;
        } catch (Exception e) {
            logger.println(ExceptionUtils.getStackTrace(e));
            throw Throwables.propagate(e);
        }
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) getJenkinsInstance().getDescriptorOrDie(getClass());
    }

    @Extension(ordinal = 111)
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
