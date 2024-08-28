package com.ericsson.cifwk.taf.analysis;

import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * Sample tests for giving some output for test analysis module in Trigger Plugin
 */
public class TestAnalysisTest {

    private static final TafConfiguration config = TafConfigurationProvider.provide();

    @Test
    @TestId(id = "TE_TP_ANALYSIS_BLOCKER_01", title = "Sample blocker test 1")
    public void blockerTest1() throws Exception {
        passOrFailDependingOnCurrentTestId();
    }

    @Test
    @TestId(id = "TE_TP_ANALYSIS_BLOCKER_02", title = "Sample blocker test 2")
    public void blockerTest2() throws Exception {
        passOrFailDependingOnCurrentTestId();
    }

    @Test
    @TestId(id = "TE_TP_ANALYSIS_NORMAL_01", title = "Sample normal test 1")
    public void normalTest1() throws Exception {
        passOrFailDependingOnCurrentTestId();
    }

    @Test
    @TestId(id = "TE_TP_ANALYSIS_NORMAL_02", title = "Sample normal test 2")
    public void normalTest2() throws Exception {
        passOrFailDependingOnCurrentTestId();
    }

    @Test
    @TestId(id = "TE_TP_ANALYSIS_MINOR_01", title = "Sample minor test 1")
    public void minorTest1() throws Exception {
        passOrFailDependingOnCurrentTestId();
    }

    @Test
    @TestId(id = "TE_TP_ANALYSIS_MINOR_02", title = "Sample minor test 2")
    public void minorTest2() throws Exception {
        passOrFailDependingOnCurrentTestId();
    }

    private void passOrFailDependingOnCurrentTestId() throws Exception {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        StackTraceElement testMethodInStack = stack[2];
        Class testClass = Class.forName(testMethodInStack.getClassName());
        Method testMethod = testClass.getMethod(testMethodInStack.getMethodName());
        TestId testIdAnnotation = testMethod.getAnnotation(TestId.class);
        if (testIdAnnotation != null) {
            String testId = testIdAnnotation.id();
            if (!StringUtils.isBlank(testId)) {
                // Find out should we fail it or not - up to external setting (set in TP)
                String testIdFailOrOK = config.getString(testId);
                if (StringUtils.equalsIgnoreCase(testIdFailOrOK, "fail")) {
                    Assert.fail("Failing assertion because " + testId + " test was configured to do so by setting in Trigger job");
                } else if (StringUtils.equalsIgnoreCase(testIdFailOrOK, "exception")) {
                    throw new RuntimeException("Fails with exception because " + testId + " test was configured to do so by setting in Trigger job");
                }
            }
        }
    }
}
