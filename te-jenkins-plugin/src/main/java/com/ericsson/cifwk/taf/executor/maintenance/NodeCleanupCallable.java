package com.ericsson.cifwk.taf.executor.maintenance;

import com.ericsson.cifwk.taf.executor.cleanup.TeNodeCleanupTask;
import hudson.remoting.Callable;
import org.jenkinsci.remoting.RoleChecker;

import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Invokes all implementations of {@link TeNodeCleanupTask} on the TE node.
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 22/08/2017
 */
class NodeCleanupCallable implements Callable<Void, Exception> {

    // Jenkins agent is using JUL, and we should too, otherwise log is not getting to the console
    private static final Logger LOGGER = Logger.getLogger("Node cleanup");

    @Override
    public Void call() throws Exception {
        LOGGER.info("Node cleanup requested by Jenkins master");
        ServiceLoader<TeNodeCleanupTask> nodeCleanupTasks = ServiceLoader.load(TeNodeCleanupTask.class);
        List<TeNodeCleanupTask> nodeCleanupTaskList = newArrayList(nodeCleanupTasks);
        nodeCleanupTaskList.parallelStream().forEach(task -> {
            LOGGER.log(Level.INFO, "Running task \"{0}\"", task.getDescription());
            task.doCleanup();
        });
        return null;
    }

    @Override
    public void checkRoles(RoleChecker checker) throws SecurityException {
    }
}
