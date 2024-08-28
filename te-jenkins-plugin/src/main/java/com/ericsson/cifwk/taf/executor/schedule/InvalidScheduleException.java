package com.ericsson.cifwk.taf.executor.schedule;

import java.util.NoSuchElementException;

public class InvalidScheduleException extends ScheduleException {

    public InvalidScheduleException(String message) {
        super(message);
    }

    public InvalidScheduleException(String message, NoSuchElementException e) {
        super(message, e);
    }

    public InvalidScheduleException(String message, Exception cause) {
        super(message, cause);
    }

}
