package com.ericsson.cifwk.taf.executor.healthcheck;

import com.ericsson.cifwk.taf.executor.cluster.cloud.CloudSlaveProviderValidator;
import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 19/06/2017
 */
@RunWith(MockitoJUnitRunner.class)
public class CheckJenkinsMasterWithCloudTest {

    @Mock
    private CloudSlaveProviderValidator validatorForWorkingProvider;

    @Mock
    private CloudSlaveProviderValidator validatorForFailingProvider;

    @Mock
    private Jenkins jenkins;

    @Mock
    private GlobalTeSettings globalTeSettings;

    @InjectMocks
    private CheckJenkinsMaster unit;

    @Before
    public void setUp() {
        doReturn(true).when(validatorForWorkingProvider).isProviderSetUp();
        doAnswer(invocation -> {
            HealthCheckContext context = (HealthCheckContext) invocation.getArguments()[0];
            context.ok(new HealthParam("name", "scope"));
            return null;
        }).when(validatorForWorkingProvider).healthCheck(any(HealthCheckContext.class));

        doReturn(true).when(validatorForFailingProvider).isProviderSetUp();
        doAnswer(invocation -> {
            HealthCheckContext context = (HealthCheckContext) invocation.getArguments()[0];
            context.fail(new HealthParam("name", "scope"), "failed");
            return null;
        }).when(validatorForFailingProvider).healthCheck(any(HealthCheckContext.class));
    }

    @Test
    public void checkForAvailableNodes_allProvidersAreFine() throws Exception {
        cloudHealthCheck(singletonList(validatorForWorkingProvider), true);
    }

    @Test
    public void checkForAvailableNodes_oneProviderIsBad() throws Exception {
        cloudHealthCheck(asList(validatorForFailingProvider, validatorForWorkingProvider), true);
    }

    @Test
    public void checkForAvailableNodes_allProvidersAreBad() throws Exception {
        cloudHealthCheck(singletonList(validatorForFailingProvider), false);
    }

    private void cloudHealthCheck(List<CloudSlaveProviderValidator> providerAdapters, boolean expectedToBeHealthy) throws Exception {
        DefaultHealthCheckContext context = new DefaultHealthCheckContext();
        unit.cloudAvailabilityCheck(context, providerAdapters);
        assertThat(context.isHealthy()).isEqualTo(expectedToBeHealthy);
    }

}