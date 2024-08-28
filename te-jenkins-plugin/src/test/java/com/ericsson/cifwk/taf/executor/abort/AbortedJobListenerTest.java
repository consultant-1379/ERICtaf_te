package com.ericsson.cifwk.taf.executor.abort;

import com.ericsson.duraci.datawrappers.ExecutionId;
import com.google.common.base.Optional;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.stapler.HttpResponse;

import static org.mockito.Mockito.*;

public class AbortedJobListenerTest {

    private AbortedJobListener abortedJobListener;
    private AbortedJobListener unit;
    private AbstractBuild abstractBuild;
    private TaskListener listener;
    private ExecutionId executionId;

    @Before
    public void setup() throws Exception {
        abortedJobListener = new AbortedJobListener();
        unit = spy(abortedJobListener);
        abstractBuild = mock(AbstractBuild.class);
        listener = mock(TaskListener.class);
        executionId = new ExecutionId("1234");
        doReturn(Optional.of(executionId)).when(unit).getExecutionIdOptional(abstractBuild);
        HttpResponse httpResponse = mock(HttpResponse.class);
        doReturn(httpResponse).when(abstractBuild).doStop();
    }

    @Test
    public void stopStartOfAbortedJobTest() throws Exception{
        unit.setExecutionAsAborted(executionId);
        unit.onStarted(abstractBuild, listener);
        verify(abstractBuild, times(1)).doStop();
    }
    @Test
    public void notAbortedTest() throws Exception {
        unit.onStarted(abstractBuild, listener);
        verify(abstractBuild, times(0)).doStop();
    }
}
