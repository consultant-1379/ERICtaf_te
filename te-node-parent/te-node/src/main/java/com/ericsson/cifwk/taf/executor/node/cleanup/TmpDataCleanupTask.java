package com.ericsson.cifwk.taf.executor.node.cleanup;

import com.ericsson.cifwk.taf.executor.NodeConfigurationProvider;
import com.ericsson.cifwk.taf.executor.cleanup.OldTmpFileFilter;
import com.ericsson.cifwk.taf.executor.node.WorkspaceDataProvider;

import java.io.File;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 30/01/2017
 */
public class TmpDataCleanupTask extends OldFileCleanupTask {

    @Override
    protected File[] getEligibleFiles() {
        if (!NodeConfigurationProvider.getInstance().shouldCleanTmpFolder()) {
            logger.info("Tmp folder won't be cleaned, according to settings");
            return new File[]{};
        }
        logger.info("Looking for old tmp files to delete");
        String directoryName = WorkspaceDataProvider.getTmpDataRootDirectoryName();
        File directory = new File(directoryName);
        return directory.listFiles(new OldTmpFileFilter(getMaxLifeTimeInHours()));
    }

    @Override
    public String getDescription() {
        return "Tmp data cleanup";
    }
}
