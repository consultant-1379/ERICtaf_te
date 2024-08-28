package com.ericsson.cifwk.taf.execution.operator.impl;

import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.assertions.TafAsserts;
import com.ericsson.cifwk.taf.execution.operator.TeLogVisitor;
import com.ericsson.cifwk.taf.execution.utils.UrlUtils;
import com.ericsson.cifwk.taf.executor.api.TafTeJenkinsJob;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelJobStartedEvent;
import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.collect.Collections2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Operator
public class RemoteLogOperatorImpl extends AbstractLogOperatorImpl {

    private final static Logger LOGGER = LoggerFactory.getLogger(RemoteLogOperatorImpl.class);
    private static final String TE_MASTER_INTERNAL = "jenkinsm1";
    protected static final String TE_LOGS_FILE_FOLDER = "/var/log/te_logs";

    private final CLICommandHelper cmdHelper;

    public RemoteLogOperatorImpl() {
        super();
        try {
            cmdHelper = new CLICommandHelper(teMasterHost);
            cmdHelper.openShell();
        } catch (Exception e) {
            LOGGER.error("Connection to host failed", e);
            throw Throwables.propagate(e);
        }
    }

    @Override
    public String getAllureLogUrl(EiffelJobStartedEvent jobStartedEvent) {
        return jobStartedEvent.getLogReferences().get("allure_log_url").getUri();
    }

    @Override
    public String[] getTeConsoleLogs(String logDirectory) {
        String cmd = String.format("find %s -maxdepth 1 -type f -name \\*", logDirectory);
        String response = cmdHelper.simpleExec(cmd);
        LOGGER.debug("Response: " + response);
        List<String> responseList = Arrays.asList(response.split(System.getProperty("line.separator")));
        Collection<String> result = Collections2.filter(responseList, Predicates.containsPattern(TE_LOGS_FILE_FOLDER));
        return result.toArray(new String[result.size()]);
    }

    private int getDirectoryFileCount(String directory) {
        return Integer.parseInt(cmdHelper.simpleExec("ls | wc -l").trim());
    }

    public String[] getAllureLogXmls(String logDirectory) {
        String cmd = String.format("find %s -maxdepth 1 -type f -name \\*.xml", logDirectory);
        String response = cmdHelper.simpleExec(cmd);
        LOGGER.debug("Response: " + response);
        String[] allureXmls = response.split("\\r\\n");
        return allureXmls;
    }

    @Override
    public boolean isDirectory(String logDirectory) {
        String response = cmdHelper.simpleExec("file " + logDirectory);
        LOGGER.debug("Response: " + response);
        String type = response.split(": ")[1].trim();
        return type.equals("directory");
    }

    @Override
    public boolean allureLogIndexExists(String logDirectory) {
        boolean result = false;
        String cmd = "ls " + logDirectory + "/index.html";
        String response = cmdHelper.simpleExec(cmd);
        LOGGER.debug("Response: " + response);
        if (response.contains("index.html") && !response.contains("No such")) {
            result = true;
        }
        return result;
    }

    @Override
    public void closeShell() {
        try {
            cmdHelper.interactWithShell("exit");
            cmdHelper.expectShellClosure(8);
        } catch (Exception e) {
            LOGGER.error(String.format("Closing connection with host '%s' has failed", teMasterHostAddress), e);
            throw Throwables.propagate(e);
        }
        TafAsserts.assertTrue("Checking command helper was closed successfully", cmdHelper.isClosed());
    }

    @Override
    public void verifyTeBuildLog(TafTeJenkinsJob build, TeLogVisitor visitor) {
        String fullLogUrl = UrlUtils.replaceHost(build.getFullLogUrl(), teMasterHostAddress);
        verifyTeBuildLog(fullLogUrl, visitor);
    }
}
