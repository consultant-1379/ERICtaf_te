package com.ericsson.cifwk.taf.executor.schedule.model;

import com.ericsson.cifwk.taf.executor.api.schedule.model.Schedule;

import java.util.Collections;

import static java.util.Arrays.asList;
import static com.ericsson.cifwk.taf.executor.schedule.model.ScheduleHelper.*;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 16/02/2016
 */
public class SampleSchedules {

    private SampleSchedules() {}

    public static Schedule sampleCdbFull() {
        return schedule(
                scheduleItem("1", "g.r", "a1", "1.0", asList("1.xml"), asList("performance", "stress"), false, 123),
                scheduleItem("2", "g.r", "a2", "1.1", asList("2.xml"), Collections.<String>emptyList(), false, null),
                scheduleItemGroup(
                        true,
                        scheduleItem("3", "g.r", "a3", "1.2", asList("3.xml", "4.xml"), asList("acceptance"), false, null),
                        scheduleItemGroup(
                                false,
                                scheduleItem("4", "g.r", "a1", "1.0", asList("5.xml"), asList("acceptance"), true, 456),
                                scheduleItem("5", "g.r", "a2", "1.1", asList("6.xml", "7.xml", "8.xml"), Collections.<String>emptyList(), false, null)
                        )
                ),
                scheduleItem("6", "g.r", "a3", "1.2", asList("9.xml"), Collections.<String>emptyList(), false, null)
        );
    }

    public static Schedule simpleFlow() {
        return schedule(
                scheduleItem("Item's No. 1", "g.r", "a1", null, asList("1.xml"), asList("performance", "stress"), false, 123),
                scheduleItem("Item's No. 2", "g.r", "a2", null, asList("2.xml"), Collections.<String>emptyList(), false, null),
                scheduleItem("Item's No. 3", "g.r", "a3", null, asList("3.xml"), Collections.<String>emptyList(), false, null)
        );
    }

    public static Schedule withManualItems() {
        return schedule(
                scheduleItem("1", "g.r", "a1", "1.0", asList("1.xml"), asList("performance", "stress"), false, 123),
                scheduleItem("2", "g.r", "a2", "1.1", asList("2.xml"), Collections.<String>emptyList(), false, null),
                scheduleItemGroup(
                        true,
                        scheduleItem("3", "g.r", "a3", "1.2", asList("3.xml", "4.xml"), asList("acceptance"), false, null),
                        scheduleItemGroup(
                                false,
                                scheduleItem("4", "g.r", "a1", "1.0", asList("5.xml"), asList("acceptance"), true, 456),
                                scheduleItem("5", "g.r", "a2", "1.1", asList("6.xml", "7.xml", "8.xml"), Collections.<String>emptyList(), false, null)
                        )
                ),
                scheduleItem("6", "g.r", "a3", "1.2", asList("9.xml"), Collections.<String>emptyList(), false, null),
                manualItem("11", "22")
        );
    }

}
