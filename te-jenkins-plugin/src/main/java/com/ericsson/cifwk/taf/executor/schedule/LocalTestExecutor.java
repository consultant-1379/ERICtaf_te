package com.ericsson.cifwk.taf.executor.schedule;

import com.ericsson.cifwk.taf.executor.TAFExecutor;
import com.ericsson.cifwk.taf.executor.TestExecution;
import com.ericsson.cifwk.taf.executor.TestExecutionResult;
import com.google.common.base.Preconditions;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.ServiceLoader;

public class LocalTestExecutor implements TafTestExecutor {

    @Override
    public TestExecutionResult runTests(TestExecution execution, PrintStream buildLog) {
        ServiceLoader<TAFExecutor> load = ServiceLoader.load(TAFExecutor.class);
        Iterator<TAFExecutor> iterator = load.iterator();
        Preconditions.checkState(iterator.hasNext(), "TAF Executor is not provided.");
        TAFExecutor executor = load.iterator().next();
        buildLog.println("Using target executor " + executor);
        return executor.execute(execution, buildLog);
    }

}
