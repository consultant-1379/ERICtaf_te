package com.ericsson.cifwk.taf.executor.healthcheck;

import java.io.Serializable;

public class HealthParam implements Serializable {

    private String name;
    private boolean passed = true;
    private String description = "";
    private String scope;

    public static HealthParam ok(String name, String scope) {
        return new HealthParam(name, scope);
    }

    public static HealthParam fail(String name, String scope, String description) {
        return new HealthParam(name, scope).setPassed(false).setDescription(description);
    }

    public HealthParam(String name, String scope) {
        this.name = name;
        this.scope = scope;
    }

    public HealthParam(boolean passed) {
        this.passed = passed;
    }

    public boolean isPassed() {
        return passed;
    }

    public HealthParam setPassed(boolean passed) {
        this.passed = passed;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public HealthParam setDescription(String fullDesc) {
        this.description = fullDesc;
        return this;
    }

    public String getScope() {
        return scope;
    }

    public HealthParam setScope(String scope) {
        this.scope = scope;
        return this;
    }

    public String getName() {
        return name;
    }

}
