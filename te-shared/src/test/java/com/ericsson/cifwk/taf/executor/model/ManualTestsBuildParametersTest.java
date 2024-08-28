package com.ericsson.cifwk.taf.executor.model;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ManualTestsBuildParametersTest {

    @Test
    public void testDefined() throws Exception {
        ManualTestsBuildParameters unit = new ManualTestsBuildParameters();
        unit.setManualTestCampaignIdsAsCsv(" ");
        assertFalse(unit.defined());
        unit.setManualTestCampaignIdsAsCsv("1,2,3");
        assertTrue(unit.defined());
    }
}