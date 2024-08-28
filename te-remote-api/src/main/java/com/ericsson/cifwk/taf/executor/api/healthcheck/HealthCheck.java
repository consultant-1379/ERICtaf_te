package com.ericsson.cifwk.taf.executor.api.healthcheck;

import com.google.gson.annotations.Expose;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class HealthCheck {

    @Expose
    private String description;
    @Expose
    private String name;
    @Expose
    private Boolean passed;
    @Expose
    private String scope;

    public HealthCheck() { }

    public HealthCheck(String description, String name, Boolean passed, String scope) {
        this.description = description;
        this.name = name;
        this.passed = passed;
        this.scope = scope;
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

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public String toString() {
        return "HealthCheck{" +
                "description='" + description + '\'' +
                ", name='" + name + '\'' +
                ", passed=" + passed +
                ", scope='" + scope + '\'' +
                '}';
    }

}
