package com.ericsson.cifwk.taf.executor.listeners;

import com.ericsson.cifwk.taf.executor.security.TestRuntimeSecurityManager;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IExecutionListener;

import java.io.File;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 04/01/2016
 */
public class TeExecutionListener extends AbstractTeListener implements IExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeExecutionListener.class);

    @Override
    public void onExecutionStart() {
        createSuiteRunsDirectory();
        applyRuntimeRestrictions();
    }

    private void applyRuntimeRestrictions() {
        Integer maxThreadsLimit = getMaxThreadsLimit();
        if (maxThreadsLimit != null) {
            LOGGER.info("Runtime restriction is set by TE: maximal thread count allowed to create in this test run is {}",
                    maxThreadsLimit);
            System.setSecurityManager(new TestRuntimeSecurityManager());
        }
    }

    @VisibleForTesting
    Integer getMaxThreadsLimit() {
        String maxThreadLimitStr = getThreadLimitPropertyValue();
        if (maxThreadLimitStr == null || maxThreadLimitStr.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(maxThreadLimitStr);
        } catch (NumberFormatException e) {
            LOGGER.warn("Erroneous thread limitation property ({}) value defined: {}, no limit will be set",
                    TestRuntimeSecurityManager.THREADS_LIMIT_PROPERTY,
                    maxThreadLimitStr);
            return null;
        }
    }

    @VisibleForTesting
    String getThreadLimitPropertyValue() {
        return System.getProperty(TestRuntimeSecurityManager.THREADS_LIMIT_PROPERTY);
    }

    private void createSuiteRunsDirectory() {
        File outputDir = getWorkingDir();
        LOGGER.info("Creating suite runs directory - " + outputDir.getAbsolutePath());
        outputDir.mkdirs();
    }

    @Override
    public void onExecutionFinish() {

    }

}
