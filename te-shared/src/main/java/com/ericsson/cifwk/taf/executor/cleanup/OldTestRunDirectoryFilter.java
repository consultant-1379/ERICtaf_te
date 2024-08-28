package com.ericsson.cifwk.taf.executor.cleanup;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Filter for old testware runtime directories that have to be deleted now.
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 25/01/2017
 */
public class OldTestRunDirectoryFilter extends OldFileFilter {
    private static final Logger LOGGER = Logger.getLogger(OldTestRunDirectoryFilter.class.getName());
    public OldTestRunDirectoryFilter(int maxLifeTimeInHours) {
        super(maxLifeTimeInHours);
    }

    @Override
    public boolean accept(File dir, String fileName) {
        // Should watch only after subdirectories of test run workspace

        if (isTestRunWorkspace(dir)) {
            File target = new File(dir, fileName);
            if (target.isDirectory()) {
                return ( isOld(target) && isFinishexists(target));
            }
        }
        return false;
    }

}
