package com.ericsson.cifwk.taf.executor;

/**
 *
 */
public final class NodeSettings {

    private NodeSettings() {
    }

    /**
     * References Jenkins master environment (e.g., http://myjenkins:8091/jenkins/).
     * Optional. If not set, a broadcast discovery will be used.
     */
    public static final String JENKINS_MASTER_URL = "taf.executor.node.master";

    /**
     * Desired node name. Swarm plugin may take it as is or add postfix to it.
     * Optional. If not set, the current host name will be used.
     */
    public static final String JENKINS_NODE_NAME = "taf.executor.node.name";

    /**
     * If defined, passed as a value for <code>hudson.plugins.swarm.Options#disableClientsUniqueId</code>.
     * If <code>true</code>, adds unique hash to the end of the node name.
     * Optional. If not set, the hash will be added.
     */
    public static final String DISABLE_UNIQUE_CLIENT_ID = "taf.executor.node.disableUniqueId";

    /**
     * If defined, passed as a value for <code>hudson.plugins.swarm.Options#noRetryAfterConnected</code>.
     * If <code>true</code>, slave doesn't try to reconnect if disconnected.
     * Optional. If not set, default behaviour will apply.
     */
    public static final String NO_RETRY_AFTER_CONNECTED = "taf.executor.node.noRetryAfterConnected";

    /**
     * If defined, passed as a value for <code>hudson.plugins.swarm.Options#retryInterval</code>.
     * Time to wait before retry in seconds.
     * Optional.
     */
    public static final String RECONNECTION_INTERVAL = "taf.executor.node.reconnectionIntervalInSeconds";

    /**
     * Jenkins user name for authentication on master. If defined, password will be required as well.
     * Optional. If not set, no authentication will be performed.
     */
    public static final String JENKINS_USERNAME = "taf.executor.jenkins.username";

    /**
     * Jenkins user password.
     * Optional if Jenkins username is not defined.
     */
    public static final String JENKINS_PASSWORD = "taf.executor.jenkins.password";

    /**
     * Description of the test node
     */
    public static final String DESCRIPTION = "taf.executor.node.description";

    /**
     * Jenkins slave file system root
     */
    public static final String FS_ROOT = "taf.executor.node.fsRoot";

    /**
     * Number of executor threads this Jenkins slave will have
     */
    public static final String EXECUTOR_COUNT = "taf.executor.node.executors";

    /**
     * Delay during host name lookup attempts, in milliseconds
     */
    public static final String SELF_LOOKUP_REATTEMPT_DELAY = "taf.executor.name_lookup.reattempt.delayInMillis";

    /**
     * Timeout for host name lookup attempts, in milliseconds
     */
    public static final String SELF_LOOKUP_REATTEMPT_TIMEOUT = "taf.executor.name_lookup.reattempt.timeoutInMillis";

    /**
     * Should tmp folder be cleaned as a part of maintenance?
     */
    public static final String SHOULD_CLEANUP_TMP = "taf_te.node.cleanup.clean_tmp_folder";

    public static final String TAF_MAVEN_PLUGIN_VERSION = "taf.maven_plugin.version";

    public static final String TAF_SUREFIRE_PROVIDER_VERSION = "taf.surefire_provider.version";

    public static final String TAF_MANUAL_TESTS_PLUGIN_VERSION = "taf.manual_tests_plugin.version";

    public static final String MAX_TEST_DATA_LIFETIME_IN_SECONDS = "taf_te.node.cleanup.max_runtime_files_lifetime_in_seconds";

    public static final String TIMEOUT_TIMER_POLL_IN_SECONDS = "taf.schedule_node.timeout_timer.poll_every_seconds";

    public static final String TE_VERSION = "te_version";

}
