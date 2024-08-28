package com.ericsson.cifwk.taf.scenarios.teststeps;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.scenarios.operators.SimpleUiOperator;
import com.ericsson.cifwk.taf.scenarios.operators.SimpleUiOperatorImpl;
import com.ericsson.cifwk.taf.scenarios.views.TafOverflowPageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;

public class TafOverflowPageTestSteps {

    private static final Logger LOG = LoggerFactory.getLogger(TafOverflowPageTestSteps.class);

    @Inject
    Provider<SimpleUiOperatorImpl> simpleUiOperatorProvider;

    @Inject
    TestContext context;

    @TestStep(id = "initBrowser")
    public void initBrowser() {
        SimpleUiOperator operator = simpleUiOperatorProvider.get();
        operator.initBrowser();
    }

    @TestStep(id = "openPage")
    public void openPage() {
        SimpleUiOperator operator = simpleUiOperatorProvider.get();
        operator.openPage("https://taf-stackoverflow.seli.wh.rnd.internal.ericsson.com/index.php");
    }

    @TestStep(id = "searchFor")
    public void searchFor() {
        SimpleUiOperator operator = simpleUiOperatorProvider.get();
        TafOverflowPageModel view = operator.getView(TafOverflowPageModel.class);
        view.searchFor("taf ui");
    }

    @TestStep(id = "validate")
    public void validate() {
        SimpleUiOperator operator = simpleUiOperatorProvider.get();
        TafOverflowPageModel view = operator.getView(TafOverflowPageModel.class);
        view.verifyResults();
    }

    @TestStep(id = "pauseAfter")
    public void pauseAfter() {
        SimpleUiOperator operator = simpleUiOperatorProvider.get();
        operator.pauseAfter();
    }

    @TestStep(id = "cleanUp")
    public void clearnup() {
        SimpleUiOperator operator = simpleUiOperatorProvider.get();
        operator.closeBrowser();
    }

}
