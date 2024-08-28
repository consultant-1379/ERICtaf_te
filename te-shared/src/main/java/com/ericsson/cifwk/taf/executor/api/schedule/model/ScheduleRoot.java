package com.ericsson.cifwk.taf.executor.api.schedule.model;

import java.util.List;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 15/02/2016
 */
public class ScheduleRoot extends AbstractEnvironmentPropertiesAwareChild {

    // Not null if this is embedded schedule
    private final transient ScheduleChild parent;
    private final List<ScheduleEnvironmentProperty> environmentProperties;

    public ScheduleRoot(ScheduleChild parent, List<ScheduleEnvironmentProperty> environmentProperties) {
        this.parent = parent;
        this.environmentProperties = environmentProperties;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return null;
    }

    @Override
    public List<ScheduleEnvironmentProperty> getDefinedEnvironmentProperties() {
        return environmentProperties;
    }

    @Override
    public ScheduleChild getParent() {
        return parent;
    }

}
