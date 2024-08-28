package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.api.TafTeBuildDetails;
import com.ericsson.cifwk.taf.executor.api.TafTeBuildTriggerResponse;
import com.ericsson.cifwk.taf.executor.api.TafTeJenkinsJob;
import com.ericsson.cifwk.taf.executor.api.TriggeringTask;
import com.ericsson.cifwk.taf.executor.api.TriggeringTaskBuilder;
import com.ericsson.cifwk.taf.executor.helpers.TafScheduleProjectAdapter;
import com.ericsson.cifwk.taf.executor.spi.TestTAFExecutorService;
import com.ericsson.duraci.datawrappers.ArtifactGav;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Node;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.TestBuilder;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class ParallelExecutionITest extends RestServiceAwareITest{

    public static final String REPOSITORY_URL = "http://repositoryUrl";
    public static final String SCHEDULE_NAME = "schedule.name";
    public static final String SCHEDULE_GROUP_ID = "schedule-groupId";
    public static final String SCHEDULE_ARTIFACT_ID = "schedule-artifactId";
    public static final String SCHEDULE_VERSION = "schedule-version";
    private static final String SLAVE_1_NAME = "slave1";
    private static final String SLAVE_2_NAME = "slave2";

    private TafScheduleProjectAdapter scheduleProjectAdapter;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        scheduleProjectAdapter = getScheduleProjectAdapterBuilder().build();

        URL scheduleUrl = this.getClass().getClassLoader().getResource("schedule/big_parallel.xml");
        String scheduleXml = Resources.toString(scheduleUrl, Charset.defaultCharset());

        setUpSchedulerProjectForSchedule("big_parallel.xml", scheduleXml);

        createSlaveNode(SLAVE_1_NAME, 3);
        createSlaveNode(SLAVE_2_NAME, 3);
    }

    @After
    public void tearDown() throws Exception {
        deleteAllJobs();
        shutdownSlave(SLAVE_1_NAME);
        shutdownSlave(SLAVE_2_NAME);
    }

    @Test
    public void slaveLogsShouldBeSynchronized() throws Exception {
        setUpExecutionProject();
        TafTeBuildTriggerResponse triggerResponse = triggerBuildFor(testwareVersion, "", new String[]{"big_parallel.xml"});
        String jobExecutionIdStr = triggerResponse.getJobExecutionId();

        TafTeBuildDetails buildDetails = waitUntilBuildFinished(jobExecutionIdStr);

        List<TafTeJenkinsJob> executors = buildDetails.getTafTeJenkinsJobs(TafTeJenkinsJob.Type.EXECUTOR);
        for (TafTeJenkinsJob executorJob : executors) {
            String fullLogUrl = executorJob.getFullLogUrl();
            String logText = getValidResponseFromUrl(fullLogUrl);
            verifyExecutorLog(logText, executorJob.getScheduleItemName());
        }
    }

    @Test
    public void shouldUseAllSlavesIfUnspecified() throws Exception {
        Set<String> nodesUsed = Sets.newHashSet();
        setUpDummyExecutionProject(nodesUsed);

        TafTeBuildTriggerResponse triggerResponse = triggerBuildFor(testwareVersion, "", new String[]{"big_parallel.xml"});
        String jobExecutionIdStr = triggerResponse.getJobExecutionId();

        waitUntilBuildFinished(jobExecutionIdStr);

        Assert.assertThat(nodesUsed, hasItem(SLAVE_1_NAME));
        Assert.assertThat(nodesUsed, hasItem(SLAVE_2_NAME));
    }

    private void verifyExecutorLog(String logText, String scheduleItemName) {
        String recordPattern = String.format(TestTAFExecutorService.LOG_RECORD_PATTERN, scheduleItemName);
        Pattern pattern = Pattern.compile(recordPattern, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(logText);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        Assert.assertEquals(String.format("Log text is inconsistent: %s; failed to match pattern %s enough times",
                        logText, recordPattern),
                TestTAFExecutorService.JOB_STEPS_AMOUNT, count);
    }

    private TafExecutionProject setUpDummyExecutionProject(final Set<String> nodeRegistry) throws IOException {
        TestBuilder dummyBuilder = new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                Node builtOn = build.getBuiltOn();
                nodeRegistry.add(builtOn.getNodeName());
                return true;
            }
        };

        return setUpExecutionProjectWithCustomBuilder(dummyBuilder);
    }

    private void setUpSchedulerProjectForSchedule(String schedulePath, String scheduleXmlToUse) {
        scheduleProjectAdapter.resolveScheduleAs(new ArtifactGav(SCHEDULE_GROUP_ID, SCHEDULE_ARTIFACT_ID, SCHEDULE_VERSION),
                schedulePath, scheduleXmlToUse);
    }

    @Override
    protected TriggeringTask createTriggeringTask(String packageVersion, String sutResource, String[] pathsToSchedules) {
        TriggeringTaskBuilder taskBuilder = new TriggeringTaskBuilder()
                .withCiFwkPackage("com.ericsson.cifwk.taf.executor", "te-taf-testware", packageVersion)
                .withNexusURI(REPOSITORY_URL)
                .withTestWare("com.ericsson.cifwk.taf.executor", "te-taf-testware", packageVersion)
                .withSutResource(sutResource);
        for (String pathToSchedule : pathsToSchedules) {
            taskBuilder.withSchedule(SCHEDULE_GROUP_ID, SCHEDULE_ARTIFACT_ID, SCHEDULE_VERSION, pathToSchedule);
        }
        return taskBuilder.build();
    }
}
