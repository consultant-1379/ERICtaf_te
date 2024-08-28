package com.ericsson.cifwk.taf.executor;

import com.google.common.annotations.VisibleForTesting;
import hudson.plugins.swarm.ModeOptionHandler;
import hudson.plugins.swarm.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 03/08/2017
 */
class ClientOptionsBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientOptionsBuilder.class);

    private final Options clientOptions;

    private final NodeConfigurationProvider configurationProvider;

    private String hostAddress;

    private String teVersion;

    ClientOptionsBuilder() {
        this(new Options(), NodeConfigurationProvider.getInstance());
    }

    @VisibleForTesting
    ClientOptionsBuilder(Options clientOptions, NodeConfigurationProvider configurationProvider) {
        this.clientOptions = clientOptions;
        this.configurationProvider = configurationProvider;
    }

    public ClientOptionsBuilder withTeVersionDeclared(String teVersion) {
        this.teVersion = teVersion;
        return this;
    }

    public ClientOptionsBuilder withHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
        return this;
    }

    public Options build() {
        applyNonCustomizableSettings();
        optionallyDefineMasterUrl();
        defineAgentName();
        defineAgentDescription();
        defineFsRoot();
        defineExecutorCount();
        optionallyDisableUniqueClientId();
        optionallyDisableReconnection();
        optionallyDefineReconnectionInterval();
        optionallyAddAuthenticationDetails();
        return clientOptions;
    }

    private void applyNonCustomizableSettings() {
        checkState(isNotBlank(hostAddress));
        // Instruction to kill the agents with the same name
        clientOptions.deleteExistingClients = true;

        clientOptions.mode = ModeOptionHandler.EXCLUSIVE;
        clientOptions.disableSslVerification = true;

        clientOptions.labels = new ArrayList<>();
        clientOptions.labels.add(TAFExecutor.TAF_NODE_LABEL);
        clientOptions.labels.add(hostAddress.replaceAll("\\.", ""));
    }

    private void optionallyDefineMasterUrl() {
        String masterUrl = configurationProvider.getJenkinsMasterUrl();
        if (masterUrl != null) {
            LOGGER.info("Jenkins master defined: {}", masterUrl);
            clientOptions.master = masterUrl;
        } else {
            LOGGER.info("Jenkins master undefined; will use the default way to locate master");
        }
    }

    @VisibleForTesting
    void defineAgentName() {
        String nodeName = configurationProvider.getJenkinsNodeName();
        clientOptions.name = isNotBlank(nodeName) ? nodeName : hostAddress;
        LOGGER.info("Desired agent name for this process: {}", clientOptions.name);
    }

    private void defineAgentDescription() {
        clientOptions.description = String.format("%s version:%s", configurationProvider.getNodeDescription(), teVersion);
    }

    private void defineFsRoot() {
        clientOptions.remoteFsRoot = new File(configurationProvider.getNodeFsRoot());
    }

    private void defineExecutorCount() {
        clientOptions.executors = configurationProvider.getExecutorCount();
        LOGGER.info("Number of executor threads: {}", clientOptions.executors);
    }

    @VisibleForTesting
    void optionallyDisableUniqueClientId() {
        Boolean disableUniqueClientId = configurationProvider.shouldDisableUniqueClientId();
        if (disableUniqueClientId != null) {
            LOGGER.info("Disable unique client ID: {}", disableUniqueClientId);
            clientOptions.disableClientsUniqueId = disableUniqueClientId;
        }
    }

    @VisibleForTesting
    void optionallyDisableReconnection() {
        Boolean noRetryAfterConnected = configurationProvider.noRetryAfterConnected();
        if (noRetryAfterConnected != null) {
            LOGGER.info("No retry after connected: {}", noRetryAfterConnected);
            clientOptions.noRetryAfterConnected = noRetryAfterConnected;
        }
    }

    private void optionallyDefineReconnectionInterval() {
        Integer reconnectionInterval = configurationProvider.getReconnectionIntervalInSeconds();
        if (reconnectionInterval != null) {
            LOGGER.info("Reconnection interval: {} seconds", reconnectionInterval);
            clientOptions.retryInterval = reconnectionInterval;
        }
    }

    @VisibleForTesting
    void optionallyAddAuthenticationDetails() {
        String username = configurationProvider.getJenkinsUsername();
        if (isNotBlank(username)) {
            String password = configurationProvider.getJenkinsPassword();
            if (isBlank(password)) {
                throw new IllegalStateException(format("Jenkins username is defined ('%s'), but the password is missing", username));
            }
            LOGGER.info("Will authenticate on master as {}", username);
            clientOptions.username = username;
            clientOptions.password = password;
        }
    }

}
