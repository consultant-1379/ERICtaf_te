package com.ericsson.cifwk.taf.executor.node.cleanup;

import com.ericsson.cifwk.taf.executor.cleanup.OldTestRunDirectoryFilter;
import com.ericsson.cifwk.taf.executor.node.WorkspaceDataProvider;

import java.io.File;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 24/01/2017
 */
public class TestDataCleanupTask extends OldFileCleanupTask {

    @Override
    protected File[] getEligibleFiles() {
        logger.info("Looking for old test data files to delete");
        String workspaceDirectoryName = WorkspaceDataProvider.getWorkspaceDirectoryName();
        File workspaceDirectory = new File(workspaceDirectoryName);
        return workspaceDirectory.listFiles(new OldTestRunDirectoryFilter(getMaxLifeTimeInHours()));
    }

    @Override
    public String getDescription() {
        return "Test data cleanup";
    }
}
