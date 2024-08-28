package com.ericsson.cifwk.taf.executor.healthcheck;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.ericsson.cifwk.taf.executor.healthcheck.TeNodeAllureServiceAccessibilityCheckCallable.extractBaseAddress;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 01/06/2017
 */
public class TeNodeAllureServiceAccessibilityCheckCallableTest {

    private TeNodeAllureServiceAccessibilityCheckCallable unit =
            new TeNodeAllureServiceAccessibilityCheckCallable("node", "http://myservice/api/", "dummyService");

    private HealthParam healthParam = new HealthParam("Allure service", "healthParam");

    @Before
    public void setUp() {
        unit = spy(unit);
    }

    @Test
    public void shouldExtractBaseAddress() {
        assertEquals("http://myservice", extractBaseAddress("http://myservice/api/reports"));
        assertEquals("http://myservice:8080", extractBaseAddress("http://myservice:8080/api/reports"));
    }

    @Test
    public void shouldFailOnNegativeResponse() throws Exception {
        doReturn(HttpStatus.SC_NOT_FOUND).when(unit).queryAllureServiceUrl(anyString());
        unit.doCheck(healthParam);
        assertThat(healthParam.isPassed()).isFalse();
        assertThat(healthParam.getDescription()).containsPattern("Allure service .* is not accessible");
    }

    @Test
    public void shouldFailOnException() throws Exception {
        doThrow(new IOException()).when(unit).queryAllureServiceUrl(anyString());
        unit.doCheck(healthParam);
        assertThat(healthParam.isPassed()).isFalse();
        assertThat(healthParam.getDescription()).containsPattern("Failed to check Allure service .* accessibility");
    }

    @Test
    public void shouldPassOnPositiveResponse() throws Exception {
        doReturn(HttpStatus.SC_OK).when(unit).queryAllureServiceUrl(anyString());
        unit.doCheck(healthParam);
        assertThat(healthParam.isPassed()).isTrue();
    }
}