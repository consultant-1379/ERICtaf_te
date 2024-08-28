package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.model.ScheduleBuildParameters;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TafScheduleBuilderITest {

    private String tmpDir = System.getProperty("java.io.tmpdir");
    private TafScheduleBuilder unit = new TafScheduleBuilder();
    private PrintStream logger = mock(PrintStream.class);
    private ScheduleBuildParameters buildParameters = new ScheduleBuildParameters();

    @Before
    public void setUp() {
        unit = spy(unit);
        buildParameters.setAllureLogDir(tmpDir);
        doReturn(buildParameters).when(unit).getBuildParameters(any(TafScheduleBuild.class));
    }

    @Test
    public void writeTriggerDetailsToAllureLogs() throws Exception {
        final String isoVersion = "2.4.13";
        final String jobName = "X_TRIGGER#3";
        final String jobUrl = "https://fem114-eiffel004:8443/job/Deployment-5221_TAF_Entry_Loop/";

        Properties properties = new Properties();
        properties.setProperty("ISO version", isoVersion);
        properties.setProperty("Jenkins job name", jobName);
        properties.setProperty("Link to Jenkins job", jobUrl);
        Assert.assertTrue(unit.writeTriggerDetailsToAllureLogs(properties, logger, buildParameters));

        Path pathToPropertyFile = Paths.get(tmpDir, TafScheduleBuilder.TP_ENVIRONMENT_PROPERTIES_FILE_NAME);
        try {
            File propertyFile = pathToPropertyFile.toFile();
            Assert.assertTrue(propertyFile.exists());
            Properties retrievedProps = new Properties();
            InputStream inStream = Files.newInputStream(pathToPropertyFile);
            retrievedProps.load(inStream);
            inStream.close();
            Assert.assertEquals(isoVersion, retrievedProps.getProperty("ISO version"));
            Assert.assertEquals(jobName, retrievedProps.getProperty("Jenkins job name"));
            Assert.assertEquals(jobUrl, retrievedProps.getProperty("Link to Jenkins job"));
        } finally {
            Files.deleteIfExists(pathToPropertyFile);
        }
    }

    @Test
    public void writeTriggerDetailsToAllureLogsRetryMechanism() {
        Path path = Paths.get(tmpDir + "/" + TafScheduleBuilder.TP_ENVIRONMENT_PROPERTIES_FILE_NAME);
        Properties properties = new Properties();
        properties.setProperty("Some string", "some other string");

        doReturn((false)).when(unit).writeTriggerPropsToFile(properties, path, logger);

        unit.writeTriggerDetailsToAllureLogs(properties, logger, buildParameters);

        verify(unit, times(TafScheduleBuilder.TRIGGER_PROPS_WRITE_RETRY_COUNT)).writeTriggerPropsToFile(properties, path, logger);
    }
}