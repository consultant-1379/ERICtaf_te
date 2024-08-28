package com.ericsson.cifwk.taf.executor.listeners;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 04/01/2016
 */
abstract class AbstractTeListener {

    /**
     * Returns the TE run's working directory
     * @return the directory where current TE test POM file resides and gets executed
     */
    protected File getWorkingDir() {
        Path currentRelativePath = Paths.get("");
        File workingDir = currentRelativePath.toAbsolutePath().toFile();
        return new File(workingDir, "suites-run");
    }

}
