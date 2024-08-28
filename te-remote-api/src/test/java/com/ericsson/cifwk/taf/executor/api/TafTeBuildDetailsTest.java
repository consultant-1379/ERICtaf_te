package com.ericsson.cifwk.taf.executor.api;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;

public class TafTeBuildDetailsTest {

    @Test
    public void testOverallResultsForBuildInProgress() throws Exception {
        List<TafTeJenkinsJob> jobs = Arrays.asList(
                createJob(TafTeJenkinsJobImpl.Type.SCHEDULER, 1, TafTeJenkinsJob.RunStatus.BUILDING, null),
                createJob(TafTeJenkinsJobImpl.Type.FLOW, 1, TafTeJenkinsJob.RunStatus.BUILDING, null),
                createJob(TafTeJenkinsJobImpl.Type.EXECUTOR, 1, TafTeJenkinsJob.RunStatus.BUILDING, null),
                createJob(TafTeJenkinsJobImpl.Type.EXECUTOR, 2, TafTeJenkinsJob.RunStatus.COMPLETE, TafTeJenkinsJob.Result.SUCCESS)
        );

        TafTeBuildDetails unit = new TafTeBuildDetails(jobs, "http://allureLogs", "aa-bb-cc", null);
        Assert.assertThat(unit.getAllureLogUrl(), equalTo("http://allureLogs"));
        Assert.assertThat(unit.getJobExecutionId(), equalTo("aa-bb-cc"));
        Assert.assertNull(unit.getSuccess());
    }

    @Test
    public void testOverallResultsForCompleteSuccessfulBuild() throws Exception {
        List<TafTeJenkinsJob> jobs = Arrays.asList(
                createJob(TafTeJenkinsJobImpl.Type.SCHEDULER, 1, TafTeJenkinsJob.RunStatus.COMPLETE, TafTeJenkinsJob.Result.SUCCESS),
                createJob(TafTeJenkinsJobImpl.Type.FLOW, 1, TafTeJenkinsJob.RunStatus.COMPLETE, TafTeJenkinsJob.Result.SUCCESS),
                createJob(TafTeJenkinsJobImpl.Type.EXECUTOR, 1, TafTeJenkinsJob.RunStatus.COMPLETE, TafTeJenkinsJob.Result.SUCCESS),
                createJob(TafTeJenkinsJobImpl.Type.EXECUTOR, 2, TafTeJenkinsJob.RunStatus.COMPLETE, TafTeJenkinsJob.Result.SUCCESS)
        );
        TafTeBuildDetails unit = new TafTeBuildDetails(jobs, null, null, null);
        Assert.assertThat(unit.getSuccess(), equalTo(Boolean.TRUE));
        Assert.assertThat(unit.getBuildComplete(), equalTo(Boolean.TRUE));
    }

    @Test
    public void testOverallResultsForBuildingFailingBuild() throws Exception {
        List<TafTeJenkinsJob> jobs = Arrays.asList(
                createJob(TafTeJenkinsJobImpl.Type.SCHEDULER, 1, TafTeJenkinsJob.RunStatus.BUILDING, null),
                createJob(TafTeJenkinsJobImpl.Type.FLOW, 1, TafTeJenkinsJob.RunStatus.BUILDING, null),
                createJob(TafTeJenkinsJobImpl.Type.EXECUTOR, 1, TafTeJenkinsJob.RunStatus.COMPLETE, TafTeJenkinsJob.Result.SUCCESS),
                createJob(TafTeJenkinsJobImpl.Type.EXECUTOR, 2, TafTeJenkinsJob.RunStatus.COMPLETE, TafTeJenkinsJob.Result.FAILURE)
        );
        TafTeBuildDetails unit = new TafTeBuildDetails(jobs, null, null, null);
        Assert.assertThat(unit.getSuccess(), equalTo(Boolean.FALSE));
        Assert.assertThat(unit.getBuildComplete(), equalTo(Boolean.FALSE));
    }

    @Test
    public void testOverallResultsForCompleteFailedBuild() throws Exception {
        List<TafTeJenkinsJob> jobs = Arrays.asList(
                createJob(TafTeJenkinsJobImpl.Type.SCHEDULER, 1, TafTeJenkinsJob.RunStatus.COMPLETE, TafTeJenkinsJob.Result.SUCCESS),
                createJob(TafTeJenkinsJobImpl.Type.FLOW, 1, TafTeJenkinsJob.RunStatus.COMPLETE, TafTeJenkinsJob.Result.SUCCESS),
                createJob(TafTeJenkinsJobImpl.Type.EXECUTOR, 1, TafTeJenkinsJob.RunStatus.COMPLETE, TafTeJenkinsJob.Result.SUCCESS),
                createJob(TafTeJenkinsJobImpl.Type.EXECUTOR, 2, TafTeJenkinsJob.RunStatus.COMPLETE, TafTeJenkinsJob.Result.FAILURE)
        );
        TafTeBuildDetails unit = new TafTeBuildDetails(jobs, null, null, null);
        Assert.assertThat(unit.getSuccess(), equalTo(Boolean.FALSE));
        Assert.assertThat(unit.getBuildComplete(), equalTo(Boolean.TRUE));
    }

    @Test
    public void testOverallResultsForJustStartedBuild() throws Exception {
        List<TafTeJenkinsJob> jobs = Arrays.asList(
                createJob(TafTeJenkinsJobImpl.Type.SCHEDULER, 1, TafTeJenkinsJob.RunStatus.BUILDING, null)
        );
        TafTeBuildDetails unit = new TafTeBuildDetails(jobs, null, null, null);
        Assert.assertNull(unit.getSuccess());
        Assert.assertThat(unit.getBuildComplete(), equalTo(Boolean.FALSE));
    }

    @Test
    public void testOverallResultsForJustStartedBuildWithFlow() throws Exception {
        List<TafTeJenkinsJob> jobs = Arrays.asList(
                createJob(TafTeJenkinsJobImpl.Type.SCHEDULER, 1, TafTeJenkinsJob.RunStatus.BUILDING, null),
                createJob(TafTeJenkinsJobImpl.Type.FLOW, 1, TafTeJenkinsJob.RunStatus.BUILDING, null)
        );
        TafTeBuildDetails unit = new TafTeBuildDetails(jobs, null, null, null);
        Assert.assertNull(unit.getSuccess());
        Assert.assertThat(unit.getBuildComplete(), equalTo(Boolean.FALSE));
    }

    @Test
    public void testOverallResultsForFailedToStart() throws Exception {
        TafTeBuildDetails unit = new TafTeBuildDetails(Lists.<TafTeJenkinsJob>newArrayList(), "http://allureLogs", "aa-bb-cc", null);
        Assert.assertThat(unit.getSuccess(), equalTo(Boolean.FALSE));
        Assert.assertThat(unit.getBuildComplete(), equalTo(Boolean.TRUE));
    }

    @Test
    public void testOverallResultsForStartedButFailedToSpawnBuild() throws Exception {
        List<TafTeJenkinsJob> jobs = Arrays.asList(
                createJob(TafTeJenkinsJobImpl.Type.SCHEDULER, 1, TafTeJenkinsJob.RunStatus.COMPLETE, TafTeJenkinsJob.Result.FAILURE)
        );
        TafTeBuildDetails unit = new TafTeBuildDetails(jobs, null, null, null);
        Assert.assertThat(unit.getSuccess(), equalTo(Boolean.FALSE));
        Assert.assertThat(unit.getBuildComplete(), equalTo(Boolean.TRUE));
    }

    @Test
    public void testOverallResultsForAbortedBuild() throws Exception {
        List<TafTeJenkinsJob> jobs = Arrays.asList(
                createJob(TafTeJenkinsJobImpl.Type.SCHEDULER, 1, TafTeJenkinsJob.RunStatus.COMPLETE, TafTeJenkinsJob.Result.FAILURE),
                createJob(TafTeJenkinsJobImpl.Type.FLOW, 1, TafTeJenkinsJob.RunStatus.COMPLETE, TafTeJenkinsJob.Result.SUCCESS)
        );
        TafTeBuildDetails unit = new TafTeBuildDetails(jobs, null, null, null);
        Assert.assertThat(unit.getSuccess(), equalTo(Boolean.FALSE));
        Assert.assertThat(unit.getBuildComplete(), equalTo(Boolean.TRUE));
    }

    @Test
    public void testOverallResultsForAbortedSchedulerJob() throws Exception {
        List<TafTeJenkinsJob> jobs = Arrays.asList(
                // Scheduler was aborted, but one of the spawned jobs is still running
                createJob(TafTeJenkinsJobImpl.Type.SCHEDULER, 1, TafTeJenkinsJob.RunStatus.COMPLETE, TafTeJenkinsJob.Result.FAILURE),
                createJob(TafTeJenkinsJobImpl.Type.FLOW, 1, TafTeJenkinsJob.RunStatus.COMPLETE, TafTeJenkinsJob.Result.SUCCESS),
                createJob(TafTeJenkinsJobImpl.Type.EXECUTOR, 1, TafTeJenkinsJob.RunStatus.COMPLETE, TafTeJenkinsJob.Result.SUCCESS),
                createJob(TafTeJenkinsJobImpl.Type.EXECUTOR, 2, TafTeJenkinsJob.RunStatus.BUILDING, null)
        );
        TafTeBuildDetails unit = new TafTeBuildDetails(jobs, null, null, null);
        Assert.assertThat(unit.getSuccess(), equalTo(Boolean.FALSE));
        Assert.assertThat(unit.getBuildComplete(), equalTo(Boolean.FALSE));
    }

    @Test
    public void testGetJobByType() throws Exception {
        List<TafTeJenkinsJob> jobs = Arrays.asList(
                createJob(TafTeJenkinsJobImpl.Type.SCHEDULER, 1, TafTeJenkinsJob.RunStatus.COMPLETE, TafTeJenkinsJob.Result.FAILURE),
                createJob(TafTeJenkinsJobImpl.Type.EXECUTOR, 1, TafTeJenkinsJob.RunStatus.COMPLETE, TafTeJenkinsJob.Result.SUCCESS),
                createJob(TafTeJenkinsJobImpl.Type.EXECUTOR, 2, TafTeJenkinsJob.RunStatus.BUILDING, null)
        );
        TafTeBuildDetails unit = new TafTeBuildDetails(jobs, null, null, null);
        Assert.assertEquals(3, unit.getTafTeJenkinsJobs().size());
        Assert.assertEquals(2, unit.getTafTeJenkinsJobs(TafTeJenkinsJob.Type.EXECUTOR).size());
        Assert.assertEquals(1, unit.getTafTeJenkinsJobs(TafTeJenkinsJob.Type.SCHEDULER).size());
        Assert.assertEquals(0, unit.getTafTeJenkinsJobs(TafTeJenkinsJob.Type.FLOW).size());
        Assert.assertEquals(3, unit.getTafTeJenkinsJobs(TafTeJenkinsJob.Type.SCHEDULER, TafTeJenkinsJob.Type.EXECUTOR).size());
    }

    private TafTeJenkinsJob createJob(TafTeJenkinsJobImpl.Type type, int number,
                                            TafTeJenkinsJob.RunStatus runStatus, TafTeJenkinsJob.Result result) {
        String name = type.toString();
        return new TafTeJenkinsJobImpl(type, name, name, number,
                "http://" + name + "/" + number,
                "http://" + name + "/" + number + "/log",
                runStatus, result);
    }

}