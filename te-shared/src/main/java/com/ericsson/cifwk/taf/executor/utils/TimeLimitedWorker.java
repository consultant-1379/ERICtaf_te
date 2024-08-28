package com.ericsson.cifwk.taf.executor.utils;

import com.google.common.base.Optional;

public interface TimeLimitedWorker<T> {

    Optional<T> doWork();

}
