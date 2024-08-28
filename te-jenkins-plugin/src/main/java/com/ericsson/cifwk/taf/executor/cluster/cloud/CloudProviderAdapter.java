package com.ericsson.cifwk.taf.executor.cluster.cloud;

import com.ericsson.cifwk.taf.executor.healthcheck.HealthCheckContext;
import hudson.slaves.Cloud;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 21/06/2017
 */
public interface CloudProviderAdapter {

    Class<? extends Cloud> getCloudClass();

    Cloud getCloud();

    String getCloudName();

    void checkCloudAvailability(HealthCheckContext context);
}
