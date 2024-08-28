package com.ericsson.cifwk.taf.executor.healthcheck;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 05/06/2017
 */
public class HealthCheckBadgeTest {

    @Test
    public void getUrlName() {
        HealthCheckBadge unit = new HealthCheckBadge();
        assertEquals("descriptorByName/com.ericsson.cifwk.taf.executor.healthcheck.HealthCheck/healthCheck", unit.getUrlName());
    }

}