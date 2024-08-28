package com.ericsson.cifwk.taf.executor.api.schedule.model;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 11/03/2016
 */
public class ManualTestData implements Serializable {

    private Set<String> testCampaignIds;

    public ManualTestData() {
        testCampaignIds = Sets.newTreeSet(new TestCampaignIdComparator());
    }

    public ManualTestData(Iterable<String> testCampaignIds) {
        this();
        Preconditions.checkArgument(testCampaignIds != null);
        this.testCampaignIds.addAll(Sets.newHashSet(testCampaignIds));
    }

    public void addTestCampaign(String testCampaignId) {
        testCampaignIds.add(testCampaignId);
    }

    public final void addTestCampaigns(Collection<String> testCampaignIds) {
        this.testCampaignIds.addAll(testCampaignIds);
    }

    public Set<String> getTestCampaignIds() {
        return testCampaignIds;
    }

    public boolean present() {
        return !testCampaignIds.isEmpty();
    }

    public static ManualTestData from(String manualTestCampaignIdsAsCsv) throws NumberFormatException {
        if (StringUtils.isBlank(manualTestCampaignIdsAsCsv)) {
            return new ManualTestData();
        }
        Iterable<String> split = Splitter.on(",").omitEmptyStrings().trimResults().split(manualTestCampaignIdsAsCsv);
        return new ManualTestData(split);
    }

    public String getTestCampaignIdsAsCsv() {
        return Joiner.on(",").skipNulls().join(testCampaignIds);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ManualTestData that = (ManualTestData) o;

        if (!testCampaignIds.equals(that.testCampaignIds)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return testCampaignIds.hashCode();
    }

    @Override
    public String toString() {
        return "ManualTestItems{" +
                "testCampaignIds=" + testCampaignIds +
                '}';
    }

    private class TestCampaignIdComparator implements Serializable, Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            if (StringUtils.isNumeric(o1) && StringUtils.isNumeric(o2)) {
                return new Long(o1).compareTo(new Long(o2));
            }
            return o1.compareTo(o2);
        }

    }
}
