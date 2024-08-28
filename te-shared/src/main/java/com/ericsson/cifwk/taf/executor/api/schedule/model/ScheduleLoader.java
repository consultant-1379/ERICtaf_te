package com.ericsson.cifwk.taf.executor.api.schedule.model;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 01/02/2016
 */
public interface ScheduleLoader {

    String load(ScheduleLocation location);

    ScheduleLocation getScheduleIncludeLocation(String address);
}
