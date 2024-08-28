package com.ericsson.cifwk.taf.executor.spi;

import com.ericsson.cifwk.taf.executor.TAFExecutor;
import com.ericsson.cifwk.taf.executor.TestExecution;
import com.ericsson.cifwk.taf.executor.TestExecutionResult;
import com.ericsson.cifwk.taf.executor.TestResult;
import com.google.common.base.Throwables;

import java.io.PrintStream;

public class TestTAFExecutorService implements TAFExecutor {

    public static final int JOB_STEPS_AMOUNT = 100;

    public static final String LOG_RECORD_PATTERN = "Executing '%s' schedule item \\- [0-9]+ of [0-9]+ build steps";

    private static final String LOG_RECORD_TEMPLATE = "Executing '%s' schedule item - %d of %d build steps";


    @Override
    public TestExecutionResult execute(TestExecution execution, PrintStream buildLog) {
        buildLog.println("Starting TAF execution of " + execution);

        try {
            for (int i = 1; i <= JOB_STEPS_AMOUNT; i++) {
                buildLog.println(String.format(LOG_RECORD_TEMPLATE, execution.getName(), i, JOB_STEPS_AMOUNT));
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        }

        return new TestExecutionResult(TestResult.Status.SUCCESS, null);
    }

}
