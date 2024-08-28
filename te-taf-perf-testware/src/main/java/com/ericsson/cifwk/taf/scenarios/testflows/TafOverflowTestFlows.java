package com.ericsson.cifwk.taf.scenarios.testflows;

import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenarios.teststeps.TafOverflowPageTestSteps;

import javax.inject.Inject;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;

public class TafOverflowTestFlows {

    @Inject
    private TafOverflowPageTestSteps testSteps;

    public TestStepFlow getPageFlow() {
        return flow("taf-overflow-navigation-flow")
                .addTestStep(annotatedMethod(testSteps, "initBrowser"))
                .addTestStep(annotatedMethod(testSteps, "openPage"))
                .addTestStep(annotatedMethod(testSteps, "searchFor"))
                .addTestStep(annotatedMethod(testSteps, "validate"))
                .addTestStep(annotatedMethod(testSteps, "cleanUp"))
                .addTestStep(annotatedMethod(testSteps, "pauseAfter"))
                .withDataSources(dataSource("ericsson-posts"))
                        .build();
    }


}
