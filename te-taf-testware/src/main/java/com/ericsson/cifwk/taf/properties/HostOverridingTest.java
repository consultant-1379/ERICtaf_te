package com.ericsson.cifwk.taf.properties;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import org.testng.annotations.Test;

import static org.junit.Assert.assertEquals;

public class HostOverridingTest extends AbstractConfigurationAwareTest {

    @Test
    // This test relies on MS1 host properties overridding
    public void realHostValuesShouldMatchExpected() {
        verifyHostIpAddress("host_for_profile_tests");
    }

    private void verifyHostIpAddress(String hostName) {
        Host host = DataHandler.getHostByName(hostName);
        String expectedIp = tafConfiguration.getString(String.format("host.%s.expectedIp", hostName));
        assertEquals(expectedIp, host.getIp());
    }

}
