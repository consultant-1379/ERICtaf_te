package com.ericsson.cifwk.taf.scenarios.teststeps;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.scenarios.operators.SimpleUiOperator;
import com.ericsson.cifwk.taf.scenarios.operators.SimpleUiOperatorImpl;
import com.ericsson.cifwk.taf.scenarios.views.EricssonPageModel;
import com.ericsson.cifwk.taf.scenarios.views.TafOverflowPageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;

public class EricssonPageTestSteps {

    private static final Logger LOG = LoggerFactory.getLogger(EricssonPageTestSteps.class);

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

    @TestStep(id = "searchForPublication")
    public void searchForPublication(@Input("publication") String publication) {
        SimpleUiOperator operator = simpleUiOperatorProvider.get();
        TafOverflowPageModel view = operator.getView(TafOverflowPageModel.class);
        view.searchFor(publication);
    }

    @TestStep(id = "validatePublication")
    public void validate(@Input("publicationName") String name) {
        SimpleUiOperator operator = simpleUiOperatorProvider.get();
        EricssonPageModel view = operator.getView(EricssonPageModel.class);
        if ("NotFound".equals(name)) {
            view.verifyNoResultsFound();
        } else {
            view.verifyResults(name);
        }
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
