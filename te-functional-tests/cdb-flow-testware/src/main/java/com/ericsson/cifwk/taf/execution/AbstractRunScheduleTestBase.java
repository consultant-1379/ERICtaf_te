package com.ericsson.cifwk.taf.execution;

import com.beust.jcommander.internal.Lists;
import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.execution.operator.AllureReportOperator;
import com.ericsson.cifwk.taf.execution.operator.EventRepositoryOperator;
import com.ericsson.cifwk.taf.execution.operator.JenkinsOperator;
import com.ericsson.cifwk.taf.execution.operator.LogOperator;
import com.ericsson.cifwk.taf.execution.operator.RestOperator;
import com.ericsson.cifwk.taf.execution.operator.TeLogVisitor;
import com.ericsson.cifwk.taf.execution.operator.impl.AllureReportOperatorImpl;
import com.ericsson.cifwk.taf.execution.operator.impl.EventRepositoryOperatorImpl;
import com.ericsson.cifwk.taf.execution.operator.impl.JenkinsOperatorImpl;
import com.ericsson.cifwk.taf.execution.operator.impl.RestOperatorImpl;
import com.ericsson.cifwk.taf.execution.operator.model.TestDataContext;
import com.ericsson.cifwk.taf.execution.operator.model.allure.OverviewTabModel;
import com.ericsson.cifwk.taf.execution.operator.model.allure.XUnitTabModel;
import com.ericsson.cifwk.taf.execution.operator.model.jenkins.Build;
import com.ericsson.cifwk.taf.execution.operator.model.jenkins.Jenkins;
import com.ericsson.cifwk.taf.execution.operator.model.jenkins.JobReference;
import com.ericsson.cifwk.taf.executor.api.ScheduleRequest;
import com.ericsson.cifwk.taf.executor.api.TafTeBuildDetails;
import com.ericsson.cifwk.taf.executor.api.TafTeBuildTriggerResponse;
import com.ericsson.cifwk.taf.executor.api.TafTeJenkinsJob;
import com.ericsson.cifwk.taf.executor.api.TeRestServiceClient;
import com.ericsson.cifwk.taf.executor.api.TriggeringTask;
import com.ericsson.cifwk.taf.executor.utils.TimeLimitedTask;
import com.ericsson.cifwk.taf.executor.utils.TimeLimitedWorker;
import com.ericsson.duraci.datawrappers.EventId;
import com.ericsson.duraci.datawrappers.ExecutionId;
import com.ericsson.duraci.datawrappers.LogReference;
import com.ericsson.duraci.datawrappers.ResultCode;
import com.ericsson.duraci.eiffelmessage.messages.EiffelEvent;
import com.ericsson.duraci.eiffelmessage.messages.EiffelMessage;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelBaselineDefinedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelJobFinishedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelJobStartedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelTestCaseFinishedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelTestCaseStartedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelTestSuiteFinishedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelTestSuiteStartedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.virtual.FinishedEvent;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

public abstract class AbstractRunScheduleTestBase extends TafTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRunScheduleTestBase.class);

    private static final String TE_LOGS_DIR = "te-console-logs";

    @Inject
    Provider<JenkinsOperatorImpl> jenkinsOperatorProvider;

    @Inject
    Provider<EventRepositoryOperatorImpl> eventRepositoryOperatorProvider;

    @Inject
    Provider<RestOperatorImpl> restOperatorProvider;

    @Inject
    Provider<AllureReportOperatorImpl> allureReportOperatorProvider;

    RestOperator restOperator;
    EventRepositoryOperator erOperator;
    EiffelModelHolder eiffelModelHolder;

    @BeforeMethod
    public void setUp() {
        restOperator = restOperatorProvider.get();
        restOperator.init(TestDataContext.getTeMasterHost());
        erOperator = eventRepositoryOperatorProvider.get();
    }

    protected void verifyThreadLimitation(String suite, final int expectedMaxThreads) {
        String scheduleXml = loadResource(suite);
        ScheduleRequest schedule = new ScheduleRequest(scheduleXml, new Properties());
        TriggerResponseHolder responseHolder = triggerBuildViaRestCall("com.ericsson.cifwk.taf.executor", "te-taf-testware", schedule);
        String jobExecutionId = responseHolder.buildTriggerResponse.getJobExecutionId();

        TafTeBuildDetails buildDetails = waitUntilCompletion(jobExecutionId);
        List<TafTeJenkinsJob> executorJobs = buildDetails.getTafTeJenkinsJobs(TafTeJenkinsJob.Type.EXECUTOR);
        assertThat(executorJobs, hasSize(1));
        TafTeJenkinsJob executorJob = executorJobs.get(0);
        TafTeJenkinsJob.Result result = executorJob.getResult();
        assertEquals(TafTeJenkinsJob.Result.FAILURE, result);

        getLogOperator().verifyTeBuildLog(executorJob, new TeLogVisitor() {
            @Override
            public void verifyLog(String executorLog) {
                assertThat(executorLog, containsString("Too many active threads"));
                assertThat(executorLog, containsString("maximum allowed is " + expectedMaxThreads + " (defined by Test Executor)"));
            }
        });
    }

    protected void verifyExecutorLogs(TafTeBuildDetails tafTeBuildDetails, TeLogVisitor visitor) throws Exception {
        List<TafTeJenkinsJob> executorJobs = tafTeBuildDetails.getTafTeJenkinsJobs(TafTeJenkinsJob.Type.EXECUTOR);
        for (TafTeJenkinsJob job : executorJobs) {
            getLogOperator().verifyTeBuildLog(job, visitor);
        }
    }

    protected TriggerResponseHolder triggerBuildViaRestCall(String testwareGroupId, String testwareArtifactId, String pathToSchedule) {
        //TODO: wrong, add twVersion along with pluginVersion
        String testWareVersion = TestDataContext.getPluginVersion();
        ScheduleRequest scheduleRequest = new ScheduleRequest(pathToSchedule, testwareGroupId, testwareArtifactId, testWareVersion);
        return triggerBuildViaRestCall(restOperator.triggeringTaskBuilderFor(testwareGroupId, testwareArtifactId, scheduleRequest).build());
    }

    protected TriggerResponseHolder triggerBuildViaRestCall(String testwareGroupId, String testwareArtifactId, ScheduleRequest schedule) {
        return triggerBuildViaRestCall(restOperator.triggeringTaskBuilderFor(testwareGroupId, testwareArtifactId, schedule).build());
    }

    protected TriggerResponseHolder triggerBuildViaRestCall(TriggeringTask triggeringTask) {
        List<JobReference> jobsBeforeRun = listOfJobs();
        TafTeBuildTriggerResponse response = performRestTriggering(triggeringTask);

        LOGGER.debug("Response from RestTriggering: " + response.toString());
        assertEquals(response.getJobSchedulingStatus(), TafTeBuildTriggerResponse.Status.OK, "Triggering has failed");
        DateTime dateTime = new DateTime(response.getGeneratedAt());
        DateTime now = new DateTime();
        LOGGER.debug("Response was generated at " + dateTime.toString() + ", now = " + now.toString());

        // TODO: server time zone may be different - shouldn't rely on it
        assertTrue("Asserting time taken was less than two minutes", dateTime.plus(new Period().withMinutes(62)).isAfterNow());

        return new TriggerResponseHolder(jobsBeforeRun, response);
    }

    protected TafTeBuildDetails verifyBuildResults(TriggerResponseHolder responseHolder, boolean checkEventRepoAndLogs, boolean successfulResult) throws IOException {
        String jobExecutionId = responseHolder.buildTriggerResponse.getJobExecutionId();
        assertNotNull("Checking jobExecutionId is not null", jobExecutionId);
        String triggeringEventId = responseHolder.buildTriggerResponse.getTriggeringEventId();
        assertNotNull("Checking triggeringEventId is not null", triggeringEventId);

        verifyJobUrl(responseHolder.buildTriggerResponse);

        TafTeBuildDetails buildDetails = waitUntilCompletion(jobExecutionId);

        assertEquals(successfulResult, Boolean.TRUE.equals(buildDetails.getSuccess()), "Job failed");
        if (checkEventRepoAndLogs) {
            // Check Event Repository results
            eiffelModelHolder = verifyEventsInEventRepository(triggeringEventId, jobExecutionId);
            // Check Allure generated stuff
            verifyLogs(jobExecutionId, eiffelModelHolder.jobStartedEvent, eiffelModelHolder.testSuiteStartedEventMessages);
        }
        return buildDetails;
    }

    private void verifyLogs(String jobExecutionId, EiffelJobStartedEvent jobStartedEvent, List<EiffelMessage> jobStepStartedEventMessages) {
        String logDirectory = getTeLogDirectory(jobExecutionId);
        LOGGER.debug("LogDirectory: " + logDirectory);

        // check Allure url
        LOGGER.debug("Checking Allure");
        LogOperator logOperator = getLogOperator();
        String allureLogUrl = logOperator.getAllureLogUrl(jobStartedEvent);
        assertThat("Checking allureLogUrl contains jobExecutionId", allureLogUrl, containsString(jobExecutionId));
        assertThat("Checking allureLogUrl contains TestDataContext host", allureLogUrl, containsString(TestDataContext.getAllureReportsHttpBase()));

        // check execution log directory is created
        assertTrue(String.format("Check execution log directory '%s' is a directory", logDirectory), logOperator.isDirectory(logDirectory));

        // check all allure xmls are located in log folder, should be equal to step count
        String[] allureXmls = logOperator.getAllureLogXmls(logDirectory);
        assertEquals(allureXmls.length, 3, "Not all Allure xmls are located in log folder, should be equal to step count");

        // check Allure log is generated if upload script is defined
        if (StringUtils.isNotEmpty(TestDataContext.getReportingScriptsFolder())) {
            assertTrue("Check Allure logs generated if upload script is defined", logOperator.allureLogIndexExists(logDirectory));
        }

        //check te logs are copied to folder inside log directory
        String teLogsDirectory = logDirectory + "/" + TE_LOGS_DIR;
        String[] teConsoleLogs = logOperator.getTeConsoleLogs(teLogsDirectory);
        assertThat(teConsoleLogs.length, is(3));

        logOperator.closeShell();
    }

    protected abstract LogOperator getLogOperator();

    protected String getTeLogDirectory(String jobExecutionId) {
        return TestDataContext.getLocalReportsStorage() + "/" + jobExecutionId;
    }

    private EiffelModelHolder verifyEventsInEventRepository(String triggerEventIdStr, String executionIdStr) {
        EiffelJobStartedEvent jobStartedEvent = null;
        EiffelMessage parentSuiteStartedEventMessage = null;
        List<EiffelMessage> testSuiteStartedEventMessages = null;

        final ExecutionId executionId = new ExecutionId(executionIdStr);
        EventId triggerEventId = new EventId(triggerEventIdStr);
        EiffelMessage triggerMessage = erOperator.findEventMessage(triggerEventId, EiffelBaselineDefinedEvent.class);
        assertNotNull("Checking triggerMessage is not null in the EventRepository", triggerMessage);
        LOGGER.debug("Trigger message from event repository: " + triggerMessage.toString());
        assertEquals(triggerEventId, triggerMessage.getEventId(),
                "triggerEventId doesn't match the one in triggerMessage retrieved from the event repository");
        verifyEiffelMessageCommonData(triggerMessage);

        // Job started
        EiffelMessage jobStartedEventMessage = erOperator.findJobStartedEventMessage(executionId);
        assertNotNull("Checking jobStartedEventMessage is not null", jobStartedEventMessage);
        LOGGER.debug("JobStartedEvent message: " + jobStartedEventMessage.toString());
        EiffelEvent event = jobStartedEventMessage.getEvent();
        LOGGER.debug("JobStartedEvent: " + jobStartedEventMessage.getEventId().toString());
        assertTrue("Checking jobStartedEvent is an instance of EiffelJob StartedEvent", event instanceof EiffelJobStartedEvent);
        jobStartedEvent = (EiffelJobStartedEvent) event;
        ExecutionId jobStartedExecutionId = jobStartedEvent.getJobExecutionId();
        assertEquals(executionIdStr, jobStartedExecutionId.toString(), "executionId doesn't match jobstartedExecutionId");
        assertEquals("RFA", jobStartedEvent.getJobInstance(), "Job name doesn't match");
        // check that jobScheduleEventId is in InputEventIds
        assertThat("Check that jobScheduleEventId is in InputEventIds", jobStartedEventMessage.getInputEventIds(),
                Matchers.hasItem(Matchers.<EventId>hasToString(triggerEventIdStr)));
        verifyEiffelMessageCommonData(jobStartedEventMessage);

        //Test Suite Started Events - should be 4 - 1 sent from schedulebuild - 3 from executorbuilds
        //schedulebuild TestSuiteStartedEvent
        parentSuiteStartedEventMessage = erOperator.findTestSuiteStartedEventMessages(jobStartedExecutionId).get(0);
        assertNotNull("checking parent Test Suite started events sent by Schedule Build - should be 1 of them", parentSuiteStartedEventMessage);
        EiffelTestSuiteStartedEvent parentSuiteStartedEvent = (EiffelTestSuiteStartedEvent) parentSuiteStartedEventMessage.getEvent();
        ExecutionId parentSuiteStartedEventExecutionId = parentSuiteStartedEvent.getTestSuiteExecutionId();

        //executorBuild TestSuiteStartedEvents
        testSuiteStartedEventMessages = erOperator.findTestSuiteStartedEventMessages(parentSuiteStartedEventExecutionId);
        assertEquals(3, testSuiteStartedEventMessages.size(),
                "Count of Test Suite started events sent by Executor Build is wrong");

        for (EiffelMessage testSuiteStartedEventMessage : testSuiteStartedEventMessages) {
            EiffelTestSuiteStartedEvent temp = (EiffelTestSuiteStartedEvent) testSuiteStartedEventMessage.getEvent();
            List<EiffelMessage> eventDownstream = erOperator.getEventDownstream(testSuiteStartedEventMessage.getEventId());
            Iterable<EiffelMessage> testSuiteStartedMsgs = getEiffelMessagesByEventType(eventDownstream, EiffelTestSuiteStartedEvent.class);
            //expected number is 2 as testSuiteStartedEventMessage is return with it's downstream suite started event
            assertEquals(2, Iterables.size(testSuiteStartedMsgs), "The number of testSuiteStarted events is wrong");
            for (EiffelMessage testSuiteStartedMsg : testSuiteStartedMsgs) {
                EiffelTestSuiteStartedEvent temp2 = (EiffelTestSuiteStartedEvent) testSuiteStartedMsg.getEvent();
                // Test case events
                Iterable<EiffelMessage> testCaseStartedMsgs = getEiffelMessagesByEventType(eventDownstream, EiffelTestCaseStartedEvent.class);
                assertEquals(1, Iterables.size(testCaseStartedMsgs), "The number of test case started events is wrong");
                Iterable<EiffelMessage> testCaseFinishedMsgs = getEiffelMessagesByEventType(eventDownstream, EiffelTestCaseFinishedEvent.class);
                assertEquals(true, allFinishEventsHaveTheResult(testCaseFinishedMsgs, ResultCode.SUCCESS),
                        "Not all finish events have results");
            }
            Iterable<EiffelMessage> testSuiteFinishedMsgs = getEiffelMessagesByEventType(eventDownstream, EiffelTestSuiteFinishedEvent.class);
            assertEquals(true, allFinishEventsHaveTheResult(testSuiteFinishedMsgs, ResultCode.SUCCESS),
                    "Not all finish events have the result 'SUCCESS'");

            // test suite finished event
            EiffelTestSuiteStartedEvent testSuiteStartedEvent = (EiffelTestSuiteStartedEvent) testSuiteStartedEventMessage.getEvent();
            ExecutionId testSuiteStartedExecutionId = testSuiteStartedEvent.getTestSuiteExecutionId();
            EiffelMessage testSuiteFinishedEventMessage = erOperator.findTestSuiteFinishedEventMessage(testSuiteStartedExecutionId);
            assertNotNull("Checking testSuiteFinishedEventMessage is not null", testSuiteFinishedEventMessage);
            EiffelTestSuiteFinishedEvent testSuiteFinishedEvent = (EiffelTestSuiteFinishedEvent) testSuiteFinishedEventMessage.getEvent();
            assertEquals(ResultCode.SUCCESS, testSuiteFinishedEvent.getResultCode(),
                    "testSuiteFinishedEventMessage doesn't have result 'SUCCESS'");
            Map<String, LogReference> logReferences = testSuiteFinishedEvent.getLogReferences();
            verifyEiffelMessageCommonData(testSuiteStartedEventMessage);
        }

        // parent Test suite finished events
        EiffelMessage parenttestSuiteFinishedEventMessage = erOperator.findTestSuiteFinishedEventMessage(parentSuiteStartedEventExecutionId);
        assertNotNull("Checking testSuiteFinishedEventMessage is not null", parenttestSuiteFinishedEventMessage);
        EiffelTestSuiteFinishedEvent testSuiteFinishedEvent = (EiffelTestSuiteFinishedEvent) parenttestSuiteFinishedEventMessage.getEvent();
        assertEquals(ResultCode.SUCCESS, testSuiteFinishedEvent.getResultCode(),
                "testSuiteFinishedEventMessage doesn't have result 'SUCCESS'");

        int finishedEventLookupTimeoutInSeconds = 30;
        LOGGER.debug(String.format("Looking for Job finished Event for executionId %s, " +
                "waiting max %d seconds for it to appear in ER", executionId.toString(), finishedEventLookupTimeoutInSeconds));
        // Because have seen issues where test run fails but event is in ER by the time you check it
        EiffelMessage jobFinishedEventMessage = TimeLimitedTask.performUntilTimeout(new TimeLimitedWorker<EiffelMessage>() {
            @Override
            public Optional<EiffelMessage> doWork() {
                EiffelMessage message = erOperator.findJobFinishedEventMessage(executionId);
                return message == null ? Optional.<EiffelMessage>absent() : Optional.of(message);
            }
        }, finishedEventLookupTimeoutInSeconds);

        assertNotNull("Checking jobFinishedEventMessage is not null", jobFinishedEventMessage);
        LOGGER.debug("JobFinishedEventMessage: " + jobFinishedEventMessage.toString());
        event = jobFinishedEventMessage.getEvent();
        assertTrue("Checking jobFinishedEvent is an instance of EiffelJobFinishedEvent", event instanceof EiffelJobFinishedEvent);
        EiffelJobFinishedEvent jobFinishedEvent = (EiffelJobFinishedEvent) event;
        assertEquals(executionId, jobFinishedEvent.getJobExecutionId(), "executionId doesn't match the one in jobFinishedEvent");
        assertEquals("RFA", jobStartedEvent.getJobInstance(), "Scheduler job name is wrong");
        assertThat("Checking jobFinishedEventMessage inputId matches jobStartedEvent", jobFinishedEventMessage.getInputEventIds(),
                hasItem(jobStartedEventMessage.getEventId()));
        assertEquals(ResultCode.SUCCESS, jobFinishedEvent.getResultCode(), "jobFinishedEvent doesn't have result 'SUCCESS'");

        verifyEiffelMessageCommonData(jobFinishedEventMessage);

        EiffelModelHolder eiffelContainer = new EiffelModelHolder();
        eiffelContainer.triggerEvent = (EiffelBaselineDefinedEvent) triggerMessage.getEvent();
        eiffelContainer.jobStartedEvent = jobStartedEvent;
        eiffelContainer.testSuiteStartedEventMessages = testSuiteStartedEventMessages;
        return eiffelContainer;
    }

    protected TafTeJenkinsJob getExecutorJobWithName(TafTeBuildDetails tafTeBuildDetails, final String itemName) {
        Preconditions.checkArgument(itemName != null);
        List<TafTeJenkinsJob> tafTeJenkinsJobs = tafTeBuildDetails.getTafTeJenkinsJobs(TafTeJenkinsJob.Type.EXECUTOR);
        Optional<TafTeJenkinsJob> result = Iterables.tryFind(tafTeJenkinsJobs, new Predicate<TafTeJenkinsJob>() {
            @Override
            public boolean apply(TafTeJenkinsJob job) {
                return job.getScheduleItemName().startsWith(itemName);
            }
        });
        assertTrue(String.format("Failed to find executor job with item name '%s'", itemName), result.isPresent());
        return result.get();
    }

    private void verifyEiffelMessageCommonData(EiffelMessage message) {
        assertEquals(TestDataContext.getReportingMbDomain(), message.getDomainId(), "Checking message domainId");
    }

    private boolean allFinishEventsHaveTheResult(Iterable<EiffelMessage> finishedEventMsgs, final ResultCode resultCode) {
        return Iterables.all(finishedEventMsgs, new Predicate<EiffelMessage>() {
            @Override
            public boolean apply(EiffelMessage input) {
                return resultCode.equals(((FinishedEvent) input.getEvent()).getResultCode());
            }
        });
    }

    private Iterable<EiffelMessage> getEiffelMessagesByEventType(List<EiffelMessage> eventDownstream, final Class<? extends EiffelEvent> clazz) {
        return Iterables.filter(eventDownstream, new Predicate<EiffelMessage>() {
            @Override
            public boolean apply(EiffelMessage inputMsg) {
                EiffelEvent inputEvent = inputMsg.getEvent();
                return (inputEvent != null && clazz.isAssignableFrom(inputEvent.getClass()));
            }
        });
    }

    protected TafTeBuildDetails waitUntilCompletion(String jobExecutionIdStr) {
        LOGGER.info("Waiting for the TE execution '" + jobExecutionIdStr + "' to complete...");
        TafTeBuildDetails buildDetails;
        TeRestServiceClient client = restOperator.getTeRestServiceClient();
        do {
            buildDetails = client.getBuildDetails(jobExecutionIdStr);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // ignore
            }
        } while (!Boolean.TRUE.equals(buildDetails.getBuildComplete()));
        return buildDetails;
    }

    protected String waitUntilCompletion(JobReference reference) {
        JenkinsOperator operator = jenkinsOperatorProvider.get();

        String jobName = reference.getName();
        LOGGER.info("Waiting for the last build of '" + jobName + "' to complete...");
        Host teMasterHost = TestDataContext.getTeMasterHost();
        Build build = operator.lastBuild(jobName, teMasterHost);

        while (build.getBuilding()) {
            sleep(2);
            build = operator.lastBuild(jobName, teMasterHost);
        }

        LOGGER.info("Build of '" + jobName + "' completed");

        return build.getResult();
    }

    protected List<JobReference> listOfJobs() {
        JenkinsOperator operator = jenkinsOperatorProvider.get();

        Jenkins jenkins = operator.jenkins(TestDataContext.getTeMasterHost());
        return jenkins.getJobs();
    }

    protected JobReference findNewJob(List<JobReference> jobsBeforeRun) {
        List<Object> jobNames = Lists.newArrayList();
        Iterables.addAll(jobNames, Iterables.transform(jobsBeforeRun, new Function<JobReference, String>() {
            @Override
            public String apply(JobReference input) {
                return input.getName();
            }
        }));

        LOGGER.info("Existing jobs : " + jobNames);

        JenkinsOperator operator = jenkinsOperatorProvider.get();

        Jenkins jenkins = operator.jenkins(TestDataContext.getTeMasterHost());
        List<JobReference> jobs = jenkins.getJobs();
        for (JobReference job : jobs) {
            if (!jobNames.contains(job.getName())) {
                LOGGER.info("New job found : " + job.getName());
                return job;
            }
        }
        LOGGER.info("No new jobs found");

        throw new AssertionError("No new jobs found");
    }

    protected void verifyAllureReport(String allureLogUrl) {
        LOGGER.info("Verifying Allure report (" + allureLogUrl + ")");
        AllureReportOperator allureOperator = allureReportOperatorProvider.get();
        allureOperator.init(allureLogUrl);

        OverviewTabModel overviewTab = allureOperator.getOverviewTab();
        assertTrue(overviewTab.isEnvSectionVisible());
        Map<String, String> envData = overviewTab.getEnvironmentData();

        LOGGER.info("Verifying trigger environment data presense");
        assertNotNull(envData.get("ISO version"));
        assertNotNull(envData.get("Jenkins job name"));
        assertNotNull(envData.get("Link to Jenkins job"));
        assertNotNull(envData.get("Current sprint"));
    }

    protected void verifyAllureReportHasMissingSuiteEntries(String allureLogUrl, Map<String, List<String>> suitesTestCasesMap) {
        LOGGER.info("Verifying Allure report (" + allureLogUrl + ")");
        AllureReportOperator allureOperator = allureReportOperatorProvider.get();
        allureOperator.init(allureLogUrl);

        XUnitTabModel xunitTab = allureOperator.getXunitTab();
        allureOperator.getBrowserTab().takeScreenshot("gotXunitTab");
        assertTrue(xunitTab.isSuitesSectionAvailable());
        List<String> testSuitesFromAllure = xunitTab.getTestSuites();

        LOGGER.info("Verifying suites and testcases added");

        for (Map.Entry<String, List<String>> entry : suitesTestCasesMap.entrySet()) {
            String expectedSuiteName = entry.getKey();
            LOGGER.debug("List of suites got from allure report: {}", testSuitesFromAllure);
            LOGGER.debug("Suite name expected to be in allure: {}", expectedSuiteName);
            assertThat(testSuitesFromAllure, hasItem(expectedSuiteName));

            List<String> testCasesForSuiteFromAllure = xunitTab.getTestCasesForSuite(expectedSuiteName);
            allureOperator.getBrowserTab().takeScreenshot("TestCasesForSuite");
            String[] expectedTestCases = entry.getValue().toArray(new String[entry.getValue().size()]);
            LOGGER.debug("List of testcases from allure for the expected suite: {}", testCasesForSuiteFromAllure);
            LOGGER.debug("Expected Test cases: {}", expectedTestCases);
            assertThat(testCasesForSuiteFromAllure, hasItems(expectedTestCases));
        }
    }

    private void verifyJobUrl(TafTeBuildTriggerResponse response) throws IOException {
        String jobUrl = response.getJobUrl();
        assertNotNull("Checking jobUrl is not null", jobUrl);
    }

    private TafTeBuildTriggerResponse performRestTriggering(TriggeringTask triggeringTask) {
        return restOperator.triggerBuild(triggeringTask);
    }

    static class EiffelModelHolder {
        public EiffelBaselineDefinedEvent triggerEvent;
        public EiffelJobStartedEvent jobStartedEvent;
        public List<EiffelMessage> testSuiteStartedEventMessages;
    }

    protected static class TriggerResponseHolder {
        public List<JobReference> jobsBeforeRun;
        public TafTeBuildTriggerResponse buildTriggerResponse;

        private TriggerResponseHolder(List<JobReference> jobsBeforeRun, TafTeBuildTriggerResponse triggerResponse) {
            this.jobsBeforeRun = jobsBeforeRun;
            this.buildTriggerResponse = triggerResponse;
        }
    }

    protected void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected String loadResource(String name) {
        URL resource = Resources.getResource(name);
        try {
            return Resources.toString(resource, Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
