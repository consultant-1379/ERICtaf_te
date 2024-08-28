package com.ericsson.cifwk.taf.executor.mocks;

import com.ericsson.cifwk.taf.executor.TestExecution;
import com.ericsson.cifwk.taf.executor.TestExecutionResult;
import com.ericsson.cifwk.taf.executor.cluster.RemoteTafLauncher;
import com.ericsson.cifwk.taf.executor.schedule.TafTestExecutor;
import org.mockito.stubbing.OngoingStubbing;

import java.io.OutputStream;
import java.io.PrintStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 28/08/2017
 */
public class RemoteTafLauncherMocks {

    private RemoteTafLauncherMocks() {}

    public static RemoteTafLauncher withResult(TestExecution execution, OutputStream masterBuildOutputStream, TestExecutionResult testExecutionResult) {
        return new RemoteTafLauncherMock(execution, masterBuildOutputStream) {
            @Override
            public TafTestExecutor getTafTestExecutor() {
                return stubLauncherTests(mock(TafTestExecutor.class), testExecutionResult);
            }
        };
    }

    public static RemoteTafLauncher withException(TestExecution execution, OutputStream masterBuildOutputStream, Exception exception) {
        return new RemoteTafLauncherMock(execution, masterBuildOutputStream) {
            @Override
            public TafTestExecutor getTafTestExecutor() {
                return stubLauncherTests(mock(TafTestExecutor.class), exception);
            }
        };
    }

    public static RemoteTafLauncher withExceptionAndThenResult(TestExecution execution, OutputStream masterBuildOutputStream,
                                                               Exception exception,
                                                               TestExecutionResult testExecutionResult) {
        return new RemoteTafLauncherMock(execution, masterBuildOutputStream) {
            @Override
            public TafTestExecutor getTafTestExecutor() {
                TafTestExecutor executor = mock(TafTestExecutor.class);
                when(executor.runTests(any(TestExecution.class), any(PrintStream.class)))
                        .thenThrow(exception)
                        .thenReturn(testExecutionResult);
                return executor;
            }
        };
    }

    private static class RemoteTafLauncherMock extends RemoteTafLauncher {

        RemoteTafLauncherMock(TestExecution execution, OutputStream masterBuildOutputStream) {
            super(execution, masterBuildOutputStream);
        }

        TafTestExecutor stubLauncherTests(TafTestExecutor tafTestExecutor, TestExecutionResult testExecutionResult) {
            getStubBase(tafTestExecutor).thenReturn(testExecutionResult);
            return tafTestExecutor;
        }

        TafTestExecutor stubLauncherTests(TafTestExecutor tafTestExecutor, Exception exception) {
            getStubBase(tafTestExecutor).thenThrow(exception);
            return tafTestExecutor;
        }

        private OngoingStubbing<TestExecutionResult> getStubBase(TafTestExecutor executor) {
            return when(executor.runTests(any(TestExecution.class), any(PrintStream.class)));
        }
    }
}
