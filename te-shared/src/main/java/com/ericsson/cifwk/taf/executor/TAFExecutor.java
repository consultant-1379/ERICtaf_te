package com.ericsson.cifwk.taf.executor;

import java.io.PrintStream;

/**
 * Entry point to remote TAF test execution.
 */
public interface TAFExecutor {
    /**
     * Label for TE slave hosts
     */
    String TAF_NODE_LABEL = "taf";

    /**
     * Label for TE slave hosts with full Python/NaviCli/etc. installations required in testware
     */
    String FULL_ENV_TAF_NODE_LABEL = "uber";

    /**
     * Label for TE Jenkins master host
     */
    String TAF_MASTER_LABEL = "master";

    /**
     * Workspace subdirectory for TE Maven test runs
     */
    String TEST_RUN_WORKSPACE_SUBDIR = "te_maven_test_runs";

    TestExecutionResult execute(TestExecution execution, PrintStream buildLog);

}
