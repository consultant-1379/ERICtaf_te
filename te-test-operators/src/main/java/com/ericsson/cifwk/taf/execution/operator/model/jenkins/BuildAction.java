package com.ericsson.cifwk.taf.execution.operator.model.jenkins;

import com.google.gson.annotations.Expose;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")
public class BuildAction {

    @Expose
    private List<Cause> causes = new ArrayList<Cause>();
    @Expose
    private List<Parameter> parameters = new ArrayList<Parameter>();

    public List<Cause> getCauses() {
        return causes;
    }

    public void setCauses(List<Cause> causes) {
        this.causes = causes;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "BuildAction{" +
                "causes=" + causes +
                ", parameters=" + parameters +
                '}';
    }
}
