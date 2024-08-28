package com.ericsson.cifwk.taf.executor.api.schedule.model;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Arrays;
import java.util.Set;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ManualTestDataTest {

    @Test
    public void shouldAddTestCampaigns() throws Exception {
        ManualTestData unit = new ManualTestData();
        assertFalse(unit.present());

        unit.addTestCampaigns(Sets.newHashSet("1"));
        unit.addTestCampaigns(Sets.newHashSet("2", "3"));
        unit.addTestCampaign("4");
        verifyIds(unit);
        assertTrue(unit.present());
    }

    @Test
    public void shouldInitFromCsv() throws Exception {
        assertThat(ManualTestData.from(null).getTestCampaignIds(), hasSize(0));
        assertThat(ManualTestData.from("").getTestCampaignIds(), hasSize(0));
        ManualTestData unit = ManualTestData.from("1,2,3,4");
        verifyIds(unit);
    }

    @Test
    public void shouldGetTestCampaignIdsAsCsv() throws Exception {
        ManualTestData unit = ManualTestData.from("33,1,2,44");
        unit.addTestCampaigns(Arrays.asList("3", "5"));
        assertEquals("1,2,3,5,33,44", unit.getTestCampaignIdsAsCsv());
    }

    private void verifyIds(ManualTestData unit) {
        Set<String> testCampaignIds = unit.getTestCampaignIds();
        assertThat(testCampaignIds, hasSize(4));
        assertThat(testCampaignIds, hasItems("1", "2", "3", "4"));
    }
}