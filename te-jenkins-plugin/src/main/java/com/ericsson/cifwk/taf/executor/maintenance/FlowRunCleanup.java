package com.ericsson.cifwk.taf.executor.maintenance;

import com.cloudbees.plugins.flow.BuildFlow;
import com.cloudbees.plugins.flow.FlowRun;
import com.ericsson.cifwk.taf.executor.utils.JenkinsUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import hudson.Extension;
import hudson.model.PeriodicWork;
import jenkins.model.Jenkins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Deletes old BuildFlow projects
 */
@Extension
public class FlowRunCleanup extends PeriodicWork {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowRunCleanup.class);

    private static final int RECURRENCE_PERIOD_IN_MINUTES = 20;

    @Override
    public long getRecurrencePeriod() {
        return RECURRENCE_PERIOD_IN_MINUTES * 60 * 1000;
    }

    @Override
    protected void doRun() throws Exception {
        Jenkins jenkins = getJenkinsInstance();
        Integer deletableFlowsAgeInSeconds = getDeletableFlowsAgeInSeconds(jenkins);
        if (deletableFlowsAgeInSeconds == null) {
            LOGGER.info("Old data max life time is not set in Scheduler project config - not deleting anything now");
            return;
        }

        if (LOGGER.isInfoEnabled()) {
            int deletableFlowsAgeInDays = deletableFlowsAgeInSeconds / (24 * 3600);
            LOGGER.info(String.format("Looking for old Build Flow projects (older than %d day(s)) to delete them", deletableFlowsAgeInDays));
        }

        List<BuildFlow> buildFlows = getBuildFlows(jenkins);
        Set<String> deletedProjects = Sets.newHashSet();

        for (BuildFlow buildFlow : buildFlows) {
            if (buildFlow.isBuilding()) {
                continue;
            }
            if (isEligibleForDeletion(buildFlow, deletableFlowsAgeInSeconds)) {
                try {
                    deleteProject(buildFlow);
                    deletedProjects.add(buildFlow.getName());
                } catch (Exception e) {
                    LOGGER.error("Failed to delete project " + buildFlow.getName(), e);
                }
            }
        }

        LOGGER.info(String.format("Deleted %d BuildFlow projects - %s", deletedProjects.size(), deletedProjects));
    }

    @VisibleForTesting
    boolean isEligibleForDeletion(BuildFlow buildFlow, int deletableFlowsAgeInSeconds) {
        FlowRun firstBuild = buildFlow.getFirstBuild();
        if (firstBuild == null) {
            // Project exists but wasn't built
            return true;
        }
        long finishTimestamp = getFinishTimeInMillis(firstBuild);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(finishTimestamp);
        calendar.add(Calendar.SECOND, deletableFlowsAgeInSeconds);

        return calendar.getTime().before(getCurrentTime());
    }

    @VisibleForTesting
    List<BuildFlow> getBuildFlows(Jenkins jenkins) {
        return JenkinsUtils.getProjectsOfType(jenkins, BuildFlow.class);
    }

    Jenkins getJenkinsInstance() {
        return JenkinsUtils.getJenkinsInstance();
    }

    @VisibleForTesting
    long getFinishTimeInMillis(FlowRun firstBuild) {
        return firstBuild.getStartTimeInMillis() + firstBuild.getDuration();
    }

    @VisibleForTesting
    void deleteProject(BuildFlow buildFlow) throws java.io.IOException, InterruptedException {
        buildFlow.delete();
    }

    @VisibleForTesting
    Integer getDeletableFlowsAgeInSeconds(Jenkins jenkins) {
        return CleanupHelper.getDeletableDataAgeInSeconds(jenkins);
    }

    @VisibleForTesting
    Date getCurrentTime() {
        return new Date();
    }
}
