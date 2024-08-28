package com.ericsson.cifwk.taf.executor.node;

import com.ericsson.cifwk.taf.FakeSuiteGenerator;
import com.google.common.collect.Lists;
import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class FailedTestRunAnalyser {

    private File workingDir;
    private List<String> suitesToRun;
    private String allureLogDirPath;
    private PrintStream buildLog;

    public FailedTestRunAnalyser(File workingDir, String[] suites, String allureLogDirPath, PrintStream buildLog) {
        this.workingDir = workingDir;
        this.suitesToRun = Lists.newArrayList(suites);
        this.allureLogDirPath = allureLogDirPath;
        this.buildLog = buildLog;
    }


    public void writeSuiteWithResults() {
        Path pathToSurefireReports = Paths.get(workingDir.toString(), "target", "surefire-reports");
        boolean testsWereExecuted = pathToSurefireReports.toFile().exists();
        File suitesRunDir = new File(workingDir, "suites-run");
        File suitesMissedDir = new File(workingDir, "suites-missed");
        // Allure reports for missed suites already prepared by TAF SP at this stage, bypass them here
        String[] missedSuiteXmlList = suitesMissedDir.exists() ? suitesMissedDir.list() : new String[0];
        suitesToRun.removeAll(Arrays.asList(missedSuiteXmlList));

        if (suitesToRun.isEmpty()) {
            return;
        }

        if (!testsWereExecuted) {
            //nothing run, total fail
            writeFakeSuiteForFailedExecution();
        } else if (suitesRunDir.exists() && suitesRunDir.list().length > 0) {
            List<String> onlyStartedSuites = Lists.newArrayList();
            List<String> finishedSuites = Lists.newArrayList();
            for (String suitesRunStatus : suitesRunDir.list()) {
                if (suitesRunStatus.endsWith("start")) {
                    onlyStartedSuites.add(suitesRunStatus.replace(".start", ""));
                } else if (suitesRunStatus.endsWith("finish")) {
                    finishedSuites.add(suitesRunStatus.replace(".finish", ""));
                }
            }
            suitesToRun.removeAll(onlyStartedSuites);
            onlyStartedSuites.removeAll(finishedSuites);

            if (!onlyStartedSuites.isEmpty()) {
                generateFakeSuiteForPartialExecution(finishedSuites, onlyStartedSuites, suitesToRun);
            }
        }
    }

    private void writeFakeSuiteForFailedExecution() {
        FakeSuiteGenerator fakeSuiteGenerator = new FakeSuiteGenerator();
        try {
            String fakeSuiteContents = fakeSuiteGenerator.generateForFailedExecution(suitesToRun);
            writeSuiteFile(fakeSuiteContents);
        } catch (IOException | TemplateException e) {   // NOSONAR
            buildLog.println("Failed to generate fake suite xml in " + allureLogDirPath);
        }
    }

    private void generateFakeSuiteForPartialExecution(List<String> finishedSuites, List<String> notFinishedSuites, List<String> notRunSuites) {
        FakeSuiteGenerator fakeSuiteGenerator = new FakeSuiteGenerator();
        try {
            String fakeSuiteContents = fakeSuiteGenerator.generateForPartialExecution(finishedSuites, notFinishedSuites, notRunSuites);
            writeSuiteFile(fakeSuiteContents);
        } catch (IOException | TemplateException e) { // NOSONAR
            buildLog.println("Failed to generate fake suite xml in " + allureLogDirPath);
        }
    }

    private void writeSuiteFile(String fakeSuiteContents) throws IOException {
        String fakeSuiteName = generateRandomSuiteName();
        File allureFakeSuite = new File(allureLogDirPath, fakeSuiteName);
        FileUtils.write(allureFakeSuite, fakeSuiteContents);
        buildLog.println(
                String.format("Failed to run TAF tests; generated appropriate Allure result suite xml %s in %s",
                        fakeSuiteName,
                        allureLogDirPath));
    }

    private String generateRandomSuiteName() {return String.format("%s-testsuite.xml", UUID.randomUUID().toString());}


}
