package com.ericsson.cifwk.taf.executor.model;

import com.ericsson.cifwk.taf.executor.BuildParameterNames;
import com.ericsson.cifwk.taf.executor.annotations.Parameter;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ManualTestData;

import java.util.Set;

public class ManualTestsBuildParameters extends CommonBuildParameters {

    @Parameter(name = BuildParameterNames.MANUAL_TEST_CAMPAIGN_IDS)
    private String manualTestCampaignIdsAsCsv;

    public String getManualTestCampaignIdsAsCsv() {
        return manualTestCampaignIdsAsCsv;
    }

    public void setManualTestCampaignIdsAsCsv(String manualTestCampaignIdsAsCsv) {
        this.manualTestCampaignIdsAsCsv = manualTestCampaignIdsAsCsv;
    }

    public ManualTestData getManualTestData() {
        return ManualTestData.from(manualTestCampaignIdsAsCsv);
    }

    public boolean defined() {
        Set<String> testCampaignIds = getManualTestData().getTestCampaignIds();
        return !testCampaignIds.isEmpty();
    }
}
