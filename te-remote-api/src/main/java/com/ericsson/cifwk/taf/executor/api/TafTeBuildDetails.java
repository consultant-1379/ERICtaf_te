package com.ericsson.cifwk.taf.executor.api;

import com.ericsson.cifwk.taf.executor.api.schedule.model.Schedule;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class TafTeBuildDetails {

    private final Date generatedAt = new Date();
    private final List<TafTeJenkinsJob> tafTeJenkinsJobs;
    private final String allureLogUrl;
    private final Boolean buildComplete;
    private final Boolean success;
    private final String jobExecutionId;
    private final String errorTxt;
    private final Schedule schedule;

    public TafTeBuildDetails(String errorTxt) {
        this.errorTxt = errorTxt;
        this.tafTeJenkinsJobs = new ArrayList<>();
        this.allureLogUrl = "";
        this.jobExecutionId = "";
        this.buildComplete = null;
        this.success = null;
        this.schedule = null;
    }

    public TafTeBuildDetails(List<TafTeJenkinsJob> tafTeJenkinsJobs, String allureLogUrl, String jobExecutionId, Schedule schedule) {
        this.tafTeJenkinsJobs = tafTeJenkinsJobs;
        this.allureLogUrl = allureLogUrl;
        this.jobExecutionId = jobExecutionId;
        this.schedule = schedule;
        this.errorTxt = null;

        boolean schedulerJobFound = false;
        boolean flowJobFound = false;
        boolean executorJobFound = false;
        boolean allComplete = true;
        boolean allSucceeded = true;
        boolean schedulerJobComplete = false;

        for (TafTeJenkinsJob spawnedJob : this.tafTeJenkinsJobs) {
            if (TafTeJenkinsJob.Type.SCHEDULER == spawnedJob.getType()) {
                schedulerJobFound = true;
                schedulerJobComplete = spawnedJob.isComplete();
            }
            if (TafTeJenkinsJob.Type.FLOW == spawnedJob.getType()) {
                flowJobFound = true;
            }
            if (TafTeJenkinsJob.Type.EXECUTOR == spawnedJob.getType()) {
                executorJobFound = true;
            }
            if (!spawnedJob.isComplete()) {
                allComplete = false;
            }
            if (TafTeJenkinsJob.Result.FAILURE == spawnedJob.getResult()) {
                allSucceeded = false;
            }
        }

        boolean notTriggered = !schedulerJobFound && !flowJobFound && !executorJobFound;
        this.buildComplete = notTriggered ? Boolean.TRUE : (schedulerJobComplete && allComplete);
        if (notTriggered) {
            this.success = Boolean.FALSE;
        } else {
            this.success = allSucceeded ? (buildComplete ? allSucceeded : null) : Boolean.FALSE;
        }
    }

    /**
     * Get jobs created by build scheduling.
     * @return result includes the scheduler job, the build flow job and all the executor jobs it has created since build was triggered
     * and till the invocation of the current method. Please note that flow can generate more executors after the call,
     * because it doesn't have the complete list of executors till the very end of the overall build run.
     * The scheduler job details are returned in the response to <code>triggerBuild</code> invocation and are
     * not available in this return data set.
     */
    public List<TafTeJenkinsJob> getTafTeJenkinsJobs(TafTeJenkinsJob.Type... jobTypes) {
        if (jobTypes.length == 0) {
            return tafTeJenkinsJobs;
        }
        final Set<TafTeJenkinsJob.Type> jobTypeSet = Sets.newHashSet(jobTypes);
        Iterable<TafTeJenkinsJob> result = Iterables.filter(tafTeJenkinsJobs, new Predicate<TafTeJenkinsJob>() {
            @Override
            public boolean apply(TafTeJenkinsJob tafTeJenkinsJob) {
                return jobTypeSet.contains(tafTeJenkinsJob.getType());
            }
        });
        return Lists.newArrayList(result);
    }

    public String getAllureLogUrl() {
        return allureLogUrl;
    }

    public Schedule getSchedule() { return schedule; }

    public Boolean getBuildComplete() {
        return buildComplete;
    }

    public Boolean getSuccess() {
        return success;
    }

    public Date getGeneratedAt() {
        return generatedAt;
    }

    public String getJobExecutionId() {
        return jobExecutionId;
    }

    public String getErrorTxt() {
        return errorTxt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TafTeBuildDetails that = (TafTeBuildDetails) o;

        if (buildComplete != that.buildComplete) return false;
        if (allureLogUrl != null ? !allureLogUrl.equals(that.allureLogUrl) : that.allureLogUrl != null) return false;
        if (!generatedAt.equals(that.generatedAt)) return false;
        if (jobExecutionId != null ? !jobExecutionId.equals(that.jobExecutionId) : that.jobExecutionId != null)
            return false;
        if (schedule != null ? !schedule.equals(that.schedule) : that.schedule != null)
            return false;
        if (success != null ? !success.equals(that.success) : that.success != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = generatedAt.hashCode();
        result = 31 * result + (allureLogUrl != null ? allureLogUrl.hashCode() : 0);
        result = 31 * result + (buildComplete ? 1 : 0);
        result = 31 * result + (success != null ? success.hashCode() : 0);
        result = 31 * result + (jobExecutionId != null ? jobExecutionId.hashCode() : 0);
        result = 31 * result + (schedule != null ? schedule.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TafTeBuildDetails{" +
                "generatedAt=" + generatedAt +
                ", tafTeJenkinsJobs=" + tafTeJenkinsJobs +
                ", allureLogUrl='" + allureLogUrl + '\'' +
                ", buildComplete=" + buildComplete +
                ", success=" + success +
                ", jobExecutionId='" + jobExecutionId + '\'' +
                ", schedule='" + schedule + '\'' +
                ", errorTxt='" + errorTxt + '\'' +
                '}';
    }
}
