package com.ericsson.cifwk.taf;


import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.scenario.api.ExceptionHandler;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import com.ericsson.cifwk.taf.scenario.api.TestScenarioBuilder;
import com.ericsson.cifwk.taf.scenario.api.TestScenarioRunnerBuilder;
import com.ericsson.cifwk.taf.scenario.impl.LoggingScenarioListener;
import com.ericsson.cifwk.taf.scenarios.scheduler.ScenarioRunScheduler;
import com.ericsson.cifwk.taf.scenarios.testflows.TafOverflowTestFlows;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;

public class EricssonSiteScenarioTest extends TafTestBase {

    @Inject
    TafOverflowTestFlows flows;

    @BeforeSuite
    public void setup() {
//        UI.closeWindow(TestExecutionEvent.ON_SUITE_FINISH);
    }

    @Test
    public void testEricssonWebSite() {
        TafConfiguration config = TafConfigurationProvider.provide();

        int vUsers = config.getProperty("vUsers", int.class);
        int repeatInSeconds = config.getProperty("repeat_for_in_seconds", int.class);

        TestScenarioBuilder scenarioBuilder = scenario()
                .addFlow(flows.getPageFlow())
                .withExceptionHandler(ScenarioExceptionHandler.LOGONLY)
                .withDefaultVusers(vUsers);

        TestScenarioRunnerBuilder runnerBuilder = runner()
                .withListener(new LoggingScenarioListener())
                .withExceptionHandler(ExceptionHandler.PROPAGATE);

        //schedule scenario to run
        ScenarioRunScheduler.schedule().scenario(scenarioBuilder)
                .withRunner(runnerBuilder)
//                .withTimeSlot(20) //pause is taken if execution was faster than 60seconds
                .toRun(repeatInSeconds, TimeUnit.SECONDS);
    }

}
