package com.ericsson.cifwk.taf.execution;

import com.beust.jcommander.internal.Maps;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.assertions.TafAsserts;
import com.ericsson.cifwk.taf.execution.operator.LogOperator;
import com.ericsson.cifwk.taf.execution.operator.TeLogVisitor;
import com.ericsson.cifwk.taf.execution.operator.impl.RemoteLogOperatorImpl;
import com.ericsson.cifwk.taf.execution.operator.model.jenkins.JobReference;
import com.ericsson.cifwk.taf.executor.api.ScheduleRequest;
import com.ericsson.cifwk.taf.executor.api.TafTeBuildDetails;
import com.ericsson.cifwk.taf.executor.api.TafTeBuildTriggerResponse;
import com.ericsson.cifwk.taf.executor.api.TafTeJenkinsJob;
import com.ericsson.cifwk.taf.executor.api.TriggeringTaskBuilder;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;

/**
 * To be run in CI infrastructure
 */
public class RunScheduleCITest extends AbstractRunScheduleTestBase {

    @Inject
    private Provider<RemoteLogOperatorImpl> logOperatorProvider;

    @Override
    protected LogOperator getLogOperator() {
        return logOperatorProvider.get();
    }

    @Test
    @TestId(id = "TAF_TE_05", title = "Ensure it's possible to run legacy testware")
    public void shouldTriggerLegacyScheduleBuild() {
        TriggerResponseHolder responseHolder = triggerBuildViaRestCall("com.ericsson.cifwk.taf.executor", "te-taf-testware", "schedule/legacy.xml");

        sleep(10);
        JobReference newJob = findNewJob(responseHolder.jobsBeforeRun);
        sleep(3);
        String result = waitUntilCompletion(newJob);

        TafAsserts.assertEquals("Checking job finished sucessfully", "SUCCESS", result);
    }

    @Test
    @TestId(id = "TAF_TE_06", title = "Ensure it's possible to trigger the build via REST call")
    public void shouldTriggerBuildViaRestCall() throws Exception {
        TriggerResponseHolder responseHolder = triggerBuildViaRestCall("com.ericsson.cifwk.taf.executor", "te-taf-testware", "schedule/complex.xml");
        TafTeBuildDetails tafTeBuildDetails = verifyBuildResults(responseHolder, true, true);
        verifyAllureReport(tafTeBuildDetails.getAllureLogUrl());
    }

    @Test
    @TestId(id = "TAF_TE_07", title = "Ensure it's possible to trigger the build with plain schedule XML")
    public void shouldTriggerBuildXmlSchedule() throws Exception {
        String scheduleXml = "<?xml version=\"1.0\"?>\n" +
                "<schedule xmlns=\"http://taf.lmera.ericsson.se/schema/te\"\n" +
                "          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "          xsi:schemaLocation=\"http://taf.lmera.ericsson.se/schema/te http://taf.lmera.ericsson.se/schema/te/schedule/xml\">\n" +
                "\n" +
                "    <item>\n" +
                "        <name>Step's No. 1</name>\n" +
                "        <component>com.ericsson.cifwk.taf.executor:te-taf-testware</component>\n" +
                "        <suites>success.xml</suites>\n" +
                "    </item>\n" +
                "\n" +
                "    <item>\n" +
                "        <name>Step's No. 2</name>\n" +
                "        <component>com.ericsson.cifwk.taf.executor:te-taf-testware</component>\n" +
                "        <suites>success.xml</suites>\n" +
                "    </item>\n" +
                "\n" +
                "    <item>\n" +
                "        <name>Step's No. 3</name>\n" +
                "        <component>com.ericsson.cifwk.taf.executor:te-taf-testware</component>\n" +
                "        <suites>success.xml</suites>\n" +
                "    </item>\n" +
                "\n" +
                "</schedule>";
        ScheduleRequest schedule = new ScheduleRequest(scheduleXml, new Properties());
        TriggerResponseHolder responseHolder = triggerBuildViaRestCall("com.ericsson.cifwk.taf.executor", "te-taf-testware", schedule);
        TafTeBuildDetails tafTeBuildDetails = verifyBuildResults(responseHolder, true, true);

        verifyAllureReport(tafTeBuildDetails.getAllureLogUrl());
    }

    @Test
    @TestId(id = "TAF_TE_08", title = "Ensure it's possible to trigger the build with custom version of a bom, a pom and TAF")
    public void shouldTriggerWithDependancyMgtUpdates() throws Exception {
        final String actualTafVersionInTests = "2.37.1";
        final String minTafVersion = "2.37.23";
        final String bomGav = "com.ericsson.oss.testware.bom:enm-test-library-bom:1.0.422:bom";
        final String pomGav = "com.ericsson.oss.testware.taf:host-configurator:1.0.105";

        final String scheduleXml = "<?xml version=\"1.0\"?>\n" +
                "<schedule xmlns=\"http://taf.lmera.ericsson.se/schema/te\"\n" +
                "          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "          xsi:schemaLocation=\"http://taf.lmera.ericsson.se/schema/te http://taf.lmera.ericsson.se/schema/te/schedule/xml\">\n" +
                "\n" +
                "    <item>\n" +
                "        <name>Step 1</name>\n" +
                "        <component>com.ericsson.cifwk.taf.executor:te-taf-testware</component>\n" +
                "        <suites>success.xml</suites>\n" +
                "    </item>\n" +
                "\n" +
                "</schedule>";
        ScheduleRequest schedule = new ScheduleRequest(scheduleXml, new Properties());
        TriggeringTaskBuilder triggeringTaskBuilder =
                restOperator.triggeringTaskBuilderFor("com.ericsson.cifwk.taf.executor", "te-taf-testware", schedule);
        triggeringTaskBuilder.withMinTafVersion(minTafVersion);
        triggeringTaskBuilder.withUserDefinedGAVs(bomGav + "\n" + pomGav);
        TriggerResponseHolder responseHolder = triggerBuildViaRestCall(triggeringTaskBuilder.build());
        TafTeBuildDetails tafTeBuildDetails = verifyBuildResults(responseHolder, false, true);

        // Verify TAF data in the executor jobs' logs
        verifyExecutorLogs(tafTeBuildDetails, new TeLogVisitor() {
            @Override
            public void verifyLog(String executorLog) {
                Assert.assertThat(executorLog, Matchers.containsString("Checking if remote version of Taf is greater than build specified version, " + minTafVersion));
                Assert.assertThat(executorLog, Matchers.containsString("Remote version of Taf is: " + actualTafVersionInTests));
                Assert.assertThat(executorLog, Matchers.containsString("Copying all-taf-sdk-" + minTafVersion));

                Assert.assertThat(executorLog, Matchers.containsString("Setting com.ericsson.oss.testware.taf:host-configurator:1.0.105 to be added to DependencyManagement"));
                Assert.assertThat(executorLog, Matchers.containsString("Setting com.ericsson.oss.testware.bom:enm-test-library-bom:1.0.422:bom to be added to DependencyManagement"));
                Assert.assertThat(executorLog, Matchers.containsString("Setting com.ericsson.oss.testware.bom:enm-test-library-bom:1.0.422:bom as a bom with a scope of import"));
                Assert.assertThat(executorLog, Matchers.not(Matchers.containsString(" not equal to bom so it is ignored")));
                Assert.assertThat(executorLog, Matchers.not(Matchers.containsString("ERROR creating GAV object from")));
                Assert.assertThat(executorLog, Matchers.containsString("com.ericsson.oss.testware.taf:enm-taf-test-library  1.0.102 -> 1.0.87"));
                Assert.assertThat(executorLog, Matchers.containsString("com.ericsson.oss.testware.taf:host-configurator ... 1.0.105 -> 1.0.87"));
                Assert.assertThat(executorLog, Matchers.containsString("Setting com.ericsson.oss.testware.taf:host-configurator:1.0.105 to be added to DependencyManagement"));
            }
        });
    }

    @Test
    @TestId(id = "TAF_TE_09", title = "Verify generation of fake suites for missing suites")
    public void shouldGenerateMissingSuiteAllureXml() throws Exception {
        String scheduleXml = "<?xml version=\"1.0\"?>\n" +
                "<schedule xmlns=\"http://taf.lmera.ericsson.se/schema/te\"\n" +
                "          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "          xsi:schemaLocation=\"http://taf.lmera.ericsson.se/schema/te http://taf.lmera.ericsson.se/schema/te/schedule/xml\">\n" +
                "\n" +
                "    <item>\n" +
                "        <name>Step 1</name>\n" +
                "        <component>com.ericsson.cifwk.taf.executor:te-taf-testware</component>\n" +
                "        <suites>success.xml,suite_doesnt_exist.xml</suites>\n" +
                "    </item>\n" +
                "\n" +
                "</schedule>";
        ScheduleRequest schedule = new ScheduleRequest(scheduleXml, new Properties());
        TriggerResponseHolder responseHolder = triggerBuildViaRestCall("com.ericsson.cifwk.taf.executor", "te-taf-testware", schedule);
        String jobExecutionId = responseHolder.buildTriggerResponse.getJobExecutionId();

        TafTeBuildDetails buildDetails = waitUntilCompletion(jobExecutionId);

        Map<String, List<String>> suitesFailuresMap = Maps.newHashMap();
        suitesFailuresMap.put("Missing suites", Arrays.asList("suite_doesnt_exist.xml"));
        verifyAllureReportHasMissingSuiteEntries(buildDetails.getAllureLogUrl(), suitesFailuresMap);
    }

    @Test
    @TestId(id = "TAF_TE_10", title = "Verify generation of fake suite for JVM crash")
    public void shouldGenerateAllureReportWhenJvmCrashForSuiteHappened() throws Exception {
        String scheduleXml = "<?xml version=\"1.0\"?>\n" +
                "<schedule xmlns=\"http://taf.lmera.ericsson.se/schema/te\"\n" +
                "          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "          xsi:schemaLocation=\"http://taf.lmera.ericsson.se/schema/te http://taf.lmera.ericsson.se/schema/te/schedule/xml\">\n" +
                "\n" +
                "    <item>\n" +
                "        <name>Step 1</name>\n" +
                "        <component>com.ericsson.cifwk.taf.executor:te-taf-testware</component>\n" +
                "        <suites>success.xml,crash.xml</suites>\n" +
                "    </item>\n" +
                "\n" +
                "</schedule>";
        ScheduleRequest schedule = new ScheduleRequest(scheduleXml, new Properties());
        TriggerResponseHolder responseHolder = triggerBuildViaRestCall("com.ericsson.cifwk.taf.executor", "te-taf-testware", schedule);
        String jobExecutionId = responseHolder.buildTriggerResponse.getJobExecutionId();

        TafTeBuildDetails buildDetails = waitUntilCompletion(jobExecutionId);

        Map<String, List<String>> suitesFailuresMap = Maps.newHashMap();
        suitesFailuresMap.put("Failed to get executed", Arrays.asList("success.xml", "crash.xml"));
        verifyAllureReportHasMissingSuiteEntries(buildDetails.getAllureLogUrl(), suitesFailuresMap);
    }

    @Test
    @TestId(id = "TAF_TE_11", title = "TAF Surefire Provider should detect failures")
    public void shouldUseTafSurefireProviderToDetectFailures() throws Exception {
        TriggerResponseHolder responseHolder =
                triggerBuildViaRestCall("com.ericsson.cifwk.taf.executor", "te-taf-testware",
                        "schedule/taf_surefire_failure_detection_test.xml");
        TafTeBuildTriggerResponse buildTriggerResponse = responseHolder.buildTriggerResponse;
        TafTeBuildDetails buildDetails = waitUntilCompletion(buildTriggerResponse.getJobExecutionId());
        List<TafTeJenkinsJob> execJobs = buildDetails.getTafTeJenkinsJobs(TafTeJenkinsJob.Type.EXECUTOR);
        for (TafTeJenkinsJob execJob : execJobs) {
            assertEquals(execJob.getResult(), TafTeJenkinsJob.Result.FAILURE);
        }
    }

    @Test
    @TestId(id = "TAF_TE_12", title = "Should be able to use embedded test schedules")
    public void shouldRunIncludedSchedule() throws Exception {
        TriggerResponseHolder responseHolder =
                triggerBuildViaRestCall("com.ericsson.cifwk.taf.executor", "te-taf-testware", "schedule/with_include.xml");
        TafTeBuildDetails tafTeBuildDetails = verifyBuildResults(responseHolder, false, false);

        List<TafTeJenkinsJob> tafTeJenkinsJobs = tafTeBuildDetails.getTafTeJenkinsJobs(TafTeJenkinsJob.Type.EXECUTOR);
        // 2 items: 1 primary, 1 included
        assertThat(tafTeJenkinsJobs, hasSize(2));
        Set<String> allItemNames = Sets.newHashSet(Iterables.transform(tafTeJenkinsJobs, new Function<TafTeJenkinsJob, String>() {
            @Override
            public String apply(TafTeJenkinsJob tafTeJenkinsJob) {
                return tafTeJenkinsJob.getScheduleItemName();
            }
        }));
        assertThat(allItemNames, hasItem("Step 1"));
        assertThat(allItemNames, hasItem("1"));
    }

    @Test
    @TestId(id = "TAF_TE_14", title = "Should be able to add manual tests to report")
    public void shouldRunManualTests() throws Exception {
        String scheduleXml = loadResource("schedules/with_manual_items.xml");
        ScheduleRequest schedule = new ScheduleRequest(scheduleXml, new Properties());
        TriggerResponseHolder responseHolder = triggerBuildViaRestCall("com.ericsson.cifwk.taf.executor", "te-taf-testware", schedule);
        TafTeBuildDetails tafTeBuildDetails = verifyBuildResults(responseHolder, false, true);

        // Make sure manual tests were run
        getExecutorJobWithName(tafTeBuildDetails, "Manual tests");

        // Make sure manual tests were added to the overall test result set
        String logDirectory = getTeLogDirectory(responseHolder.buildTriggerResponse.getJobExecutionId());
        LogOperator logOperator = getLogOperator();
        String[] allureXmls = logOperator.getAllureLogXmls(logDirectory);
        // 2 automated + 3 manual reports
        assertEquals(5, allureXmls.length);
    }

    @Test
    @TestId(id = "TAF_TE_15", title = "Should be able to use Java 8 version for tests")
    public void shouldUseArbitraryJavaVersion() throws Exception {
        TriggerResponseHolder responseHolder =
                triggerBuildViaRestCall("com.ericsson.cifwk.taf.executor", "te-taf-java8-testware", "schedule/with_java8.xml");
        verifyBuildResults(responseHolder, false, true);
    }

    @Test
    @TestId(id = "TAF_TE_16", title = "Should be able to set custom settings for tests runtime")
    public void shouldUseCustomRuntimeSettings() throws Exception {
        String scheduleXml = loadResource("schedules/with_runtime_settings.xml");
        ScheduleRequest schedule = new ScheduleRequest(scheduleXml, new Properties());
        TriggerResponseHolder responseHolder = triggerBuildViaRestCall("com.ericsson.cifwk.taf.executor", "te-taf-testware", schedule);
        TafTeBuildDetails tafTeBuildDetails = verifyBuildResults(responseHolder, false, true);

        // Double-check that JVM settings were applied: class loading should have been verbose
        TafTeJenkinsJob jvmSettingsBuild = getExecutorJobWithName(tafTeBuildDetails, "JVM settings tests");
        getLogOperator().verifyTeBuildLog(jvmSettingsBuild, new TeLogVisitor() {
            @Override
            public void verifyLog(String teLogAsString) {
                // These lines appear only with -verbose:class JVM key applied to Java process init
                assertThat(teLogAsString, containsString("[Loaded com.ericsson.cifwk.taf.properties.MemoryTest from"));
                assertThat(teLogAsString, containsString("[Loaded com.ericsson.cifwk.taf.executor.listeners.TeSuiteListener from"));
            }
        });
    }

    @Test
    @TestId(id = "TAF_TE_17", title = "Should be able to set global max limit for threads for testware JVM in TE")
    public void shouldUseDefaultSettingsForThreadLimitation() throws Exception {
        verifyThreadLimitation("schedules/runtime_limitations_default.xml", 1000);
    }

    @Test
    @TestId(id = "TAF_TE_18", title = "Should be able to set max limit for threads for testware JVM in schedule")
    public void shouldUseScheduleSettingsForThreadLimitation() throws Exception {
        verifyThreadLimitation("schedules/runtime_limitations.xml", 20);
    }

}
