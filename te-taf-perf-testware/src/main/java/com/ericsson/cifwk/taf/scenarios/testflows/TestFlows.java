package com.ericsson.cifwk.taf.scenarios.testflows;

import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenarios.teststeps.EricssonPageTestSteps;

import javax.inject.Inject;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;

public class TestFlows {

    @Inject
    private EricssonPageTestSteps ericssonPageTestSteps;

    public TestStepFlow getEricssonPageFlow() {
        return flow("ericsson-navigation-flow")
                .addTestStep(annotatedMethod(ericssonPageTestSteps, "initBrowser"))
                .addTestStep(annotatedMethod(ericssonPageTestSteps, "openPage"))
                .addTestStep(annotatedMethod(ericssonPageTestSteps, "searchForPublication"))
                .addTestStep(annotatedMethod(ericssonPageTestSteps, "validatePublication"))
                .addTestStep(annotatedMethod(ericssonPageTestSteps, "cleanUp"))
                .addTestStep(annotatedMethod(ericssonPageTestSteps, "pauseAfter"))
                .withDataSources(dataSource("ericsson-posts"))
                        .build();
    }


}
