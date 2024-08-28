package com.ericsson.cifwk.taf.executor.node;

import com.ericsson.cifwk.taf.executor.TestExecution;
import com.ericsson.cifwk.taf.executor.TestResult;
import com.ericsson.cifwk.taf.executor.TestwareRuntimeLimitations;
import com.ericsson.cifwk.taf.executor.security.TestRuntimeSecurityManager;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ericsson.cifwk.taf.executor.node.WorkspaceDataProvider.getWorkspaceDirectoryName;
import static com.google.common.collect.Lists.newArrayList;
import static java.nio.file.Files.createDirectories;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TafTestRunnerITest extends AbstractTafTestRunnerITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TafTestRunnerITest.class);

    private static final String RELEASE_TESTWARE = "com.ericsson.cifwk.taf.executor:te-taf-testware:2.14";
    private static final String RELEASE_REPOSITORY_URL = "https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/service/local/repositories/releases/content/";

    @BeforeClass
    public static void prepare() throws Exception {
        createDirectories(Paths.get(getWorkspaceDirectoryName()));
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(allureDir);
        File testRunDirectory = runner.getWorkingDir();
        if (testRunDirectory != null && testRunDirectory.exists()) {
            FileUtils.deleteDirectory(testRunDirectory);
        }
        testOutputStream.close();
    }

    @Test
    public void shouldRunSuccessfulTest() throws Exception {
        TestResult testResult = runSuite("success.xml", allureLogDirName);
        assertThat(testResult.getStatus(), equalTo(TestResult.Status.SUCCESS));
        assertTrue(allureDir.exists());
        String[] attachmentFileList = allureDir.list(new AllureAttachmentFilenameFilter());
        assertThat(attachmentFileList.length, greaterThanOrEqualTo(1));
    }

    @Test
    @Ignore("Hangs up the Jenkins job, temporarily ignored")
    public void shouldRunCrashingVm() throws Exception {
        TestResult testResult = runSuite("crash.xml", allureLogDirName);
        assertThat(testResult.getStatus(), equalTo(TestResult.Status.ERROR));
        assertTrue(allureDir.exists());
    }

    @Test
    public void shouldRunFailingTest() throws Exception {
        TestResult testResult = runSuite("failure.xml", allureLogDirName);
        assertThat(testResult.getStatus(), equalTo(TestResult.Status.ERROR));
        assertTrue(allureDir.exists());
    }

    @Test
    public void shouldAbortHungTest() throws Exception {
        TestResult testResult = runSuite("hang.xml", allureLogDirName, 40, null);
        assertThat(testResult.getStatus(), equalTo(TestResult.Status.FAILURE));
        assertTrue(allureDir.exists());
    }

    @Test
    public void shouldAbortTestWithEndlessLoop() throws Exception {
        TestResult testResult = runSuite("endless_loop.xml", allureLogDirName, 40, null);
        assertThat(testResult.getStatus(), equalTo(TestResult.Status.FAILURE));
        assertTrue(allureDir.exists());
    }

    @Test
    public void shouldFailWholeExecutionIfOneSuiteHasFailures() throws Exception {
        TestResult testResult = runSuite("failure.xml,success.xml", allureLogDirName);
        assertThat(testResult.getStatus(), equalTo(TestResult.Status.ERROR));
        assertTrue(allureDir.exists());
    }

    @Test
    public void shouldFailWholeExecutionIfSuitesWithSameName() throws Exception {
        TestResult testResult = runSuite("failure.xml,failure.xml,success.xml", allureLogDirName);
        assertThat(testResult.getStatus(), equalTo(TestResult.Status.ERROR));
        //allureDir is not created as the execution did not start, therefore no resource directories
        assertTrue(allureDir.exists());
        assertThat(allureDir.list().length, is(1));
        assertThat(allureDir.list()[0], containsString("suite.xml"));
    }

    @Test
    public void shouldFailOnMissingSuite() throws Exception {
        TestResult testResult = runSuite("success.xml,no_such_suite.xml", allureLogDirName);
        assertThat(testResult.getStatus(), equalTo(TestResult.Status.ERROR));
    }

    @Test
    public void shouldFailOnConfigFailure() throws Exception {
        TestResult testResult = runSuite("before_suite_fails.xml,before_class_fails.xml,before_method_fails.xml,after_method_fails.xml,after_class_fails.xml,after_suite_fails.xml", allureLogDirName);
        assertThat(testResult.getStatus(), equalTo(TestResult.Status.ERROR));
    }

    @Test
    public void shouldNotCreatedReportDirIfReportingDisabled() throws Exception {
        FileUtils.deleteDirectory(allureDir);
        TestResult testResult = runSuite("success.xml", null);
        assertThat(testResult.getStatus(), equalTo(TestResult.Status.SUCCESS));
        assertTrue(!allureDir.exists());
    }

    /**
     * @see com.ericsson.cifwk.taf.FakeSuiteGenerator#generateForFailedExecution(Collection)
     */
    @Test
    public void shouldGenerateFakeAllureSuiteForFailedExecution() throws Exception {
        TestResult testResult = runSuite("success.xml", allureLogDirName, newArrayList("not.existing:gav:1.2.3"));
        assertThat(testResult.getStatus(), equalTo(TestResult.Status.ERROR));

        assertTrue(allureDir.exists());
        String[] fileNames = allureDir.list();
        assertThat(fileNames.length, is(1));
        assertThat(fileNames[0], containsString("suite.xml"));

        File allureFakeSuiteXml = new File(allureDir, fileNames[0]);
        String fakeSuiteXmlContent = FileUtils.readFileToString(allureFakeSuiteXml);
        assertThat(fakeSuiteXmlContent, containsString("success.xml"));
    }

    /**
     * @see com.ericsson.cifwk.taf.FakeSuiteGenerator#generateForPartialExecution(Collection, Collection, Collection)
     */
    @Test
    public void shouldGenerateFakeAllureSuiteForPartiallyFailedExecution() throws Exception {
        TestResult testResult = runSuite("before_suite_fails.xml,success.xml,crash.xml", allureLogDirName);
        assertThat(testResult.getStatus(), equalTo(TestResult.Status.ERROR));
        assertTrue(allureDir.exists());
        File[] files = allureDir.listFiles(new AllureFakeSuiteFileFilter());
        assertThat(files.length, is(1));

        File allureFakeSuiteXml = files[0];
        String fakeSuiteXmlContent = FileUtils.readFileToString(allureFakeSuiteXml);
        assertThat(fakeSuiteXmlContent, containsString("success.xml"));
        assertThat(fakeSuiteXmlContent, containsString("before_suite_fails.xml"));
        assertThat(fakeSuiteXmlContent, containsString("crash.xml"));
    }

    @Test
    public void runSuccessfulTestWithMinTafVersionGreaterThanTafVersionInTestware() throws Exception {
        TestResult testResult = runSuiteWithMinTafVersion("success.xml", allureLogDirName, "2.31.17");
        assertThat(testResult.getStatus(), equalTo(TestResult.Status.SUCCESS));
        assertTrue(allureDir.exists());
        File pom = runner.getPomForRun().getFile();
        assertEquals("<tafversion>2.31.17</tafversion>", checkPomForVersion(pom));
    }

    @Test
    public void runSuccessfulTestWithMinTafVersionLessThanTafVersionInTestware() throws Exception {
        TestResult testResult = runSuiteWithMinTafVersion("success.xml", allureLogDirName, "2.31.1");
        assertThat(testResult.getStatus(), equalTo(TestResult.Status.SUCCESS));
        assertTrue(allureDir.exists());
        File pom = runner.getPomForRun().getFile();
        assertEquals("<tafversion>2.31.16</tafversion>", checkPomForVersion(pom));
    }

    @Test
    public void shouldRunOnlyParticularTestGroupsIfDefined() throws Exception {
        TestResult testResult = runSuite("groups_test.xml", allureLogDirName, 3600, Lists.<String>newArrayList(), "group1");
        assertThat(testResult.getStatus(), equalTo(TestResult.Status.SUCCESS));

        String testSuiteReport = getSurefireTestReport("TEST-groupsTestSuite.xml");

        verifySurefireTestReport(
                testSuiteReport, 3,
                new String[] {"<testcase name=\"group1_test1\"", "<testcase name=\"group1_test2\"", "<testcase name=\"groups12_test1"},
                new String[] {"<testcase name=\"group2"});
    }

    @Test
    public void shouldRunNoTestsIfTestGroupNotFound() throws Exception {
        TestExecution.Builder execBuilder = snapshotTestExecutionBuilder("groups_test.xml", allureLogDirName, Lists.<String>newArrayList());
        TestExecution execution = execBuilder
                .withGroup("group2")
                .withMiscProperties("globalTestGroups=noSuchGroup")
                .build();
        runner.setUp(execution);

        // Build should not fail because 0 tests is treated as OK by TAF test Maven plugin if groups are defined
        assertThat(runner.runTest().getStatus(), equalTo(TestResult.Status.SUCCESS));

        String testSummary = getSurefireTestReport("groupsTestSuite.txt");
        assertThat(testSummary, containsString("Tests run: 0"));
    }

    @Test
    public void shouldRunOnlyGlobalTestGroupsIfDefined() throws Exception {
        TestExecution.Builder execBuilder = snapshotTestExecutionBuilder("groups_test.xml", allureLogDirName, Lists.<String>newArrayList());
        TestExecution execution = execBuilder
                .withGroup("group1")
                .withMiscProperties("globalTestGroups=group2")
                .build();
        runner.setUp(execution);

        assertThat(runner.runTest().getStatus(), equalTo(TestResult.Status.SUCCESS));

        String testSuiteReport = getSurefireTestReport("TEST-groupsTestSuite.xml");

        verifySurefireTestReport(
                testSuiteReport, 2,
                new String[] {"<testcase name=\"groups12_test1\"", "<testcase name=\"group2_test1\""},
                new String[] {"<testcase name=\"group1_"});
    }

    @Test
    public void shouldUseSecurityManagerToControlThreads() throws Exception {
        TestwareRuntimeLimitations runtimeLimitations = new TestwareRuntimeLimitations();
        int maxThreadCount = 45;
        runtimeLimitations.setMaxThreadCount(maxThreadCount);
        TestExecution.Builder execBuilder = TestExecution.builder()
                .withTestware(RELEASE_TESTWARE)
                .withSuites("runtime_limitations.xml")
                .withRepositoryUrl(RELEASE_REPOSITORY_URL)
                .withAllureLogDir(allureLogDirName)
                .withRuntimeLimitations(runtimeLimitations);

        TestExecution execution = execBuilder.build();

        runner.setUp(execution);

        TestResult testResult = runner.runTest();
        assertThat(testResult.getStatus(), equalTo(TestResult.Status.ERROR));
        checkTestOutputForString("Runtime restriction is set by TE: maximal thread count " +
                "allowed to create in this test run is " + maxThreadCount);
        checkTestOutputForString("Too many active threads");

        // Make sure the attachment with thread dump is created
        assertTrue(allureDir.exists());

        String[] attachmentFileList = allureDir.list(new AllureAttachmentFilenameFilter());
        assertNotNull(attachmentFileList);
        // should be at least 2 attachments: one for main output, another for thread dump
        assertThat(attachmentFileList.length, greaterThanOrEqualTo(2));

        File[] suiteReportXmls = allureDir.listFiles(new AllureSuiteFilenameFilter());
        assertNotNull(suiteReportXmls);
        LOGGER.debug("Found the following suite report XMLs: {}", asList(suiteReportXmls));
        assertEquals(1, suiteReportXmls.length);

        // suite report XML should contain link to thread dump attachment
        Path workingDir = getWorkingDirectory();
        Path suiteReportXmlPath = workingDir.resolve("target/allure-results/" + suiteReportXmls[0].getName());
        String suiteReportXmlContent = Files.toString(suiteReportXmlPath.toFile(), Charset.defaultCharset());
        assertThat(suiteReportXmlContent,
                containsString("<attachment title=\"" + TestRuntimeSecurityManager.THREAD_DUMP_ATTACHMENT_NAME + "\""));
    }

    private void verifySurefireTestReport(String testReport, int totalTestCaseCount, String[] presentChunks, String[] absentChunks) {
        Pattern pattern = Pattern.compile(".*<testcase name.*");
        Matcher matcher = pattern.matcher(testReport);
        int testCaseCount = 0;
        while (matcher.find()) {
            testCaseCount++;
        }
        assertEquals(totalTestCaseCount, testCaseCount);

        for (String presentChunk : presentChunks) {
            assertThat(testReport, containsString(presentChunk));
        }

        for (String absentChunk : absentChunks) {
            assertThat(testReport, not(containsString(absentChunk)));
        }
    }

    private String getSurefireTestReport(String reportName) throws IOException {
        Path workingDir = getWorkingDirectory();
        Path testReportPath = workingDir.resolve("target/surefire-reports/" + reportName);
        return Files.toString(testReportPath.toFile(), Charset.defaultCharset());
    }

    private Path getWorkingDirectory() {
        File pomFile = runner.getPomForRun().getFile();
        return pomFile.toPath().getParent();
    }

    private TestResult runSuite(String suite, String reportingDir, List<String> additionalDependencies) {
        return runSuite(suite, reportingDir, 0, additionalDependencies, null);
    }

    private TestResult runSuite(String suite, String reportingDir) {
        return runSuite(suite, reportingDir, 0, null, null);
    }

    private TestResult runSuite(String suite, String reportingDir, int timeoutInSeconds, List<String> additionalDependencies) {
        return runSuite(suite, reportingDir, timeoutInSeconds, additionalDependencies, null);
    }

    private TestResult runSuite(String suite, String reportingDir, int timeoutInSeconds, List<String> additionalDependencies, String groups) {
        TestExecution.Builder execBuilder = snapshotTestExecutionBuilder(suite, reportingDir, additionalDependencies);
        if (timeoutInSeconds != 0) {
            execBuilder.withTimeoutInSeconds(String.valueOf(timeoutInSeconds));
        }
        if (StringUtils.isNotBlank(groups)) {
            execBuilder.withGroup(groups);
        }
        TestExecution execution = execBuilder.build();

        runner.setUp(execution);

        return runner.runTest();
    }

    private TestExecution.Builder snapshotTestExecutionBuilder(String suite, String reportingDir, List<String> additionalDependencies) {
        return TestExecution.builder()
            .withTestware(RELEASE_TESTWARE)
            .withSuites(suite)
            .withRepositoryUrl(RELEASE_REPOSITORY_URL)
            .withGeneralJobExecutionId("1234567890")
            .withAllureLogDir(reportingDir)
            .withAdditionalDependencies(additionalDependencies);
    }

    private TestResult runSuiteWithMinTafVersion(String suite, String reportingDir, String minTafVersion) {
        TestExecution.Builder execBuilder = TestExecution.builder()
                .withTestware(RELEASED_TESTWARE)
                .withSuites(suite)
                .withRepositoryUrl(RELEASES_REPOSITORY_URL)
                .withAllureLogDir(reportingDir)
                .withMinTafVersion(minTafVersion);
        TestExecution execution = execBuilder.build();

        runner.setUp(execution);

        return runner.runTest();
    }

    private String checkPomForVersion(File pom) {
        try (Scanner scanner = new Scanner(pom)) {
            //now read the file line by line...
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains("<tafversion>")) {
                    return line.trim();
                }
            }
        } catch (FileNotFoundException e) {
            //handle this
        }
        return "taf version not found";
    }

    private void checkTestOutputForString(String needle) {
        String content = new String(testOutputStream.toByteArray(), StandardCharsets.UTF_8);
        assertThat(content, containsString(needle));
    }
}
