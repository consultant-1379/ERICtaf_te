package com.ericsson.cifwk.taf.executor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ericsson.cifwk.taf.executor.NodeSettings.DESCRIPTION;
import static com.ericsson.cifwk.taf.executor.NodeSettings.DISABLE_UNIQUE_CLIENT_ID;
import static com.ericsson.cifwk.taf.executor.NodeSettings.EXECUTOR_COUNT;
import static com.ericsson.cifwk.taf.executor.NodeSettings.FS_ROOT;
import static com.ericsson.cifwk.taf.executor.NodeSettings.JENKINS_MASTER_URL;
import static com.ericsson.cifwk.taf.executor.NodeSettings.JENKINS_NODE_NAME;
import static com.ericsson.cifwk.taf.executor.NodeSettings.JENKINS_PASSWORD;
import static com.ericsson.cifwk.taf.executor.NodeSettings.JENKINS_USERNAME;
import static com.ericsson.cifwk.taf.executor.NodeSettings.MAX_TEST_DATA_LIFETIME_IN_SECONDS;
import static com.ericsson.cifwk.taf.executor.NodeSettings.NO_RETRY_AFTER_CONNECTED;
import static com.ericsson.cifwk.taf.executor.NodeSettings.RECONNECTION_INTERVAL;
import static com.ericsson.cifwk.taf.executor.NodeSettings.SELF_LOOKUP_REATTEMPT_DELAY;
import static com.ericsson.cifwk.taf.executor.NodeSettings.SELF_LOOKUP_REATTEMPT_TIMEOUT;
import static com.ericsson.cifwk.taf.executor.NodeSettings.SHOULD_CLEANUP_TMP;
import static com.ericsson.cifwk.taf.executor.NodeSettings.TAF_MANUAL_TESTS_PLUGIN_VERSION;
import static com.ericsson.cifwk.taf.executor.NodeSettings.TAF_MAVEN_PLUGIN_VERSION;
import static com.ericsson.cifwk.taf.executor.NodeSettings.TAF_SUREFIRE_PROVIDER_VERSION;
import static com.ericsson.cifwk.taf.executor.NodeSettings.TE_VERSION;
import static com.ericsson.cifwk.taf.executor.NodeSettings.TIMEOUT_TIMER_POLL_IN_SECONDS;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 21/08/2017
 */
public class NodeConfigurationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeConfigurationProvider.class);

    private static NodeConfigurationProvider INSTANCE = new NodeConfigurationProvider();

    private Configuration configuration;

    NodeConfigurationProvider() {
        this(loadConfiguration());
    }

    @VisibleForTesting
    NodeConfigurationProvider(Configuration configuration) {
        this.configuration = configuration;
    }

    public static NodeConfigurationProvider getInstance() {
        return INSTANCE;
    }

    private static Configuration loadConfiguration() {
        LOGGER.info("Loading node configuration");

        CompositeConfiguration config = new CompositeConfiguration();
        config.addConfiguration(new SystemConfiguration());
        try {
            config.addConfiguration(new PropertiesConfiguration("settings.properties"));
        } catch (ConfigurationException e) {
            throw Throwables.propagate(e);
        }

        LOGGER.info("Node configuration successfully loaded");
        return config;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getTafMavenPluginVersion() {
        return configuration.getString(TAF_MAVEN_PLUGIN_VERSION);
    }

    public String getTafSurefireProviderVersion() {
        return configuration.getString(TAF_SUREFIRE_PROVIDER_VERSION);
    }

    public String getManualTestsPluginVersion() {
        return configuration.getString(TAF_MANUAL_TESTS_PLUGIN_VERSION);
    }

    public String getTeVersion() {
        return configuration.getString(TE_VERSION);
    }

    public String getJenkinsMasterUrl() {
        return configuration.getString(JENKINS_MASTER_URL);
    }

    public String getJenkinsNodeName() {
        return configuration.getString(JENKINS_NODE_NAME);
    }

    public String getNodeDescription() {
        return configuration.getString(DESCRIPTION);
    }

    public String getNodeFsRoot() {
        return configuration.getString(FS_ROOT);
    }

    public String getJenkinsUsername() {
        return configuration.getString(JENKINS_USERNAME);
    }

    public String getJenkinsPassword() {
        return configuration.getString(JENKINS_PASSWORD);
    }

    public boolean shouldCleanTmpFolder() {
        return configuration.getBoolean(SHOULD_CLEANUP_TMP, true);
    }

    public int getTimeoutTimerPollInSeconds() {
        return configuration.getInt(TIMEOUT_TIMER_POLL_IN_SECONDS, 5);
    }

    public short getExecutorCount() {
        return configuration.getShort(EXECUTOR_COUNT);
    }

    public int getSelfLookupReattemptDelayInMillis() {
        return configuration.getInt(SELF_LOOKUP_REATTEMPT_DELAY, 10000);
    }

    public int getSelfLookupReattemptTimeoutInMillis() {
        return configuration.getInt(SELF_LOOKUP_REATTEMPT_TIMEOUT, 65000);
    }

    public int getMaxFileLifetimeInSeconds() {
        return configuration.getInt(MAX_TEST_DATA_LIFETIME_IN_SECONDS, 86400);
    }

    public Boolean shouldDisableUniqueClientId() {
        return configuration.getBoolean(DISABLE_UNIQUE_CLIENT_ID, null);
    }

    public Boolean noRetryAfterConnected() {
        return configuration.getBoolean(NO_RETRY_AFTER_CONNECTED, null);
    }

    public Integer getReconnectionIntervalInSeconds() {
        return configuration.getInteger(RECONNECTION_INTERVAL, null);
    }
}
