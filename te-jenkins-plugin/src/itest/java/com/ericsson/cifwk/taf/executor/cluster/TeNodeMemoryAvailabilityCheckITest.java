package com.ericsson.cifwk.taf.executor.cluster;

import com.ericsson.cifwk.taf.executor.healthcheck.HealthParam;
import com.ericsson.cifwk.taf.executor.healthcheck.TeNodeMemoryAvailabilityCheckCallable;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

/**
 * Created by xyakkir on 4/10/19.
 */
public class TeNodeMemoryAvailabilityCheckITest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TeNodeMemoryAvailabilityCheckITest.class);

    @Test
    public void testDoCheckNegative() {
        if(!System.getProperty("os.name").contains("Windows")) {
            TeNodeMemoryAvailabilityCheckCallable checkDisk = new TeNodeMemoryAvailabilityCheckCallable("master", 95);
            HealthParam checkHealthParam = new HealthParam("Memory Space", "master");
            String response = checkDisk.doCheck(checkHealthParam);
            assertThat(response, containsString("\"passed\":false"));
        }
        else
            LOGGER.info("Skipping test case testDoCheckNegative in Windows");
    }

    @Test
    public void testDoCheckPositive() {
        if(!System.getProperty("os.name").contains("Windows")) {
            TeNodeMemoryAvailabilityCheckCallable checkDisk = new TeNodeMemoryAvailabilityCheckCallable("master", 5);
            HealthParam checkHealthParam = new HealthParam("Memory Space", "master");
            String response = checkDisk.doCheck(checkHealthParam);
            assertThat(response, containsString("\"passed\":true"));
        }
        else
            LOGGER.info("Skipping test case testDoCheckNegative in Windows");
    }
}
