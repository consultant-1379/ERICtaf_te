package com.ericsson.cifwk.taf.executor.abort;

import com.ericsson.cifwk.taf.executor.utils.JenkinsUtils;
import com.ericsson.duraci.datawrappers.ExecutionId;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.ParametersAction;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import java.util.HashSet;


@Extension
public class AbortedJobListener extends RunListener<AbstractBuild<?,?>> {

    final HashSet<ExecutionId> abortedExecutions = new HashSet<>();

    @Override
    public void onStarted(AbstractBuild<?, ?> abstractBuild, TaskListener listener) {
        Optional<ExecutionId> runExecutionId = getExecutionIdOptional(abstractBuild);
        if(runExecutionId.isPresent()) {
            if (abortedExecutions.contains(runExecutionId.get())) {
                try {
                    abstractBuild.doStop();
                } catch (Exception e) { // NOSONAR
                    listener.getLogger().append("Failed to stop spawned job of aborted job ").append(e.getMessage());
                }
            }
            else {
                super.onStarted(abstractBuild, listener);
            }
        }
    }

    @VisibleForTesting
    Optional<ExecutionId> getExecutionIdOptional(AbstractBuild<?, ?> abstractBuild) {
        return JenkinsUtils.findExecutionId(abstractBuild.getActions(ParametersAction.class));
    }

    public void setExecutionAsAborted(ExecutionId executionId) {
        abortedExecutions.add(executionId);
    }

    public void remove(ExecutionId executionId) {
        abortedExecutions.remove(executionId);
    }

}
