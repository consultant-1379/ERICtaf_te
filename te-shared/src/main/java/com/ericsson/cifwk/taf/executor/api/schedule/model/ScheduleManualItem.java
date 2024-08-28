package com.ericsson.cifwk.taf.executor.api.schedule.model;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 17/03/2016
 */
public class ScheduleManualItem implements ScheduleChild {

    private final ManualTestData manualTestData;

    public ScheduleManualItem(ManualTestData manualTestData) {
        this.manualTestData = manualTestData;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitManualTestItem(manualTestData);
    }

    @Override
    public ScheduleChild getParent() {
        return null;
    }

    public ManualTestData getManualTestData() {
        return manualTestData;
    }
}
