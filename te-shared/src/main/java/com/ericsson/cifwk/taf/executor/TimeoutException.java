package com.ericsson.cifwk.taf.executor;

public class TimeoutException extends RuntimeException {
    private static final long serialVersionUID = -724374976561341335L;

    public TimeoutException(String message) {
        super(message);
    }
}
