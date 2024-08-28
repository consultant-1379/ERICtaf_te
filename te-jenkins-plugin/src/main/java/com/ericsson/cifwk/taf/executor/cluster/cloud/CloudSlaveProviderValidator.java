package com.ericsson.cifwk.taf.executor.cluster.cloud;

import com.ericsson.cifwk.taf.executor.healthcheck.HealthCheckContext;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 15/06/2017
 *
 * Interface for the TE adapters for the cloud slave providers
 */
public interface CloudSlaveProviderValidator {

    String DEFAULT_HEALTH_CHECKS_SCOPE = "Jenkins cloud plugins for TE grid slave provisioning";

    /**
     * @return validator's name
     */
    String getProviderName();

    /**
     * @return <code>true</code> if appropriate cloud is set up in Jenkins to provide TE slaves - regardless of whether it's set up
     * correctly or not. Use {@link #healthCheck(HealthCheckContext)} to check if provider is working well.
     */
    boolean isProviderSetUp();

    /**
     * Performs cloud health check and updates <code>context</code> accordingly.
     * @param context current context that is updated by health check.
     */
    void healthCheck(HealthCheckContext context);

    CloudProviderAdapter getAdapter() throws ClassNotFoundException;

}
