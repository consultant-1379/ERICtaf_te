package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.api.schedule.model.ManualTestData;
import com.ericsson.cifwk.taf.executor.model.ExecutorBuildParameters;
import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;
import com.ericsson.cifwk.taf.executor.model.ManualTestsBuildParameters;
import com.ericsson.cifwk.taf.executor.model.ScheduleBuildParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TestExecution implements Serializable {

    private static final long serialVersionUID = 8258324872921813224L;
    private static final Logger LOGGER = LoggerFactory.getLogger(TestExecution.class);
    private final String name;
    private final String testware;
    private final String[] suites;
    private final String groups;
    private final String generalExecutionId;
    private final String parentEventId;
    private final String parentExecutionId;
    private final String testExecutionId;
    private final String mbHost;
    private final String mbExchange;
    private final String mbDomain;
    private final String logUrl;
    private final String repositoryUrl;
    private final String scriptUrl;
    private final String configUrl;
    private final String timeoutInSeconds;
    private final String scheduleName;
    private final String allureLogDir;
    private final String allureServiceUrl;
    private final String jenkinsWorkspaceUri;
    private final String miscProperties;
    private final List<String> additionalDependencies;
    private final TestwareRuntimeLimitations runtimeLimitations;
    private ManualTestData manualTestData;
    private String allureVersion;
    private String minTafVersion;
    private List<String> userDefinedGAVs;
    private String envPropertyJson;
    private String testPomLocation;
    private String skipTests;
    private final String enableLdap;
    private final String teUsername;
    private final String tePassword;

    private TestExecution(Builder builder) {
        generalExecutionId = builder.generalExecutionId;
        name = builder.name;
        testware = builder.testware;
        suites = builder.suites;
        groups = builder.groups;
        parentEventId = builder.parentEventId;
        testExecutionId = builder.testExecutionId;
        parentExecutionId = builder.parentExecutionId;
        mbHost = builder.mbHost;
        mbDomain = builder.mbDomain;
        mbExchange = builder.mbExchange;
        logUrl = builder.logUrl;
        repositoryUrl = builder.repositoryUrl;
        scriptUrl = builder.scriptUrl;
        configUrl = builder.configUrl;
        timeoutInSeconds = builder.timeoutInSeconds;
        scheduleName = builder.scheduleName;
        allureLogDir = builder.allureLogDir;
        allureServiceUrl= builder.allureServiceUrl;
        jenkinsWorkspaceUri = builder.workspaceUri;
        allureVersion = builder.allureVersion;
        minTafVersion = builder.minTafVersion;
        userDefinedGAVs = builder.userDefinedGAVs;
        miscProperties = builder.miscProperties;
        additionalDependencies = builder.additionalDependencies;
        envPropertyJson = builder.envPropertyJson;
        manualTestData = builder.manualTestData;
        runtimeLimitations = builder.runtimeLimitations;
        skipTests = builder.skipTests;
        enableLdap = builder.enableLdap;
        teUsername = builder.teUsername;
        tePassword = builder.tePassword;
    }

    public String getName() {
        return name;
    }

    public String getTestware() {
        return testware;
    }

    public String[] getSuites() {
        return suites;
    }

    public String getGroups() {
        return groups;
    }

    public String getParentEventId() {
        return parentEventId;
    }

    public String getParentExecutionId() {
        return parentExecutionId;
    }

    public String getTestExecutionId() {
        return testExecutionId;
    }

    public String getMbHost() {
        return mbHost;
    }

    public String getMbExchange() {
        return mbExchange;
    }

    public String getMbDomain() {
        return mbDomain;
    }

    public String getLogUrl() {
        return logUrl;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public String getScriptUrl() {
        return scriptUrl;
    }

    public String getConfigUrl() {
        return configUrl;
    }

    public String getEnableLdap() { return enableLdap;}

    public String getTeUsername() { return teUsername;}

    public String getTePassword() { return tePassword;}

    public String getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getAllureLogDir() {
        return allureLogDir;
    }

    public String getAllureServiceUrl() {
        if (enableLdap != null && teUsername != null && tePassword != null){
            if ( enableLdap.equalsIgnoreCase("true") ) {
                String[] urlsplit = allureServiceUrl.split("//");
                String totalAllureServiceUrl = urlsplit[0] + "//" + teUsername + ":" + tePassword + "@" + urlsplit[1];
                return totalAllureServiceUrl;
            } else {
                return allureServiceUrl;
            }
        }
        else {
            return allureServiceUrl;
        }
    }

    public String getJenkinsWorkspaceUri() {
        return jenkinsWorkspaceUri;
    }

    public String getAllureVersion() {
        return allureVersion;
    }

    public String getMinTafVersion() {
        return minTafVersion;
    }

    public List<String> getUserDefinedGAVs() {
        return userDefinedGAVs;
    }

    public String getMiscProperties() {
        return miscProperties;
    }

    public List<String> getAdditionalDependencies() {
        return additionalDependencies;
    }

    public String getEnvPropertyJson() {
        return envPropertyJson;
    }

    public ManualTestData getManualTestData() {
        return manualTestData;
    }

    public boolean isManualTestExecution() {
        return manualTestData != null;
    }

    public TestwareRuntimeLimitations getRuntimeLimitations() {
        return runtimeLimitations;
    }

    public String getGeneralExecutionId() {
        return generalExecutionId;
    }

    public String getTestPomLocation() {
        return testPomLocation;
    }

    public void setTestPomLocation(String testPomLocation) {
        this.testPomLocation = testPomLocation;
    }

    public String getSkipTests() {
        return skipTests;
    }

    public void setSkipTests(final String skipTests) {
        this.skipTests = skipTests;
    }

    @Override
    public String toString() {
        StringBuilder allUserDefinedGAVs = new StringBuilder("");
        for(String str : userDefinedGAVs) {
            allUserDefinedGAVs.append(str).append(",");
        }

        return "TestExecution{" +
            "generalExecutionId='" + generalExecutionId + '\'' +
            ", name='" + name + '\'' +
            ", testware='" + testware + '\'' +
            ", suites=" + Arrays.toString(suites) +
            ", groups='" + groups + '\'' +
            ", parentEventId='" + parentEventId + '\'' +
            ", parentExecutionId='" + parentExecutionId + '\'' +
            ", testExecutionId='" + testExecutionId + '\'' +
            ", mbHost='" + mbHost + '\'' +
            ", mbExchange='" + mbExchange + '\'' +
            ", mbDomain='" + mbDomain + '\'' +
            ", logUrl='" + logUrl + '\'' +
            ", repositoryUrl='" + repositoryUrl + '\'' +
            ", scriptUrl='" + scriptUrl + '\'' +
            ", configUrl='" + configUrl + '\'' +
            ", timeoutInSeconds='" + timeoutInSeconds + '\'' +
            ", scheduleName='" + scheduleName + '\'' +
            ", allureLogDir='" + allureLogDir + '\'' +
            ", allureServiceUrl='" + allureServiceUrl + '\'' +
            ", jenkinsWorkspaceUri='" + jenkinsWorkspaceUri + '\'' +
            ", miscProperties='" + miscProperties + '\'' +
            ", additionalDependencies=" + additionalDependencies +
            ", manualTestData=" + manualTestData +
            ", allureVersion='" + allureVersion + '\'' +
            ", minTafVersion='" + minTafVersion + '\'' +
            ", userDefinedGAVs='" + allUserDefinedGAVs.toString() + '\'' +
            ", envPropertyJson='" + envPropertyJson + '\'' +
            ", runtimeLimitations='" + runtimeLimitations + '\'' +
            ", testPomLocation='" + testPomLocation + '\'' +
            ", skipTests= '" + skipTests + '\'' +
            ", enable_ldap= '" + enableLdap + '\'' +
            '}';
    }

    public static class Builder {

        private String name;
        private String testware;
        private String[] suites = {};
        private String groups;
        private String generalExecutionId;
        private String parentEventId;
        private String parentExecutionId;
        private String testExecutionId;
        private String mbHost;
        private String mbExchange;
        private String mbDomain;
        private String logUrl;
        private String repositoryUrl;
        private String scriptUrl;
        private String configUrl;
        private String timeoutInSeconds;
        private String scheduleName;
        private String allureLogDir;
        private String workspaceUri;
        private String allureVersion;
        private String minTafVersion;
        private List<String> userDefinedGAVs;
        private String miscProperties;
        List<String> additionalDependencies;
        private String envPropertyJson;
        private ManualTestData manualTestData;
        private TestwareRuntimeLimitations runtimeLimitations;
        private String allureServiceUrl;
        private String skipTests;
        private String enableLdap;
        private String teUsername;
        private String tePassword;

        private Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withTestware(String testware) {
            this.testware = testware;
            return this;
        }

        public Builder withSuites(String suites) {
            List<String> suitesList = new ArrayList<>();
            if (suites != null && !suites.trim().isEmpty()) {
                for (String suite : suites.split(",")) {
                    suite = suite.trim();
                    if (!suite.isEmpty()) {
                        suitesList.add(suite);
                    }
                }
                this.suites = suitesList.toArray(new String[]{});
            } else {
                this.suites = new String[]{};
            }
            return this;
        }

        public Builder withEnableLdap(String enableLdap){
            this.enableLdap = enableLdap;
            return this;
        }

        public Builder withTeUsername(String teUsername){
            this.teUsername = teUsername;
            return this;
        }

        public Builder withTePassword(String tePassword){
            this.tePassword = tePassword;
            return this;
        }

        public Builder withSuites(String... suites) {
            this.suites = suites;
            return this;
        }

        public Builder withGroup(String groups) {
            this.groups = groups;
            return this;
        }

        public Builder withParentEventId(String parentEventId) {
            this.parentEventId = parentEventId;
            return this;
        }

        public Builder withParentExecutionId(String parentExecutionId) {
            this.parentExecutionId = parentExecutionId;
            return this;
        }

        public Builder withTestExecutionId(String testExecutionId) {
            this.testExecutionId = testExecutionId;
            return this;
        }

        public Builder withMbHost(String mbHost) {
            this.mbHost = mbHost;
            return this;
        }

        public Builder withMbExchange(String mbExchange) {
            this.mbExchange = mbExchange;
            return this;
        }

        public Builder withMbDomain(String mbDomain) {
            this.mbDomain = mbDomain;
            return this;
        }

        public Builder withLog(String logUrl) {
            this.logUrl = logUrl;
            return this;
        }

        public Builder withRepositoryUrl(String repositoryUrl) {
            this.repositoryUrl = repositoryUrl;
            return this;
        }

        public Builder withScriptUrl(String scriptUrl) {
            this.scriptUrl = scriptUrl;
            return this;
        }

        public Builder withConfigUrl(String configUrl) {
            this.configUrl = configUrl;
            return this;
        }

        public Builder withTimeoutInSeconds(String timeoutInSeconds) {
            this.timeoutInSeconds = timeoutInSeconds;
            return this;
        }

        public Builder withScheduleName(String scheduleName) {
            this.scheduleName = scheduleName;
            return this;
        }

        public Builder withAllureLogDir(String allureLogDirectory) {
            this.allureLogDir = allureLogDirectory;
            return this;
        }

        public Builder withJenkinsWorkspace(String workspaceUri) {
            this.workspaceUri = workspaceUri;
            return this;
        }

        public Builder withAllureVersion(String allureVersion) {
            this.allureVersion = allureVersion;
            return this;
        }

        public Builder withMinTafVersion(String minTafVersion) {
            this.minTafVersion = minTafVersion;
            return this;
        }

        public Builder withUserDefinedGAVs(List<String> userDefinedGAVs) {
            this.userDefinedGAVs = userDefinedGAVs;
            return this;
        }

        public Builder withAdditionalDependencies(List<String> additionalDependencies) {
            this.additionalDependencies = additionalDependencies;
            return this;
        }

        public TestExecution build() {
            return new TestExecution(this);
        }

        public Builder withMiscProperties(String miscProperties) {
            this.miscProperties = miscProperties;
            return this;
        }

        public Builder withEnvPropertyJson(String envPropertiesJson) {
            this.envPropertyJson = envPropertiesJson;
            return this;
        }

        public Builder withManualTestData(ManualTestData manualTestData) {
            this.manualTestData = manualTestData;
            return this;
        }

        public Builder withRuntimeLimitations(TestwareRuntimeLimitations runtimeLimitations) {
            this.runtimeLimitations = runtimeLimitations;
            return this;
        }

        public Builder withSkipTests(String skipTests) {
            this.skipTests = skipTests;
            return this;
        }

        public Builder from(GlobalTeSettings globalTeSettings, ScheduleBuildParameters mainBuildParameters, ExecutorBuildParameters currentExecutionParams) {
            from(globalTeSettings, mainBuildParameters);
            withName(currentExecutionParams.getTestStepName());
            withTestware(currentExecutionParams.getTafTestwareGav());
            withSuites(currentExecutionParams.getTafSuites());
            withGroup(currentExecutionParams.getTafGroups());
            withParentExecutionId(currentExecutionParams.getEiffelScheduleItemExecutionId());
            withTimeoutInSeconds(currentExecutionParams.getTimeoutInSeconds());
            withEnvPropertyJson(currentExecutionParams.getEnvPropertyJson());
            withRuntimeLimitations(currentExecutionParams.getRuntimeLimitations());

            return this;
        }

        public Builder from(GlobalTeSettings globalTeSettings, ScheduleBuildParameters mainBuildParameters, ManualTestsBuildParameters currentExecutionParams) {
            from(globalTeSettings, mainBuildParameters);
            withManualTestData(currentExecutionParams.getManualTestData());

            return this;
        }

        public Builder withGeneralJobExecutionId(String executionId) {
            this.generalExecutionId = executionId;
            return this;
        }

        public Builder withAllureServiceUrl(String allureServiceUrl) {
            this.allureServiceUrl = allureServiceUrl;
            return this;
        }

        private Builder from(GlobalTeSettings globalTeSettings, ScheduleBuildParameters mainBuildParameters) {
            withGeneralJobExecutionId(mainBuildParameters.getExecutionId());
            withScheduleName(mainBuildParameters.getScheduleName());
            withTestExecutionId(mainBuildParameters.getEiffelTestExecutionId());
            withMbHost(globalTeSettings.getMbHostWithPort());
            withMbExchange(globalTeSettings.getMbExchange());
            withMbDomain(globalTeSettings.getReportMbDomainId());
            withRepositoryUrl(mainBuildParameters.getRepositoryUrl());
            withConfigUrl(mainBuildParameters.getConfigUrl());
            withConfigUrl(mainBuildParameters.getConfigUrl());
            withAllureServiceUrl(globalTeSettings.getAllureServiceUrl());
            withAllureLogDir(mainBuildParameters.getAllureLogDir());
            withAllureVersion(globalTeSettings.getAllureVersion());
            withMinTafVersion(mainBuildParameters.getMinTafVersion());
            withUserDefinedGAVs(mainBuildParameters.getUserDefinedGAVs());
            withMiscProperties(mainBuildParameters.getMiscProperties());

            return this;
        }
    }

}
