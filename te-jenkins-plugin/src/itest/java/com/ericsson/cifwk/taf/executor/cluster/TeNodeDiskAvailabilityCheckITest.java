package com.ericsson.cifwk.taf.executor.cluster;

import com.ericsson.cifwk.taf.executor.healthcheck.HealthParam;
import com.ericsson.cifwk.taf.executor.healthcheck.TeNodeDiskAvailabilityCheckCallable;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

/**
 * Created by ekellmi on 4/13/16.
 */
public class TeNodeDiskAvailabilityCheckITest {

    @Test
    public void testDoCheckNegative() {
        TeNodeDiskAvailabilityCheckCallable checkDisk = new TeNodeDiskAvailabilityCheckCallable("master", 10000);
        HealthParam checkHealthParam = new HealthParam("Disk Space", "master");
        String response = checkDisk.doCheck(checkHealthParam);
        assertThat(response, containsString("\"passed\":false"));
    }

    @Test
    public void testDoCheckPositive() {
        TeNodeDiskAvailabilityCheckCallable checkDisk = new TeNodeDiskAvailabilityCheckCallable("master", 0);
        HealthParam checkHealthParam = new HealthParam("Disk Space", "master");
        String response = checkDisk.doCheck(checkHealthParam);
        assertThat(response, containsString("\"passed\":true"));
    }
}
