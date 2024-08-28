package com.ericsson.cifwk.taf.executor.node.cleanup;

import com.ericsson.cifwk.taf.executor.NodeConfigurationProvider;
import com.ericsson.cifwk.taf.executor.cleanup.FileCleanupUtils;
import com.ericsson.cifwk.taf.executor.cleanup.TeNodeCleanupTask;

import java.io.File;
import java.util.logging.Logger;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 30/01/2017
 */
abstract class OldFileCleanupTask implements TeNodeCleanupTask {

    protected final Logger logger = Logger.getLogger(this.getClass().getName());

    private final int maxLifeTimeInHours;

    public OldFileCleanupTask() {
        NodeConfigurationProvider configurationProvider = NodeConfigurationProvider.getInstance();
        int maxLifeTimeInHours = configurationProvider.getMaxFileLifetimeInSeconds() / 3600;
        this.maxLifeTimeInHours = maxLifeTimeInHours;
    }

    @Override
    public void doCleanup() {
        File[] dirsToDelete = getEligibleFiles();
        FileCleanupUtils.deleteFiles(logger, dirsToDelete);
    }

    protected abstract File[] getEligibleFiles();

    int getMaxLifeTimeInHours() {
        return maxLifeTimeInHours;
    }
}
