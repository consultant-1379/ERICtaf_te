package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.api.TriggeringTask;
import com.ericsson.cifwk.taf.executor.api.TriggeringTaskBuilder;
import com.ericsson.cifwk.taf.executor.cluster.ClusterBuildITest;
import com.ericsson.cifwk.taf.executor.cluster.RemoteTafLauncher;
import com.ericsson.cifwk.taf.executor.helpers.ScheduleProjectHelper;
import com.ericsson.cifwk.taf.executor.schedule.TafTestExecutor;
import com.ericsson.cifwk.taf.executor.utils.MessageBusUtils;
import hudson.model.Result;
import hudson.model.TopLevelItemDescriptor;
import hudson.remoting.RequestAbortedException;
import hudson.tasks.Builder;
import hudson.util.RunList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.PretendSlave;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RetryIfNodeFailsITest extends RestServiceAwareITest {

    private static final String SCHEDULE_NAME = "schedule.name";
    private static final String SCHEDULE_GROUP_ID = "schedule-groupId";
    private static final String SCHEDULE_ARTIFACT_ID = "schedule-artifactId";
    private static final String SCHEDULE_VERSION = "schedule-version";

    private TafExecutionProject project;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        setUpScheduleProject();

        MessageBusUtils.setGenericBus(ScheduleProjectHelper.mockMessageBus());

        project = setUpExecutionProject();
        List<Builder> builders = project.getBuildersList();
        builders.clear();

        TafTestExecutor executor = mock(TafTestExecutor.class);
        when(executor.runTests(any(TestExecution.class), any(PrintStream.class)))
                .thenThrow(new RequestAbortedException(new IllegalArgumentException()))
                .thenReturn(new TestExecutionResult(TestResult.Status.SUCCESS, null));

        TafExecutionBuilder tafExecutionBuilder = new TafExecutionBuilder() {
            @Override
            public RemoteTafLauncher getRemoteTafLauncher(TestExecution execution, OutputStream remoteOutputStream) {
                return new RemoteTafLauncher(execution, remoteOutputStream) {
                    @Override
                    public TafTestExecutor getTafTestExecutor() {
                        return executor;
                    }
                };
            }
        };
        builders.add(tafExecutionBuilder);
    }

    @After
    public void tearDown() throws Exception {
        deleteAllJobs();
    }

    private void setUpScheduleProject() throws IOException {
        TopLevelItemDescriptor descriptor =
                (TopLevelItemDescriptor) jenkins().getDescriptor(TafScheduleProject.class);
        TafScheduleProject scheduleProject = (TafScheduleProject) jenkins().createProject(descriptor, TAF_SCHEDULE_JOB, true);
        scheduleProject.reportMbDomainId = CommonTestConstants.MB_DOMAIN;

        ScheduleProjectHelper helper = ScheduleProjectHelper.builder()
                .withComponents("one")
                .build();

        ArtifactHelper artifactHelper = mock(ArtifactHelper.class);
        when(artifactHelper.resolveArtefact(
                eq(CommonTestConstants.REPOSITORY_URL),
                eq(SCHEDULE_GROUP_ID + ":" + SCHEDULE_ARTIFACT_ID + ":" + SCHEDULE_VERSION),
                eq(SCHEDULE_NAME))).thenReturn(helper.getSchedule());
        scheduleProject.artifactHelper = artifactHelper;
    }

    @Test
    public void testBuildRetryIfOtherNodeAvailable() throws Exception {
        final String SLAVE1_NAME = "slave1";
        final String SLAVE2_NAME = "slave2";

        ClusterBuildITest.TestLauncher faker1 = new ClusterBuildITest.TestLauncher(0);
        PretendSlave slave1 = jenkinsContext.createPretendSlave(faker1);
        slave1.setLabelString(SLAVE1_NAME + " " + TAFExecutor.TAF_NODE_LABEL);

        ClusterBuildITest.TestLauncher faker2 = new ClusterBuildITest.TestLauncher(0);
        PretendSlave slave2 = jenkinsContext.createPretendSlave(faker2);
        slave2.setLabelString(SLAVE2_NAME + " " + TAFExecutor.TAF_NODE_LABEL);

        jenkins().addNode(slave1);

        triggerBuildFor(testwareVersion, "sutResource", new String[]{"success.xml"});
        Thread.sleep(5000);

        jenkins().addNode(slave2);
        jenkins().removeNode(slave1);

        TafExecutionBuild build1 = waitBuild(project.getBuilds(), 1, 60);

        TafExecutionBuild build2 = waitBuild(project.getBuilds(), 2, 60);

        assertThat(build1.getResult()).isEqualTo(Result.FAILURE);
        assertThat(build2.getResult()).isEqualTo(Result.SUCCESS);
        assertThat(build2.getBuiltOn().getLabelString()).contains(SLAVE2_NAME);
    }

    TafExecutionBuild waitBuild(RunList<TafExecutionBuild> builds, int buildNumber, int seconds) throws InterruptedException {
        TafExecutionBuild build = builds.getLastBuild();
        while ((build == null || build.isBuilding() || build.getNumber() < buildNumber) && seconds > 0) {
            Thread.sleep(1000);
            seconds--;
            build = builds.getLastBuild();
        }
        if (build.isBuilding()) fail("Didn't wait for build:" + build + " complete");
        return build;
    }

    @Override
    protected TriggeringTask createTriggeringTask(String packageVersion, String sutResource, String[] pathsToSchedules) {
        TriggeringTaskBuilder taskBuilder = new TriggeringTaskBuilder()
                .withCiFwkPackage("com.ericsson.cifwk.taf.executor", "te-taf-testware", packageVersion)
                .withNexusURI(CommonTestConstants.REPOSITORY_URL)
                .withTestWare("com.ericsson.cifwk.taf.executor", "te-taf-testware", packageVersion)
                .withSutResource(sutResource)
                .withSchedule(SCHEDULE_GROUP_ID, SCHEDULE_ARTIFACT_ID, SCHEDULE_VERSION, SCHEDULE_NAME);
        return taskBuilder.build();
    }

}
