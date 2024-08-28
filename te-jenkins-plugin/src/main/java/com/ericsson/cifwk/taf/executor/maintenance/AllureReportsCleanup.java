package com.ericsson.cifwk.taf.executor.maintenance;

import com.ericsson.cifwk.taf.executor.cleanup.FileCleanupUtils;
import com.ericsson.cifwk.taf.executor.cleanup.OldFileFilter;
import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;
import com.ericsson.cifwk.taf.executor.utils.GlobalTeSettingsProvider;
import hudson.Extension;
import hudson.model.PeriodicWork;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Deletes old Allure reports
 * TODO: remove when Allure service is used
 */
@Extension
public class AllureReportsCleanup extends PeriodicWork {

    private static final Logger LOGGER = Logger.getLogger(AllureReportsCleanup.class.getName());

    private static final int RECURRENCE_PERIOD_IN_MINUTES = 60 * 8;

    private static final int REPORTS_MAX_LIFE_TIME_IN_HOURS = 24;

    @Override
    public long getRecurrencePeriod() {
        // Minutes to millis
        return RECURRENCE_PERIOD_IN_MINUTES * 60 * 1000;
    }

    @Override
    protected void doRun() throws Exception {
        GlobalTeSettings globalTeSettings = GlobalTeSettingsProvider.getInstance().provide();
        if (globalTeSettings.isShouldUploadToOssLogs()) {
            LOGGER.log(Level.INFO,
                    "Looking for old Allure reports (older than {0} hour(-s)) to delete them", REPORTS_MAX_LIFE_TIME_IN_HOURS);
            String localReportsStorage = globalTeSettings.getLocalReportsStorage();
            File reportsStorageDir = new File(localReportsStorage);
            File[] folders = reportsStorageDir.listFiles(new OldFileFilter(REPORTS_MAX_LIFE_TIME_IN_HOURS));
            FileCleanupUtils.deleteFiles(LOGGER, folders);
        }
    }

}
