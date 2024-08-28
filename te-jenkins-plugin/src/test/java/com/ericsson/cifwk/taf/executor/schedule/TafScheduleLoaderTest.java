package com.ericsson.cifwk.taf.executor.schedule;

import com.ericsson.cifwk.taf.executor.api.schedule.model.TafScheduleLocation;
import com.ericsson.oss.axis.interfaces.scheduler.TafSchedulerService;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class TafScheduleLoaderTest {

    private TafScheduleLoader unit;

    @Before
    public void setUp() {
        TafSchedulerService tafSchedulerService = mock(TafSchedulerService.class);
        unit = new TafScheduleLoader(tafSchedulerService);
    }

    @Test
    public void testGetScheduleIncludeLocation() throws Exception {
        String urn = "urn:taf-scheduler:125";
        TafScheduleLocation scheduleIncludeLocation = (TafScheduleLocation) unit.getScheduleIncludeLocation(urn);
        assertThat(scheduleIncludeLocation.getScheduleId(), equalTo(125L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetScheduleIncludeLocation_badUrn() throws Exception {
        String urn = "urn:taf-scheduler:12ds";
        try {
            unit.getScheduleIncludeLocation(urn);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), equalTo("Included schedule URN 'urn:taf-scheduler:12ds' doesn't match the required pattern 'urn:taf\\-scheduler:([0-9]*)'"));
            throw e;
        }
    }
}