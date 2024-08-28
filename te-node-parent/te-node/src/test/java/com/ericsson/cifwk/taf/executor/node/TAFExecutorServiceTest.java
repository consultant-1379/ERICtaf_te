package com.ericsson.cifwk.taf.executor.node;

import com.ericsson.cifwk.taf.executor.TestExecution;
import com.ericsson.cifwk.taf.executor.TestExecutionResult;
import com.ericsson.cifwk.taf.executor.TestResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.PrintStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TAFExecutorServiceTest {

    @Spy
    private TAFExecutorService unit;

    @Mock
    private TafTestNode tafTestNode;

    @Mock
    private PrintStream buildLog;

    @Before
    public void setUp() {
        buildLog = mock(PrintStream.class);
        doReturn(tafTestNode).when(unit).createTafTestNode(buildLog, true);
    }

    @Test
    public void shouldExecute_ok() throws Exception {
        TestExecution goodExecution = mock(TestExecution.class);
        when(tafTestNode.execute(eq(goodExecution))).thenReturn(new TestResult(TestResult.Status.SUCCESS));

        TestExecutionResult result = unit.execute(goodExecution, buildLog);

        assertThat(result.getTestResultStatus(), equalTo(TestResult.Status.SUCCESS));
    }

    @Test
    public void shouldFailOnExceptionDuringExecution() throws Exception {
        TestExecution badExecution = mock(TestExecution.class);
        when(tafTestNode.execute(eq(badExecution))).thenThrow(new RuntimeException());

        TestExecutionResult result = unit.execute(badExecution, buildLog);

        assertThat(result.getTestResultStatus(), equalTo(TestResult.Status.ERROR));
    }

    @Test
    public void shouldGetTestRunner() {
        assertEquals(TafTestRunner.class, unit.getTestRunner(true, null).getClass());
        assertEquals(ManualTestRunner.class, unit.getTestRunner(false, null).getClass());
    }

}