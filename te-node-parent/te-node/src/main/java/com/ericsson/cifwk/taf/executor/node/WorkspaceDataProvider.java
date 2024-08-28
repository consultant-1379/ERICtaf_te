package com.ericsson.cifwk.taf.executor.node;

import com.ericsson.cifwk.taf.executor.TAFExecutor;

import java.nio.file.Paths;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 25/01/2017
 */
public class WorkspaceDataProvider {

    public static String getWorkspaceDirectoryName() {
        return Paths.get(getTmpDataRootDirectoryName(), TAFExecutor.TEST_RUN_WORKSPACE_SUBDIR).toString();
    }

    public static String getTmpDataRootDirectoryName() {
        return System.getProperty("java.io.tmpdir");
    }

}
