package com.ericsson.cifwk.taf.execution.operator;

import com.ericsson.cifwk.taf.execution.operator.model.allure.OverviewTabModel;
import com.ericsson.cifwk.taf.execution.operator.model.allure.XUnitTabModel;
import com.ericsson.cifwk.taf.ui.BrowserTab;

public interface AllureReportOperator {

    void init(String reportUrl);

    OverviewTabModel getOverviewTab();

    XUnitTabModel getXunitTab();

    BrowserTab getBrowserTab();

}
