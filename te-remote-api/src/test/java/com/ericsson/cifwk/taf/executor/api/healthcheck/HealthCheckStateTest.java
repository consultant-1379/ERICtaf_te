package com.ericsson.cifwk.taf.executor.api.healthcheck;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class HealthCheckStateTest {

    @Test
    public void testIsHealthy() throws Exception {
        List<HealthCheck> healthChecks = new ArrayList<>();

        Assert.assertFalse(new HealthCheckState(healthChecks).isHealthy());

        healthChecks.add(new HealthCheck("check1Desc", "check1", true, "scope"));
        Assert.assertTrue(new HealthCheckState(healthChecks).isHealthy());

        healthChecks.add(new HealthCheck("check2Desc", "check2", false, "scope"));
        Assert.assertFalse(new HealthCheckState(healthChecks).isHealthy());
    }

    @Test
    public void testToString() throws Exception {
        List<HealthCheck> healthChecks = new ArrayList<>();
        Assert.assertNotNull(new HealthCheckState(healthChecks).toString());

        healthChecks.add(new HealthCheck("check1Desc", "check1", true, "scope"));
        Assert.assertNotNull(new HealthCheckState(healthChecks).toString());
    }
}