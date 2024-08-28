package com.ericsson.cifwk.taf.executor.utils;

public interface EnhancedTimeLimitedWorker<T> extends TimeLimitedWorker<T> {

    String getFailedAttemptMessage();

}
