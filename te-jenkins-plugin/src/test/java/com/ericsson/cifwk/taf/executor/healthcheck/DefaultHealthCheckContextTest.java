package com.ericsson.cifwk.taf.executor.healthcheck;

import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 01/06/2017
 */
public class DefaultHealthCheckContextTest {

    private DefaultHealthCheckContext unit = new DefaultHealthCheckContext();

    @Test
    public void shouldNotBeHealthyIfAllFailed() throws Exception {
        unit.ok(new HealthParam(false));
        unit.ok(new HealthParam(false));
        assertThat(unit.isHealthy()).isFalse();
    }

    @Test
    public void shouldNotBeHealthyIfSomeFailed() throws Exception {
        unit.ok(new HealthParam(true));
        unit.ok(new HealthParam(false));
        assertThat(unit.isHealthy()).isFalse();
    }

    @Test
    public void shouldBeHealthyIfAllPassed() throws Exception {
        unit.ok(new HealthParam(true));
        unit.ok(new HealthParam(true));
        assertThat(unit.isHealthy()).isTrue();
    }

}