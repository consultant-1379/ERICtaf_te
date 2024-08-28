package com.ericsson.cifwk.taf.executor.mbean;

import com.ericsson.cifwk.taf.executor.TAFExecutor;
import com.ericsson.cifwk.taf.executor.TestExecution;
import com.ericsson.cifwk.taf.executor.TestExecutionResult;
import com.ericsson.cifwk.taf.executor.TestResult;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.ServiceLoader;

public class TafAgent extends StandardMBean implements TafAgentMBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(TafAgent.class);

    public TafAgent() throws NotCompliantMBeanException {
        super(TafAgentMBean.class);
    }

    @Override
    public void postRegister(Boolean registrationDone) {
        super.postRegister(registrationDone);
        LOGGER.info("TAF Executor MBean registered");
    }

    @Override
    public void postDeregister() {
        super.postDeregister();
        LOGGER.info("TAF Executor MBean deregistered");
    }

    @Override
    public String exec(String[] testware, String[] suites) {
/*
        TestExecution execution = TestExecution.Builder.create()
                .withTestware(testware)
                .withSuites(suites)
                .build();
        return execute(execution);
*/
        return    TestResult.Status.SUCCESS.toString();
    }

    @Override
    public TestExecutionResult execute(TestExecution execution, PrintStream buildLog) {
        ServiceLoader<TAFExecutor> load = ServiceLoader.load(TAFExecutor.class);
        Iterator<TAFExecutor> iterator = load.iterator();
        Preconditions.checkState(iterator.hasNext(), "TAF Executor is not provided.");
        TAFExecutor executor = load.iterator().next();
        return executor.execute(execution, buildLog);
    }

}
