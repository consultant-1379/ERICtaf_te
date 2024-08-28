package com.ericsson.cifwk.taf.executor.node;

import com.ericsson.cifwk.taf.executor.TestExecution;
import com.ericsson.cifwk.taf.executor.TestResult;

public class TafTestNode {

    final TestRunner runner;

    public TafTestNode(TestRunner runner) {
        this.runner = runner;
    }

    public TestResult execute(TestExecution execution) {
        runner.setUp(execution);
        try {
            return runner.runTest();
        } finally {
            runner.tearDown();
        }
    }

}
