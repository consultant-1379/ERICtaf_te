package com.ericsson.cifwk.taf.executor.schedule;

import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleLoader;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleLocation;
import com.ericsson.cifwk.taf.executor.api.schedule.model.TafScheduleLocation;
import com.ericsson.oss.axis.interfaces.scheduler.TafScheduleInfo;
import com.ericsson.oss.axis.interfaces.scheduler.TafSchedulerService;
import com.ericsson.oss.axis.interfaces.scheduler.exceptions.TafSchedulerException;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 01/02/2016
 */
public class TafScheduleLoader implements ScheduleLoader {

    private static final Pattern SCHEDULE_URN = Pattern.compile("urn:taf\\-scheduler:([0-9]*)");

    private final TafSchedulerService client;

    public TafScheduleLoader(String tafSchedulerUrl) {
        this(new TafSchedulerService(tafSchedulerUrl, "", ""));
    }

    public TafScheduleLoader(TafSchedulerService tafSchedulerService) {
        this.client = tafSchedulerService;
    }

    @Override
    public String load(ScheduleLocation location) {
        Preconditions.checkArgument(location instanceof TafScheduleLocation, "expected instance of " + TafScheduleLocation.class.getName());
        TafScheduleLocation tafScheduleLocation = (TafScheduleLocation) location;
        try {
            TafScheduleInfo scheduleById = client.getScheduleById(tafScheduleLocation.getScheduleId());
            return scheduleById.getXml();
        } catch (TafSchedulerException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public ScheduleLocation getScheduleIncludeLocation(String address) {
        Matcher matcher = SCHEDULE_URN.matcher(address);
        if (matcher.matches()) {
            String scheduleIdStr = matcher.group(1);
            return new TafScheduleLocation(Long.parseLong(scheduleIdStr));
        } else {
            throw new IllegalArgumentException(String.format("Included schedule URN '%s' doesn't match the required pattern '%s'",
                    address, SCHEDULE_URN));
        }
    }

}
