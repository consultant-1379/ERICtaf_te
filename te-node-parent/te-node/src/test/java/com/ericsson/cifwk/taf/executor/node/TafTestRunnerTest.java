package com.ericsson.cifwk.taf.executor.node;

import com.ericsson.cifwk.taf.executor.TestExecution;
import com.ericsson.cifwk.taf.executor.TestwareRuntimeLimitations;
import com.ericsson.cifwk.taf.executor.maven.PomValues;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TafTestRunnerTest {

    private TafTestRunner unit;
    private StringBuffer log = new StringBuffer();

    @Before
    public void setUp() throws Exception {
        PrintStream buildLog = mock(PrintStream.class);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                log.append(invocation.getArguments()[0]);
                return null;
            }
        }).when(buildLog).println(anyString());
        unit = new TafTestRunner(buildLog);
        unit = spy(unit);
    }

    @Test
    public void shouldFindTafMavenPid() throws Exception {
        boolean result = unit.findTafMavenPid("[INFO] [com.ericsson.cifwk.taf.management.TafBootstrap] Current process ID is 8664");
        assertThat(result, is(true));
        assertThat(unit.getTafMavenProcessId(), equalTo(8664));
    }

    @Test
    public void shouldPopulateAllureParameters() throws Exception {
        String jobExecutionId = randomUUID().toString();
        String serviceUrl = "http://localhost/api/reports";
        String logDir = "/vat/log/allure/";
        String version = "1.2.3";
        TestExecution execution = TestExecution.builder()
            .withGeneralJobExecutionId(jobExecutionId)
            .withTestware("com.ericsson.cifwk.taf.executor:te-taf-testware:1.0.21")
            .withSuites("suite1.xml,suite2.xml")
            .withRepositoryUrl("https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/releases/")
            .withAllureServiceUrl(serviceUrl)
            .withAllureLogDir(logDir)
            .withAllureVersion(version)
            .build();

        doNothing().when(unit).createWorkDir();
        doNothing().when(unit).createPom();
        doNothing().when(unit).copyAllureArchiveDescriptor(any(TestExecution.class));
        doNothing().when(unit).monitorProcessTimeout(eq(execution));

        unit.setUp(execution);

        PomValues pomValues = unit.getValues();
        assertEquals(serviceUrl, pomValues.getAllureServiceUrl());
        assertEquals(logDir, pomValues.getAllureLogDir());
        assertEquals(version, pomValues.getAllureVersion());
    }

    @Test
    public void shouldPopulateMbParameters() throws Exception {
        String mbDomain = "mbDomain";
        String mbExchange = "mbExchange";
        String mbHost = "mbHost";

        TestExecution execution = TestExecution.builder()
                .withTestware("com.ericsson.cifwk.taf.executor:te-taf-testware:1.0.21")
                .withSuites("suite1.xml,suite2.xml")
                .withRepositoryUrl("https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/releases/")
                .withMbDomain(mbDomain)
                .withMbExchange(mbExchange)
                .withMbHost(mbHost).build();

        doNothing().when(unit).createWorkDir();
        doNothing().when(unit).createPom();
        doNothing().when(unit).copyAllureArchiveDescriptor(any(TestExecution.class));
        doNothing().when(unit).monitorProcessTimeout(eq(execution));

        unit.setUp(execution);

        PomValues pomValues = unit.getValues();
        assertEquals(mbHost, pomValues.getMbHost());
        assertEquals(mbExchange, pomValues.getMbExchange());
        assertEquals(mbDomain, pomValues.getMbDomain());
    }

    @Test
    public void shouldGetCurrentUser() {
        assertTrue(StringUtils.isNotBlank(unit.getCurrentUser()));
    }

    @Test
    public void shouldPopulateMiscProperties() {
        PomValues values = fromMiscProperties("");
        assertNull(values.getGroups());

        values = fromMiscProperties("globalTestGroups=globalGroup1");
        assertThat(values.getGroups(), equalTo("globalGroup1"));

        values = fromMiscProperties("globalTestGroups=globalGroup1" + System.getProperty("line.separator") + "anotherOption=125");
        assertThat(values.getGroups(), equalTo("globalGroup1"));
    }

    @Test
    public void shouldSetCustomJavaIfRequired() {
        doNothing().when(unit).createPom();
        doNothing().when(unit).copyAllureArchiveDescriptor(any(TestExecution.class));
        doReturn("/usr/local/java7").when(unit).getSystemEnvProperty(eq("JAVA7_HOME"));
        doReturn("/usr/local/java8").when(unit).getSystemEnvProperty(eq("JAVA8_HOME"));
        doReturn(null).when(unit).getSystemEnvProperty(eq("JAVA1111_HOME"));
        doReturn("/usr/local/java").when(unit).getSystemEnvProperty(eq("JAVA_HOME"));

        verifyJavaVersionSet(8, "/usr/local/java8");
        verifyJavaVersionSet(7, "/usr/local/java7");
        verifyJavaVersionSet(1111, null);
        assertThat(log.toString(), containsString("WARNING: the OS environment setting required for required " +
                "Java version 1111 ('JAVA1111_HOME') is not defined, so it's impossible to locate the required Java. " +
                "Build will proceed with default JAVA_HOME (/usr/local/java)"));
    }

    @Test
    public void shouldGetMaxThreadsLimitation() {
        ScheduleEnvironmentPropertyProvider propertyProvider = mock(ScheduleEnvironmentPropertyProvider.class);
        TestwareRuntimeLimitations defaultLimitations = mock(TestwareRuntimeLimitations.class);

        when(propertyProvider.getMaxThreads()).thenReturn(null).thenReturn(null).thenReturn(20);
        when(defaultLimitations.getMaxThreadCount()).thenReturn(null).thenReturn(40).thenReturn(40);

        assertNull(unit.getMaxThreads(propertyProvider, defaultLimitations));
        assertEquals(40, unit.getMaxThreads(propertyProvider, defaultLimitations).intValue());
        assertEquals(20, unit.getMaxThreads(propertyProvider, defaultLimitations).intValue());
    }

    @Test
    public void shouldAddGavsToTestRunnerValues() {
        assertThat(unit.values.getUserDefinedBOMs(), nullValue());
        assertThat(unit.values.getUserDefinedPOMs(), nullValue());
        List<String> gavsAsString = new ArrayList<>();
        gavsAsString.add("com.ericsson.oss.testware.taf:host-configurator:1.0.105");
        gavsAsString.add("com.ericsson.oss.testware.taf:enm-base:1.0.105");
        gavsAsString.add("com.ericsson.oss.testware.bom:enm-test-library-bom:1.0.422:bom");

        TestExecution execution = TestExecution.builder()
                .withUserDefinedGAVs(gavsAsString)
                .build();
        unit.addUserDefinedGAVs(execution);

        assertThat(unit.values.getUserDefinedBOMs().size(), equalTo(1));
        assertThat(unit.values.getUserDefinedPOMs().size(), equalTo(2));
    }

    @Test
    public void shouldThrowExceptionWhenGavFormattedIncorrectly() {
        List<String> gavsAsString = new ArrayList<>();
        gavsAsString.add("com.ericsson.oss.testware.taf:host-configurator;1.0.105");
        TestExecution execution = TestExecution.builder()
                .withUserDefinedGAVs(gavsAsString)
                .build();
        try {
            unit.addUserDefinedGAVs(execution);
            fail("addUserDefinedGavs did not fail when only 2 ':' present");
        } catch (GavParsingException gpe) {
            assertThat(gpe.getMessage(), equalTo("Could not parse GAV from string 'com.ericsson.oss.testware.taf:host-configurator;1.0.105' using delimiter ':'"));
        }

        gavsAsString = new ArrayList<>();
        gavsAsString.add("com.ericsson.oss.testware.bom:enm-test-library-bom:1.0.422:boms");
        execution = TestExecution.builder()
                .withUserDefinedGAVs(gavsAsString)
                .build();
        try {
            unit.addUserDefinedGAVs(execution);
            fail("addUserDefinedGavs did not fail when bom parameter specified incorrectly");
        } catch (GavParsingException gpe) {
            assertThat(gpe.getMessage(), equalTo("Last parameter of 'com.ericsson.oss.testware.bom:enm-test-library-bom:1.0.422:boms' not equal to bom"));
        }
    }

    private void verifyJavaVersionSet(int version, String expectedSetting) {
        TestExecution execution = testExecutionWithJavaVersion(version);
        unit.setUp(execution);
        InvocationRequest invocationRequest = mock(InvocationRequest.class);
        unit.setCustomJavaIfRequired(invocationRequest);
        if (expectedSetting != null) {
            verify(invocationRequest).setJavaHome(new File(expectedSetting));
        } else {
            verify(invocationRequest, never()).setJavaHome(any(File.class));
        }
    }

    private TestExecution testExecutionWithJavaVersion(int version) {
        return TestExecution.builder()
                .withEnvPropertyJson("[{\"type\":\"jvm\",\"key\":\"version\",\"value\":\"" + version + "\"}]")
                .withTestware("com.ericsson.cifwk.taf.executor:te-taf-testware:1.0.50")
                .build();
    }

    private PomValues fromMiscProperties(String miscPropertiesCsv) {
        TestExecution execution = TestExecution.builder()
                .withMiscProperties(miscPropertiesCsv)
                .build();

        unit.populateMiscPropertiesAndGroups(execution);

        return unit.getValues();
    }
}
