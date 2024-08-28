package com.ericsson.cifwk.taf.executor;

import java.io.Serializable;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 26/06/2017
 */
public class TestExecutionResult implements Serializable {

    private TestResult.Status testResultStatus;
    private String testPomLocation;

    public TestExecutionResult() {}

    public TestExecutionResult(TestResult.Status testResultStatus, String testPomLocation) {
        this.testResultStatus = testResultStatus;
        this.testPomLocation = testPomLocation;
    }

    public TestResult.Status getTestResultStatus() {
        return testResultStatus;
    }

    public void setTestResultStatus(TestResult.Status testResultStatus) {
        this.testResultStatus = testResultStatus;
    }

    public String getTestPomLocation() {
        return testPomLocation;
    }

    public void setTestPomLocation(String testPomLocation) {
        this.testPomLocation = testPomLocation;
    }

    @Override
    public String toString() {
        return "TestExecutionResult{" +
                "testResultStatus=" + testResultStatus +
                ", testPomLocation='" + testPomLocation + '\'' +
                '}';
    }
}
