package com.ericsson.cifwk.taf.executor;

public class TestResult {

    public static enum Status {
        SUCCESS, ERROR, FAILURE
    }

    private Status status;

    public TestResult(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

}
