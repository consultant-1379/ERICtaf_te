package com.ericsson.cifwk.taf.execution.operator;

import com.ericsson.cifwk.taf.executor.api.ScheduleRequest;
import com.ericsson.duraci.datawrappers.EventId;
import com.ericsson.duraci.datawrappers.ExecutionId;
import com.ericsson.duraci.eiffelmessage.messages.EiffelEvent;
import com.ericsson.duraci.eiffelmessage.messages.EiffelMessage;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelBaselineDefinedEvent;

import java.util.List;

/**
 *
 */
public interface EventRepositoryOperator {

    EiffelMessage findJobStartedEventMessage(ExecutionId executionId);

    EiffelMessage findJobFinishedEventMessage(ExecutionId executionId);

    List<EiffelMessage> findEventMessages(ExecutionId executionId, Class<? extends EiffelEvent> eventClass);

    EiffelMessage findEventMessage(ExecutionId executionId, Class<? extends EiffelEvent> eventClass);

    EiffelMessage findEventMessage(EventId eventId, Class<? extends EiffelEvent> eventClass);

    List<EiffelMessage> findTestSuiteStartedEventMessages(ExecutionId parentExecutionId);

    EiffelMessage findTestSuiteFinishedEventMessage(ExecutionId jobStepExecutionId);

    List<EiffelMessage> findJobStepStartedEventMessages(ExecutionId jobExecutionId);

    EiffelMessage findJobStepFinishedEventMessage(ExecutionId jobStepExecutionId);

    List<EiffelMessage> getEventDownstream(EventId eventId);

    ScheduleRequest getSchedule(EiffelBaselineDefinedEvent triggerEvent);

}
