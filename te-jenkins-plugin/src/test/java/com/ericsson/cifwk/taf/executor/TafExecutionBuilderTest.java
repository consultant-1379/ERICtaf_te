package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.eiffel.EiffelMessageBus;
import com.ericsson.cifwk.taf.executor.model.ExecutorBuildParameters;
import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;
import com.ericsson.cifwk.taf.executor.model.ManualTestsBuildParameters;
import com.ericsson.cifwk.taf.executor.model.ScheduleBuildParameters;
import com.ericsson.cifwk.taf.executor.model.TeBuildMainParameters;
import com.ericsson.duraci.datawrappers.EventId;
import com.ericsson.duraci.datawrappers.ExecutionId;
import com.ericsson.duraci.datawrappers.ResultCode;
import com.ericsson.duraci.eiffelmessage.messages.EiffelEvent;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import jenkins.model.Jenkins;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.PrintStream;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyMapOf;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TafExecutionBuilderTest {

    @Mock(answer = Answers.RETURNS_MOCKS)
    private AbstractBuild build;

    @Mock
    private Jenkins jenkins;

    @Mock
    private EiffelMessageBus messageBus;

    private TafExecutionBuilder unit = new TafExecutionBuilder();
    private ScheduleBuildParameters mainBuildParameters = new ScheduleBuildParameters();
    private ExecutorBuildParameters currentBuildParameters = new ExecutorBuildParameters();
    private ManualTestsBuildParameters manualTestsBuildParameters = new ManualTestsBuildParameters();
    private GlobalTeSettings globalTeSettings = new GlobalTeSettings();
    private final String repoUrl = "http://myNexus";


    @Before
    public void setUp() {
        unit = spy(unit);
        doReturn(new TeBuildMainParameters(globalTeSettings, mainBuildParameters)).when(unit).getMainParameters(anyString());
        doReturn(currentBuildParameters).when(unit).getCurrentBuildParameters(any(AbstractBuild.class), eq(ExecutorBuildParameters.class));
        doReturn(manualTestsBuildParameters).when(unit).getCurrentBuildParameters(any(AbstractBuild.class), eq(ManualTestsBuildParameters.class));
        mainBuildParameters.setRepositoryUrl(repoUrl);
        mainBuildParameters.setCommonTestProperties("");
    }

    @Test
    public void perform_shouldSetMavenRepoUrl() throws Exception {
        stubUnitForTestExecutionCheck(unit);

        BuildListener listener = mock(BuildListener.class);
        when(listener.getLogger()).thenReturn(mock(PrintStream.class));
        unit.perform(build, mock(Launcher.class), listener);

        ArgumentCaptor<TestExecution> argument = ArgumentCaptor.forClass(TestExecution.class);
        verify(unit).executeOnRemoteSlave(any(Launcher.class), any(PrintStream.class), argument.capture());
        TestExecution testExecution = argument.getValue();

        Assert.assertEquals(repoUrl, testExecution.getRepositoryUrl());
    }

    @Test
    public void shouldSkipTests(){
        String lineSeparator = System.getProperty("line.separator");
        Assert.assertEquals("true", getSkipTestValue("blahblah"+lineSeparator+"skipTests=true"+lineSeparator+"blahblah"));
        Assert.assertEquals("false", getSkipTestValue("blahblah"+lineSeparator+"skipTests=true=true"+lineSeparator+"blahblah"));
        Assert.assertEquals("false", getSkipTestValue("blahblah"));
        Assert.assertEquals("false", getSkipTestValue(""));
        Assert.assertEquals("false", getSkipTestValue("blahblah"+lineSeparator+"skipTests"+lineSeparator+"blahblah"));
        Assert.assertEquals("false", getSkipTestValue("blahblah"+lineSeparator+"skipTests="+lineSeparator+"blahblah"));
    }

    private String getSkipTestValue(final String skipTestValue) {
        return unit.getSkipTestValue(skipTestValue);
    }

    private void stubUnitForTestExecutionCheck(TafExecutionBuilder unit) throws Exception {
        doReturn(jenkins).when(unit).getJenkinsInstance();

        initMessageBus();

        doReturn(messageBus).when(unit).getEiffelMessageBus(eq(globalTeSettings));

        doReturn(new String[] {"logs", "scripts"}).when(unit).getLogAndScriptUrls(eq(build), any(PrintStream.class));
        doReturn("workspaceUri").when(unit).getWorkspaceUri(eq(build));
        doNothing().when(unit).logBuildParameters(anyMapOf(String.class, String.class), any(PrintStream.class));
        doReturn(new TestExecutionResult(TestResult.Status.SUCCESS, null))
                .when(unit).executeOnRemoteSlave(any(Launcher.class), any(PrintStream.class), any(TestExecution.class));
        doNothing().when(unit).sendFinish(eq(messageBus), any(ResultCode.class), anyMap(), any(ExecutionId.class));

        when(build.getBuildVariables()).thenReturn(mainBuildParameters.getAllParameters());
    }

    private void initMessageBus() {
        messageBus = mock(EiffelMessageBus.class);
        when(messageBus.sendStart(any(EiffelEvent.class), any(ExecutionId.class)))
                .thenReturn(new EventId());
        when(messageBus.sendStart(any(EiffelEvent.class), any(ExecutionId.class), any(EventId.class)))
                .thenReturn(new EventId());
    }

}