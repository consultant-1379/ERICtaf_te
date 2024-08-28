package com.ericsson.cifwk.taf.executor.node;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(MockitoJUnitRunner.class)
public class FailedTestRunAnalyserTest {

    @Mock
    PrintStream printStream;
    private FailedTestRunAnalyser failedTestRunAnalyser;
    private File tempDir;
    private URL resource;
    private String partiallyFailedExecution;
    private String completelyFailedExecution;

    @Before
    public void setUp() throws IOException {
        tempDir = Files.createTempDir();
        failedTestRunAnalyser = getUnitForSuites("finished.xml", "started_only.xml", "not_run.xml");

        resource = this.getClass().getResource("/run-analyser-results/partially-failed-execution.xml");
        partiallyFailedExecution = Resources.toString(resource, Charset.defaultCharset()).replace("\n", "").replace("\r", "");

        resource = this.getClass().getResource("/run-analyser-results/completely-failed-execution.xml");
        completelyFailedExecution = Resources.toString(resource, Charset.defaultCharset()).replace("\n", "").replace("\r", "");
    }

    private FailedTestRunAnalyser getUnitForSuites(String... suites) {
        return new FailedTestRunAnalyser(tempDir, suites, tempDir.getAbsolutePath(), printStream);
    }

    @Test
    public void shouldWriteReportForPartiallyFailedExecution() throws IOException {
        Path pathToSurefireReports = Paths.get(tempDir.toString(), "target", "surefire-reports");
        pathToSurefireReports.toFile().mkdirs();
        File runSuitesDir = new File(tempDir, "suites-run");
        runSuitesDir.mkdir();
        File missedSuitesDir = new File(tempDir, "suites-missed");
        missedSuitesDir.mkdir();

        new File(missedSuitesDir, "missing.xml").createNewFile();
        new File(runSuitesDir, "finished.xml.start").createNewFile();
        new File(runSuitesDir, "finished.xml.finish").createNewFile();
        new File(runSuitesDir, "started_only.xml.start").createNewFile();

        failedTestRunAnalyser = getUnitForSuites("finished.xml", "started_only.xml", "not_run.xml", "missing.xml");
        failedTestRunAnalyser.writeSuiteWithResults();

        File[] suiteFiles = tempDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.contains("testsuite.xml");
            }
        });
        assertThat(suiteFiles.length, is(1));

        String suiteXmlContent = getAsNormalizedString(suiteFiles[0]);
        assertThat(suiteXmlContent, equalTo(partiallyFailedExecution));
    }

    @Test
    public void shouldNotWriteFailedSuiteForOnlyPassedAndMissing() throws IOException {
        Path pathToSurefireReports = Paths.get(tempDir.toString(), "target", "surefire-reports");
        pathToSurefireReports.toFile().mkdirs();
        File runSuitesDir = new File(tempDir, "suites-run");
        runSuitesDir.mkdir();
        File missedSuitesDir = new File(tempDir, "suites-missed");
        missedSuitesDir.mkdir();

        new File(missedSuitesDir, "missing.xml").createNewFile();
        new File(runSuitesDir, "finished.xml.start").createNewFile();
        new File(runSuitesDir, "finished.xml.finish").createNewFile();

        failedTestRunAnalyser = getUnitForSuites("finished.xml", "missing.xml");
        failedTestRunAnalyser.writeSuiteWithResults();

        File[] suiteFiles = tempDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.contains("testsuite.xml");
            }
        });
        assertThat(suiteFiles.length, is(0));
    }

    @Test
    public void shouldWriteSuiteForCompletelyFailedExecution() throws IOException {
        failedTestRunAnalyser.writeSuiteWithResults();

        File[] suiteFiles = tempDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.contains("testsuite.xml");
            }
        });
        assertThat(suiteFiles.length, is(1));

        String suiteXmlContent = getAsNormalizedString(suiteFiles[0]);
        assertThat(suiteXmlContent, equalTo(completelyFailedExecution));
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(tempDir);
    }

    private String getAsNormalizedString(File suiteFile) throws IOException {
        return FileUtils.readFileToString(suiteFile)
                .replaceAll("start=\"\\d+\"", "start=\"1450276237421\"")
                .replaceAll("stop=\"\\d+\"", "stop=\"1450276237421\"")
                .replace("\n", "").replace("\r", "");
    }
}
