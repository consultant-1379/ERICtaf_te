package com.ericsson.cifwk.taf.executor.maven;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class PomValues {

    private GAV testware;
    private String suites;
    private String groups;

    private String generalExecutionId;
    private String parentEventId;
    private String parentExecutionId;
    private String testExecutionId;
    private String mbHost;
    private String mbDomain;
    private String mbExchange;
    private String logUrl;
    private String repositoryUrl;
    private String configUrl;
    private String allureLogDir;
    private String allureServiceUrl;
    private String allureVersion;
    private List<GAV> additionalDependencies;
    private Map<String,String> systemProperties = Maps.newHashMap();
    private String minTafVersion;
    private String skipTests;
    private List<GAV> userDefinedPOMs;
    private List<GAV> userDefinedBOMs;

    public GAV getTestware() {
        return testware;
    }

    public void setTestware(GAV testware) {
        this.testware = testware;
    }

    public List<GAV> getAdditionalDependencies() {
        return additionalDependencies;
    }

    public void setAdditionalDependencies(List<GAV> additionalDependencies) {
        this.additionalDependencies = additionalDependencies;
    }

    public String getGeneralExecutionId() {
        return generalExecutionId;
    }

    public void setGeneralExecutionId(String generalExecutionId) {
        this.generalExecutionId = generalExecutionId;
    }

    public String getSuites() {
        return suites;
    }

    public void setSuites(String suites) {
        this.suites = suites;
    }

    public String getGroups() {
        return groups;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }

    public String getParentEventId() {
        return parentEventId;
    }

    public void setParentEventId(String parentEventId) {
        this.parentEventId = parentEventId;
    }

    public String getParentExecutionId() {
        return parentExecutionId;
    }

    public void setParentExecutionId(String parentExecutionId) {
        this.parentExecutionId = parentExecutionId;
    }

    public String getTestExecutionId() {
        return testExecutionId;
    }

    public void setTestExecutionId(String testExecutionId) {
        this.testExecutionId = testExecutionId;
    }

    public String getAllureServiceUrl() {
        return allureServiceUrl;
    }

    public void setAllureServiceUrl(String allureServiceUrl) {
        this.allureServiceUrl = allureServiceUrl;
    }

    public String getMbHost() {
        return mbHost;
    }

    public void setMbHost(String mbHost) {
        this.mbHost = mbHost;
    }

    public String getMbDomain() {
        return mbDomain;
    }

    public void setMbDomain(String mbDomain) {
        this.mbDomain = mbDomain;
    }

    public String getMbExchange() {
        return mbExchange;
    }

    public void setMbExchange(String mbExchange) {
        this.mbExchange = mbExchange;
    }

    public String getLogUrl() {
        return logUrl;
    }

    public void setLogUrl(String logUrl) {
        this.logUrl = logUrl;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public void setConfigUrl(String configUrl) {
        this.configUrl = configUrl;
    }

    public String getConfigUrl() {
        return configUrl;
    }

    public String getAllureLogDir() {
        return allureLogDir;
    }

    public void setAllureLogDir(String allureLogDir) {
        this.allureLogDir = allureLogDir;
    }

    public String getAllureVersion() {
        return allureVersion;
    }

    public void setAllureVersion(String allureVersion) {
        this.allureVersion = allureVersion;
    }

    public String getMinTafVersion() {
        return minTafVersion;
    }

    public void setMinTafVersion(String minTafVersion) {
        this.minTafVersion = minTafVersion;
    }

    public List<GAV> getUserDefinedBOMs() {
        return userDefinedBOMs;
    }

    public void setUserDefinedBOMs(List<GAV> userDefinedBOMs) {
        this.userDefinedBOMs = userDefinedBOMs;
    }

    public List<GAV> getUserDefinedPOMs() {
        return userDefinedPOMs;
    }

    public void setUserDefinedPOMs(List<GAV> userDefinedPOMs) {
        this.userDefinedPOMs = userDefinedPOMs;
    }

    public Map<String, String> getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(Map<String, String> systemProperties) {
        Preconditions.checkArgument(systemProperties != null);
        this.systemProperties = systemProperties;
    }

    public String getSkipTests() {
        return skipTests;
    }

    public void setSkipTests(final String skipTests) {
        this.skipTests = skipTests;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("generalExecutionId", generalExecutionId)
                .add("testware", testware)
                .add("suites", suites)
                .add("groups", groups)
                .add("parentEventId", parentEventId)
                .add("parentExecutionId", parentExecutionId)
                .add("mbHost", mbHost)
                .add("mbDomain", mbDomain)
                .add("mbExchange", mbExchange)
                .add("logUrl", logUrl)
                .add("repositoryUrl", repositoryUrl)
                .add("configUrl", configUrl)
                .add("allureLogDir", allureLogDir)
                .add("allureServiceUrl", allureServiceUrl)
                .add("allureVersion", allureVersion)
                .add("additionalDependencies", additionalDependencies)
                .add("systemProperties", systemProperties)
                .add("minTafVersion", minTafVersion)
                .add("userDefinedBOMs", userDefinedBOMs)
                .add("userDefinedPOMs", userDefinedPOMs)
                .toString();
    }
}
