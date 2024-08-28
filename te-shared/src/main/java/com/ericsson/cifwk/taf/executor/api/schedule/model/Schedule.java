package com.ericsson.cifwk.taf.executor.api.schedule.model;

import java.util.ArrayList;
import java.util.List;

public final class Schedule {

    private final List<ScheduleChild> children;
    private final List<ScheduleEnvironmentProperty> environmentProperties;

    public Schedule(List<ScheduleChild> children) {
        this(children, new ArrayList<ScheduleEnvironmentProperty>());
    }

    public Schedule(List<ScheduleChild> children, List<ScheduleEnvironmentProperty> environmentProperties) {
        this.children = children;
        this.environmentProperties = environmentProperties;
    }

    public List<ScheduleChild> getChildren() {
        return children;
    }

    public List<ScheduleEnvironmentProperty> getEnvironmentProperties() {
        return environmentProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Schedule schedule = (Schedule) o;

        if (children != null ? !children.equals(schedule.children) : schedule.children != null) return false;
        if (environmentProperties != null ? !environmentProperties.equals(schedule.environmentProperties) : schedule.environmentProperties != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = children != null ? children.hashCode() : 0;
        result = 31 * result + (environmentProperties != null ? environmentProperties.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "children=" + children +
                ", environmentProperties=" + environmentProperties +
                '}';
    }
}
