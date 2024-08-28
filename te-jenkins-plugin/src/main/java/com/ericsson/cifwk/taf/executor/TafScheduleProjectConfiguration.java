package com.ericsson.cifwk.taf.executor;

import hudson.util.Secret;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.Serializable;

class TafScheduleProjectConfiguration implements Serializable {
    private static final long serialVersionUID = 8029976418672932066L;

    private String reportMbHost;
    private Integer reportMbPort;
    private String reportMbUsername;
    private Secret reportMbPassword;
    private String reportMbExchange;
    private String reportMbDomainId;
    private String reportsHost;
    private String allureServiceUrl;
    private String allureServiceBackendUrl;
    private String localReportsStorage;
    private String reportingScriptsFolder;
    private Integer minExecutorDiskSpaceGB;
    private Integer minExecutorMemorySpaceGB;
    private String allureVersion;
    private Integer deletableFlowsAgeInDays;
    private boolean uploadToOssLogs;
    private TestwareRuntimeLimitations runtimeLimitations = new TestwareRuntimeLimitations();

    private TafScheduleProjectConfiguration() {
    }

    public static TafScheduleProjectConfiguration from(JSONObject jsonForm) {
        JSONArray projectConfig = jsonForm.getJSONArray("taf-schedule-project");

        TafScheduleProjectConfiguration instance = new TafScheduleProjectConfiguration();

        JSONObject reportMbConfig = projectConfig.getJSONObject(0);
        instance.reportMbHost = reportMbConfig.getString("reportMbHost");
        instance.reportMbPort = reportMbConfig.getInt("reportMbPort");
        instance.reportMbUsername = reportMbConfig.getString("reportMbUsername");
        instance.reportMbPassword = Secret.fromString(reportMbConfig.getString("reportMbPassword"));
        instance.reportMbExchange = reportMbConfig.getString("reportMbExchange");
        instance.reportMbDomainId = reportMbConfig.getString("reportMbDomainId");

        JSONObject otherConfig = projectConfig.getJSONObject(1);
        instance.reportsHost = otherConfig.getString("reportsHost").trim();
        instance.allureServiceUrl = otherConfig.getString("allureServiceUrl").trim();
        instance.allureServiceBackendUrl = otherConfig.getString("allureServiceBackendUrl").trim();
        instance.localReportsStorage = otherConfig.getString("localReportsStorage").trim();
        instance.reportingScriptsFolder = otherConfig.getString("reportingScriptsFolder").trim();
        instance.uploadToOssLogs = otherConfig.getBoolean("uploadToOssLogs");
        instance.minExecutorDiskSpaceGB = otherConfig.getInt("minExecutorDiskSpaceGB");
        instance.minExecutorMemorySpaceGB = otherConfig.getInt("minExecutorMemorySpaceGB");
        instance.allureVersion = otherConfig.getString("allureVersion").trim();
        instance.deletableFlowsAgeInDays = otherConfig.getInt("deletableFlowsAgeInDays");

        JSONObject advancedOptionsConfig = jsonForm.getJSONObject("advanced-te-options");
        JSONObject runtimeLimitations = advancedOptionsConfig.getJSONObject("runtimeLimitations");
        if (!runtimeLimitations.isEmpty() && !runtimeLimitations.isNullObject()) {
            Integer maxThreadCountInTestware = runtimeLimitations.optInt("maxThreadCount");
            instance.runtimeLimitations.setMaxThreadCount(maxThreadCountInTestware);
        } else {
            instance.runtimeLimitations = null;
        }

        return instance;
    }

    public String getReportMbHost() {
        return reportMbHost;
    }

    public Integer getReportMbPort() {
        return reportMbPort;
    }

    public String getReportMbUsername() {
        return reportMbUsername;
    }

    public Secret getReportMbPassword() {
        return reportMbPassword;
    }

    public String getReportMbExchange() {
        return reportMbExchange;
    }

    public String getReportMbDomainId() {
        return reportMbDomainId;
    }

    public String getReportsHost() {
        return reportsHost;
    }

    public String getLocalReportsStorage() {
        return localReportsStorage;
    }

    public String getReportingScriptsFolder() {
        return reportingScriptsFolder;
    }

    public Integer getMinExecutorDiskSpaceGB() {
        return minExecutorDiskSpaceGB;
    }

    public Integer getMinExecutorMemorySpaceGB() {
        return minExecutorMemorySpaceGB;
    }

    public Integer getDeletableFlowsAgeInDays() {
        return deletableFlowsAgeInDays;
    }

    public String getAllureVersion() {
        return allureVersion;
    }

    public boolean isUploadToOssLogs() {
        return uploadToOssLogs;
    }

    public String getAllureServiceUrl() {
        return allureServiceUrl;
    }

    public String getAllureServiceBackendUrl() {
        return allureServiceBackendUrl;
    }

    public TestwareRuntimeLimitations getRuntimeLimitations() {
        return runtimeLimitations;
    }
}
