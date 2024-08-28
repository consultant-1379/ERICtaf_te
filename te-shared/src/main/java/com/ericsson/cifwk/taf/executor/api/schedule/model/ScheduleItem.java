package com.ericsson.cifwk.taf.executor.api.schedule.model;

import com.google.common.base.Objects;

import java.util.List;

public final class ScheduleItem extends AbstractEnvironmentPropertiesAwareChild {

    private final String name;
    private final ScheduleComponent component;
    private final List<String> suites;
    private final List<String> groups;
    private final String agentLabel;
    private final boolean stopOnFail;
    private final Integer timeoutInSeconds;
    private final List<ScheduleEnvironmentProperty> environmentProperties;
    private transient ScheduleChild parent;

    public ScheduleItem(ScheduleChild parent, String name,
                        ScheduleComponent component,
                        List<String> suites,
                        List<String> groups,
                        String agentLabel,
                        boolean stopOnFail,
                        Integer timeoutInSeconds,
                        List<ScheduleEnvironmentProperty> environmentProperties) {
        this.name = name;
        this.component = component;
        this.suites = suites;
        this.groups = groups;
        this.agentLabel = agentLabel;
        this.stopOnFail = stopOnFail;
        this.timeoutInSeconds = timeoutInSeconds;
        this.environmentProperties = environmentProperties;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public ScheduleComponent getComponent() {
        return component;
    }

    public List<String> getSuites() {
        return suites;
    }

    public List<String> getGroups() {
        return groups;
    }

    public String getAgentLabel() {
        return agentLabel;
    }

    public int getTimeoutInSeconds() {
        return (timeoutInSeconds == null) ? 0 : timeoutInSeconds;
    }    
    
    public boolean isStopOnFail() {
        return stopOnFail;
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
        return visitor.visitItem(name, component, suites, groups, agentLabel, stopOnFail, timeoutInSeconds,
                getEffectiveEnvironmentProperties());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduleItem that = (ScheduleItem) o;
        return stopOnFail == that.stopOnFail &&
                Objects.equal(name, that.name) &&
                Objects.equal(component, that.component) &&
                Objects.equal(suites, that.suites) &&
                Objects.equal(groups, that.groups) &&
                Objects.equal(agentLabel, that.agentLabel) &&
                Objects.equal(timeoutInSeconds, that.timeoutInSeconds) &&
                Objects.equal(environmentProperties, that.environmentProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, component, suites, groups, agentLabel, stopOnFail, timeoutInSeconds, environmentProperties);
    }

    @Override
    public String toString() {
        return "ScheduleItem{" +
                "name='" + name + '\'' +
                ", component=" + component +
                ", suites=" + suites +
                ", groups=" + groups +
                ", agentLabel=" + agentLabel +
                ", stopOnFail=" + stopOnFail +
                ", timeoutInSeconds=" + timeoutInSeconds +
                ", environmentProperties=" + environmentProperties +
                '}';
    }
}
