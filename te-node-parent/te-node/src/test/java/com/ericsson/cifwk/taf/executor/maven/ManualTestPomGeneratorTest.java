package com.ericsson.cifwk.taf.executor.maven;

import com.ericsson.cifwk.taf.executor.NodeConfigurationProvider;
import com.ericsson.cifwk.taf.executor.TestExecution;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ManualTestData;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class ManualTestPomGeneratorTest extends AbstractPomGeneratorTest {

    @Mock
    private NodeConfigurationProvider configurationProvider;

    @Spy
    private ManualTestPomGenerator generator;

    @Before
    public void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true); // ignore whitespace differences
        doReturn(configurationProvider).when(generator).nodeConfigProvider();
        doReturn("2.0.16").when(configurationProvider).getManualTestsPluginVersion();
    }

    @Test
    public void emitHappyPath() throws Exception {
        TestExecution executionDetails = TestExecution.builder()
            .withRepositoryUrl("https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/releases/")
            .withAllureLogDir("/Temp/LOG_STORAGE/ca76696a-8373-4c5b-b889-205b2bd37a0f/")
            .withManualTestData(ManualTestData.from("33,1,2,44"))
            .withAllureServiceUrl("http://localhost/api/reports")
            .withGeneralJobExecutionId("1234567890")
            .build();
        String actual = generatePomFromSetValues(executionDetails);
        String expected = loadXmlFrom("assert/manual_test_pom_ok.xml");
        XMLAssert.assertXMLEqual(expected, actual);
    }

    private String generatePomFromSetValues(TestExecution executionDetails) {
        StringOutputStream stream = new StringOutputStream();
        generator.emit(executionDetails, stream);
        return stream.toString();
    }

}
