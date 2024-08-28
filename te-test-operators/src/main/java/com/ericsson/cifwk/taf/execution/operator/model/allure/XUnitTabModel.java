package com.ericsson.cifwk.taf.execution.operator.model.allure;

import com.ericsson.cifwk.taf.ui.UI;
import com.ericsson.cifwk.taf.ui.core.SelectorType;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.GenericViewModel;
import com.ericsson.cifwk.taf.ui.sdk.Link;

import java.util.ArrayList;
import java.util.List;

public class XUnitTabModel extends GenericViewModel {

    @UiComponentMapping(selectorType = SelectorType.XPATH, selector = "//h4[contains(@class, 'ng-binding') and text() = 'Test suites']")
    private UiComponent suitesSection;

    @UiComponentMapping(".btn-status-pending")
    private Link pendingButton;

    @UiComponentMapping(selectorType = SelectorType.CSS, selector = "a[ui-sref='xunit']")
    private Link xUnitLink;

    public List<String> getTestSuites() {
        pendingButton.click();
        List<String> names = new ArrayList<>();
        List<UiComponent> testSuites = getViewComponents(SelectorType.CSS, ".test-list .testsuite-row", UiComponent.class);
        for (UiComponent testSuite : testSuites) {
            names.add(testSuite.getChildren().get(0).getText());
        }
        return names;
    }

    public List<String> getTestCasesForSuite(String suite) {
        List<UiComponent> testSuites = getViewComponents(SelectorType.CSS, ".test-list .testsuite-row", UiComponent.class);
        for (UiComponent testSuite : testSuites) {
            if (testSuite.getChildren().get(0).getText().equals(suite)) {
                testSuite.click();
                break;
            }
        }

        //TODO Check for presence of a UI component and remove pause
        UI.pause(3000);
        List<String> names = new ArrayList<>();
        List<UiComponent> testCasesTableRows = getViewComponents(SelectorType.CSS, "tr[ng-repeat=\"testcase in list.items\"]", UiComponent.class);
        for (UiComponent testCase : testCasesTableRows) {
            List<UiComponent> children = testCase.getChildren();
            if (children.size() >= 2) {
                names.add(children.get(1).getText());
            }
        }
        return names;
    }

    public boolean isSuitesSectionAvailable() {
        return suitesSection.exists();
    }

    public UiComponent getSuitesSection() {
        return suitesSection;
    }

    public void goToXUnitTab() {
        if (xUnitLink.exists()) {
            xUnitLink.click();
        } else {
            throw new RuntimeException("Link to XUnit tab not found!");
        }
    }
}
