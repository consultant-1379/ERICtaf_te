package com.ericsson.cifwk.taf.executor.api;

import com.ericsson.cifwk.taf.executor.api.healthcheck.HealthCheck;
import com.ericsson.cifwk.taf.executor.api.schedule.model.Schedule;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleChild;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleComponent;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleEnvironmentProperty;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleItem;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleItemGroup;
import com.google.gson.GsonBuilder;
import org.apache.http.client.HttpClient;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableWithSize;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNot;
import org.hamcrest.text.IsEmptyString;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TeRestServiceClientImplTest {

    @Test
    public void shouldGetHostDetails() throws Exception {
        TeRestServiceClientImpl unit = new TeRestServiceClientImpl("localhost", 8080);
        Assert.assertEquals("localhost", unit.getHostAddress());
        Assert.assertEquals(8080, unit.getHostPort());
    }

    @Test
    public void shouldGetHttpClient() throws Exception {
        TeRestServiceClientImpl unit = new TeRestServiceClientImpl("localhost", 8080);
        HttpClient result = unit.getHttpClient();
        Assert.assertNotNull(result);
        Assert.assertNotSame(unit.httpClient, result);
    }

    @Test
    public void buildTriggerUrl() throws Exception {
        TeRestServiceClientImpl unit = new TeRestServiceClientImpl("localhost", 8080);
        Assert.assertEquals("http://localhost:8080/jenkins/descriptorByName/" +
                "com.ericsson.cifwk.taf.executor.TeRestService/trigger", unit.buildTriggerUrl());
    }

    @Test
    public void buildSpawnedJobsInfoUrl() throws Exception {
        TeRestServiceClientImpl unit = new TeRestServiceClientImpl("localhost", 8080);
        Assert.assertEquals("http://localhost:8080/jenkins/descriptorByName/" +
                "com.ericsson.cifwk.taf.executor.TeRestService/getSpawnedJobsDetails?jobExecutionId=aa-bb-cc",
                unit.buildInfoUrl("aa-bb-cc"));
    }

    @Test
    public void testBuildHealthCheckUrl() {
        TeRestServiceClientImpl unit = new TeRestServiceClientImpl("localhost", 8080);
        Assert.assertEquals("http://localhost:8080/jenkins/descriptorByName/" +
                        "com.ericsson.cifwk.taf.executor.healthcheck.HealthCheck/healthCheck",
                unit.buildHealthCheckUrl());
    }

    @Test
    public void testDeserializeTeHealthCheckDetails() {
        String healthCheckJson = "[\n" +
                "  {\n" +
                "    \"description\": \"\",\n" +
                "    \"name\": \"Jenkins URL is set\",\n" +
                "    \"passed\": true,\n" +
                "    \"scope\": \"Jenkins\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"description\": \"\",\n" +
                "    \"name\": \"Jenkins master has workers\",\n" +
                "    \"passed\": true,\n" +
                "    \"scope\": \"Jenkins\"\n" +
                "  }\n" +
                "]\n";


        TeRestServiceClientImpl unit = new TeRestServiceClientImpl("localhost", 8080);
        List<HealthCheck> healthChecks = unit.deserializeTeHealthCheckDetails(healthCheckJson);

        MatcherAssert.assertThat(healthChecks.size(), Is.is(2));
        MatcherAssert.assertThat(healthChecks.remove(0).getPassed(), Is.is(true));
        MatcherAssert.assertThat(healthChecks.remove(0).getPassed(), Is.is(true));
    }

    @Test
    public void shouldDeserializeJobInfos() throws Exception {

        String buildDetailsJson = "{\"generatedAt\":\"Apr 20, 2015 10:01:56 AM\",\"tafTeJenkinsJobs\":[{\"type\":" +
                "\"SCHEDULER\",\"name\":\"SCHEDULER\",\"itemName\":\"SCHEDULER\",\"number\":1,\"url\":" +
                "\"http://SCHEDULER/1\",\"fullLogUrl\":\"http://SCHEDULER/1/log\",\"runStatus\":\"COMPLETE\",\"result\":\"SUCCESS\"},{" +
                "\"type\":\"FLOW\",\"name\":\"FLOW\",\"itemName\":\"FLOW\",\"number\":1,\"url\":\"http://FLOW/1\"," +
                "\"fullLogUrl\":\"http://FLOW/1/log\"," +
                "\"runStatus\":\"COMPLETE\",\"result\":\"SUCCESS\"},{\"type\":\"EXECUTOR\",\"name\":\"EXECUTOR\"," +
                "\"itemName\":\"EXECUTOR\",\"number\":1,\"url\":\"http://EXECUTOR/1\",\"fullLogUrl\":" +
                "\"http://EXECUTOR/1/log\",\"runStatus\":" +
                "\"COMPLETE\",\"result\":\"SUCCESS\"},{\"type\":\"EXECUTOR\",\"name\":\"EXECUTOR\",\"itemName\":" +
                "\"EXECUTOR\",\"number\":2,\"url\":\"http://EXECUTOR/2\",\"fullLogUrl\":\"http://EXECUTOR/2/log\"," +
                "\"runStatus\":\"COMPLETE\",\"result\":" +
                "\"FAILURE\"},{\"type\":\"EXECUTOR\",\"name\":\"EXECUTOR\",\"itemName\":\"EXECUTOR\",\"number\":3," +
                "\"url\":\"http://EXECUTOR/3\",\"fullLogUrl\":\"http://EXECUTOR/3/log\"," +
                "\"runStatus\":\"COMPLETE\",\"result\":\"FAILURE\"}]," +
                "\"allureLogUrl\":\"allureLogUrl\",\"buildComplete\":true,\"success\":false,\"jobExecutionId\":" +
                "\"jobExecutionId\",\"schedule\":{\"children\":[{\"name\":\"Item1\",\"component\":{\"groupId\":" +
                "\"group1\",\"artifactId\":\"artifact1\"},\"stopOnFail\":true},{\"children\":[{\"name\":\"Item2\"," +
                "\"component\":{\"groupId\":\"group2\",\"artifactId\":\"artifact2\"},\"stopOnFail\":true},{" +
                "\"name\":\"Item3\",\"component\":{\"groupId\":\"group3\",\"artifactId\":\"artifact3\"}," +
                "\"stopOnFail\":true}],\"parallel\":true}]}}";
        verifyDeserializedBuildDetails(buildDetailsJson);
    }

    @Test
    public void shouldDeserializeTafTeBuildDetails() throws Exception {
        List<TafTeJenkinsJob> jobs = Arrays.asList(
                createJob(TafTeJenkinsJobImpl.Type.SCHEDULER, 1, TafTeJenkinsJob.RunStatus.COMPLETE, TafTeJenkinsJob.Result.SUCCESS),
                createJob(TafTeJenkinsJobImpl.Type.FLOW, 1, TafTeJenkinsJob.RunStatus.COMPLETE, TafTeJenkinsJob.Result.SUCCESS),
                createJob(TafTeJenkinsJobImpl.Type.EXECUTOR, 1, TafTeJenkinsJob.RunStatus.COMPLETE, TafTeJenkinsJob.Result.SUCCESS),
                createJob(TafTeJenkinsJobImpl.Type.EXECUTOR, 2, TafTeJenkinsJob.RunStatus.COMPLETE, TafTeJenkinsJob.Result.FAILURE),
                createJob(TafTeJenkinsJobImpl.Type.EXECUTOR, 3, TafTeJenkinsJob.RunStatus.COMPLETE, TafTeJenkinsJob.Result.FAILURE)
        );
        ScheduleComponent scheduleComponent1 = new ScheduleComponent("group1", "artifact1");
        List<ScheduleEnvironmentProperty> emptyEnvironmentProperties = Arrays.asList();
        ScheduleChild scheduleChild1 = new ScheduleItem(null, "Item1", scheduleComponent1, null, null, null, true, null, emptyEnvironmentProperties);
        ScheduleComponent scheduleComponent2 = new ScheduleComponent("group2", "artifact2");
        ScheduleChild scheduleChild2 = new ScheduleItem(null, "Item2", scheduleComponent2, null, null, null, true, null, emptyEnvironmentProperties);
        ScheduleComponent scheduleComponent3 = new ScheduleComponent("group3", "artifact3");
        ScheduleChild scheduleChild3 = new ScheduleItem(null, "Item3", scheduleComponent3, null, null, null, true, null, emptyEnvironmentProperties);
        List<ScheduleChild> groupList = Arrays.asList(scheduleChild2, scheduleChild3);
        ScheduleChild scheduleChild4 = new ScheduleItemGroup(null, groupList, true, emptyEnvironmentProperties);
        Schedule schedule = new Schedule(Arrays.asList(scheduleChild1, scheduleChild4), emptyEnvironmentProperties);
        TafTeBuildDetails tafTeBuildDetails = new TafTeBuildDetails(jobs, "allureLogUrl", "jobExecutionId", schedule);

        String json = new GsonBuilder().create().toJson(tafTeBuildDetails);
        verifyDeserializedBuildDetails(json);
    }

    private void verifyDeserializedBuildDetails(String buildDetailsJson) {
        TeRestServiceClientImpl unit = new TeRestServiceClientImpl();
        TafTeBuildDetails tafTeBuildDetails = unit.deserializeTafTeBuildDetails(buildDetailsJson);

        List<TafTeJenkinsJob> spawnedJobs = tafTeBuildDetails.getTafTeJenkinsJobs();
        Schedule schedule = tafTeBuildDetails.getSchedule();
        Assert.assertThat(spawnedJobs, IsIterableWithSize.<TafTeJenkinsJob>iterableWithSize(5));

        for (TafTeJenkinsJob spawnedJob : spawnedJobs) {
            Assert.assertThat(spawnedJob.getName(), IsNot.not(IsEmptyString.isEmptyOrNullString()));
            Assert.assertThat(spawnedJob.getNumber(), Is.is(CoreMatchers.both(Matchers.greaterThan(0)).and(Matchers.lessThan(4))));
            Assert.assertNotNull(spawnedJob.getRunStatus());
            Assert.assertNotNull(spawnedJob.getType());
            Assert.assertThat(spawnedJob.getFullLogUrl(), IsNot.not(IsEmptyString.isEmptyOrNullString()));
            Assert.assertThat(spawnedJob.getUrl(), IsNot.not(IsEmptyString.isEmptyOrNullString()));
        }
        ScheduleItem scheduleItem1 = (ScheduleItem) schedule.getChildren().get(0);
        Assert.assertEquals("artifact1", scheduleItem1.getComponent().getArtifactId());
        ScheduleItemGroup scheduleGroup = (ScheduleItemGroup) schedule.getChildren().get(1);
        Assert.assertEquals(true, scheduleGroup.isParallel());
        Assert.assertEquals(2, scheduleGroup.getChildren().size());
    }

    private TafTeJenkinsJob createJob(TafTeJenkinsJob.Type type, int number,
                                      TafTeJenkinsJob.RunStatus runStatus, TafTeJenkinsJob.Result result) {
        String name = type.toString();
        return new TafTeJenkinsJobImpl(type, name, name, number,
                "http://" + name + "/" + number,
                "http://" + name + "/" + number + "/log",
                runStatus, result);
    }
}
