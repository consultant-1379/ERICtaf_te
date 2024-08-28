package com.ericsson.cifwk.taf.executor.schedule;

import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleComponent;
import com.google.common.base.Functions;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScheduleItemGavResolver {

    private final ScheduleComponent scheduleArtifact;

    private final Map<ScheduleComponent, ScheduleComponent> testWareComponentMap;

    public ScheduleItemGavResolver(ScheduleComponent scheduleArtifact, Collection<String> testWareGAVs) {
        this.scheduleArtifact = scheduleArtifact;
        Set<ScheduleComponent> testWareComponents = parseGAVs(testWareGAVs);
        this.testWareComponentMap = Maps.uniqueIndex(testWareComponents, Functions.<ScheduleComponent>identity());
    }

    public ScheduleComponent resolve(ScheduleComponent component) {
        // If the version is already defined, we don't need a resolution
        if (!StringUtils.isBlank(component.getVersion())) {
            return component;
        }
        // Search in testware components
        ScheduleComponent testWareComponent = testWareComponentMap.get(component);
        if (testWareComponent != null) {
            return testWareComponent;
        }
        // If it's not in the list of testwares, but has the same GA as schedule,
        // we propagate schedule's artifact's version to it
        if (component.equals(scheduleArtifact)) {
            component.setVersion(scheduleArtifact.getVersion());
            return component;
        } else {
            return null;
        }
    }

    private static Set<ScheduleComponent> parseGAVs(Collection<String> gavs) {
        Set<ScheduleComponent> components = new HashSet<>(gavs.size());
        for (String gav : gavs) {
            components.add(ScheduleComponent.parseGav(gav));
        }
        return components;
    }
}
