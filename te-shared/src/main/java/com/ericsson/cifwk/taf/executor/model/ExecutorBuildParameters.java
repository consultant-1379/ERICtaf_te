package com.ericsson.cifwk.taf.executor.model;

import com.ericsson.cifwk.taf.executor.BuildParameterNames;
import com.ericsson.cifwk.taf.executor.TestwareRuntimeLimitations;
import com.ericsson.cifwk.taf.executor.annotations.Parameter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 25/01/2016
 */
public class ExecutorBuildParameters extends CommonBuildParameters {

    @Parameter(name = BuildParameterNames.EIFFEL_SCHEDULE_ITEM_EXECUTION_ID)
    private String eiffelScheduleItemExecutionId;

    @Parameter(name = BuildParameterNames.TAF_DEPENDENCIES)
    private String tafTestwareGav;

    @Parameter(name = BuildParameterNames.TAF_SUITES)
    private String tafSuites;

    @Parameter(name = BuildParameterNames.TAF_GROUPS)
    private String tafGroups;

    @Parameter(name = BuildParameterNames.STEP_NAME)
    private String testStepName;

    @Parameter(name = BuildParameterNames.TIMEOUT_IN_SECONDS)
    private String timeoutInSeconds;

    @Parameter(name = BuildParameterNames.ENV_PROPERTIES_JSON)
    private String envPropertyJson;

    @Parameter(name = BuildParameterNames.TESTWARE_RUNTIME_LIMITATIONS_JSON)
    private String runtimeLimitationsJson;

    private final Gson gson = new GsonBuilder().create();

    public String getEiffelScheduleItemExecutionId() {
        return eiffelScheduleItemExecutionId;
    }

    public void setEiffelScheduleItemExecutionId(String eiffelScheduleItemExecutionId) {
        this.eiffelScheduleItemExecutionId = eiffelScheduleItemExecutionId;
    }

    public String getTafTestwareGav() {
        return tafTestwareGav;
    }

    public void setTafTestwareGav(String tafTestwareGav) {
        this.tafTestwareGav = tafTestwareGav;
    }

    public String getTafSuites() {
        return tafSuites;
    }

    public void setTafSuites(String tafSuites) {
        this.tafSuites = tafSuites;
    }

    public String getTafGroups() {
        return tafGroups;
    }

    public void setTafGroups(String tafGroups) {
        this.tafGroups = tafGroups;
    }

    public String getTestStepName() {
        return testStepName;
    }

    public void setTestStepName(String testStepName) {
        this.testStepName = testStepName;
    }

    public String getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    public void setTimeoutInSeconds(String timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
    }

    public String getEnvPropertyJson() {
        return envPropertyJson;
    }

    public void setEnvPropertyJson(String envPropertyJson) {
        this.envPropertyJson = envPropertyJson;
    }

    public TestwareRuntimeLimitations getRuntimeLimitations() {
        synchronized (gson) {
            return gson.fromJson(runtimeLimitationsJson, TestwareRuntimeLimitations.class);
        }
    }

    public void setRuntimeLimitationsJson(String runtimeLimitationsJson) {
        this.runtimeLimitationsJson = runtimeLimitationsJson;
    }
}
