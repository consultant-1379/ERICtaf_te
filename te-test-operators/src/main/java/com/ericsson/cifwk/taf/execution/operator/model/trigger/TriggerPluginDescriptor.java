package com.ericsson.cifwk.taf.execution.operator.model.trigger;


public class TriggerPluginDescriptor {

    String hpiPath;

    public void setHpiPath(String hpiPath) {
        this.hpiPath = hpiPath;
    }

    public String getHpiPath() {
        return hpiPath;
    }

    public String getRpmPath() {
        return null;
    }

    public String getPluginName() {
        return "taf-trigger";
    }
}
