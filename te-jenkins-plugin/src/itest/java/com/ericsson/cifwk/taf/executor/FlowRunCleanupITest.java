package com.ericsson.cifwk.taf.executor;

import com.cloudbees.plugins.flow.BuildFlow;
import com.ericsson.cifwk.taf.executor.api.TafTeBuildTriggerResponse;
import com.ericsson.cifwk.taf.executor.api.TriggeringTask;
import com.ericsson.cifwk.taf.executor.api.TriggeringTaskBuilder;
import com.ericsson.cifwk.taf.executor.helpers.TafScheduleProjectAdapter;
import com.ericsson.cifwk.taf.executor.maintenance.FlowRunCleanup;
import com.ericsson.cifwk.taf.executor.utils.JenkinsUtils;
import com.ericsson.duraci.datawrappers.ArtifactGav;
import hudson.ExtensionList;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.PeriodicWork;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.TestBuilder;

import java.io.IOException;

import static com.ericsson.cifwk.taf.executor.api.TafTeBuildTriggerResponse.Status.OK;
import static org.junit.Assert.assertEquals;

public class FlowRunCleanupITest extends WithSlaveTest {

    private static final String SCHEDULE_GROUP_ID = "schedule-groupId";
    private static final String SCHEDULE_ARTIFACT_ID = "schedule-artifactId";
    private static final String SCHEDULE_VERSION = "schedule-version";

    private TafScheduleProjectAdapter scheduleProjectAdapter;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        String[][] components = components("first");
        String schedule = schedule(components);
        this.scheduleProjectAdapter = getScheduleProjectAdapterBuilder().build();
        this.scheduleProjectAdapter.resolveScheduleByDefaultAs(
                new ArtifactGav(SCHEDULE_GROUP_ID, SCHEDULE_ARTIFACT_ID, SCHEDULE_VERSION),
                schedule);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void shouldDeleteOldBuildFlows() throws Exception {
        setUpExecutionProjectWithCustomBuilder(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                return true;
            }
        });
        TafTeBuildTriggerResponse triggerResponse = triggerBuildFor(testwareVersion, "", new String[] {"success.xml"});

        assertEquals(OK, triggerResponse.getJobSchedulingStatus());

        String jobExecutionIdStr = triggerResponse.getJobExecutionId();

        waitUntilBuildFinished(jobExecutionIdStr);

        assertEquals(1, JenkinsUtils.getProjectsOfType(jenkins(), BuildFlow.class).size());

        ExtensionList<PeriodicWork> periodicWorks = PeriodicWork.all();
        FlowRunCleanup flowRunCleanup = periodicWorks.get(FlowRunCleanup.class);
        Assert.assertNotNull(flowRunCleanup);

        scheduleProjectAdapter.getProject().setDeletableFlowsAgeInSeconds(1);

        Thread.sleep(1100);

        flowRunCleanup.run();

        // It takes Jenkins some time to delete the job (async) - so need to wait
        Thread.sleep(7000);

        assertEquals(0, JenkinsUtils.getProjectsOfType(jenkins(), BuildFlow.class).size());
    }

    @Override
    protected TriggeringTask createTriggeringTask(String packageVersion, String sutResource, String[] pathsToSchedules) {
        TriggeringTaskBuilder taskBuilder = new TriggeringTaskBuilder()
                .withCiFwkPackage("com.ericsson.cifwk.taf.executor", "te-taf-testware", packageVersion)
                .withNexusURI(CommonTestConstants.REPOSITORY_URL)
                .withTestWare("com.ericsson.cifwk.taf.executor", "te-taf-testware", packageVersion)
                .withSlaveHost(TAFExecutor.TAF_MASTER_LABEL)
                .withSutResource(sutResource);
        for (String pathToSchedule : pathsToSchedules) {
            taskBuilder.withSchedule(SCHEDULE_GROUP_ID, SCHEDULE_ARTIFACT_ID, SCHEDULE_VERSION, pathToSchedule);
        }
        return taskBuilder.build();
    }

}
