package com.ericsson.cifwk.taf.executor.schedule;

import com.ericsson.cifwk.taf.executor.ArtifactHelper;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleLoader;

public class ScheduleLoaderFactory {

    public ScheduleLoaderFactory() {
    }

    public ScheduleLoader createForTafSchedule(String tafSchedulerUrl) {
        return new TafScheduleLoader(tafSchedulerUrl);
    }

    public ScheduleLoader createForGavSchedule(String repositoryUrl, ScheduleItemGavResolver scheduleItemGavResolver) {
        return new ScheduleGavLoader(repositoryUrl, scheduleItemGavResolver, new ArtifactHelper());
    }

}
