package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.mbean.TafAgent;
import com.ericsson.cifwk.taf.executor.mbean.TafAgentMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * Starts TAF TE Node process. Should be run as a service.
 */
public class Bootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String[] args) throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.startService();
    }

    public void startService() throws Exception {
        LOGGER.info("Starting TAF TE node");

        startTeAgent();

        LOGGER.info("TAF TE node successfully started");
        sleep();
    }

    private void startTeAgent() throws Exception {
        LOGGER.info("Starting TE Jenkins agent");

        connectToJenkins();
        registerMBean();

        LOGGER.info("TE Jenkins agent successfully started");
    }

    private void sleep() throws InterruptedException {
        while (true) {
            Thread.sleep(60 * 1000);
        }
    }

    private void connectToJenkins() {
        JenkinsNode jenkinsNode = new JenkinsNode();
        Thread thread = new Thread(jenkinsNode);
        thread.setDaemon(true);
        thread.start();
    }

    private void registerMBean() throws Exception {
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = new ObjectName(TafAgentMBean.MBEAN_NAME);
        mbeanServer.registerMBean(new TafAgent(), objectName);
    }

}
