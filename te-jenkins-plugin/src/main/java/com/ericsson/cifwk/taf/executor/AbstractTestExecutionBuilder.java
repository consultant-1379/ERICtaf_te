package com.ericsson.cifwk.taf.executor;


import com.ericsson.cifwk.taf.executor.cluster.RemoteTafLauncher;
import com.ericsson.cifwk.taf.executor.model.BuildParametersHolder;
import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;
import com.ericsson.cifwk.taf.executor.model.ScheduleBuildParameters;
import com.ericsson.cifwk.taf.executor.model.TeBuildMainParameters;
import com.ericsson.cifwk.taf.executor.utils.JenkinsUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.remoting.RemoteOutputStream;
import hudson.remoting.VirtualChannel;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.Map;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 16/03/2016
 */
abstract class AbstractTestExecutionBuilder extends Builder {

    protected boolean isTePipelineOk(String executionId, PrintStream logger) {
        TeBuildMainParameters globalSettings = getMainParameters(executionId);
        GlobalTeSettings globalTeSettings = globalSettings.getGlobalTeSettings();
        if (globalTeSettings == null) {
            logger.println("ERROR: cannot find the scheduler project!");
            return false;
        }
        ScheduleBuildParameters mainBuildParameters = globalSettings.getScheduleBuildParameters();
        if (mainBuildParameters == null) {
            logger.println("ERROR: cannot find the corresponding scheduler build!");
            return false;
        }
        return true;
    }

    protected TeBuildMainParameters getMainParameters(String executionId) {
        return TeBuildMainParameters.lookup(executionId);
    }

    protected <T extends BuildParametersHolder> T getCurrentBuildParameters(AbstractBuild<?, ?> build, Class<T> holderClass) {
        return JenkinsUtils.getBuildParameters(build, holderClass);
    }

    protected String getWorkspaceUri(AbstractBuild<?, ?> build) {
        FilePath workspace = build.getWorkspace();
        String workspaceUri = null;
        if (workspace != null && workspace.isRemote()) {
            workspaceUri = workspace.getRemote();
        }
        return workspaceUri;
    }

    protected Jenkins getJenkinsInstance() {
        return JenkinsUtils.getJenkinsInstance();
    }

    protected void logBuildParameters(Map<String, String> variables, PrintStream logger) {
        logger.println("Starting TAF tests execution.");
        logger.println("-------------------------------------------------------------------");
        for (Map.Entry<String, String> variable : variables.entrySet()) {
            logger.format("| %s => '%s'%n", variable.getKey(), variable.getValue());
        }
        logger.println("-------------------------------------------------------------------");
    }

    protected TestExecutionResult executeOnRemoteSlave(Launcher launcher, PrintStream logger, TestExecution execution) throws Exception {
        try (RemoteOutputStream remoteOutputStream = new RemoteOutputStream(logger)) {
            RemoteTafLauncher tafLauncher = getRemoteTafLauncher(execution, remoteOutputStream);
            VirtualChannel channel = launcher.getChannel();
            logger.println("Current host is " + InetAddress.getLocalHost().getHostAddress() + ", thread is " + Thread.currentThread());
            logger.println("Starting remote execution on channel " + channel);
            long startedAt = System.currentTimeMillis();

            String resultAsString = channel.call(tafLauncher);

            TestExecutionResult testExecutionResult = new Gson().fromJson(resultAsString, TestExecutionResult.class);
            logger.println("Finished remote execution in " + (System.currentTimeMillis() - startedAt)
                    + " millis with the result '" + testExecutionResult.getTestResultStatus() + "'");

            return testExecutionResult;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @VisibleForTesting
    public RemoteTafLauncher getRemoteTafLauncher(TestExecution execution, OutputStream remoteOutputStream) {
        return new RemoteTafLauncher(execution, remoteOutputStream);
    }

}
