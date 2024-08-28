package com.ericsson.cifwk.taf.executor.schedule;

import com.ericsson.cifwk.taf.executor.TestExecution;
import com.ericsson.cifwk.taf.executor.TestExecutionResult;

import java.io.PrintStream;

public interface TafTestExecutor {

    TestExecutionResult runTests(TestExecution execution, PrintStream buildLog);

}
