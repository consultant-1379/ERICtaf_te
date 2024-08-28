package com.ericsson.cifwk.taf.executor.model;

import com.ericsson.cifwk.taf.executor.TestwareRuntimeLimitations;
import com.ericsson.duraci.datawrappers.MessageBus;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Common Test Executor settings (taken from Jenkins project configuration)
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 22/01/2016
 */
public class GlobalTeSettings {

    private String reportsHost;
    private String allureServiceUrl;
    private String allureServiceBackendUrl;
    private String localReportsStorage;
    private String reportingScriptsFolder;
    private boolean shouldUploadToOssLogs;
    private String allureVersion;
    private MessageBus messageBus;

    private String mbHostWithPort;
    private String mbExchange;
    private String reportMbDomainId;
    private TestwareRuntimeLimitations defaultRuntimeLimitations;
    private Integer minExecutorDiskSpaceGB;
    private Integer minExecutorMemorySpaceGB;
    private Map<String, String> agentJobsMap;

    public String getReportsHost() {
        return reportsHost;
    }

    public void setReportsHost(String reportsHost) {
        this.reportsHost = reportsHost;
    }

    public String getLocalReportsStorage() {
        return localReportsStorage;
    }

    public void setLocalReportsStorage(String localReportsStorage) {
        this.localReportsStorage = localReportsStorage;
    }

    public String getReportingScriptsFolder() {
        return reportingScriptsFolder;
    }

    public void setReportingScriptsFolder(String reportingScriptsFolder) {
        this.reportingScriptsFolder = reportingScriptsFolder;
    }

    public String getAllureVersion() {
        return allureVersion;
    }

    public void setAllureVersion(String allureVersion) {
        this.allureVersion = allureVersion;
    }

    public String getMbHostWithPort() {
        return mbHostWithPort;
    }

    public void setMbHostWithPort(String mbHostWithPort) {
        this.mbHostWithPort = mbHostWithPort;
    }

    public String getMbExchange() {
        return mbExchange;
    }

    public void setMbExchange(String mbExchange) {
        this.mbExchange = mbExchange;
    }

    public String getReportMbDomainId() {
        return reportMbDomainId;
    }

    public void setReportMbDomainId(String reportMbDomainId) {
        this.reportMbDomainId = reportMbDomainId;
    }

    public MessageBus getMessageBus() {
        return messageBus;
    }

    public void setMessageBus(MessageBus messageBus) {
        this.messageBus = messageBus;
    }

    public Map<String, String> getAgentJobsMap() {
        return agentJobsMap;
    }

    public void setAgentJobsMap(Map<String, String> agentJobsMap) {
        this.agentJobsMap = agentJobsMap;
    }

    public boolean isShouldUploadToOssLogs() {
        return shouldUploadToOssLogs;
    }

    public void setShouldUploadToOssLogs(boolean shouldUploadToOssLogs) {
        this.shouldUploadToOssLogs = shouldUploadToOssLogs;
    }

    public TestwareRuntimeLimitations getDefaultRuntimeLimitations() {
        return defaultRuntimeLimitations;
    }

    public void setDefaultRuntimeLimitations(TestwareRuntimeLimitations defaultRuntimeLimitations) {
        this.defaultRuntimeLimitations = defaultRuntimeLimitations;
    }

    public void setAllureServiceUrl(String allureServiceUrl) {
        this.allureServiceUrl = allureServiceUrl;
    }

    public String getAllureServiceUrl() {
        return allureServiceUrl;
    }

    public void setAllureServiceBackendUrl(String allureServiceBackendUrl) {
        this.allureServiceBackendUrl = allureServiceBackendUrl;
    }

    public String getAllureServiceBackedUrl() {
        return allureServiceBackendUrl;
    }

    public boolean isAllureServiceDefined() {
        return isNotBlank(allureServiceUrl);
    }

    public void setMinExecutorDiskSpaceGB(Integer minExecutorDiskSpaceGB) {
        this.minExecutorDiskSpaceGB = minExecutorDiskSpaceGB;
    }

    public Integer getMinExecutorDiskSpaceGB() {
        return minExecutorDiskSpaceGB;
    }

    public void setMinExecutorMemorySpaceGB(Integer minExecutorMemorySpaceGB) {
        this.minExecutorMemorySpaceGB = minExecutorMemorySpaceGB;
    }

    public Integer getMinExecutorMemorySpaceGB() {
        return minExecutorMemorySpaceGB;
    }

}
