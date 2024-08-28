package com.ericsson.cifwk.taf.executor.utils;

import com.ericsson.cifwk.taf.executor.TimeoutException;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeLimitedTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeLimitedTask.class);

    public static <T> T performUntilTimeout(TimeLimitedWorker<T> callback, int timeoutInSeconds) {
        return performUntilTimeout(callback, timeoutInSeconds, 1000);
    }

    public static <T> T performUntilTimeout(TimeLimitedWorker<T> callback, int timeoutInSeconds, int sleepAfterFailureInMillis) {
        int millis = timeoutInSeconds * 1000;
        long start = System.currentTimeMillis();
        while (true) {
            Optional<T> result = callback.doWork();
            if (result.isPresent()) {
                return result.get();
            }
            if (System.currentTimeMillis() - start >= millis) {
                throw new TimeoutException("Waiting has timed out");
            }
            if (callback instanceof EnhancedTimeLimitedWorker) {
                String failedAttemptMessage = ((EnhancedTimeLimitedWorker) callback).getFailedAttemptMessage();
                LOGGER.error(String.format("%s. Making next attempt in %d millis", failedAttemptMessage, sleepAfterFailureInMillis));
            }
            sleep(sleepAfterFailureInMillis);
        }
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw Throwables.propagate(e);
        }
    }

}
