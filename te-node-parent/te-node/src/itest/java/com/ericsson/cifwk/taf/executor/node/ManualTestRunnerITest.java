package com.ericsson.cifwk.taf.executor.node;

import com.ericsson.cifwk.taf.executor.NodeConfigurationProvider;
import com.ericsson.cifwk.taf.executor.TestExecution;
import com.ericsson.cifwk.taf.executor.TestResult;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ManualTestData;
import com.ericsson.cifwk.taf.executor.maven.AbstractPomGenerator;
import com.ericsson.cifwk.taf.executor.maven.ManualTestPomGenerator;
import com.ericsson.cifwk.taf.itest.EmbeddedJetty;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 16/03/2016
 */
public class ManualTestRunnerITest extends AbstractTafTestRunnerITest {

    private static final String SNAPSHOT_REPOSITORY_URL = "https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/snapshots/";

    private static final String TAF_MAVEN_MANUAL_TEST_PLUGIN_VERSION = "2.33.9";

    private static final Integer FAKE_TMS_PORT = 6789;

    private static EmbeddedJetty jetty;

    private ManualTestRunner runner;

    private File allureDir = new File(getTmpDirPath("allureManualLogStorage"));

    @BeforeClass
    public static void beforeTests() throws Exception {
        // Embedded Jetty to avoid requesting real TMS, which would be out of itests scope
        jetty = EmbeddedJetty.build()
                .withServlet(new FakeTmsServlet(), "/tm-server/api/*")
                .withPort(FAKE_TMS_PORT)
                .start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        jetty.stop();
    }

    @Before
    public void setUp() throws Exception {
        FileUtils.deleteDirectory(allureDir);

        final NodeConfigurationProvider configurationProvider = spy(NodeConfigurationProvider.getInstance());
        doReturn(TAF_MAVEN_MANUAL_TEST_PLUGIN_VERSION).when(configurationProvider).getManualTestsPluginVersion();

        ManualTestPomGenerator pomGenerator = new ManualTestPomGenerator() {
            @Override
            public void emit(TestExecution testExecution, OutputStream outputStream, String additionalMavenPlugins) {
                super.emit(testExecution, outputStream, systemPropsInjection());
            }

            @Override
            protected NodeConfigurationProvider nodeConfigProvider() {
                return configurationProvider;
            }
        };
        runner = new ManualTestRunner(pomGenerator, System.out);
    }

    @Test
    public void shouldRunSuccessfulTest() throws Exception {
        TestResult testResult = runTestCampaigns("373,139,814");
        assertThat(testResult.getStatus(), equalTo(TestResult.Status.SUCCESS));
        assertTrue(allureDir.exists());
        String[] suiteFileList = allureDir.list(new AllureSuiteFilenameFilter());
        assertThat(suiteFileList.length, equalTo(3));
    }

    private TestResult runTestCampaigns(String campaignIds) {
        TestExecution executionDetails = TestExecution.builder()
            .withManualTestData(ManualTestData.from(campaignIds))
            .withRepositoryUrl(SNAPSHOT_REPOSITORY_URL)
            .withGeneralJobExecutionId("1234567890")
            .withAllureLogDir(allureDir.toString())
            .build();
        runner.setUp(executionDetails);
        return runner.runTest();
    }

    private String systemPropsInjection() {
        AdvancedManualPomGenerator pomGenerator = new AdvancedManualPomGenerator();
        StringOutputStream outputStream = new StringOutputStream();
        pomGenerator.emit("system_property_injection.ftl",
                Collections.singletonMap("port", (Object)String.valueOf(FAKE_TMS_PORT)), outputStream);
        return outputStream.toString();
    }

    private class AdvancedManualPomGenerator extends AbstractPomGenerator {

        @Override
        public void emit(String templateFileName, Map<String, Object> parameterMap, OutputStream outputStream) {
            super.emit(templateFileName, parameterMap, outputStream);
        }
    }

    private class StringOutputStream extends OutputStream {

        private StringBuilder string = new StringBuilder();

        @Override
        public void write(int b) throws IOException {
            this.string.append((char) b);
        }

        @Override
        public String toString() {
            return this.string.toString();
        }
    }

}
