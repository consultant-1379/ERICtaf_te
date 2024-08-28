package com.ericsson.cifwk.taf.executor.api.schedule.model;

import java.util.List;

public final class ScheduleItemGroup extends AbstractEnvironmentPropertiesAwareChild {

    private final List<ScheduleChild> children;
    private final List<ScheduleEnvironmentProperty> environmentProperties;
    private final boolean parallel;
    private transient ScheduleChild parent;

    public ScheduleItemGroup(ScheduleChild parent, List<ScheduleChild> children, boolean parallel,
                             List<ScheduleEnvironmentProperty> environmentProperties) {
        this.children = children;
        this.parallel = parallel;
        this.environmentProperties = environmentProperties;
        this.parent = parent;
    }

    public List<ScheduleChild> getChildren() {
        return children;
    }

    public boolean isParallel() {
        return parallel;
    }

    @Override
    public List<ScheduleEnvironmentProperty> getDefinedEnvironmentProperties() {
        return environmentProperties;
    }

    @Override
    public ScheduleChild getParent() {
        return parent;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitItemGroup(children, parallel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScheduleItemGroup that = (ScheduleItemGroup) o;

        if (parallel != that.parallel) return false;
        if (children != null ? !children.equals(that.children) : that.children != null) return false;
        if (environmentProperties != null ? !environmentProperties.equals(that.environmentProperties) : that.environmentProperties != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = children != null ? children.hashCode() : 0;
        result = 31 * result + (environmentProperties != null ? environmentProperties.hashCode() : 0);
        result = 31 * result + (parallel ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ScheduleItemGroup{" +
                "children=" + children +
                ", environmentProperties=" + environmentProperties +
                ", parallel=" + parallel +
                '}';
    }
}
