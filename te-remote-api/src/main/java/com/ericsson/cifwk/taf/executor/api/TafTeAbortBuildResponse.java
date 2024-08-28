package com.ericsson.cifwk.taf.executor.api;

import java.util.Date;

public class TafTeAbortBuildResponse {

    private final Date generatedAt = new Date();
    private String message;

    public TafTeAbortBuildResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Date getGeneratedAt() {
        return generatedAt;
    }

    @Override
    public String toString() {
        return "TafTeBuildTriggerResponse{" +
                "generatedAt=" + generatedAt +
                ", message='" + message + '\'' +
                '}';
    }
}
