package com.ericsson.cifwk.taf.executor.schedule.model;

import com.ericsson.cifwk.taf.executor.api.schedule.model.ManualTestData;
import com.ericsson.cifwk.taf.executor.api.schedule.model.Schedule;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleChild;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleComponent;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleEnvironmentProperty;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleItem;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleItemGroup;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleManualItem;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

public final class ScheduleHelper {

    private ScheduleHelper() {
    }

    public static Schedule schedule(ScheduleChild... children) {
        return schedule(Lists.<ScheduleEnvironmentProperty>newArrayList(), children);
    }

    public static Schedule schedule(List<ScheduleEnvironmentProperty> environmentProperties, ScheduleChild... children) {
        return new Schedule(Arrays.asList(children), environmentProperties);
    }

    public static ScheduleItem scheduleItem(String name,
                                            String groupId,
                                            String artifactId,
                                            String version,
                                            List<String> suites,
                                            List<String> groups,
                                            boolean stopOnFail,
                                            Integer timeoutInSeconds) {
        return scheduleItem(name, groupId, artifactId, version, suites, groups, null, stopOnFail, timeoutInSeconds,
                Lists.<ScheduleEnvironmentProperty>newArrayList());
    }

    public static ScheduleItem scheduleItem(String name,
                                            String groupId,
                                            String artifactId,
                                            String version,
                                            List<String> suites,
                                            List<String> groups,
                                            String agentLabel,
                                            boolean stopOnFail,
                                            Integer timeoutInSeconds,
                                            List<ScheduleEnvironmentProperty> environmentProperties) {
        ScheduleComponent component = new ScheduleComponent(groupId, artifactId, version);
        return new ScheduleItem(null, name, component, suites, groups, agentLabel, stopOnFail, timeoutInSeconds, environmentProperties);
    }

    public static ScheduleItemGroup scheduleItemGroup(boolean parallel,
                                                      ScheduleChild... children) {
        return scheduleItemGroup(parallel, Lists.<ScheduleEnvironmentProperty>newArrayList(), children);
    }

    public static ScheduleItemGroup scheduleItemGroup(boolean parallel,
                                                      List<ScheduleEnvironmentProperty> environmentProperties,
                                                      ScheduleChild... children) {
        return new ScheduleItemGroup(null, Arrays.asList(children), parallel, environmentProperties);
    }

    public static ScheduleEnvironmentProperty envProperty(String type, String key, String value) {
        return new ScheduleEnvironmentProperty(type, key, value);
    }

    public static List<ScheduleEnvironmentProperty> envProperties(ScheduleEnvironmentProperty... scheduleEnvironmentProperties) {
        return Arrays.asList(scheduleEnvironmentProperties);
    }

    public static ScheduleManualItem manualItem(String testCampaignId, String...  otherTestCampaignIds) {
        ManualTestData manualTestData = new ManualTestData();
        List<String> allCampaignIds = Lists.newArrayList(testCampaignId);
        allCampaignIds.addAll(Arrays.asList(otherTestCampaignIds));
        manualTestData.addTestCampaigns(allCampaignIds);
        return new ScheduleManualItem(manualTestData);
    }

}
