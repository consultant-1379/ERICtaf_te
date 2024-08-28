package com.ericsson.cifwk.taf.execution.operator.impl;

import com.beust.jcommander.internal.Lists;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.execution.operator.TeLogVisitor;
import com.ericsson.cifwk.taf.executor.api.TafTeJenkinsJob;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelJobStartedEvent;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

@Operator
public class LocalLogOperatorImpl extends AbstractLogOperatorImpl {

    @Override
    public String getAllureLogUrl(EiffelJobStartedEvent jobStartedEvent) {
        return jobStartedEvent.getLogReferences().get("allure_log_url").getUri();
    }

    @Override
    public String[] getTeConsoleLogs(String logDirectory) {
        File teConsoleLogsDirectory = new File(logDirectory);

        if (teConsoleLogsDirectory.exists() && teConsoleLogsDirectory.isDirectory()) {
            List<String> teConsoleLogs = Lists.newArrayList();

            File[] teLogs = teConsoleLogsDirectory.listFiles();
            if (teLogs == null) {
                return new String[0];
            }
            for (File log : teLogs) {
                if (log.isFile()) {
                    teConsoleLogs.add(log.toString());
                }
            }
            return teConsoleLogs.toArray(new String[teConsoleLogs.size()]);
        }
        return new String[0];
    }

    @Override
    public boolean isDirectory(String directory) {
        File logFolder = new File(directory);
        return logFolder.isDirectory();
    }

    @Override
    public String[] getAllureLogXmls(String logDirectory) {
        File executionLogFolder = new File(logDirectory);

        File[] allureXmlDirs = executionLogFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                return name.contains("testsuite.xml");
            }
        });

        ArrayList<String> allureLogXmlDirList = new ArrayList<>();

        for (File file : allureXmlDirs) {
            allureLogXmlDirList.add(file.toString());
        }

        return allureLogXmlDirList.toArray(new String[allureLogXmlDirList.size()]);
    }

    @Override
    public boolean allureLogIndexExists(String logDirectory) {
        File logFolder = new File(logDirectory + "/index.html");
        return logFolder.exists();
    }

    @Override
    public void closeShell() {
    }

    @Override
    public void verifyTeBuildLog(TafTeJenkinsJob build, TeLogVisitor visitor) {
        verifyTeBuildLog(build.getFullLogUrl(), visitor);
    }

}
