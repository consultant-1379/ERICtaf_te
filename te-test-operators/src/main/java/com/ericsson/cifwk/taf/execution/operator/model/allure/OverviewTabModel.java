package com.ericsson.cifwk.taf.execution.operator.model.allure;

import com.ericsson.cifwk.taf.ui.core.SelectorType;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.GenericViewModel;
import com.ericsson.cifwk.taf.ui.sdk.Link;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OverviewTabModel extends GenericViewModel {

    @UiComponentMapping(selectorType = SelectorType.XPATH, selector = "//h3[contains(@class, 'widget_title') and contains(@class, 'ng-binding') and text() = 'environment']")
    private UiComponent envSection;

    @UiComponentMapping(selectorType = SelectorType.CSS, selector = "a[ui-sref='overview']")
    private Link overviewLink;

    public Map<String, String> getEnvironmentData() {
        Map<String, String> environmentData = new HashMap<>();
        if (!isEnvSectionVisible()) {
            return environmentData;
        }
        List<UiComponent> envVariableRows = getViewComponents(SelectorType.XPATH, "//widget-key-value/table[contains(@class, 'test-list')]/tbody/tr[contains(@class, 'list-item')]", UiComponent.class);
        for (UiComponent envVariableRow : envVariableRows) {
            List<UiComponent> tds = envVariableRow.getChildren();
            environmentData.put(tds.get(0).getText(), tds.get(1).getText());
        }

        return environmentData;
    }

    public boolean isEnvSectionVisible() {
        return envSection.exists();
    }

    public void goToOverviewTab() {
        if (overviewLink.exists()) {
            overviewLink.click();
        } else {
            throw new RuntimeException("Link to Overview tab not found!");
        }
    }

    public UiComponent getEnvSection() {
        return envSection;
    }
}
