package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.api.ArtifactInfo;
import com.ericsson.cifwk.taf.executor.api.ScheduleRequest;
import com.ericsson.cifwk.taf.executor.api.ScheduleSource;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class TafScheduleParametersActionBuilderTest {

    private TafScheduleParametersActionBuilder unit;

    @Before
    public void setUp() throws Exception {
        unit = new TafScheduleParametersActionBuilder(mock(TafScheduleProject.class));
        unit = spy(unit);
    }

    @Test
    public void testWithSchedule_gav() throws Exception {
        ScheduleRequest scheduleRequest = getScheduleRequest(ScheduleSource.MAVEN_GAV);
        unit.withSchedule(scheduleRequest);
        verify(unit).add(BuildParameterNames.SCHEDULE_NAME, scheduleRequest.getName());
        ArtifactInfo artifact = scheduleRequest.getArtifact();
        verify(unit).add(BuildParameterNames.SCHEDULE_ARTIFACT, artifact.getGavString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithSchedule_incomplete() throws Exception {
        ScheduleRequest scheduleRequest = getScheduleRequest(ScheduleSource.MAVEN_GAV);
        scheduleRequest.setArtifact(null);
        try {
            unit.withSchedule(scheduleRequest);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Schedule information is incomplete"));
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithSchedule_unknown() throws Exception {
        ScheduleRequest scheduleRequest = getScheduleRequest(ScheduleSource.PLAIN_XML);
        scheduleRequest.setXml(null);
        try {
            unit.withSchedule(scheduleRequest);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Unknown schedule type"));
            throw e;
        }
    }

    @Test
    public void testWithSchedule_xml() throws Exception {
        ScheduleRequest scheduleRequest = getScheduleRequest(ScheduleSource.PLAIN_XML);
        unit.withSchedule(scheduleRequest);
        verify(unit).add(eq(BuildParameterNames.SCHEDULE_NAME), anyString());
        verify(unit).add(eq(BuildParameterNames.SCHEDULE_ARTIFACT), anyString());
        verify(unit).add(eq(BuildParameterNames.SCHEDULE), eq(scheduleRequest.getXml()));
    }

    private ScheduleRequest getScheduleRequest(ScheduleSource scheduleSource) {
        ScheduleRequest result = new ScheduleRequest();
        result.setTestPropertiesAsString("x=y");
        switch (scheduleSource) {
            case TAF_SCHEDULER:
                result.setTafSchedulerSourceUri("http://tafScheduler/1/2");
                result.setXml("<taf_scheduler_xml/>");
                break;
            case MAVEN_GAV:
                result.setArtifact(new ArtifactInfo("g", "a", "v"));
                result.setName("schedule.xml");
                break;
            case PLAIN_XML:
                result.setXml("<plain_xml/>");
                break;
        }
        return result;
    }

}