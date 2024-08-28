package com.ericsson.cifwk.taf.executor.node;

import com.ericsson.cifwk.taf.executor.TAFExecutor;
import com.ericsson.cifwk.taf.executor.TestExecution;
import com.ericsson.cifwk.taf.executor.TestExecutionResult;
import com.ericsson.cifwk.taf.executor.TestResult;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.PrintStream;
import java.util.Date;

public class TAFExecutorService implements TAFExecutor {

    @Override
    public TestExecutionResult execute(TestExecution execution, PrintStream buildLog) {
        buildLog.println("Starting test execution at " + new Date());

        TafTestNode testNode = createTafTestNode(buildLog, !execution.isManualTestExecution());
        TestResult testResult;
        try {
            testResult = testNode.execute(execution);
        } catch (Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            buildLog.println(String.format("Failed to execute TAF tests: %n%s", stackTrace));
            testResult = new TestResult(TestResult.Status.ERROR);
        }
        TestResult.Status status = testResult.getStatus();
        return new TestExecutionResult(status, execution.getTestPomLocation());
    }

    @VisibleForTesting
    TafTestNode createTafTestNode(PrintStream buildLog, boolean automatedTests) {
        TestRunner runner = getTestRunner(automatedTests, buildLog);
        return new TafTestNode(runner);
    }

    @VisibleForTesting
    TestRunner getTestRunner(boolean automatedTests, PrintStream buildLog) {
        TestRunner runner;
        if (automatedTests) {
            runner = new TafTestRunner(buildLog);
        } else {
            runner = new ManualTestRunner(buildLog);
        }
        return runner;
    }

}
