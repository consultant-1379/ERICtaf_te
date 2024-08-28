package com.ericsson.cifwk.taf.executor.schedule.parser.xml;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;
import java.util.Set;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 14/03/2016
 */
@Root(name = "manual-item")
public class ManualTestItem {

    @ElementList(name = "test-campaigns", type = TestCampaign.class, required = true)
    private List<TestCampaign> testCampaigns;

    public List<TestCampaign> getTestCampaigns() {
        return testCampaigns;
    }

    public void setTestCampaigns(List<TestCampaign> testCampaigns) {
        this.testCampaigns = testCampaigns;
    }

    public Set<String> getTestCampaignIds() {
        return Sets.newHashSet(Iterables.transform(testCampaigns, new Function<TestCampaign, String>() {
            @Override
            public String apply(TestCampaign input) {
                return input.getId();
            }
        }));
    }

}
