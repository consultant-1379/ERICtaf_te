package com.ericsson.cifwk.taf.executor.cleanup;

import com.ericsson.cifwk.taf.executor.TAFExecutor;

import java.io.File;
import java.util.logging.Logger;

/**
 * Filter for old tmp files that have to be deleted now.
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 25/01/2017
 */
public class OldTmpFileFilter extends OldFileFilter {

    public OldTmpFileFilter(int maxLifeTimeInHours) {
        super(maxLifeTimeInHours);
    }

    @Override
    public boolean accept(File dir, String fileName) {
        // Don't touch test workspace, another routine takes care of that
        if (!(isTestRunWorkspace(dir))) {
            //Don't delete /tmp/te_maven_runs
            if (!(fileName.equalsIgnoreCase(TAFExecutor.TEST_RUN_WORKSPACE_SUBDIR))) {
                File target = new File(dir, fileName);
                return isOld(target);
            } else
                return false;
        }
        return false;
    }
}
