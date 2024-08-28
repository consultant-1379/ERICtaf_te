package com.ericsson.cifwk.taf.executor;

import com.google.common.base.Preconditions;

import java.io.Serializable;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 03/01/2017
 */
public class TestwareRuntimeLimitations implements Serializable {

    static final String REQUIRED_MAX_THREAD_COUNT_MSG = "Max thread count should be a positive number";

    private Integer maxThreadCount;

    public Integer getMaxThreadCount() {
        return maxThreadCount;
    }

    public void setMaxThreadCount(Integer maxThreadCount) {
        Preconditions.checkArgument(maxThreadCount != null && maxThreadCount > 0, REQUIRED_MAX_THREAD_COUNT_MSG);
        this.maxThreadCount = maxThreadCount;
    }

    @Override
    public String toString() {
        return "TestwareRuntimeLimitations{" +
                "maxThreadCount=" + maxThreadCount +
                '}';
    }
}
