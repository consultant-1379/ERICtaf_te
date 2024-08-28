package com.ericsson.cifwk.taf.executor.schedule;

import java.util.NoSuchElementException;

public class ScheduleException extends RuntimeException {

    public ScheduleException(String message) {
        super(message);
    }

    public ScheduleException(String message, NoSuchElementException e) {
        super(message, e);
    }

    public ScheduleException(String message, Exception cause) {
        super(message, cause);
    }

}
