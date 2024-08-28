package com.ericsson.cifwk.taf.executor.eiffel;

import com.ericsson.duraci.datawrappers.EventId;
import com.ericsson.duraci.datawrappers.ExecutionId;

public final class EiffelParent {

    private final EventId eventId;
    private final ExecutionId executionId;

    public EiffelParent(EventId eventId, ExecutionId executionId) {
        this.eventId = eventId;
        this.executionId = executionId;
    }

    public EventId getEventId() {
        return eventId;
    }

    public ExecutionId getExecutionId() {
        return executionId;
    }

}
