package com.ericsson.cifwk.taf.executor;

import com.cloudbees.plugins.flow.FlowRun;
import com.ericsson.cifwk.taf.executor.abort.AbortedJobListener;
import com.ericsson.cifwk.taf.executor.api.TafTeAbortBuildResponse;
import com.ericsson.duraci.datawrappers.ExecutionId;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hudson.model.Executor;
import jenkins.model.Jenkins;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.StringWriter;
import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TeRestServiceAbortTest {

    private final Gson gson = new GsonBuilder().create();

    private TeRestService unit;
    private AbortedJobListener abortedJobListener;
    private ExecutionId executionId = new ExecutionId("1234");
    private TafScheduleBuild tafScheduleBuild = mock(TafScheduleBuild.class);
    private Jenkins jenkins = mock(Jenkins.class);
    private TafExecutionBuild tafExecutionBuild = mock(TafExecutionBuild.class);
    private TafExecutionProject tafExecutionProject = mock(TafExecutionProject.class);
    private FlowRun flowRun = mock(FlowRun.class);
    private StaplerRequest staplerRequest = mock(StaplerRequest.class);
    private StaplerResponse staplerResponse = mock(StaplerResponse.class);

    @Before
    public void setUp() throws Exception {
        unit = new TeRestService();
        unit = spy(unit);

        doReturn(jenkins).when(unit).getJenkinsInstance();

        TafScheduleProject tafScheduleProject = mock(TafScheduleProject.class);
        doReturn(tafScheduleProject).when(unit).getProjectOfType(eq(jenkins), eq(TafScheduleProject.class));

        doReturn(tafScheduleBuild).when(unit).waitForSchedulerJob(eq(tafScheduleProject), any(ExecutionId.class));
        doReturn(tafScheduleBuild).when(unit).findSchedulerJob(eq(jenkins), any(ExecutionId.class));

        doReturn(Arrays.asList(tafExecutionBuild)).when(unit).getTafExecutionBuilds(any(ExecutionId.class), eq(tafExecutionProject));


        doReturn(flowRun).when(unit).getFlowRun(any(ExecutionId.class), eq(jenkins));

        abortedJobListener = mock(AbortedJobListener.class);
        doReturn(abortedJobListener).when(unit).getAbortedJobListener(jenkins);
    }

    @Test
    public void testDoAbort() throws Exception {
        Executor flowExecutor = mock(Executor.class);
        Executor scheduleExecutor = mock(Executor.class);
        StringWriter writer = new StringWriter();
        when(staplerResponse.getCompressedWriter(eq(staplerRequest))).thenReturn(writer);

        doReturn("1234").when(unit).getRequestBody(staplerRequest);
        when(flowRun.getExecutor()).thenReturn(flowExecutor, flowExecutor, null, null);
        doReturn(scheduleExecutor).when(tafScheduleBuild).getExecutor();
        doReturn(tafExecutionProject).when(unit).getProjectOfType(jenkins, TafExecutionProject.class);
        HttpResponse response = unit.doAbort(staplerRequest, staplerResponse);
        response.generateResponse(staplerRequest, staplerResponse, null);

        String responseStr = writer.getBuffer().toString();
        TafTeAbortBuildResponse tafTeBuildTriggerResponse = gson.fromJson(responseStr, TafTeAbortBuildResponse.class);
        verify(tafScheduleBuild).abort();
        verify(abortedJobListener).setExecutionAsAborted(executionId);
        verify(tafExecutionBuild).abort();
        verify(flowExecutor).interrupt();
        verify(abortedJobListener).remove(executionId);
        Assert.assertTrue(tafTeBuildTriggerResponse.getMessage().contains("All Spawned Jobs are aborted"));
    }

    @Test
    public void testDoAbortHangingSpawnedJob() throws Exception {
        Executor flowExecutor = mock(Executor.class);
        Executor scheduleExecutor = mock(Executor.class);
        StringWriter writer = new StringWriter();
        when(staplerResponse.getCompressedWriter(eq(staplerRequest))).thenReturn(writer);

        doReturn("1234").when(unit).getRequestBody(staplerRequest);
        when(flowRun.getExecutor()).thenReturn(flowExecutor, flowExecutor, flowExecutor, flowExecutor);
        doReturn(scheduleExecutor).when(tafScheduleBuild).getExecutor();
        doReturn(tafExecutionProject).when(unit).getProjectOfType(jenkins, TafExecutionProject.class);
        HttpResponse response = unit.doAbort(staplerRequest, staplerResponse);
        response.generateResponse(staplerRequest, staplerResponse, null);

        String responseStr = writer.getBuffer().toString();
        TafTeAbortBuildResponse tafTeBuildTriggerResponse = gson.fromJson(responseStr, TafTeAbortBuildResponse.class);
        verify(tafScheduleBuild).abort();
        verify(abortedJobListener).setExecutionAsAborted(executionId);
        verify(tafExecutionBuild).abort();
        verify(flowExecutor, atLeast(1)).interrupt();
        verify(abortedJobListener).remove(executionId);
        Assert.assertTrue(tafTeBuildTriggerResponse.getMessage().contains("All spawned jobs were not successfully aborted"));
    }

    @Test
    public void testAbortScheduleBuild() throws Exception {
        unit.abortScheduleBuild(executionId, abortedJobListener, tafScheduleBuild);
        verify(abortedJobListener).setExecutionAsAborted(executionId);
        verify(tafScheduleBuild).abort();
    }

    @Test
    public void testAbortExecutionJobs() throws Exception {
        doReturn(tafExecutionProject).when(unit).getProjectOfType(jenkins, TafExecutionProject.class);
        unit.abortExecutionJobs(executionId, jenkins);
        verify(tafExecutionBuild).abort();
    }

    @Test
    public void testAbortExecutionJobsExecutionProjectNotExist() throws Exception {
        doReturn(null).when(unit).getProjectOfType(jenkins, TafExecutionProject.class);
        verify(unit, times(0)).getTafExecutionBuilds(any(ExecutionId.class), any(TafExecutionProject.class));
    }

    @Test
    public void testAbortFlowRun() throws Exception {
        Executor executor = mock(Executor.class);
        doReturn(executor).when(flowRun).getExecutor();
        unit.abortFlowRun(flowRun);
        verify(flowRun).getExecutor();
        verify(executor).interrupt();
    }

    @Test
    public void testAbortNotRunningFlowRun() throws Exception {
        doReturn(null).when(flowRun).getExecutor();
        unit.abortFlowRun(flowRun);
        verify(flowRun).getExecutor();
    }
}
