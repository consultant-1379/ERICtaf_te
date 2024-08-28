package com.ericsson.cifwk.taf.executor.cluster.cloud;

import com.ericsson.cifwk.taf.executor.healthcheck.DefaultHealthCheckContext;
import com.ericsson.cifwk.taf.executor.healthcheck.HealthCheckContext;
import hudson.util.FormValidation;
import org.csanchez.jenkins.plugins.kubernetes.KubernetesCloud;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Stubber;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;


/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 15/06/2017
 */
@RunWith(MockitoJUnitRunner.class)
public class KubernetesCloudSlaveProviderAdapterTest {

    @Mock
    private KubernetesCloud kubeCloud;

    @Mock
    private KubernetesCloud.DescriptorImpl kubeCloudDescriptor;

    @Spy
    private KubernetesCloudSlaveProviderAdapter unit;

    @Before
    public void setUp() {
        doReturn(kubeCloudDescriptor).when(kubeCloud).getDescriptor();
        doReturn("http://server.com").when(kubeCloud).getServerUrl();
        doReturn(kubeCloud).when(unit).getCloud();
    }

    @Test
    public void checkCloudAvailability_happyPath() throws Exception {
        checkCloudAvailability(CloudValidationResult.OK, true);
    }

    @Test
    public void checkCloudAvailability_warning() throws Exception {
        checkCloudAvailability(CloudValidationResult.WARNING, true);
    }

    @Test
    public void checkCloudAvailability_notAvailable() throws Exception {
        checkCloudAvailability(CloudValidationResult.ERROR, false);
    }

    @Test
    public void checkCloudAvailability_exception() throws Exception {
        checkCloudAvailability(CloudValidationResult.EXCEPTION, false);
    }

    private void checkCloudAvailability(CloudValidationResult validationResult, boolean expectedToBeHealthy) throws Exception {
        mockCloudValidation(validationResult);
        HealthCheckContext healthCheckContext = new DefaultHealthCheckContext();
        unit.checkCloudAvailability(healthCheckContext);
        assertThat(healthCheckContext.isHealthy()).isEqualTo(expectedToBeHealthy);
    }

    private void mockCloudValidation(CloudValidationResult validationResult) throws Exception {
        Stubber stubber = null;
        switch (validationResult) {
            case OK:
                stubber = doReturn(FormValidation.ok());
                break;
            case ERROR:
                stubber = doReturn(FormValidation.error("Error"));
                break;
            case WARNING:
                stubber = doReturn(FormValidation.warning("Warning"));
                break;
            case EXCEPTION:
                stubber = doThrow(new RuntimeException());
                break;
        }
        stubber.when(kubeCloudDescriptor).doTestConnection(
                anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyString(), anyInt(), anyInt()
        );
    }

    private enum CloudValidationResult {
        OK, ERROR, WARNING, EXCEPTION
    }

}