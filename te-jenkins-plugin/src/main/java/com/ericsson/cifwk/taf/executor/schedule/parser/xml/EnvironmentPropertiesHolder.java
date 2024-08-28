package com.ericsson.cifwk.taf.executor.schedule.parser.xml;

import org.simpleframework.xml.ElementList;

import java.util.List;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 15/02/2016
 */
abstract class EnvironmentPropertiesHolder {

    @ElementList(name = "env-properties", type = EnvironmentProperty.class, required = false)
    protected List<EnvironmentProperty> environmentProperties;

    public List<EnvironmentProperty> getEnvironmentProperties() {
        return environmentProperties;
    }

    public void setEnvironmentProperties(List<EnvironmentProperty> environmentProperties) {
        this.environmentProperties = environmentProperties;
    }

}
