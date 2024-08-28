package com.ericsson.cifwk.taf.executor.api;

/**
 * Description of TAF TE Jenkins job
 */
public interface TafTeJenkinsJob {

    /**
     * Type of the TAF TE job - Scheduler, Flow or Executor (that is created by Flow)
     * @return
     */
    Type getType();

    /**
     * Get the result of the completed TAF TE job - <code>Result.SUCCESS</code> or <code>Result.FAILURE</code>.
     * If the build was aborted, <code>Result.FAILURE</code> is returned.
     * @return job result for completed job (RunStatus = COMPLETE), or <code>null</code> for the running job
     * (RunStatus = BUILDING).
     */
    Result getResult();

    /**
     * Get Jenkins job name - e.g., "TAF_EXECUTOR#1"
     * @return
     */
    String getName();

    /**
     * <p>Get the name of the TE schedule item that is run in this job - e.g., "CDB job part 1".</p>
     * <p>Equals to item's &lt;name&gt; tag contents in schedule XML.</p>
     * @return  schedule item name for <code>EXECUTOR</code> job, <code>null</code> for other job types.
     */
    String getScheduleItemName();

    /**
     * Get the current job execution status
     * @return
     */
    RunStatus getRunStatus();

    /**
     * Returns <code>true</code> if the current job is complete, <code>false</code> otherwise
     * @return <code>true</code> if the current job is complete, <code>false</code> otherwise
     */
    boolean isComplete();

    /**
     * Get Jenkins job number
     * @return
     */
    int getNumber();

    /**
     * Get job details URL
     * @return
     */
    String getUrl();

    /**
     * Get full job log URL
     * @return
     */
    String getFullLogUrl();

    enum Type {
        SCHEDULER, EXECUTOR, FLOW
    }

    enum RunStatus {
        COMPLETE, BUILDING
    }

    enum Result {
        SUCCESS, FAILURE, ABORTED
    }
}
