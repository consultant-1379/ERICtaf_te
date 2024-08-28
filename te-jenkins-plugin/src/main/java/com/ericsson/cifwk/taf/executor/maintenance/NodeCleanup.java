package com.ericsson.cifwk.taf.executor.maintenance;

import hudson.Extension;
import hudson.model.Node;
import hudson.model.PeriodicWork;
import hudson.remoting.VirtualChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static com.ericsson.cifwk.taf.executor.utils.JenkinsUtils.getJenkinsInstance;
import static com.ericsson.cifwk.taf.executor.utils.JenkinsUtils.getTeNodes;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

/**
 * Schedules data cleanup on each Jenkins agent node.
 *
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 21/08/2017
 */
@Extension
public class NodeCleanup extends PeriodicWork {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeCleanup.class);

    private static final int RECURRENCE_PERIOD_IN_SECONDS = 300;

    @Override
    public long getInitialDelay() {
        return 1000;
    }

    @Override
    public long getRecurrencePeriod() {
        return RECURRENCE_PERIOD_IN_SECONDS * 1000;
    }

    @Override
    protected void doRun() throws Exception {
        List<Node> nodes = getTeNodes(getJenkinsInstance());
        if (!nodes.isEmpty()) {
            List<String> nodeNames = nodes.stream().map(Node::getNodeName).collect(toList());
            LOGGER.info("Running TE node maintenance tasks on current nodes {}", nodeNames);
            runOnEveryNode(nodes);
        } else {
            LOGGER.info("No TE nodes currently attached");
        }
    }

    private void runOnEveryNode(List<Node> nodes) {
        nodes.forEach(node -> {
            VirtualChannel channel = node.getChannel();
            if (channel == null) {
                LOGGER.warn("Cannot schedule cleanup on node {} because the remote channel is unavailable", node.getNodeName());
                return;
            }
            try {
                channel.callAsync(new NodeCleanupCallable());
            } catch (IOException e) {
                String nodeName = node.getNodeName();
                LOGGER.error(format("Error invoking cleanup on node %s", nodeName), e);
            }
        });
    }

}
