package com.ericsson.cifwk.taf.execution;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.execution.operator.JenkinsOperator;
import com.ericsson.cifwk.taf.execution.operator.RabbitMqOperator;
import com.ericsson.cifwk.taf.execution.operator.TafExecutorOperator;
import com.ericsson.cifwk.taf.execution.operator.impl.JenkinsOperatorImpl;
import com.ericsson.cifwk.taf.execution.operator.impl.RabbitMqOperatorImpl;
import com.ericsson.cifwk.taf.execution.operator.impl.TafExecutorOperatorImpl;
import com.ericsson.cifwk.taf.execution.operator.model.TestDataContext;
import com.ericsson.cifwk.taf.execution.operator.model.jenkins.SchedulerJobConfig;
import com.ericsson.cifwk.taf.executor.api.healthcheck.HealthCheck;
import com.google.common.base.Optional;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;

import static org.testng.Assert.assertTrue;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 22/02/2016
 */
public class RabbitMqHealthCheckTest extends TafTestBase {

    @Inject
    private Provider<JenkinsOperatorImpl> jenkinsOperatorProvider;

    @Inject
    private Provider<RabbitMqOperatorImpl> rabbitMqOperatorProvider;

    @Inject
    private Provider<TafExecutorOperatorImpl> tafExecutorOperatorProvider;

    private JenkinsOperator jenkinsOperator;
    private RabbitMqOperator rabbitMqOperator;
    private TafExecutorOperator tafExecutorOperator;

    @BeforeTest
    public void setUp() {
        jenkinsOperator = jenkinsOperatorProvider.get();
        rabbitMqOperator = rabbitMqOperatorProvider.get();
        tafExecutorOperator = tafExecutorOperatorProvider.get();
    }

    @Test
    @TestId(id = "TAF_TE_13", title = "Should create RabbitMQ exchange if it doesn't exist")
    public void shouldCreateExchangeIfItDoesntExist() throws Exception {
        Optional<SchedulerJobConfig> optionalMainConfig = jenkinsOperator.getMainJobConfig(TestDataContext.getTeMasterHost());
        assertTrue(optionalMainConfig.isPresent(), "Failed to retrieve the main TE job's configuration");
        SchedulerJobConfig mainConfig = optionalMainConfig.get();

        Host mbHost = DataHandler.getHostByName("reporting_message_bus");
        // Delete existing exchange
        String requiredMbExchange = mainConfig.getReportMbExchange();
        if (rabbitMqOperator.exchangeExists(mbHost, requiredMbExchange)) {
            rabbitMqOperator.deleteExchange(mbHost, requiredMbExchange);
        }

        // Run healthcheck
        List<HealthCheck> healthChecks = tafExecutorOperator.healthCheck(TestDataContext.getTeMasterHost());
        for (HealthCheck healthCheck : healthChecks) {
            assertTrue(healthCheck.getPassed(), String.format("Healthcheck '%s' failed", healthCheck.toString()));
        }

        assertTrue(rabbitMqOperator.exchangeExists(mbHost, requiredMbExchange),
                String.format("Exchange '%s' wasn't created after health check", requiredMbExchange));
    }

}
