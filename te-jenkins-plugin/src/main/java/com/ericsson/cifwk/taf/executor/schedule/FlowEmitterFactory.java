package com.ericsson.cifwk.taf.executor.schedule;

import com.ericsson.cifwk.taf.executor.api.schedule.model.Schedule;
import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;
import com.ericsson.cifwk.taf.executor.model.ScheduleBuildParameters;

public class FlowEmitterFactory {

    public FlowEmitter create(Schedule schedule, ScheduleItemGavResolver scheduleItemGavResolver,
                              GlobalTeSettings globalTeSettings, ScheduleBuildParameters mainBuildParameters) {
        return new FlowEmitter(schedule, scheduleItemGavResolver, globalTeSettings, mainBuildParameters);
    }
}
