package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.api.Host;
import com.ericsson.cifwk.taf.executor.api.TriggeringTask;
import com.ericsson.cifwk.taf.executor.api.TriggeringTaskBuilder;
import com.ericsson.cifwk.taf.executor.cluster.RemoteTafLauncher;
import com.ericsson.cifwk.taf.executor.eiffel.EiffelMessageBus;
import com.ericsson.cifwk.taf.executor.helpers.ScheduleProjectHelper;
import com.ericsson.cifwk.taf.executor.mocks.RemoteTafLauncherMocks;
import com.ericsson.cifwk.taf.executor.utils.MessageBusUtils;
import com.ericsson.duraci.datawrappers.ResultCode;
import com.ericsson.duraci.eiffelmessage.messages.EiffelMessage;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelJobFinishedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelJobStartedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelTestSuiteFinishedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelTestSuiteStartedEvent;
import com.ericsson.duraci.eiffelmessage.sending.MessageSender;
import hudson.model.FreeStyleProject;
import hudson.model.TopLevelItemDescriptor;
import hudson.model.labels.LabelAtom;
import hudson.remoting.RequestAbortedException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.OutputStream;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EiffelEventsITest extends WithSlaveTest {

    private static final String TEST_EXECUTOR = "teJobName";
    private static final String SCHEDULE_NAME = "schedule.name";
    private static final String SCHEDULE_GROUP_ID = "schedule-groupId";
    private static final String SCHEDULE_ARTIFACT_ID = "schedule-artifactId";
    private static final String SCHEDULE_VERSION = "schedule-version";

    private ArrayList<EiffelMessage> messages;
    private TafScheduleProject scheduleProject;
    private FreeStyleProject executorProject;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        final MessageSender messageSender = mock(MessageSender.class);
        EiffelMessageBus messageBus = new EiffelMessageBus(CommonTestConstants.MB_DOMAIN) {
            @Override
            public void connect(String hostName, String exchange) {
                this.sender = messageSender;
            }
        };
        messageBus.connect(null, null);

        MessageBusUtils.setGenericBus(messageBus);

        messages = new ArrayList<>();
        doAnswer(invocation -> {
            Object[] arguments = invocation.getArguments();
            EiffelMessage message = (EiffelMessage) arguments[0];
            messages.add(message);
            return null;
        }).when(messageSender).send(any(EiffelMessage.class));

        TopLevelItemDescriptor descriptor =
                (TopLevelItemDescriptor) jenkins().getDescriptor(TafScheduleProject.class);
        scheduleProject = (TafScheduleProject) jenkins().createProject(descriptor, TAF_SCHEDULE_JOB, true);
        scheduleProject.reportMbDomainId = CommonTestConstants.MB_DOMAIN;

        // TODO: replace with TafExecutionProject
        executorProject = jenkinsContext.createFreeStyleProject(TEST_EXECUTOR);
        executorProject.setAssignedLabel(new LabelAtom(TAFExecutor.TAF_NODE_LABEL));
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        deleteJob(TAF_SCHEDULE_JOB);
        deleteJob(TEST_EXECUTOR);
    }

    @Test
    public void testTwo() throws Exception {
        final TafExecutionBuilder tafExecutionBuilder = new TafExecutionBuilder() {
            @Override
            public RemoteTafLauncher getRemoteTafLauncher(TestExecution execution, OutputStream remoteOutputStream) {
                return RemoteTafLauncherMocks.withResult(execution, remoteOutputStream, new TestExecutionResult(TestResult.Status.SUCCESS, null));
            }
        };

        setTafExecutionBuilder(tafExecutionBuilder);

        TafScheduleBuild build = testComponents("one", "two");
        checkEventFlow(2);

        String executionId = build.getBuildVariables().get(BuildParameterNames.EIFFEL_JOB_EXECUTION_ID);
        EiffelJobStartedEvent jobStartedEvent = (EiffelJobStartedEvent) messages.get(0).getEvent();

        assertEquals(executionId, jobStartedEvent.getJobExecutionId().toString());
    }

    private boolean setTafExecutionBuilder(TafExecutionBuilder tafExecutionBuilder) {
        return executorProject.getBuildersList().add(tafExecutionBuilder);
    }

    @Test
    @Ignore("Ignored because this test causes others to fail if run in parallel")
    public void testAbort() throws Exception {
        final TafExecutionBuilder tafExecutionBuilder = new TafExecutionBuilder() {
            @Override
            public RemoteTafLauncher getRemoteTafLauncher(TestExecution execution, OutputStream remoteOutputStream) {
                return RemoteTafLauncherMocks.withException(execution, remoteOutputStream, new RequestAbortedException(new RuntimeException()));
            }
        };
        setTafExecutionBuilder(tafExecutionBuilder);

        testComponents("one");
        checkEventFlow(1);

        EiffelTestSuiteFinishedEvent finishedEvent =
                (EiffelTestSuiteFinishedEvent) messages.get(3).getEvent();
        assertEquals(ResultCode.ABORTED, finishedEvent.getResultCode());
    }

    @Test
    public void testException() throws Exception {
        final TafExecutionBuilder tafExecutionBuilder = new TafExecutionBuilder() {
            @Override
            public RemoteTafLauncher getRemoteTafLauncher(TestExecution execution, OutputStream remoteOutputStream) {
                return RemoteTafLauncherMocks.withException(execution, remoteOutputStream, new RuntimeException());
            }
        };
        setTafExecutionBuilder(tafExecutionBuilder);

        testComponents("one");
        checkEventFlow(1);

        EiffelTestSuiteFinishedEvent finishedEvent =
                (EiffelTestSuiteFinishedEvent) messages.get(3).getEvent();
        assertEquals(ResultCode.FAILURE, finishedEvent.getResultCode());
    }

    private TafScheduleBuild testComponents(String... components) throws InterruptedException {
        ScheduleProjectHelper helper = ScheduleProjectHelper.builder()
                .withComponents(components)
                .build();

        ArtifactHelper artifactHelper = mock(ArtifactHelper.class);
        when(artifactHelper.resolveArtefact(
                eq(CommonTestConstants.REPOSITORY_URL),
                eq(SCHEDULE_GROUP_ID + ":" + SCHEDULE_ARTIFACT_ID + ":" + SCHEDULE_VERSION),
                eq(SCHEDULE_NAME))).thenReturn(helper.getSchedule());
        scheduleProject.artifactHelper = artifactHelper;

        triggerBuildFor(testwareVersion, "sutResource", new String[] { "success.xml" });
        return ScheduleProjectHelper.waitForBuild(scheduleProject.getBuilds(), 60);
    }

    private void checkEventFlow(int jobStepCount) {
        assertEquals(jobStepCount * 2 + 4, messages.size());
        EiffelMessage firstMessage = messages.get(0);
        EiffelMessage lastMessage = messages.get(jobStepCount * 2 + 3);
        EiffelMessage secondMessage = messages.get(1);
        EiffelMessage secondLastMessage = messages.get(jobStepCount * 2 + 2);
        assertTrue(firstMessage.getEvent() instanceof EiffelJobStartedEvent);
        assertTrue(lastMessage.getEvent() instanceof EiffelJobFinishedEvent);
        assertTrue(secondMessage.getEvent() instanceof EiffelTestSuiteStartedEvent);
        assertTrue(secondLastMessage.getEvent() instanceof EiffelTestSuiteFinishedEvent);
        for (int i = 1; i <= jobStepCount; i++) {
            EiffelMessage startMessage = messages.get(i * 2);
            EiffelMessage finishMessage = messages.get(i * 2 + 1);
            assertTrue(startMessage.getEvent() instanceof EiffelTestSuiteStartedEvent);
            assertTrue(finishMessage.getEvent() instanceof EiffelTestSuiteFinishedEvent);
            assertTrue(startMessage.getInputEventIds().get(0).equals(firstMessage.getEventId()));
            assertTrue(finishMessage.getInputEventIds().get(0).equals(startMessage.getEventId()));
        }
    }

    @Override
    protected TriggeringTask createTriggeringTask(String packageVersion, String sutResource, String[] pathsToSchedules) {
        TriggeringTaskBuilder taskBuilder = new TriggeringTaskBuilder()
                .withCiFwkPackage("com.ericsson.cifwk.taf.executor", "te-taf-testware", packageVersion)
                .withNexusURI(CommonTestConstants.REPOSITORY_URL)
                .withTestWare("com.ericsson.cifwk.taf.executor", "te-taf-testware", packageVersion)
                .withSlaveHost(new Host("hostName_1", "127.0.0.1", 0, "user", "password"))
                .withSutResource(sutResource)
                .withSchedule(SCHEDULE_GROUP_ID, SCHEDULE_ARTIFACT_ID, SCHEDULE_VERSION, SCHEDULE_NAME);
        return taskBuilder.build();
    }

}
