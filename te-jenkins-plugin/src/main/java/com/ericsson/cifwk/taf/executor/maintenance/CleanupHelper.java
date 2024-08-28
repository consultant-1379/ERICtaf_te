package com.ericsson.cifwk.taf.executor.maintenance;

import com.ericsson.cifwk.taf.executor.utils.JenkinsUtils;
import com.ericsson.cifwk.taf.executor.TafScheduleProject;
import jenkins.model.Jenkins;

class CleanupHelper {

    public static Integer getDeletableDataAgeInSeconds(Jenkins jenkins) {
        TafScheduleProject scheduleProject = JenkinsUtils.getProjectOfType(jenkins, TafScheduleProject.class);
        if (scheduleProject == null) {
            // Plugin installed, but the scheduler project is still not set up
            return null;
        }
        return scheduleProject.getDeletableFlowsAgeInSeconds();
    }
}
