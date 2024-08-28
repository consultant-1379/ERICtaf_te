package com.ericsson.cifwk.taf.executor.cluster;

import com.ericsson.cifwk.taf.executor.TestExecution;
import com.ericsson.cifwk.taf.executor.TestExecutionResult;
import com.ericsson.cifwk.taf.executor.schedule.LocalTestExecutor;
import com.ericsson.cifwk.taf.executor.schedule.TafTestExecutor;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import hudson.remoting.Callable;
import org.jenkinsci.remoting.RoleChecker;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class RemoteTafLauncher implements Callable<String, Exception>, Serializable {

    private static final long serialVersionUID = -5594713333790329784L;

    private final TestExecution execution;

    private final OutputStream masterOutputStream; // NOSONAR


    public RemoteTafLauncher(TestExecution execution, OutputStream masterBuildOutputStream) {
        this.execution = execution;
        this.masterOutputStream = masterBuildOutputStream;
    }

    @Override
    public String call() throws Exception {
        String runStartedMsg = "Running RemoteTafLauncher on "
                + InetAddress.getLocalHost().getHostAddress()
                + ", thread is " + Thread.currentThread();
        PrintStream masterPrintStream = new PrintStream(masterOutputStream, false, StandardCharsets.UTF_8.name());
        masterPrintStream.println(runStartedMsg);
        TafTestExecutor executor = getTafTestExecutor();
        masterPrintStream.println("Using executor " + executor);
        TestExecutionResult executionResult = executor.runTests(execution, masterPrintStream);
        return new Gson().toJson(executionResult);
    }

    @VisibleForTesting
    public TafTestExecutor getTafTestExecutor() {
        return new LocalTestExecutor();
    }

    @Override
    public void checkRoles(RoleChecker roleChecker) throws SecurityException {
    }
}
