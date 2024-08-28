package com.ericsson.cifwk.taf.executor.model;

import com.ericsson.cifwk.taf.executor.TafScheduleBuild;
import com.ericsson.cifwk.taf.executor.TafScheduleProject;
import com.ericsson.cifwk.taf.executor.utils.GlobalTeSettingsProvider;
import com.ericsson.cifwk.taf.executor.utils.JenkinsUtils;
import com.google.common.annotations.VisibleForTesting;
import jenkins.model.Jenkins;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 27/01/2016
 */
public final class TeBuildMainParameters {

    private final GlobalTeSettings globalTeSettings;
    private final ScheduleBuildParameters scheduleBuildParameters;

    @VisibleForTesting
    public TeBuildMainParameters(GlobalTeSettings globalTeSettings, ScheduleBuildParameters scheduleBuildParameters) {
        this.globalTeSettings = globalTeSettings;
        this.scheduleBuildParameters = scheduleBuildParameters;
    }

    public static TeBuildMainParameters lookup(String executionId) {
        ScheduleBuildParameters scheduleBuildParameters = null;
        GlobalTeSettings globalTeSettings = null;

        Jenkins jenkins = JenkinsUtils.getJenkinsInstance();
        TafScheduleProject schedulerProject = JenkinsUtils.getProjectOfType(jenkins, TafScheduleProject.class);
        if (schedulerProject != null) {
            TafScheduleBuild schedulerJob = JenkinsUtils.findSchedulerJob(schedulerProject, executionId);
            scheduleBuildParameters = JenkinsUtils.getBuildParameters(schedulerJob, ScheduleBuildParameters.class);
            globalTeSettings = GlobalTeSettingsProvider.getInstance().provide();
        }

        return new TeBuildMainParameters(globalTeSettings, scheduleBuildParameters);
    }

    public GlobalTeSettings getGlobalTeSettings() {
        return globalTeSettings;
    }

    public ScheduleBuildParameters getScheduleBuildParameters() {
        return scheduleBuildParameters;
    }

}
