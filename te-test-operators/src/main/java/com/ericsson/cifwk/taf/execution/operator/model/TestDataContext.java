package com.ericsson.cifwk.taf.execution.operator.model;

import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;

import java.util.Properties;

public class TestDataContext {

    private static final TafConfiguration TAF_CONFIG = TafConfigurationProvider.provide();

    // If test needs to put something
    private static final InheritableThreadLocal<Properties> RUNTIME_PROPERTIES = new InheritableThreadLocal<Properties>() {
        @Override
        protected Properties initialValue() {
            return new Properties();
        }
    };

    private TestDataContext() {}

    // References to names in property files
    private static final String PLUGIN_VERSION_KEY = "pluginVersion";
    private static final String NEXUS_URI_KEY = "nexusURI";
    private static final String ALLURE_REPORTS_HTTP_BASE_KEY = "reportsHost";
    private static final String REPORTING_MB_EXCHANGE_KEY = "mbReportExchange";
    private static final String REPORTING_MB_DOMAIN_KEY = "mbReportDomainId";
    private static final String LOG_STORAGE_KEY = "localReportsStorage";
    private static final String REPORTING_SCRIPTS_FOLDER_KEY = "reportingScriptsFolder";
    private static final String MIN_EXECUTOR_DISK_SPACE_GB_KEY = "minExecutorDiskSpaceGB";
    private static final String MIN_EXECUTOR_MEMORY_SPACE_GB_KEY = "minExecutorMemorySpaceGB";
    private static final String ALLURE_VERSION_KEY = "allureVersion";
    private static final String ALLURE_SERVICE_URL_KEY = "allureServiceUrl";
    private static final String ALLURE_SERVICE_BACKEND_URL_KEY = "allureServiceBackendUrl";

    // References to names in host JSON files
    private static final String TE_MASTER_HOST_NAME = "te_master";
    private static final String TE_SLAVE_HOST_NAME = "te_slave";
    private static final String REPORTING_MB_HOST_NAME = "reporting_message_bus";

    public static Host getTeMasterHost() {
        return DataHandler.getHostByName(TE_MASTER_HOST_NAME);
    }

    public static Host getTeSlaveHost() {
        return DataHandler.getHostByName(TE_SLAVE_HOST_NAME);
    }

    public static Host getReportingMbHost() {
        return mbHost();
    }

    public static String getReportingMbHostAddress() {
        return mbHost().getIp();
    }

    public static Integer getReportingMbPort() {
        return mbHost().getPort(Ports.AMQP);
    }

    public static String getReportingMbUsername() {
        User mbHostUser = mbHostUser();
        return mbHostUser.getUsername();
    }

    public static String getReportingMbPassword() {
        User mbHostUser = mbHostUser();
        return mbHostUser.getPassword();
    }

    private static Host mbHost() {
        //TODO: replace with new DH (TafDataHandler) when released
        return DataHandler.getHostByName(REPORTING_MB_HOST_NAME);
    }

    private static User mbHostUser() {
        return mbHost().getUsers(UserType.ADMIN).get(0);
    }

    public static String getLocalReportsStorage() {
        return getConfigProperty(LOG_STORAGE_KEY);
    }

    public static String getReportingScriptsFolder() {
        return getConfigProperty(REPORTING_SCRIPTS_FOLDER_KEY);
    }

    public static String getMinExecutorDiskSpaceGb() {
        return getConfigProperty(MIN_EXECUTOR_DISK_SPACE_GB_KEY);
    }

    public static String getMinExecutorMemorySpaceGb() {
        return getConfigProperty(MIN_EXECUTOR_MEMORY_SPACE_GB_KEY);
    }

    public static String getPluginVersion() {
        return getConfigProperty(PLUGIN_VERSION_KEY);
    }

    public static String getAllureVersion() {
        return getConfigProperty(ALLURE_VERSION_KEY);
    }

    public static String getAllureServiceUrl() {
        return getConfigProperty(ALLURE_SERVICE_URL_KEY);
    }

    public static String getAllureReportsHttpBase() {
        return getConfigProperty(ALLURE_REPORTS_HTTP_BASE_KEY);
    }

    public static String getReportingMbExchange() {
        return getConfigProperty(REPORTING_MB_EXCHANGE_KEY);
    }

    public static String getReportingMbDomain() {
        return getConfigProperty(REPORTING_MB_DOMAIN_KEY);
    }

    public static String getNexusUri() {
        return getConfigProperty(NEXUS_URI_KEY);
    }

    public static String getConfigProperty(String name) {
        return TAF_CONFIG.getString(name);
    }

    public static Properties getAllConfigProperties() {
        return TAF_CONFIG.getProperties();
    }

    public static void setRuntimeProperty(String name, String value) {
        getThreadLocalRuntimeProperties().put(name, value);
    }

    public static String getRuntimeProperty(String name) {
        return getThreadLocalRuntimeProperties().getProperty(name);
    }

    private static Properties getThreadLocalRuntimeProperties() {
        return RUNTIME_PROPERTIES.get();
    }

    public static String defaultExecutorJobConfigFilePath() {
        return "data/templates/executor_job.xml.ftl";
    }

    public static String defaultSchedulerJobConfigFilePath() {
        return "data/templates/scheduler_job.xml.ftl";
    }

}
