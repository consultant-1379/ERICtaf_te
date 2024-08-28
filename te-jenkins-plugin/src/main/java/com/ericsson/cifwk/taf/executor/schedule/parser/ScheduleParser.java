package com.ericsson.cifwk.taf.executor.schedule.parser;

import com.ericsson.cifwk.taf.executor.api.schedule.model.ManualTestData;
import com.ericsson.cifwk.taf.executor.api.schedule.model.Schedule;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleChild;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleComponent;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleEnvironmentProperty;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleItem;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleItemGroup;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleLoader;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleLocation;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleManualItem;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleRoot;
import com.ericsson.cifwk.taf.executor.schedule.ScheduleGavLoader;
import com.ericsson.cifwk.taf.executor.schedule.parser.xml.EnvironmentProperty;
import com.ericsson.cifwk.taf.executor.schedule.parser.xml.ManualTestItem;
import com.ericsson.cifwk.taf.executor.schedule.parser.xml.ScheduleChildNode;
import com.ericsson.cifwk.taf.executor.schedule.parser.xml.ScheduleNode;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import org.simpleframework.xml.Serializer;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class ScheduleParser {

    private final Serializer serializer;
    private final ScheduleLoader loader;
    private ScheduleValidator scheduleValidator;

    public ScheduleParser(Serializer serializer, ScheduleLoader loader) {
        this(serializer, loader, ScheduleValidator.withDefaultSchema());
    }

    public ScheduleParser(Serializer serializer, ScheduleLoader loader, ScheduleValidator scheduleValidator) {
        this.serializer = serializer;
        this.loader = loader;
        this.scheduleValidator = scheduleValidator;
    }

    public Schedule parse(String xml, ScheduleLocation location) {
        ManualTestData manualTestData = new ManualTestData();
        Schedule schedule = parse(null, xml, new ScheduleIncludeStack(location), manualTestData);
        if (!manualTestData.present()) {
            return schedule;
        }
        // To run basic schedule items in parallel with manual tests
        return postProcessScheduleWithManualTests(manualTestData, schedule);
    }

    private Schedule parse(ScheduleChild parent, String xml, ScheduleIncludeStack includeStack, ManualTestData finalManualTestData) {
        xml= xml.trim().replaceFirst("^([\\W]+)<","<");
        scheduleValidator.validate(xml);
        try {
            ScheduleNode scheduleNode = serializer.read(ScheduleNode.class, xml);
            ManualTestItem scheduleManualTests = scheduleNode.getManualTestItem();
            if (scheduleManualTests != null) {
                finalManualTestData.addTestCampaigns(scheduleManualTests.getTestCampaignIds());
            }
            List<ScheduleEnvironmentProperty> environmentProperties = from(scheduleNode.getEnvironmentProperties());
            ScheduleRoot scheduleRoot = new ScheduleRoot(parent, environmentProperties);
            List<ScheduleChild> children = parseChildren(scheduleRoot, scheduleNode.getChildren(), includeStack, finalManualTestData);
            return new Schedule(children, environmentProperties);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private List<ScheduleChild> parseChildren(ScheduleChild parent, List<ScheduleChildNode> childNodes,
                                              ScheduleIncludeStack includeStack, ManualTestData manualTestData) {
        List<ScheduleChild> children = new ArrayList<>();
        for (ScheduleChildNode childNode : childNodes) {
            ScheduleChild child = parseChild(parent, childNode, includeStack, manualTestData);
            if (child != null) {
                children.add(child);
            }
        }
        return children;
    }

    private Schedule postProcessScheduleWithManualTests(ManualTestData manualTestData, Schedule schedule) {
        ScheduleChild firstChild = schedule.getChildren().get(0);
        ScheduleChild root = firstChild.getParent();
        ScheduleItemGroup basicItems =
                new ScheduleItemGroup(root, schedule.getChildren(), false, newArrayList());
        ScheduleManualItem scheduleManualItem = new ScheduleManualItem(manualTestData);
        ScheduleItemGroup topGroup = new ScheduleItemGroup(root, asList(basicItems, scheduleManualItem), true, newArrayList());
        return new Schedule(singletonList(topGroup), schedule.getEnvironmentProperties());
    }

    private List<ScheduleEnvironmentProperty> from(List<EnvironmentProperty> environmentProperties) {
        if (environmentProperties == null) {
            return newArrayList();
        }
        return environmentProperties.stream()
                .map((input) -> new ScheduleEnvironmentProperty(input.getType(), input.getKey(), input.getValue()))
                .collect(toList());
    }

    private ScheduleChild parseChild(final ScheduleChild parent, ScheduleChildNode childNode,
                                     final ScheduleIncludeStack includeStack, final ManualTestData manualTestData) {
        return childNode.accept(new ScheduleChildNode.Visitor<ScheduleChild>() {

            @Override
            public ScheduleChild visitItem(String name, String component, String suites,
                                           String groups, String agentLabel, boolean stopOnFail, Integer timeoutInSeconds,
                                           List<EnvironmentProperty> environmentProperties) {
                ScheduleComponent scheduleComponent = ScheduleGavLoader.getScheduleComponent(component);
                List<String> suiteList = getCommaSeparatedList(suites);
                List<String> groupList = getCommaSeparatedList(groups);
                return new ScheduleItem(parent, name, scheduleComponent, suiteList, groupList, agentLabel,
                        stopOnFail, timeoutInSeconds, from(environmentProperties));
            }

            @Override
            public ScheduleChild visitItemGroup(List<ScheduleChildNode> childNodes,
                                                boolean parallel, List<EnvironmentProperty> environmentProperties) {
                List<ScheduleChild> children = newArrayList();
                ScheduleItemGroup scheduleItemGroup = new ScheduleItemGroup(parent, children, parallel, from(environmentProperties));
                children.addAll(parseChildren(scheduleItemGroup, childNodes, includeStack, manualTestData));
                return scheduleItemGroup;
            }

            @Override
            public ScheduleChild visitInclude(String address) {
                ScheduleLocation include = loader.getScheduleIncludeLocation(address);
                includeStack.push(include);
                String xml = loader.load(include);
                Schedule schedule = parse(parent, xml, includeStack, manualTestData);
                includeStack.pop();
                return new ScheduleItemGroup(parent, schedule.getChildren(), false, schedule.getEnvironmentProperties());
            }
        });
    }

    private static List<String> getCommaSeparatedList(String list) {
        Iterable<String> suitesIterable = Splitter.on(',')
                .trimResults()
                .omitEmptyStrings()
                .split(Strings.nullToEmpty(list));
        return ImmutableList.copyOf(suitesIterable);
    }

}
