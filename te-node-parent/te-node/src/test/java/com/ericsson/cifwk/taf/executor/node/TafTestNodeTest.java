package com.ericsson.cifwk.taf.executor.node;

import com.ericsson.cifwk.taf.executor.TestExecution;
import com.ericsson.cifwk.taf.executor.TestResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TafTestNodeTest {

    TafTestRunner runner;
    TafTestNode node;

    @Before
    public void setUp() throws Exception {
        runner = mock(TafTestRunner.class);
        node = new TafTestNode(runner);
    }

    @Test
    public void shouldExecute_ok() throws Exception {
        TestExecution execution = mock(TestExecution.class);
        when(runner.runTest()).thenReturn(new TestResult(TestResult.Status.SUCCESS));

        TestResult result = node.execute(execution);

        assertThat(result.getStatus(), equalTo(TestResult.Status.SUCCESS));
        Mockito.verify(runner).setUp(eq(execution));
        Mockito.verify(runner).tearDown();
    }

    @Test(expected = RuntimeException.class)
    public void shouldExecute_error() throws Exception {
        TestExecution execution = mock(TestExecution.class);
        when(runner.runTest()).thenThrow(new RuntimeException());

        try {
            node.execute(execution);
        } finally {
            Mockito.verify(runner).setUp(eq(execution));
            Mockito.verify(runner).tearDown();
        }
    }

}
