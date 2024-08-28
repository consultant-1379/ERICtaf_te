package com.ericsson.cifwk.taf.executor.model;

import com.ericsson.cifwk.taf.executor.BuildParameterNames;
import com.ericsson.cifwk.taf.executor.annotations.Parameter;

import java.util.Map;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 22/01/2016
 */
public abstract class CommonBuildParameters implements BuildParametersHolder {

    @Parameter(name = BuildParameterNames.EIFFEL_JOB_EXECUTION_ID)
    protected String executionId;

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    @Override
    public final Map<String, String> getAllParameters() {
        return BuildParameterHolderFactory.asMap(this);
    }

}
