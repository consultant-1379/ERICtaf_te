package com.ericsson.cifwk.taf.executor.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.xml.XmlSuite;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.String.format;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 14/12/2015
 */
public class TeSuiteListener extends AbstractTeListener implements ISuiteListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeSuiteListener.class);

    @Override
    public void onStart(ISuite suite) {
        createSuiteMarkerFile(suite, "start");
    }

    @Override
    public void onFinish(ISuite suite) {
        createSuiteMarkerFile(suite, "finish");
    }

    private void createSuiteMarkerFile(ISuite suite, String phase) {
        File outputDir = getWorkingDir();
        String suiteFileName = getSuiteFileName(suite);
        File file = new File(outputDir, suiteFileName + "." + phase);
        try {
            file.createNewFile();
            LOGGER.info("Created suite {} file at {}", phase, file.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error(format("Failed to create %s file for %s", phase, suite.getName()), e);
        }
    }

    private String getSuiteFileName(ISuite suite) {
        XmlSuite xmlSuite = suite.getXmlSuite();
        Path path = Paths.get(xmlSuite.getFileName());
        return path.getFileName().toString();
    }

}
