package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.utils.EnhancedTimeLimitedWorker;
import com.ericsson.cifwk.taf.executor.utils.TimeLimitedTask;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import hudson.plugins.swarm.Client;
import hudson.plugins.swarm.Options;
import hudson.plugins.swarm.SwarmClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.jar.Manifest;

import static java.lang.String.format;

public class JenkinsNode implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(JenkinsNode.class);

    @VisibleForTesting
    static final String UNRESOLVED_HOSTNAME_PREFIX = "unresolved_hostname";

    private final NodeConfigurationProvider configurationProvider;

    JenkinsNode() {
        this(NodeConfigurationProvider.getInstance());
    }

    @VisibleForTesting
    JenkinsNode(NodeConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    @Override
    public void run() {
        try {
            connectToJenkins();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @VisibleForTesting
    void connectToJenkins() throws IOException, InterruptedException {
        String implementationVersion = getTeAgentVersion();
        String hostAddress = getCurrentHostAddress();
        LOGGER.info("Current host address is {}", hostAddress);

        Options options = new ClientOptionsBuilder()
                .withHostAddress(hostAddress)
                .withTeVersionDeclared(implementationVersion)
                .build();
        startClient(options);
    }

    private String getTeAgentVersion() throws IOException {
        try (InputStream manifestStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("META-INF/MANIFEST.MF")) {
            Manifest manifest = new Manifest(manifestStream);
            String version = manifest.getMainAttributes().getValue("Plugin-Version");
            LOGGER.info("TE agent version: {}", version);
            return version;
        }
    }

    @VisibleForTesting
    void startClient(Options clientOptions) throws InterruptedException {
        Client client = new Client(clientOptions);
        SwarmClient swarmClient = new SwarmClient(clientOptions);
        LOGGER.info("Connecting to master as: {}", clientOptions.name);

        client.run(swarmClient);
    }

    @VisibleForTesting
    String getCurrentHostAddress() {
        final int delay = configurationProvider.getSelfLookupReattemptDelayInMillis();
        final int timeout = configurationProvider.getSelfLookupReattemptTimeoutInMillis();
        try {
            return getCurrentAddressOrTimeout(delay, timeout / 1000);
        } catch (TimeoutException e) { // NOSONAR
            String randomHostName = createUnknownHostName();
            LOGGER.warn(format("Failed to resolve the current address to report as name to master - " +
                    "using random name '%s'", randomHostName));
            return randomHostName;
        }
    }

    private String getCurrentAddressOrTimeout(int delay, int timeout) throws TimeoutException {
        return TimeLimitedTask.performUntilTimeout(new EnhancedTimeLimitedWorker<String>() {
            @Override
            public String getFailedAttemptMessage() {
                return "Failed to get the current host address";
            }

            @Override
            public Optional<String> doWork() {
                try {
                    InetAddress localHost = getLocalHost();
                    return Optional.of(localHost.getCanonicalHostName());   // Full DNS, e.g. my_te_agent.ericsson.se
                } catch (UnknownHostException e) { // NOSONAR
                    return Optional.absent();
                }
            }
        }, timeout, delay);
    }

    @VisibleForTesting
    InetAddress getLocalHost() throws UnknownHostException {
        return InetAddress.getLocalHost();
    }

    private String createUnknownHostName() {
        return UNRESOLVED_HOSTNAME_PREFIX + "_" + System.currentTimeMillis();
    }

}
