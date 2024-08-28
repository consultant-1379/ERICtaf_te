package com.ericsson.cifwk.taf.executor.schedule.parser.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import static org.apache.commons.lang.StringUtils.isBlank;

@Root(name = "item")
public final class ItemNode extends EnvironmentPropertiesHolder implements ScheduleChildNode {

    @Element
    private String name;

    @Element
    private String component;

    @Element
    private String suites;

    @Element(required = false)
    private String tags;

    @Element(required = false)
    private String groups;

    @Element(required = false)
    private String agentLabel;

    @Attribute(name = "stop-on-fail", required = false)
    private boolean stopOnFail;

    @Attribute(name = "timeout-in-seconds", required = false)
    private Integer timeoutInSeconds;

    public String getName() {
        return name;
    }    
    
    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getSuites() {
        return suites;
    }

    public void setSuites(String suite) {
        this.suites = suite;
    }

    public String getGroups() {
        return isBlank(tags) ? groups : tags;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }

    public String getAgentLabel() {
        return agentLabel;
    }

    public void setAgentLabel(String agentLabel) {
        this.agentLabel = agentLabel;
    }

    public boolean isStopOnFail() {
        return stopOnFail;
    }

    public void setStopOnFail(boolean stopOnFail) {
        this.stopOnFail = stopOnFail;
    }

    public Integer getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    public void setTimeoutInSeconds(Integer timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitItem(name, component, suites, getGroups(), agentLabel, stopOnFail, timeoutInSeconds, environmentProperties);
    }
}
