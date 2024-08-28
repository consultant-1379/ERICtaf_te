package com.ericsson.cifwk.taf.execution.operator;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.executor.api.ScheduleRequest;
import com.ericsson.cifwk.taf.executor.api.TafTeAbortBuildResponse;
import com.ericsson.cifwk.taf.executor.api.TafTeBuildTriggerResponse;
import com.ericsson.cifwk.taf.executor.api.TeRestServiceClient;
import com.ericsson.cifwk.taf.executor.api.TriggeringTask;
import com.ericsson.cifwk.taf.executor.api.TriggeringTaskBuilder;

public interface RestOperator {

    void init(Host teHost);

    TafTeBuildTriggerResponse triggerBuild(String testwareGroupId, String testwareArtifactId,
                                           String pathToSchedule);

    TafTeBuildTriggerResponse triggerBuild(String testwareGroupId, String testwareArtifactId,
                                           ScheduleRequest schedule);

    TafTeBuildTriggerResponse triggerBuild(TriggeringTask triggeringTask);

    TafTeAbortBuildResponse abortBuild(String executionId);

    TeRestServiceClient getTeRestServiceClient();

    TriggeringTaskBuilder triggeringTaskBuilderFor(String testwareGroupId, String testwareArtifactId, String pathToSchedule);

    TriggeringTaskBuilder triggeringTaskBuilderFor(String testwareGroupId, String testwareArtifactId, ScheduleRequest schedule);

}
