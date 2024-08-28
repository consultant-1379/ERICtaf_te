package com.ericsson.cifwk.taf.executor.healthcheck;

import java.util.List;

/**
*
*/
public interface HealthCheckContext {

    void ok(HealthParam param);

    void fail(HealthParam param, String description);

    void merge(HealthCheckContext anotherContext);

    List<HealthParam> health();

    boolean isHealthy();
}
