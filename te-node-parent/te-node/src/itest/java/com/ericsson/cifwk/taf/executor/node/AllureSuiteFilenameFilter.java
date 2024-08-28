package com.ericsson.cifwk.taf.executor.node;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 16/03/2016
 */
public class AllureSuiteFilenameFilter implements FilenameFilter {

    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith("suite.xml");
    }

}
