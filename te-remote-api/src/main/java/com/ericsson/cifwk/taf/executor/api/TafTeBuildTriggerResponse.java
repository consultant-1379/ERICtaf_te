package com.ericsson.cifwk.taf.executor.api;

import java.util.Date;

public class TafTeBuildTriggerResponse {

    private final Date generatedAt = new Date();
    private Status jobSchedulingStatus;
    private String errorTxt;
    private String triggeringEventId;
    private String jobExecutionId;
    private String jobUrl;

    public TafTeBuildTriggerResponse(String triggeringEventId, String jobExecutionId, String jobUrl) {
        this(triggeringEventId, jobExecutionId, jobUrl, Status.OK, null);
    }

    public TafTeBuildTriggerResponse(String triggeringEventId,
                                     String jobExecutionId,
                                     String jobUrl,
                                     Status jobSchedulingStatus,
                                     String errorTxt) {
        this.triggeringEventId = triggeringEventId;
        this.errorTxt = null;
        this.jobSchedulingStatus = jobSchedulingStatus;
        this.jobExecutionId = jobExecutionId;
        this.jobUrl = jobUrl;
        this.errorTxt = errorTxt;
    }

    public TafTeBuildTriggerResponse(String errorTxt) {
        this.errorTxt = errorTxt;
        if (errorTxt.contains("Aborting Spawned Jobs Result")) {
            this.jobSchedulingStatus = Status.ABORTED;
        }
        else {
            this.jobSchedulingStatus = Status.FAILURE;
        }
    }

    public static enum Status {
        OK, FAILURE, ABORTED
    }

    public Status getJobSchedulingStatus() {
        return jobSchedulingStatus;
    }

    public String getErrorTxt() {
        return errorTxt;
    }

    public String getJobExecutionId() {
        return jobExecutionId;
    }

    public String getJobUrl() {
        return jobUrl;
    }

    public String getTriggeringEventId() {
        return triggeringEventId;
    }

    public Date getGeneratedAt() {
        return generatedAt;
    }

    @Override
    public String toString() {
        return "TafTeBuildTriggerResponse{" +
                "generatedAt=" + generatedAt +
                ", jobSchedulingStatus=" + jobSchedulingStatus +
                ", errorTxt='" + errorTxt + '\'' +
                ", triggeringEventId=" + triggeringEventId +
                ", jobExecutionId=" + jobExecutionId +
                ", jobUrl='" + jobUrl + '\'' +
                '}';
    }
}
