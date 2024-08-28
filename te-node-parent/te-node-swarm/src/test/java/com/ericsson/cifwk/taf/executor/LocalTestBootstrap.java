package com.ericsson.cifwk.taf.executor;

/**
 * Initiates a Jenkins slave process for running local acceptance tests.
 */
public class LocalTestBootstrap extends Bootstrap {

    public static void main(String[] args) throws Exception {
        System.setProperty(NodeSettings.JENKINS_MASTER_URL, "http://localhost:8091/jenkins/");
        System.setProperty(NodeSettings.SHOULD_CLEANUP_TMP, "false");
        new LocalTestBootstrap().startService();
    }

}
