package com.ericsson.cifwk.taf.executor.emulator;

import com.ericsson.cifwk.taf.executor.CommonTestConstants;
import com.ericsson.cifwk.taf.executor.eiffel.EiffelMessageBus;
import com.ericsson.duraci.datawrappers.Environment;
import com.ericsson.duraci.datawrappers.ExecutionId;
import com.ericsson.duraci.datawrappers.ResultCode;
import com.ericsson.duraci.eiffelmessage.messages.EiffelEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

final class EiffelMessageGenerator {

    private static final String JOB_INSTANCE = "TEST_SCHEDULER";
    private static final int JOB_EXECUTION_NUMBER = 1;

    private final EiffelMessageBus messageBus;
    private AtomicLong testId = new AtomicLong();

    private EiffelMessageGenerator(EiffelMessageBus messageBus) {
        this.messageBus = messageBus;
    }

    public static EiffelMessageGenerator connect(String hostName, String exchange) {
        EiffelMessageBus messageBus = new EiffelMessageBus(CommonTestConstants.MB_DOMAIN);
        messageBus.connect(hostName, exchange);
        return new EiffelMessageGenerator(messageBus);
    }

    public EiffelEvent jobStarted(String executionId) {
        EiffelJobStartedEvent event = EiffelJobStartedEvent.Factory
                .create(JOB_INSTANCE, executionId, JOB_EXECUTION_NUMBER);
        messageBus.sendStart(event, event.getJobExecutionId());
        return event;
    }

    public EiffelEvent jobFinished(ResultCode resultCode) {
        ExecutionId parentExecutionId = messageBus.getSentParent().getExecutionId();
        EiffelJobFinishedEvent event = EiffelJobFinishedEvent.Factory
                .create(JOB_INSTANCE, parentExecutionId, JOB_EXECUTION_NUMBER, resultCode, null);
        messageBus.sendFinish(event);
        return event;
    }

    public EiffelEvent jobStepStarted() {
        ExecutionId parentExecutionId = messageBus.getSentParent().getExecutionId();
        EiffelJobStepStartedEvent event = EiffelJobStepStartedEvent.Factory
                .create(parentExecutionId, 0, 0);
        messageBus.sendStart(event, event.getJobStepExecutionId());
        return event;
    }

    public EiffelEvent jobStepFinished(ResultCode resultCode) {
        ExecutionId parentExecutionId = messageBus.getSentParent().getExecutionId();
        EiffelJobStepFinishedEvent event = EiffelJobStepFinishedEvent.Factory
                .create(resultCode, null, parentExecutionId);
        messageBus.sendFinish(event);
        return event;
    }

    public EiffelEvent testSuiteStarted(String name) {
        ExecutionId parentExecutionId = messageBus.getSentParent().getExecutionId();
        EiffelTestSuiteStartedEvent event = EiffelTestSuiteStartedEvent.Factory
                .create(parentExecutionId, "Debug", name);
        messageBus.sendStart(event, event.getTestSuiteExecutionId());
        return event;
    }

    public EiffelEvent testSuiteFinished(ResultCode resultCode) {
        ExecutionId parentExecutionId = messageBus.getSentParent().getExecutionId();
        EiffelTestSuiteFinishedEvent event = EiffelTestSuiteFinishedEvent.Factory
                .create(resultCode, null, parentExecutionId);
        messageBus.sendFinish(event);
        return event;
    }

    public EiffelEvent testCaseStarted(String name) {
        ExecutionId parentExecutionId = messageBus.getSentParent().getExecutionId();
        List<Environment> parameters = Collections.<Environment>emptyList();
        EiffelTestCaseStartedEvent event = EiffelTestCaseStartedEvent.Factory
                .create(parentExecutionId, "debug@" + testId.incrementAndGet(), name, parameters);
        messageBus.sendStart(event, event.getTestCaseExecutionId());
        return event;
    }

    public EiffelEvent testCaseFinished(ResultCode resultCode) {
        ExecutionId parentExecutionId = messageBus.getSentParent().getExecutionId();
        EiffelTestCaseFinishedEvent event = EiffelTestCaseFinishedEvent.Factory
                .create(resultCode, null, parentExecutionId);
        messageBus.sendFinish(event);
        return event;
    }

    public void shutdown() {
        messageBus.disconnect();
    }
}
