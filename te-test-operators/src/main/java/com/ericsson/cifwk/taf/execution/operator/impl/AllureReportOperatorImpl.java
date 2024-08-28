package com.ericsson.cifwk.taf.execution.operator.impl;

import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.execution.operator.AllureReportOperator;
import com.ericsson.cifwk.taf.execution.operator.model.allure.OverviewTabModel;
import com.ericsson.cifwk.taf.execution.operator.model.allure.XUnitTabModel;
import com.ericsson.cifwk.taf.ui.Browser;
import com.ericsson.cifwk.taf.ui.BrowserSetup;
import com.ericsson.cifwk.taf.ui.BrowserTab;
import com.ericsson.cifwk.taf.ui.UI;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.sdk.ViewModel;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;

@Operator
public class AllureReportOperatorImpl implements AllureReportOperator {

    private BrowserTab reportTab;

    @Override
    public void init(String reportUrl) {
        BrowserSetup.Builder setup = BrowserSetup.build()
                .withCapability(
                        CapabilityType.PROXY,
                        new Proxy().setProxyType(Proxy.ProxyType.DIRECT));
        Browser browser = UI.newBrowser(setup);
        reportTab = browser.open(reportUrl);
    }

    @Override
    public OverviewTabModel getOverviewTab() {
        OverviewTabModel view = getViewModel(OverviewTabModel.class);
        view.goToOverviewTab();
        waitForComponent(view.getEnvSection());
        return view;
    }

    @Override
    public XUnitTabModel getXunitTab() {
        XUnitTabModel view = getViewModel(XUnitTabModel.class);
        view.goToXUnitTab();
        waitForComponent(view.getSuitesSection());
        return view;
    }

    private <T extends ViewModel> T getViewModel(Class<T> clazz) {
        return reportTab.getView(clazz);
    }

    private void waitForComponent(UiComponent uiComponent) {
        reportTab.waitUntilComponentIsDisplayed(uiComponent);
    }

    @Override
    public BrowserTab getBrowserTab() {
        return reportTab;
    }
}
