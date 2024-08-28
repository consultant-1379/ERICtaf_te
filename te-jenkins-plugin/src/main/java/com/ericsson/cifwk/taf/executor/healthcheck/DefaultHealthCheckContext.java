package com.ericsson.cifwk.taf.executor.healthcheck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultHealthCheckContext implements HealthCheckContext {

    private final List<HealthParam> params = new ArrayList<>();

    @Override
    public void ok(HealthParam check) {
        params.add(check);
    }

    @Override
    public void fail(HealthParam check, String description) {
        params.add(check.setPassed(false).setDescription(description));
    }

    @Override
    public void merge(HealthCheckContext anotherContext) {
        params.addAll(anotherContext.health());
    }

    @Override
    public List<HealthParam> health() {
        return Collections.unmodifiableList(params);
    }

    @Override
    public boolean isHealthy() {
        boolean healthy = true;
        for (HealthParam param : params) {
            healthy &= param.isPassed();
        }
        return healthy;
    }

}
