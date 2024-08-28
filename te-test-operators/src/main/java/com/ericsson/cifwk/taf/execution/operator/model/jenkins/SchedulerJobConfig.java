
package com.ericsson.cifwk.taf.execution.operator.model.jenkins;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "com.ericsson.cifwk.taf.executor.TafScheduleProject", strict = false)
public class SchedulerJobConfig {

    @Element(name = "reportMbHost")
    private String reportMbHost;

    @Element(name = "reportMbPort")
    private Integer reportMbPort;

    @Element(name = "reportMbUsername")
    private String reportMbUsername;

    @Element(name = "reportMbPassword")
    private String reportMbPassword;

    @Element(name = "reportMbExchange")
    private String reportMbExchange;

    @Element(name = "reportMbDomainId")
    private String reportMbDomainId;

    @Element(name = "reportsHost")
    private String reportsHost;

    @Element(name = "localReportsStorage")
    private String localReportsStorage;

    @Element(name = "reportingScriptsFolder")
    private String reportingScriptsFolder;

    @Element(name = "deletableFlowsAgeInDays")
    private Integer deletableFlowsAgeInDays;

    @Element(name = "uploadToOssLogs")
    private boolean uploadToOssLogs;

    public String getReportMbHost() {
        return reportMbHost;
    }

    public void setReportMbHost(String reportMbHost) {
        this.reportMbHost = reportMbHost;
    }

    public Integer getReportMbPort() {
        return reportMbPort;
    }

    public void setReportMbPort(Integer reportMbPort) {
        this.reportMbPort = reportMbPort;
    }

    public String getReportMbUsername() {
        return reportMbUsername;
    }

    public void setReportMbUsername(String reportMbUsername) {
        this.reportMbUsername = reportMbUsername;
    }

    public String getReportMbPassword() {
        return reportMbPassword;
    }

    public void setReportMbPassword(String reportMbPassword) {
        this.reportMbPassword = reportMbPassword;
    }

    public String getReportMbExchange() {
        return reportMbExchange;
    }

    public void setReportMbExchange(String reportMbExchange) {
        this.reportMbExchange = reportMbExchange;
    }

    public String getReportMbDomainId() {
        return reportMbDomainId;
    }

    public void setReportMbDomainId(String reportMbDomainId) {
        this.reportMbDomainId = reportMbDomainId;
    }

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

    public Integer getDeletableFlowsAgeInDays() {
        return deletableFlowsAgeInDays;
    }

    public void setDeletableFlowsAgeInDays(Integer deletableFlowsAgeInDays) {
        this.deletableFlowsAgeInDays = deletableFlowsAgeInDays;
    }

    public boolean isUploadToOssLogs() {
        return uploadToOssLogs;
    }

    @Override
    public String toString() {
        return "SchedulerJobConfig{" +
                "reportMbHost='" + reportMbHost + '\'' +
                ", reportMbPort=" + reportMbPort +
                ", reportMbUsername='" + reportMbUsername + '\'' +
                ", reportMbPassword=<HIDDEN>" + // NOSONAR
                ", reportMbExchange='" + reportMbExchange + '\'' +
                ", reportMbDomainId='" + reportMbDomainId + '\'' +
                ", reportsHost='" + reportsHost + '\'' +
                ", localReportsStorage='" + localReportsStorage + '\'' +
                ", reportingScriptsFolder='" + reportingScriptsFolder + '\'' +
                ", uploadToOssLogs='" + uploadToOssLogs + '\'' +
                ", deletableFlowsAgeInDays=" + deletableFlowsAgeInDays +
                '}';
    }
}
