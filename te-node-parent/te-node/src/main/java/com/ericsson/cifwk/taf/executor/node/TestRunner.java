package com.ericsson.cifwk.taf.executor.node;


import com.ericsson.cifwk.taf.executor.TestExecution;
import com.ericsson.cifwk.taf.executor.TestResult;

public interface TestRunner {

    void setUp(TestExecution execution);

    TestResult runTest();

    void tearDown();

}