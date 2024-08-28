package com.ericsson.cifwk.taf.execution.operator;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.executor.api.healthcheck.HealthCheck;

import java.util.List;

/**
 *
 */
public interface TafExecutorOperator {
    List<HealthCheck> healthCheck(Host teMasterHost);
}
