package com.ericsson.cifwk.taf.execution.operator.impl;

import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.execution.operator.RestOperator;
import com.ericsson.cifwk.taf.execution.operator.model.TestDataContext;
import com.ericsson.cifwk.taf.executor.api.ScheduleRequest;
import com.ericsson.cifwk.taf.executor.api.TafTeAbortBuildResponse;
import com.ericsson.cifwk.taf.executor.api.TafTeBuildTriggerResponse;
import com.ericsson.cifwk.taf.executor.api.TeRestServiceClient;
import com.ericsson.cifwk.taf.executor.api.TriggeringTask;
import com.ericsson.cifwk.taf.executor.api.TriggeringTaskBuilder;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;

import java.util.Properties;

@Operator
public class RestOperatorImpl implements RestOperator {

    private TeRestServiceClient client;

    @Override
    public void init(Host teHost) {
        String teHostIp = teHost.getIp();
        String httpPort = teHost.getPort().get(Ports.HTTP);
        client = TeRestServiceClient.Builder
                .forHost(teHostIp, Integer.parseInt(httpPort))
                .build();
    }

    @Override
    public TafTeBuildTriggerResponse triggerBuild(String testwareGroupId, String testwareArtifactId, String pathToSchedule) {
        return triggerBuild(triggeringTaskBuilderFor(testwareGroupId, testwareArtifactId, pathToSchedule).build());
    }

    @Override
    public TafTeBuildTriggerResponse triggerBuild(String testwareGroupId, String testwareArtifactId, ScheduleRequest schedule) {
        return triggerBuild(triggeringTaskBuilderFor(testwareGroupId, testwareArtifactId, schedule).build());
    }

    @Override
    public final TeRestServiceClient getTeRestServiceClient() {
        return client;
    }

    @Override
    public TafTeAbortBuildResponse abortBuild(String executionId) {
        return client.abortBuild(executionId);
    }

    @Override
    public TafTeBuildTriggerResponse triggerBuild(TriggeringTask triggeringTask) {
        addDummyTestTriggerDetails(triggeringTask);
        return client.triggerBuild(triggeringTask);
    }

    private void addDummyTestTriggerDetails(TriggeringTask triggeringTask) {
        ImmutableMap<String, String> map = ImmutableMap.of("ISO version", "dummyIsoVersion", "Jenkins job name", "dummyJob",
                "Link to Jenkins job", "http://myjenkins:8080/job/1", "Current sprint", "15.4");
        Properties testTriggerDetails = new Properties();
        testTriggerDetails.putAll(map);
        triggeringTask.setTestTriggerDetails(testTriggerDetails);
    }

    @Override
    public TriggeringTaskBuilder triggeringTaskBuilderFor(String testwareGroupId, String testwareArtifactId, String pathToSchedule) {
        String testWareVersion = getPluginVersion();

        TriggeringTaskBuilder taskBuilder = createBasicTaskBuilder(testwareGroupId, testwareArtifactId);
        taskBuilder.withSchedule(testwareGroupId, testwareArtifactId, testWareVersion, pathToSchedule);

        return taskBuilder;
    }

    @VisibleForTesting
    String getPluginVersion() {
        return TestDataContext.getPluginVersion();
    }

    @Override
    public TriggeringTaskBuilder triggeringTaskBuilderFor(String testwareGroupId, String testwareArtifactId, ScheduleRequest schedule) {
        TriggeringTaskBuilder taskBuilder = createBasicTaskBuilder(testwareGroupId, testwareArtifactId);
        taskBuilder.withSchedule(schedule);

        return taskBuilder;
    }

    private TriggeringTaskBuilder createBasicTaskBuilder(String testwareGroupId, String testwareArtifactId) {
        TriggeringTaskBuilder taskBuilder = new TriggeringTaskBuilder();
        String testWareVersion = getPluginVersion();
        return taskBuilder
                .withCiFwkPackage(testwareGroupId, testwareArtifactId, testWareVersion)
                .withNexusURI(getNexusUri())
                .withTestWare(testwareGroupId, testwareArtifactId, testWareVersion)
                .withSutResource("")
                .withJobType("RFA");
    }

    @VisibleForTesting
    String getNexusUri() {
        return TestDataContext.getNexusUri();
    }

}
