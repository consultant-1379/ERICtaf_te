package com.ericsson.cifwk.taf.executor;

import com.cloudbees.plugins.flow.FlowRun;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.executor.api.ScheduleRequest;
import com.ericsson.cifwk.taf.executor.api.TafTeAbortBuildResponse;
import com.ericsson.cifwk.taf.executor.api.TafTeBuildDetails;
import com.ericsson.cifwk.taf.executor.api.TafTeBuildTriggerResponse;
import com.ericsson.cifwk.taf.executor.api.TafTeJenkinsJob;
import com.ericsson.cifwk.taf.executor.api.TeRestServiceClient;
import com.ericsson.cifwk.taf.executor.api.TriggeringTask;
import com.ericsson.cifwk.taf.executor.api.TriggeringTaskBuilder;
import com.ericsson.cifwk.taf.executor.helpers.TafScheduleProjectAdapter;
import com.ericsson.cifwk.taf.executor.model.ExecutorBuildParameters;
import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;
import com.ericsson.cifwk.taf.executor.model.ScheduleBuildParameters;
import com.ericsson.cifwk.taf.executor.model.TeBuildMainParameters;
import com.ericsson.cifwk.taf.executor.utils.JenkinsUtils;
import com.ericsson.duraci.datawrappers.ArtifactGav;
import com.ericsson.duraci.datawrappers.ExecutionId;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableWithSize;
import org.hamcrest.collection.IsMapContaining;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.TestBuilder;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TafScheduleProjectITest extends WithSlaveTest {

    public static final String SCHEDULE_GROUP_ID = "schedule-groupId";
    public static final String SCHEDULE_ARTIFACT_ID = "schedule-artifactId";
    public static final String SCHEDULE_VERSION = "schedule-version";
    public static final String SLAVE_1 = TAFExecutor.TAF_MASTER_LABEL;
    public static final String SLAVE_2 = "192.168.0.9";

    public static final List<String> EXPECTED_SUITES = Arrays.asList("first.xml", "second.xml", "third.xml");
    public static final List<String> EXPECTED_DEPENDENCIES = Arrays.asList(
            "first-groupId:first-artifactId:1.0",
            "second-groupId:second-artifactId:2.0",
            "third-groupId:third-artifactId:3.0"
    );

    private static final String MISSING_SCHEDULE_NAME = "idontexist.xml";

    private int tafTeCounter = 0;

    private String executionId;
    private TafScheduleProjectAdapter scheduleProjectAdapter;
    private TafExecutionProject executorProject;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.executorProject = setUpDummyExecutionProject();

        String[][] components = components("first", "second", "third");
        String schedule = schedule(components);

        this.scheduleProjectAdapter = getScheduleProjectAdapterBuilder().build();
        this.scheduleProjectAdapter.resolveScheduleByDefaultAs(
                new ArtifactGav(SCHEDULE_GROUP_ID, SCHEDULE_ARTIFACT_ID, SCHEDULE_VERSION),
                schedule);
        this.scheduleProjectAdapter.resolveScheduleAs(
                new ArtifactGav(SCHEDULE_GROUP_ID, SCHEDULE_ARTIFACT_ID, SCHEDULE_VERSION),
                MISSING_SCHEDULE_NAME, null);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        deleteAllJobs();
    }

    @Test
    public void happyPath() throws Exception {
        executeSuccessfulTestJob();
    }

    @Test
    public void triggerShouldFailOnAbsentSchedule() throws Exception {
        TafTeBuildTriggerResponse response = triggerBuildFor(testwareVersion, "", new String[] { "idontexist.xml" });

        assertThat(response.getJobSchedulingStatus(), equalTo(TafTeBuildTriggerResponse.Status.FAILURE));
        DateTime dateTime = new DateTime(response.getGeneratedAt());
        assertTrue(dateTime.plus(new Period().withMinutes(2)).isAfterNow());
        assertThat(response.getJobUrl(), containsString(TAF_SCHEDULE_JOB));
        String jobExecutionId = response.getJobExecutionId();
        assertNotNull(jobExecutionId);

        // Find the triggered scheduler job
        ExecutionId executionId = new ExecutionId(jobExecutionId);
        TafScheduleBuild schedulerJob = JenkinsUtils.waitForSchedulerJob(scheduleProjectAdapter.getProject(), executionId, 2);
        assertNotNull(schedulerJob);
        assertEquals(Result.FAILURE, schedulerJob.getResult());
        // Should be no other jobs created
        assertEquals(0, JenkinsUtils.findExecutorJobs(executorProject, executionId).size());
        assertNull(JenkinsUtils.getFlowRun(jenkins(), executionId));
    }

    @Test
    public void shouldAbortSpawnedJob() throws Exception {
        TafTeBuildTriggerResponse response = triggerBuildFor(testwareVersion, "", new String[] { "multiple_hang.xml"});
        Thread.sleep(15000);
        String executionId = response.getJobExecutionId();
        TafTeAbortBuildResponse abortResponse = abortBuild(executionId);
        String responseText = abortResponse.getMessage();

        assertTrue(responseText, responseText.contains("All Spawned Jobs are aborted"));
        List<TafExecutionBuild> executionBuilds = JenkinsUtils.findExecutorJobs(executorProject, new ExecutionId(executionId));
        for (TafExecutionBuild tafExecutionBuild : executionBuilds) {
            assertFalse(tafExecutionBuild.isBuilding());
            assertNull(tafExecutionBuild.getExecutor());
        }
        FlowRun flowRun = JenkinsUtils.getFlowRun(jenkins(), new ExecutionId(executionId));
        assertFalse(flowRun.isBuilding());
        assertNull(flowRun.getExecutor());
    }

    @Test
    public void abortInvalidRun() throws Exception {
        TafTeBuildTriggerResponse response = triggerBuildFor(testwareVersion, "", new String[] { "idontexist.xml"});
        String executionId = response.getJobExecutionId();
        // Waiting for schedule parsing to fail, otherwise may be cancelled too early
        Thread.sleep(5000);
        TafTeAbortBuildResponse abortResponse = abortBuild(executionId);
        assertThat(abortResponse.getMessage(), containsString("Test execution run has finished or has not run. Abort invalid"));
        assertEquals(0, JenkinsUtils.findExecutorJobs(executorProject, new ExecutionId(executionId)).size());
        FlowRun flowRun = JenkinsUtils.getFlowRun(jenkins(), new ExecutionId(executionId));
        assertNull(flowRun);
        TafScheduleBuild scheduleBuild = JenkinsUtils.getSchedulerJob(jenkins(), new ExecutionId(executionId));
        assertFalse(scheduleBuild.isBuilding());
    }

    @Test
    public void shouldGetSpawnedJobsViaRestCall() throws Exception {
        executeSuccessfulTestJob();
        TeRestServiceClient teRestServiceClient = getTeRestServiceClient();
        TafTeBuildDetails buildDetails = teRestServiceClient.getBuildDetails(executionId);
        List<TafTeJenkinsJob> spawnedJobs = buildDetails.getTafTeJenkinsJobs();
        Assert.assertThat(spawnedJobs, IsIterableWithSize.<TafTeJenkinsJob>iterableWithSize(5));
        TafTeJenkinsJob flowRun = Iterables.find(spawnedJobs, new Predicate<TafTeJenkinsJob>() {
            @Override
            public boolean apply(TafTeJenkinsJob tafTeJenkinsJob) {
                return tafTeJenkinsJob.getType().equals(TafTeJenkinsJob.Type.FLOW);
            }
        });
        assertNotNull(flowRun);
        assertThat(flowRun.getName(), not(isEmptyOrNullString()));
        assertNotEquals(0, flowRun.getNumber());
        assertThat(flowRun.getFullLogUrl(), Matchers.endsWith("logText/progressiveText"));
        assertThat(flowRun.getUrl(), not(isEmptyOrNullString()));
        assertEquals(flowRun.getRunStatus(), TafTeJenkinsJob.RunStatus.COMPLETE);

        Iterable<TafTeJenkinsJob> executorJobs = Iterables.filter(spawnedJobs, new Predicate<TafTeJenkinsJob>() {
            @Override
            public boolean apply(TafTeJenkinsJob tafTeJenkinsJob) {
                return tafTeJenkinsJob.getType().equals(TafTeJenkinsJob.Type.EXECUTOR);
            }
        });
        Assert.assertThat(executorJobs, IsIterableWithSize.<TafTeJenkinsJob>iterableWithSize(3));

        for (TafTeJenkinsJob executorJob : executorJobs) {
            assertThat(executorJob.getName(), Matchers.startsWith(TAF_EXECUTE_JOB));
            assertNotEquals(0, executorJob.getNumber());
            assertThat(executorJob.getFullLogUrl(), Matchers.endsWith("logText/progressiveText"));
            assertThat(executorJob.getUrl(), containsString(TAF_EXECUTE_JOB));
            assertEquals(executorJob.getRunStatus(), TafTeJenkinsJob.RunStatus.COMPLETE);
        }
    }

    private void executeSuccessfulTestJob() throws Exception {
        TafTeBuildTriggerResponse response = triggerBuildFor(testwareVersion, "", new String[] { "success.xml" });
        // Verify the response
        assertNull(response.getErrorTxt());
        assertThat(response.getJobSchedulingStatus(), equalTo(TafTeBuildTriggerResponse.Status.OK));
        DateTime dateTime = new DateTime(response.getGeneratedAt());
        assertTrue(dateTime.plus(new Period().withMinutes(2)).isAfterNow());
        assertThat(response.getJobUrl(), containsString(TAF_SCHEDULE_JOB));
        String jobExecutionIdStr = response.getJobExecutionId();
        assertNotNull(jobExecutionIdStr);

        // Verify the triggered build
        // Find the triggered scheduler job
        ExecutionId jobExecutionId = new ExecutionId(jobExecutionIdStr);
        TafScheduleBuild schedulerJob = JenkinsUtils.waitForSchedulerJob(scheduleProjectAdapter.getProject(), jobExecutionId, 20);
        assertNotNull(schedulerJob);

        ScheduleBuildParameters buildParameters = JenkinsUtils.getBuildParameters(schedulerJob, ScheduleBuildParameters.class);
        this.executionId = buildParameters.getExecutionId();
        FlowRun flowRun = JenkinsUtils.waitForFlowRun(jenkins(), jobExecutionId, 20);
        String jobName = flowRun.getDisplayName();
        assertNotNull(jobName);

        try {
            while(schedulerJob.isBuilding()) {
                Thread.sleep(10000);
            }
        } catch (InterruptedException e) {
            // ignore
        }

        assertThat(schedulerJob.getResult(), equalTo(Result.SUCCESS));
        assertThat(flowRun.getResult(), equalTo(Result.SUCCESS));
        assertThat(tafTeCounter, is(3));
    }

    @Test
    public void shouldProvideHttpConfig() throws Exception {
        String sutResource = "[\n" +
                "    {\n" +
                "        \"hostname\": \"ms1\", \n" +
                "        \"ip\": \"overridden\", \n" +
                "        \"ports\": {\n" +
                "            \"ssh\": 2201\n" +
                "        }, \n" +
                "        \"type\": \"ms\", \n" +
                "        \"users\": [\n" +
                "            {\n" +
                "                \"password\": \"12shroot\", \n" +
                "                \"type\": \"admin\", \n" +
                "                \"username\": \"root\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }," +
                "{\n" +
                "        \"hostname\": \"sc1\", \n" +
                "        \"ip\": \"10.59.142.48\", \n" +
                "        \"nodes\": [\n" +
                "            {\n" +
                "                \"group\": \"internal_opendj\", \n" +
                "                \"hostname\": \"internal_opendj_su0\", \n" +
                "                \"ip\": \"192.110.50.4\", \n" +
                "                \"ports\": {\n" +
                "                    \"http\": 8080\n" +
                "                }, \n" +
                "                \"tunnel\": 1, \n" +
                "                \"type\": \"http\", \n" +
                "                \"users\": [\n" +
                "                    {\n" +
                "                        \"password\": \"shroot\", \n" +
                "                        \"type\": \"admin\", \n" +
                "                        \"username\": \"root\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }, \n" +
                "            {\n" +
                "                \"group\": \"internal_opendj\", \n" +
                "                \"hostname\": \"internal_opendj_su1\", \n" +
                "                \"ip\": \"192.110.50.5\", \n" +
                "                \"ports\": {\n" +
                "                    \"http\": 8080\n" +
                "                }, \n" +
                "                \"tunnel\": 2, \n" +
                "                \"type\": \"http\", \n" +
                "                \"users\": [\n" +
                "                    {\n" +
                "                        \"password\": \"shroot\", \n" +
                "                        \"type\": \"admin\", \n" +
                "                        \"username\": \"root\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ], \n" +
                "        \"ports\": {\n" +
                "            \"ssh\": 22\n" +
                "        }, \n" +
                "        \"type\": \"sc1\", \n" +
                "        \"users\": [\n" +
                "            {\n" +
                "                \"password\": \"litpc0b6lEr\", \n" +
                "                \"type\": \"admin\", \n" +
                "                \"username\": \"root\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }]";

        Properties commonTestProperties = new Properties();
        commonTestProperties.put("myProperty1", "myValue1");
        commonTestProperties.put("myProperty2", "myValue2");

        ScheduleRequest schedule = new ScheduleRequest(SCHEDULE_GROUP_ID, SCHEDULE_ARTIFACT_ID, SCHEDULE_VERSION, "success.xml");
        Properties testProperties = new Properties();
        testProperties.put("myProperty2", "myNewValue2");
        schedule.setTestProperties(testProperties);

        TriggeringTask triggeringTask = new TriggeringTaskBuilder()
                .withCiFwkPackage("com.ericsson.cifwk.taf.executor", "te-taf-testware", testwareVersion)
                .withNexusURI(CommonTestConstants.REPOSITORY_URL)
                .withSlaveHost(SLAVE_1)
                .withSlaveHost(SLAVE_2)
                .withTestWare("com.ericsson.cifwk.taf.executor", "te-taf-testware", testwareVersion)
                .withSutResource(sutResource)
                .withSchedule(schedule)
                .withTestProperties(commonTestProperties)
                .build();

        TafTeBuildTriggerResponse triggerResponse = triggerBuildForTask(triggeringTask);

        TafScheduleBuild tafScheduleBuild = JenkinsUtils.getSchedulerJob(jenkins(), new ExecutionId(triggerResponse.getJobExecutionId()));
        Assert.assertNotNull(tafScheduleBuild);
        String configUrl = tafScheduleBuild.getConfigUrl();

        String json = getValidResponseFromUrl(configUrl + "?type=hosts");
        List<com.ericsson.cifwk.taf.data.Host> hosts = TafHostUtils.generateHostListFromJson(json);
        com.ericsson.cifwk.taf.data.Host ms1 = HostTestUtils.getHostByName(hosts, "ms1");
        HostTestUtils.validateHost(ms1, HostType.MS, "ms1", "overridden", null,
                null, null, null, null, 2201, 0, "root", "12shroot", UserType.ADMIN);

        String propertiesStr = getValidResponseFromUrl(configUrl + "?type=properties");
        Properties resolvedProperties = new Properties();
        resolvedProperties.load(new StringReader(propertiesStr));
        Assert.assertEquals(2, resolvedProperties.size());
        Assert.assertThat(resolvedProperties, IsMapContaining.<Object, Object>hasEntry("myProperty1", "myValue1"));
        Assert.assertThat(resolvedProperties, IsMapContaining.<Object, Object>hasEntry("myProperty2", "myNewValue2"));
    }

    @Override
    protected TriggeringTask createTriggeringTask(String packageVersion, String sutResource, String[] pathsToSchedules) {
        Map<String, String> miscParams = Maps.newHashMap();

        TriggeringTaskBuilder taskBuilder = new TriggeringTaskBuilder()
                .withCiFwkPackage("com.ericsson.cifwk.taf.executor", "te-taf-testware", packageVersion)
                .withNexusURI(CommonTestConstants.REPOSITORY_URL)
                .withSlaveHost(SLAVE_1)
                .withSlaveHost(SLAVE_2)
                .withTestWare("com.ericsson.cifwk.taf.executor", "te-taf-testware", packageVersion)
                .withMiscProperties(miscParams)
                .withSutResource(sutResource);
        for (String pathToSchedule : pathsToSchedules) {
            taskBuilder.withSchedule(SCHEDULE_GROUP_ID, SCHEDULE_ARTIFACT_ID, SCHEDULE_VERSION, pathToSchedule);
        }
        return taskBuilder.build();
    }

    protected TafExecutionProject setUpDummyExecutionProject() throws IOException {
        TestBuilder tafTeBuilder = new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                ExecutorBuildParameters currentExecutionParams = JenkinsUtils.getBuildParameters(build, ExecutorBuildParameters.class);
                TeBuildMainParameters mainParameters = TeBuildMainParameters.lookup(currentExecutionParams.getExecutionId());
                GlobalTeSettings globalTeSettings = mainParameters.getGlobalTeSettings();
                ScheduleBuildParameters scheduleBuildParameters = mainParameters.getScheduleBuildParameters();

                assertNotNull(mainParameters);
                assertNotNull(scheduleBuildParameters);

                assertThat(globalTeSettings.getMbHostWithPort(), equalTo(CommonTestConstants.MB_HOST_NAME + ":" + CommonTestConstants.MB_PORT));
                assertThat(globalTeSettings.getMbExchange(), equalTo(CommonTestConstants.MB_EXCHANGE));
                assertThat(globalTeSettings.getReportMbDomainId(), equalTo(CommonTestConstants.MB_DOMAIN));
                assertThat(globalTeSettings.getReportsHost(), is(REPORTS_HOST));
                assertThat(globalTeSettings.getLocalReportsStorage(), is(TARGET_LOCAL_REPORTS_STORAGE));

                assertThat(scheduleBuildParameters.getRepositoryUrl(), equalTo(CommonTestConstants.REPOSITORY_URL));
                assertThat(scheduleBuildParameters.getSlaveHosts(), equalTo(SLAVE_1 + "," + SLAVE_2));
                assertThat(scheduleBuildParameters.getConfigUrl(), Matchers.endsWith("/config"));

                assertThat(EXPECTED_SUITES, hasItem(currentExecutionParams.getTafSuites()));
                assertThat(EXPECTED_DEPENDENCIES, hasItem(currentExecutionParams.getTafTestwareGav()));
                assertThat(currentExecutionParams.getTafGroups(), equalTo(SCHEDULE_GROUPS));

                tafTeCounter++;
                return true;
            }
        };
        return setUpExecutionProjectWithCustomBuilder(tafTeBuilder);
    }

}
