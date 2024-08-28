package com.ericsson.cifwk.taf.executor.model;

import com.ericsson.cifwk.taf.executor.BuildParameterNames;
import com.ericsson.cifwk.taf.executor.annotations.Parameter;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Main TE project (<code>TEST_SCHEDULER</code>) build parameters.
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 25/01/2016
 */
public class ScheduleBuildParameters extends CommonBuildParameters {

    @Parameter(name = BuildParameterNames.EIFFEL_JOB_TRIGGER_EVENT_ID)
    private String eiffelJobTriggerEventId;

    @Parameter(name = BuildParameterNames.EIFFEL_JOB_STARTED_EVENT_ID)
    private String eiffelJobStartedEventId;

    @Parameter(name = BuildParameterNames.ENABLE_LDAP)
    private String enableLdap;

    @Parameter(name = BuildParameterNames.TE_USERNAME)
    private String teUsername;

    @Parameter(name = BuildParameterNames.TE_PASSWORD)
    private String tePassword;

    @Parameter(name = BuildParameterNames.EIFFEL_SCHEDULE_STARTED_EVENT_ID)
    private String eiffelScheduleStartedEventId;

    @Parameter(name = BuildParameterNames.EIFFEL_SCHEDULE_STARTED_EXECUTION_ID)
    private String eiffelScheduleStartedExecutionId;

    @Parameter(name = BuildParameterNames.EIFFEL_TEST_EXECUTION_ID)
    private String eiffelTestExecutionId;

    @Parameter(name = BuildParameterNames.REPOSITORY_URL)
    private String repositoryUrl;

    @Parameter(name = BuildParameterNames.SCHEDULE_ARTIFACT)
    private String scheduleArtifact;

    @Parameter(name = BuildParameterNames.SCHEDULE_SOURCE)
    private String scheduleSource;

    @Parameter(name = BuildParameterNames.SCHEDULE_NAME)
    private String scheduleName;

    @Parameter(name = BuildParameterNames.SCHEDULE)
    private String scheduleXml;

    @Parameter(name = BuildParameterNames.TAF_SCHEDULER_ADDRESS)
    private String tafSchedulerUrl;

    @Parameter(name = BuildParameterNames.TESTWARE)
    private String testware;

    @Parameter(name = BuildParameterNames.SLAVE_HOSTS)
    private String slaveHosts;

    @Parameter(name = BuildParameterNames.SUT_RESOURCE)
    private String sutResource;

    @Parameter(name = BuildParameterNames.COMMON_TEST_PROPERTIES)
    private String commonTestProperties;

    @Parameter(name = BuildParameterNames.TEST_TRIGGER_DETAILS_JSON)
    private String testTriggerDetailsJson;

    @Parameter(name = BuildParameterNames.JOB_TYPE)
    private String jobType;

    @Parameter(name = BuildParameterNames.MIN_TAF_VERSION)
    private String minTafVersion;

    @Parameter(name = BuildParameterNames.USER_DEFINED_GAVS)
    private String userDefinedGAVs;

    @Parameter(name = BuildParameterNames.MISC_PROPERTIES)
    private String miscProperties;

    @Parameter(name = BuildParameterNames.CONFIG_URL)
    private String configUrl;

    @Parameter(name = BuildParameterNames.ALLURE_LOG_DIR)
    private String allureLogDir;

    @Parameter(name = BuildParameterNames.ALLURE_LOG_URL)
    private String allureLogUrl;

    public String getEiffelJobStartedEventId() {
        return eiffelJobStartedEventId;
    }

    public void setEiffelJobStartedEventId(String eiffelJobStartedEventId) {
        this.eiffelJobStartedEventId = eiffelJobStartedEventId;
    }

    public String getEiffelScheduleStartedEventId() {
        return eiffelScheduleStartedEventId;
    }

    public void setEiffelScheduleStartedEventId(String eiffelScheduleStartedEventId) {
        this.eiffelScheduleStartedEventId = eiffelScheduleStartedEventId;
    }

    public String getEiffelScheduleStartedExecutionId() {
        return eiffelScheduleStartedExecutionId;
    }

    public void setEiffelScheduleStartedExecutionId(String eiffelScheduleStartedExecutionId) {
        this.eiffelScheduleStartedExecutionId = eiffelScheduleStartedExecutionId;
    }

    public String getEiffelTestExecutionId() {
        return eiffelTestExecutionId;
    }

    public void setEiffelTestExecutionId(String eiffelTestExecutionId) {
        this.eiffelTestExecutionId = eiffelTestExecutionId;
    }

    public String getEiffelJobTriggerEventId() {
        return eiffelJobTriggerEventId;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public String getScheduleArtifact() {
        return scheduleArtifact;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public String getScheduleXml() {
        return scheduleXml;
    }

    public String getTestware() {
        return testware;
    }

    public String getSlaveHosts() {
        return slaveHosts;
    }

    public String getSutResource() {
        return sutResource;
    }

    public String getCommonTestProperties() {
        return commonTestProperties;
    }

    public String getTestTriggerDetailsJson() {
        return testTriggerDetailsJson;
    }

    public String getJobType() {
        return jobType;
    }

    public String getMinTafVersion() {
        return minTafVersion;
    }

    public List<String> getUserDefinedGAVs() {
        if(userDefinedGAVs == null) {
            return new ArrayList<String>();
        } else {
            String lineSeparator = System.getProperty("line.separator");
            return new ArrayList<String>(Arrays.asList(userDefinedGAVs.split(lineSeparator)));
        }
    }

    public String getMiscProperties() {
        return miscProperties;
    }

    public void setEiffelJobTriggerEventId(String eiffelJobTriggerEventId) {
        this.eiffelJobTriggerEventId = eiffelJobTriggerEventId;
    }

    public String getEnableLdap() {
        return enableLdap;
    }

    public void setEnableLdap(String enableLdap) {
        this.enableLdap = enableLdap;
    }

    public String getTeUsername() {
        return teUsername;
    }

    public void setTeUsername(String teUsername) {
        this.teUsername = teUsername;
    }

    public String getTePassword() {
        return tePassword;
    }

    public void setTePassword(String tePassword) {
        this.tePassword = tePassword;
    }
    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public void setScheduleArtifact(String scheduleArtifact) {
        this.scheduleArtifact = scheduleArtifact;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public void setScheduleXml(String scheduleXml) {
        this.scheduleXml = scheduleXml;
    }

    public void setTestware(String testware) {
        this.testware = testware;
    }

    public void setSlaveHosts(String slaveHosts) {
        this.slaveHosts = slaveHosts;
    }

    public void setSutResource(String sutResource) {
        this.sutResource = sutResource;
    }

    public void setCommonTestProperties(String commonTestProperties) {
        this.commonTestProperties = commonTestProperties;
    }

    public void setTestTriggerDetailsJson(String testTriggerDetailsJson) {
        this.testTriggerDetailsJson = testTriggerDetailsJson;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public void setMinTafVersion(String minTafVersion) {
        this.minTafVersion = minTafVersion;
    }

    public void setUserDefinedGAVs(String userDefinedGAVs) {
        this.userDefinedGAVs = userDefinedGAVs;
    }

    public void setMiscProperties(String miscProperties) {
        this.miscProperties = miscProperties;
    }

    public String getAllureLogDir() {
        return allureLogDir;
    }

    public void setAllureLogDir(String allureLogDir) {
        this.allureLogDir = allureLogDir;
    }

    public String getAllureLogUrl() {
        return allureLogUrl;
    }

    public void setAllureLogUrl(String allureLogUrl) {
        this.allureLogUrl = allureLogUrl;
    }

    public String getConfigUrl() {
        return configUrl;
    }

    public void setConfigUrl(String configUrl) {
        this.configUrl = configUrl;
    }

    public String getScheduleSource() {
        return scheduleSource;
    }

    public void setScheduleSource(String scheduleSource) {
        this.scheduleSource = scheduleSource;
    }

    public String getTafSchedulerUrl() {
        return tafSchedulerUrl;
    }

    public void setTafSchedulerUrl(String tafSchedulerUrl) {
        this.tafSchedulerUrl = tafSchedulerUrl;
    }

    public Map<String, String> getCommonEiffelParameters() {
        Map<String, String> allParameters = getAllParameters();
        return Maps.filterKeys(allParameters, new Predicate<String>() {
            @Override
            public boolean apply(String parameterName) {
                return BuildParameterNames.EIFFEL_JOB_STARTED_EVENT_ID.equals(parameterName) ||
                        BuildParameterNames.EIFFEL_JOB_EXECUTION_ID.equals(parameterName) ||
                        BuildParameterNames.EIFFEL_SCHEDULE_STARTED_EVENT_ID.equals(parameterName) ||
                        BuildParameterNames.EIFFEL_SCHEDULE_STARTED_EXECUTION_ID.equals(parameterName) ||
                        BuildParameterNames.EIFFEL_TEST_EXECUTION_ID.equals(parameterName) ||
                        BuildParameterNames.TE_USERNAME.equals(parameterName) ||
                        BuildParameterNames.TE_PASSWORD.equals(parameterName);
            }
        });
    }

    @Override
    public String toString() {
        return getAllParameters().toString();
    }
}
