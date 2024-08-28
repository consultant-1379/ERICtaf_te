package com.ericsson.cifwk.taf.executor.healthcheck;

import com.ericsson.cifwk.taf.executor.utils.GlobalTeSettingsProvider;
import com.ericsson.cifwk.taf.executor.utils.JenkinsUtils;
import hudson.model.Node;
import hudson.remoting.VirtualChannel;
import jenkins.model.Jenkins;

import java.util.List;

/**
 *
 */
public class CheckJenkinsNodes extends CheckJenkinsTeHost { 

    private static final String ENABLE_LDAP = System.getenv("ENABLE_LDAP");
    private static final String ENBLE_HEALTHCHECK = System.getenv("HEALTH_CHECK");

    public CheckJenkinsNodes(Jenkins jenkins) {
        super(jenkins, GlobalTeSettingsProvider.getInstance().provide());
    }

    @Override
    public void check(HealthCheckContext context) {
        List<Node> nodes = JenkinsUtils.getTeNodes(jenkins);
        checkResponsibility(context, nodes);
        if (globalTeSettings.isAllureServiceDefined()) {
            checkAllureServiceAvailability(context, nodes);
        } else {
            checkLogMountAccessibility(context, nodes);
        }
        checkNodesAvailableDiskSpace(context, nodes);

        if(null == ENABLE_LDAP && ! "true".equals(ENABLE_LDAP) &&
             (null == ENBLE_HEALTHCHECK && ! "false".equals(ENBLE_HEALTHCHECK))) {
                 checkNodesAvailableMemorySpace(context, nodes);
        }
        checkWorkers(context, nodes);
    }

    private void checkWorkers(HealthCheckContext context, List<Node> nodes) {
        nodes.forEach(node -> {
            String nodeName = node.getDisplayName();
            HealthParam check = new HealthParam("Jenkins Node has workers", nodeName);
            if (node.getNumExecutors() < 1) {
                context.fail(check, "Less than one worker configured on slave node.");
            } else {
                context.ok(check);
            }
        });
    }

    private void checkResponsibility(final HealthCheckContext context, List<Node> nodes) {
        runOnEveryNode(nodes, (String nodeName, VirtualChannel nodeChannel) -> {
                    String rootUrl = jenkins.getRootUrl();
                    HealthParam check = new HealthParam("Jenkins Node is responding", nodeName);
                    TeNodeResponseCheckCallable remoteCheck = new TeNodeResponseCheckCallable(rootUrl, nodeName);
                    runRemoteCheck(remoteCheck, context, nodeChannel, check);
                }
        );
    }

    private void checkLogMountAccessibility(final HealthCheckContext context, List<Node> nodes) {
        final int nodeCount = nodes.size();
        runOnEveryNode(nodes, (String nodeName, VirtualChannel nodeChannel) -> {
                    HealthParam check = new HealthParam("Jenkins Node has access to log mount on master", nodeName);
                    TeNodeLogMountAccessCheckCallable remoteCheck =
                            new TeNodeLogMountAccessCheckCallable(nodeName, globalTeSettings.getLocalReportsStorage(), nodeCount);
                    runRemoteCheck(remoteCheck, context, nodeChannel, check);
                }
        );
    }

    private void checkNodesAvailableDiskSpace(final HealthCheckContext context, List<Node> nodes) {
        runOnEveryNode(nodes, (String nodeName, VirtualChannel nodeChannel) -> {
                    HealthParam check = new HealthParam("Jenkins Node has adequate disk space", nodeName);
                    checkAvailableDiskSpace(context, nodeChannel, nodeName, check);
                }
        );
    }

    private void checkNodesAvailableMemorySpace(final HealthCheckContext context, List<Node> nodes) {
        runOnEveryNode(nodes, (String nodeName, VirtualChannel nodeChannel) -> {
                    HealthParam check = new HealthParam("Jenkins Node has adequate free memory ", nodeName);
                    checkNodesAvailableMemorySpace(context, nodeChannel, nodeName, check);
                }
        );
    }

    private void checkAllureServiceAvailability(final HealthCheckContext context, List<Node> nodes) {
        runOnEveryNode(nodes, (String nodeName, VirtualChannel nodeChannel) -> {
                    HealthParam check = new HealthParam("Allure service is accessible from the node", nodeName);
                    checkAllureServiceAvailability(context, nodeChannel, nodeName, check);
                }
        );
    }

    private void runOnEveryNode(List<Node> nodes, RunnableOnNode runnable) {
        nodes.forEach((node) -> runnable.run(node.getDisplayName(), node.getChannel()));
    }

    interface RunnableOnNode {
        void run(String nodeName, VirtualChannel nodeChannel);
    }
}
