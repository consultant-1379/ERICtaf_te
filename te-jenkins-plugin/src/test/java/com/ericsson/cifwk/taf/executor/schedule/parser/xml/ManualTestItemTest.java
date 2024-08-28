package com.ericsson.cifwk.taf.executor.schedule.parser.xml;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

public class ManualTestItemTest {

    @Test
    public void testGetTestCampaignIds() throws Exception {
        ManualTestItem unit = new ManualTestItem();
        unit.setTestCampaigns(Arrays.asList(testCampaign("1"), testCampaign("2")));
        assertThat(unit.getTestCampaignIds(), hasSize(2));
        assertThat(unit.getTestCampaignIds(), hasItems("1", "2"));
    }

    private TestCampaign testCampaign(String campaignId) {
        TestCampaign testCampaign = new TestCampaign();
        testCampaign.setId(campaignId);
        return testCampaign;
    }

}