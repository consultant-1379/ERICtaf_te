package com.ericsson.cifwk.taf.executor.schedule.parser.xml;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "schedule", strict = false)
public final class ScheduleNode extends BaseItemGroupNode {

    @Element(name="manual-item", required = false)
    private ManualTestItem manualTestItem;

    public ManualTestItem getManualTestItem() {
        return manualTestItem;
    }

    public void setManualTestItem(ManualTestItem manualTestItem) {
        this.manualTestItem = manualTestItem;
    }

}
