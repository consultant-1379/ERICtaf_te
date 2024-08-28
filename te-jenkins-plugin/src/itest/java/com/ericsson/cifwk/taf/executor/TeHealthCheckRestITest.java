package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.api.TeRestServiceClient;
import com.ericsson.cifwk.taf.executor.api.TriggeringTask;
import com.ericsson.cifwk.taf.executor.api.healthcheck.HealthCheck;
import com.ericsson.cifwk.taf.executor.api.healthcheck.HealthCheckState;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

public class TeHealthCheckRestITest extends RestServiceAwareITest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        disableCSRFCheck();
        deleteAllJobs();
    }

    @Test
    public void shouldRunHealthCheck() throws Exception {
        TeRestServiceClient restServiceClient = getTeRestServiceClient();
        HealthCheckState healthCheckState = restServiceClient.getTeHealthCheck();
        List<HealthCheck> healthCheckResults = healthCheckState.getHealthCheckResults();
        assertThat(healthCheckResults.size(), greaterThanOrEqualTo(1));
    }

    @Override
    protected TriggeringTask createTriggeringTask(String packageVersion, String sutResource, String[] pathsToSchedules) {
        // Nothing to trigger here
        throw new UnsupportedOperationException();
    }
}
