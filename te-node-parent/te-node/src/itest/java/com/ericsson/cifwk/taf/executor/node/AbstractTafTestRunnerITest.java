package com.ericsson.cifwk.taf.executor.node;

import com.ericsson.cifwk.taf.executor.NodeConfigurationProvider;
import com.google.common.io.Files;
import org.apache.commons.io.output.TeeOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Paths;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 16/03/2016
 */
abstract class AbstractTafTestRunnerITest {

    static final String RELEASED_TESTWARE = "com.ericsson.cifwk.taf.executor:te-taf-testware:2.0";
    static final String RELEASES_REPOSITORY_URL = "https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/releases/";
    private static final int NODE_POLL_TIMEOUT_IN_SECONDS = 5;

    ByteArrayOutputStream testOutputStream = new ByteArrayOutputStream();

    TafTestRunner runner;
    File allureDir;
    String allureLogDirName;

    public void setUp() throws Exception {
        NodeConfigurationProvider configurationProvider = spy(NodeConfigurationProvider.getInstance());
        doReturn(NODE_POLL_TIMEOUT_IN_SECONDS).when(configurationProvider).getTimeoutTimerPollInSeconds();
        runner = new TafTestRunner(new PrintStream(new TeeOutputStream(System.out, testOutputStream)));
        runner = spy(runner);
        doReturn(configurationProvider).when(runner).nodeConfigProvider();
        allureDir = Files.createTempDir();
        allureLogDirName = allureDir.getAbsolutePath();
    }

    static String getTmpDirPath(String dirName) {
        return Paths.get(System.getProperty("java.io.tmpdir"), dirName).toString();
    }
}
