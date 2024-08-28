
package com.ericsson.cifwk.taf.execution.operator.model.jenkins;

import com.google.gson.annotations.Expose;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class ParameterDefinition {

    @Expose
    private DefaultParameterValue defaultParameterValue;
    @Expose
    private String description;
    @Expose
    private String name;
    @Expose
    private String type;

    public DefaultParameterValue getDefaultParameterValue() {
        return defaultParameterValue;
    }

    public void setDefaultParameterValue(DefaultParameterValue defaultParameterValue) {
        this.defaultParameterValue = defaultParameterValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ParameterDefinition{" +
                "defaultParameterValue=" + defaultParameterValue +
                ", description='" + description + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
