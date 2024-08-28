
package com.ericsson.cifwk.taf.execution.operator.model.jenkins;

import com.google.gson.annotations.Expose;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")
public class JobAction {

    @Expose
    private List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();

    public List<ParameterDefinition> getParameterDefinitions() {
        return parameterDefinitions;
    }

    public void setParameterDefinitions(List<ParameterDefinition> parameterDefinitions) {
        this.parameterDefinitions = parameterDefinitions;
    }

    @Override
    public String toString() {
        return "JobAction{" +
                "parameterDefinitions=" + parameterDefinitions +
                '}';
    }

}
