package com.ericsson.cifwk.taf.executor.maven;

import com.ericsson.cifwk.taf.executor.NodeConfigurationProvider;
import com.google.common.collect.Maps;
import junit.framework.AssertionFailedError;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class TestPomGeneratorTest extends AbstractPomGeneratorTest {

    @Mock
    private NodeConfigurationProvider configurationProvider;

    @Spy
    private TestPomGenerator generator;

    private PomValues values;

    @Before
    public void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true); // ignore whitespace differences

        doReturn(configurationProvider).when(generator).nodeConfigProvider();
        doReturn("2.37.19").when(configurationProvider).getTafMavenPluginVersion();
        doReturn("2.31.5").when(configurationProvider).getTafSurefireProviderVersion();
        doReturn("2.14").when(configurationProvider).getTeVersion();

        values = new PomValues();
        values.setTestware(new GAV("com.ericsson.abc", "testware1", "1.0.1"));
        values.setSuites("first.xml,second.xml");
        values.setGeneralExecutionId("1234567890");
        values.setGroups("first,second");
        values.setParentEventId("parent-event-id");
        values.setParentExecutionId("parent-execution-id");
        values.setMbHost("user:password@host:port");
        values.setMbDomain("domain");
        values.setMbExchange("exchange");
        values.setLogUrl("http://host/jenkins/project/1/log");
        values.setRepositoryUrl("https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/releases/");
        values.setConfigUrl("http://host/jenkins/project/1/config");
        values.setAllureServiceUrl("http://localhost/api/reports");
        values.setAllureLogDir("/Temp/LOG_STORAGE/ca76696a-8373-4c5b-b889-205b2bd37a0f/");
    }


    @Test
    public void completeHappyPath() throws Exception {
        XMLAssert.assertXMLEqual(generatePomFromSetValues(), loadXmlFrom("assert/output.xml"));
    }

    @Test
    public void completeHappyPathWithoutAllureLogDir() throws Exception {
        values.setAllureLogDir(null);
        String expected = generatePomFromSetValues();
        String actual = loadXmlFrom("assert/output_no_allure_site.xml");
        XMLAssert.assertXMLEqual(actual, expected);
    }

    @Test
    public void testNewerVersion() {
        PrintStream printStream = mock(PrintStream.class);
        Assert.assertTrue(generator.newerVersion("Taf", "3.22.1", "3.2.1", printStream));
        Assert.assertFalse(generator.newerVersion("Taf", "3.2.1", "3.22.1", printStream));
        Assert.assertTrue(generator.newerVersion("Taf", "3.2.1", "2.2.1", printStream));
        Assert.assertTrue(generator.newerVersion("Taf", "3.2.1", "3.1.1", printStream));
        Assert.assertTrue(generator.newerVersion("Taf", "3.2.1", "3.2.0", printStream));
        Assert.assertTrue(generator.newerVersion("Taf", "3.3.3", "2.2.1", printStream));
        Assert.assertFalse(generator.newerVersion("Taf", "3.2.1", "4.2.1", printStream));
        Assert.assertFalse(generator.newerVersion("Taf", "3.2.1", "3.3.1", printStream));
        Assert.assertFalse(generator.newerVersion("Taf", "3.2.1", "3.2.2", printStream));
        Assert.assertFalse(generator.newerVersion("Taf", "3.2.1", "3.2.1", printStream));
        Assert.assertTrue(generator.newerVersion("Taf", "3.22.1", "3.2.1.1", printStream));
        Assert.assertFalse(generator.newerVersion("Taf", "3.2.1.1", "3.22.1", printStream));
        Assert.assertTrue(generator.newerVersion("Taf", "3.2.5-SNAPSHOT", "3.2.1", printStream));
        Assert.assertFalse(generator.newerVersion("Taf", "3.2.4-SNAPSHOT", "3.2.5", printStream));
        Assert.assertFalse(generator.newerVersion("Taf", "3.2.5-SNAPSHOT", "3.2.5", printStream));
        Assert.assertTrue(generator.newerVersion("Taf", "3.2.5", "3.2.5-SNAPSHOT", printStream));
        Assert.assertFalse(generator.newerVersion("Taf", "2.32.9", "2.32.9.1", printStream));
        Assert.assertTrue(generator.newerVersion("Taf", "2.32.9.1", "2.32.9", printStream));
        Assert.assertFalse(generator.newerVersion("Taf", "2.32.9.1.2.3", "2.32.9.1.2.3.1", printStream));
        Assert.assertTrue(generator.newerVersion("Taf", "2.32.9.1.2.3.1", "2.32.9.1.2.3", printStream));
    }

    @Test
    public void testVersionPattern() {
        assertEquals("2.2.37", generator.matchVersion("[INFO]   com.ericsson.cifwk:taf ............................. 2.7.14 -> 2.2.37"));
        assertEquals("2.2.37-SNAPSHOT", generator.matchVersion("[INFO]   com.ericsson.cifwk:taf ............................. 2.7.14 -> 2.2.37-SNAPSHOT"));
        Assert.assertNotEquals("2.2.37SNAPSHOT", generator.matchVersion("[INFO]   com.ericsson.cifwk:taf ............................. 2.7.14 -> 2.2.37-SNAPSHOT"));
        Assert.assertNotEquals("2.2.37-SNAPSHOT", generator.matchVersion("[INFO]   com.ericsson.cifwk:taf ............................. 2.7.14 -> 2.2.37SNAPSHOT"));
        assertEquals(null, generator.matchVersion("..."));
    }

    @Test
    public void shouldAddAdditionalDependencies() throws Exception {
        values.setAdditionalDependencies(Arrays.asList(new GAV("my-group", "my-artifact", "1.0.0")));
        String actual = generatePomFromSetValues();
        String expected = loadXmlFrom("assert/output_with_deps.xml");
        XMLAssert.assertXMLEqual(actual, expected);
    }

    @Test
    public void shouldAddSystemProperties() throws Exception {
        Map<String, String> propertyMap = Maps.newLinkedHashMap();
        propertyMap.put("taf.profile", "aProfile");
        propertyMap.put("sysProp1", "sysProp1Value");
        values.setSystemProperties(propertyMap);
        XMLAssert.assertXMLEqual(generatePomFromSetValues(), loadXmlFrom("assert/output_with_system_props.xml"));
    }

    @Test
    public void no_allure_configuration_generated() throws Exception {
        values.setAllureServiceUrl(null); // reset
        String actual = generatePomFromSetValues();
        String expected = loadXmlFrom("assert/output_with_no_allure_config.xml");
        XMLAssert.assertXMLEqual(actual, expected);
    }

    @Test
    public void skipTestsTrue() throws Exception {
        values.setSkipTests("true");
        String actual = generatePomFromSetValues();
        String expected = loadXmlFrom("assert/output_skip_tests.xml");
        XMLAssert.assertXMLEqual(actual, expected);
    }

    @Test
    public void skipTestsFalse() throws Exception {
        values.setSkipTests("false");
        String actual = generatePomFromSetValues();
        String expected = loadXmlFrom("assert/output.xml");
        XMLAssert.assertXMLEqual(actual, expected);
    }

    @Test
    public void userDefinedGAVsSpecified() throws Exception {
        List<GAV> boms = new ArrayList<>();
        boms.add(new GAV("com.ericsson.oss.testware.bom", "enm-test-library-bom", "1.0.420"));
        values.setUserDefinedBOMs(boms);
        List<GAV> poms = new ArrayList<>();
        poms.add(new GAV("com.ericsson.oss.testware.taf", "host-configurator", "1.0.104"));
        poms.add(new GAV("com.ericsson.oss.testware.taf", "enm-base", "1.0.101"));
        values.setUserDefinedPOMs(poms);

        String actual = generatePomFromSetValues();
        String expected = loadXmlFrom("assert/output_with_user_defined_gavs.xml");
        try{
            XMLAssert.assertXMLEqual(expected, actual);
        } catch(AssertionFailedError error) {
            try (PrintWriter out = new PrintWriter("target/userDefinedGAVsSpecified.xml")) {
                out.write(actual);
            }
            throw error;
        }
    }

    private String generatePomFromSetValues() {
        StringOutputStream stream = new StringOutputStream();
        generator.emit(values, stream);
        return stream.toString();
    }

}
