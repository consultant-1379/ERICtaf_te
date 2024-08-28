package com.ericsson.cifwk.taf.execution;

import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.execution.operator.JenkinsOperator;
import com.ericsson.cifwk.taf.execution.operator.impl.JenkinsOperatorImpl;
import com.ericsson.cifwk.taf.execution.operator.model.TestDataContext;

import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;

/**
 * Creates Jenkins jobs needed for TE acceptance tests, to avoid recreating them in every test
 * This is only required when running on a local TE that does not have the TEST_EXECUTOR & TEST_SCHEDULER jobs present
 */
public class TeJenkinsJobCreatingTest extends JobFactoryBase {

    @Inject
    Provider<JenkinsOperatorImpl> jenkinsOperatorProvider;

    @Test
    @TestId(id = "TAF_TE_02", title = "Create mandatory TE jobs required for test running")
    public void createJobs() throws IOException {
        JenkinsOperator operator = jenkinsOperatorProvider.get();
        recreateExecutorJob(operator, TestConstants.EXECUTOR_JOB_NAME);
        recreateFullEnvExecutorJob(operator, TestConstants.FULL_ENV_EXECUTOR_JOB_NAME);
        recreateSchedulerJob(operator, TestConstants.SCHEDULER_JOB_NAME, TestDataContext.defaultSchedulerJobConfigFilePath());
    }
}
