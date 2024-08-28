package com.ericsson.cifwk.taf.executor.api.healthcheck;


import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.List;

public class HealthCheckState {

    private final List<HealthCheck> healthChecks;

    public HealthCheckState(List<HealthCheck> healthChecks) {
        Preconditions.checkArgument(healthChecks != null);
        this.healthChecks = healthChecks;
    }

    public boolean isHealthy() {
        if (healthChecks.isEmpty()) {
            return false;
        }

        for (HealthCheck healthCheck : healthChecks) {
            if (!healthCheck.getPassed()) {
                return false;
            }
        }

        return true;
    }

    public List<HealthCheck> getHealthCheckResults() {
        return Collections.unmodifiableList(healthChecks);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("##### Health checks #####");
        if (!healthChecks.isEmpty()) {
            for (HealthCheck healthCheck : healthChecks) {
                sb.append("\r\n");
                sb.append(String.format("%s (%s) - %s",
                        healthCheck.getName(),
                        healthCheck.getScope(),
                        healthCheck.getPassed() ? "OK" : "FAIL"));
            }
        } else {
            sb.append("\r\n");
            sb.append("Health check is not received.");
        }
        return sb.toString();
    }
}
