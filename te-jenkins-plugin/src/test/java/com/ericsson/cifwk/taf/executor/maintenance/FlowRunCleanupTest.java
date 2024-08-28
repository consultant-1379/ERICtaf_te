package com.ericsson.cifwk.taf.executor.maintenance;

import com.cloudbees.plugins.flow.BuildFlow;
import com.cloudbees.plugins.flow.FlowRun;
import jenkins.model.Jenkins;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;

public class FlowRunCleanupTest {

    @Test
    public void isEligibleForDeletion() throws Exception {
        FlowRun flowRun = mock(FlowRun.class);
        BuildFlow buildFlow = mock(BuildFlow.class);
        when(buildFlow.getFirstBuild())
                .thenReturn(flowRun);

        FlowRunCleanup unit = new FlowRunCleanup();
        unit = spy(unit);
        Date oldDate = getDateTime("31/10/2010 12:21:56");
        doReturn(oldDate.getTime() + 1000).when(unit).getFinishTimeInMillis(eq(flowRun));

        doReturn(getDateTime("5/11/2010 11:21:44"))
                .doReturn(getDateTime("5/11/2010 11:21:44"))
                .doReturn(getDateTime("5/11/2010 15:21:44"))
                .when(unit).getCurrentTime();
        int deletableFlowsAgeInSeconds = 5 * 24 * 3600;
        Assert.assertTrue(unit.isEligibleForDeletion(buildFlow, 0));
        Assert.assertFalse(unit.isEligibleForDeletion(buildFlow, deletableFlowsAgeInSeconds));
        Assert.assertTrue(unit.isEligibleForDeletion(buildFlow, deletableFlowsAgeInSeconds));
    }

    @Test
    public void isEligibleForDeletion_noBuilds() throws Exception {
        BuildFlow buildFlow = mock(BuildFlow.class);
        when(buildFlow.getFirstBuild()).thenReturn(null);

        FlowRunCleanup unit = new FlowRunCleanup();
        Assert.assertTrue(unit.isEligibleForDeletion(buildFlow, 1));
    }

    @Test
    public void doRun_happyPath() throws Exception {
        FlowRunCleanup unit = new FlowRunCleanup();
        unit = spy(unit);

        Jenkins jenkins = mock(Jenkins.class);
        final int daysToLive = 5;
        final int secondsToLive = daysToLive * 24 * 3600;
        doReturn(jenkins).when(unit).getJenkinsInstance();
        doReturn(secondsToLive).when(unit).getDeletableFlowsAgeInSeconds(eq(jenkins));

        BuildFlow flow1 = mockBuildFlow("Flow1", true);
        BuildFlow flow2 = mockBuildFlow("Flow2", false);
        BuildFlow flow3 = mockBuildFlow("Flow3", false);
        List<BuildFlow> buildFlows = Arrays.asList(flow1, flow2, flow3);

        doNothing().when(unit).deleteProject(any(BuildFlow.class));
        doReturn(buildFlows).when(unit).getBuildFlows(eq(jenkins));
        doReturn(true).when(unit).isEligibleForDeletion(eq(flow1), eq(secondsToLive));
        doReturn(true).when(unit).isEligibleForDeletion(eq(flow2), eq(secondsToLive));
        doReturn(false).when(unit).isEligibleForDeletion(eq(flow3), eq(secondsToLive));

        unit.doRun();

        verify(unit, never()).deleteProject(flow1);
        verify(unit).deleteProject(flow2);
        verify(unit, never()).deleteProject(flow3);
    }

    @Test
    public void doRun_undefinedFlowRunLifeTime() throws Exception {
        FlowRunCleanup unit = new FlowRunCleanup();
        unit = spy(unit);

        Jenkins jenkins = mock(Jenkins.class);
        doReturn(jenkins).when(unit).getJenkinsInstance();
        doReturn(null).when(unit).getDeletableFlowsAgeInSeconds(eq(jenkins));

        BuildFlow flow1 = mockBuildFlow("Flow1", true);
        BuildFlow flow2 = mockBuildFlow("Flow2", false);
        BuildFlow flow3 = mockBuildFlow("Flow3", false);
        List<BuildFlow> buildFlows = Arrays.asList(flow1, flow2, flow3);

        doNothing().when(unit).deleteProject(any(BuildFlow.class));
        doReturn(buildFlows).when(unit).getBuildFlows(eq(jenkins));

        unit.doRun();

        verify(unit, never()).deleteProject(flow1);
        verify(unit, never()).deleteProject(flow2);
        verify(unit, never()).deleteProject(flow3);
    }

    private Date getDateTime(String dateTime) throws ParseException {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(dateTime);
    }

    private BuildFlow mockBuildFlow(String name, boolean inProgress) {
        BuildFlow result = mock(BuildFlow.class);
        when(result.getName()).thenReturn(name);
        when(result.isBuilding()).thenReturn(inProgress);
        return result;
    }
}