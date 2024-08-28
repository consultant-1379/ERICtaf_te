package com.ericsson.cifwk.taf.executor;

/**
 * Interface to ensure the same getter method signature for global and per-project Scheduler configuration
 */
interface TafScheduleProjectConfigurable {

    String getReportMbHost();

    Integer getReportMbPort();

    String getReportMbUsername();

    String getReportMbPassword();

    String getReportMbExchange();

    String getReportMbDomainId();

    String getReportsHost();

    String getAllureServiceUrl();

    String getAllureServiceBackendUrl();

    String getLocalReportsStorage();

    String getReportingScriptsFolder();

    Integer getMinExecutorDiskSpaceGB();

    Integer getMinExecutorMemorySpaceGB();

    String getAllureVersion();

    Integer getDeletableFlowsAgeInDays();

    boolean isUploadToOssLogs();

    TestwareRuntimeLimitations getRuntimeLimitations();
}
