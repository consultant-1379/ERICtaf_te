package com.ericsson.cifwk.taf.executor.api;

import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ScheduleRequestTest {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    @Test
    public void testSetTestPropertiesAsString() throws Exception {
        ScheduleRequest unit = new ScheduleRequest();
        unit.setTestPropertiesAsString("a=b" + LINE_SEPARATOR +
                "b=c" + LINE_SEPARATOR + "c=123.45" );

        Properties testProperties = unit.getTestProperties();

        Assert.assertEquals(3, testProperties.size());
        Assert.assertEquals("b", testProperties.getProperty("a"));
        Assert.assertEquals("c", testProperties.getProperty("b"));
        Assert.assertEquals("123.45", testProperties.getProperty("c"));
    }

    @Test
    public void testSetTestProperties() throws Exception {
        ScheduleRequest unit = new ScheduleRequest();
        Properties properties = new Properties();
        properties.put("a", "b");
        properties.put("b", "c");
        properties.put("c", "123.45");

        unit.setTestProperties(properties);

        String propertiesAsString = unit.getTestPropertiesAsString();

        Assert.assertThat(propertiesAsString, containsString("a=b"));
        Assert.assertThat(propertiesAsString, containsString("b=c"));
        Assert.assertThat(propertiesAsString, containsString("c=123.45"));
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("ScheduleRequest[g:a:v!/schedule.xml]",
                getScheduleRequest(ScheduleSource.MAVEN_GAV).toString());
        assertEquals("ScheduleRequest[xml:<plain_xml/>]",
                getScheduleRequest(ScheduleSource.PLAIN_XML).toString());
        assertEquals("ScheduleRequest[tafSchedulerSourceUri:http://tafScheduler/1/2,xml:<taf_scheduler_xml/>]",
                getScheduleRequest(ScheduleSource.TAF_SCHEDULER).toString());
    }

    @Test
    public void testIsGav() throws Exception {
        ScheduleRequest gavSchedule = getScheduleRequest(ScheduleSource.MAVEN_GAV);
        assertTrue(gavSchedule.isGav());
        gavSchedule.setName(null);
        assertTrue(gavSchedule.isGav());
        ArtifactInfo artifact = gavSchedule.getArtifact();
        artifact.setArtifactId(null);
        assertTrue(gavSchedule.isGav());
        artifact.setGroupId(null);
        assertTrue(gavSchedule.isGav());
        artifact.setVersion(null);
        assertFalse(gavSchedule.isGav());
    }

    @Test
    public void testIsPlainXml() throws Exception {
        ScheduleRequest schedule = getScheduleRequest(ScheduleSource.PLAIN_XML);
        assertTrue(schedule.isPlainXml());
        schedule.setXml(null);
        assertFalse(schedule.isPlainXml());
    }

    @Test
    public void shouldDistinguishPlainXmlFromTafSchedulerSource() throws Exception {
        ScheduleRequest schedule = getScheduleRequest(ScheduleSource.TAF_SCHEDULER);
        assertTrue(schedule.isFromTafScheduler());
        assertFalse(schedule.isPlainXml());
        schedule.setTafSchedulerSourceUri(null);
        assertFalse(schedule.isFromTafScheduler());
        assertTrue(schedule.isPlainXml());
    }

    @Test
    public void testIsComplete_tafScheduler() throws Exception {
        ScheduleRequest schedule = getScheduleRequest(ScheduleSource.TAF_SCHEDULER);
        assertTrue(schedule.isComplete());
        schedule.setXml(null);
        assertFalse(schedule.isComplete());
    }

    @Test
    public void testIsComplete_gav() throws Exception {
        ScheduleRequest schedule = getScheduleRequest(ScheduleSource.MAVEN_GAV);
        assertTrue(schedule.isComplete());
        schedule.setName(null);
        assertFalse(schedule.isComplete());
    }

    @Test
    public void testIsComplete_plainXml() throws Exception {
        ScheduleRequest schedule = getScheduleRequest(ScheduleSource.PLAIN_XML);
        assertTrue(schedule.isComplete());
        schedule.setXml(null);
        assertFalse(schedule.isComplete());
    }

    @Test
    public void testIsGavPartiallyDefined() throws Exception {
        assertFalse(ScheduleRequest.isGavPartiallyDefined(new ArtifactInfo("", "", "")));
        assertTrue(ScheduleRequest.isGavPartiallyDefined(new ArtifactInfo("g", "", "")));
        assertTrue(ScheduleRequest.isGavPartiallyDefined(new ArtifactInfo("g", "a", "")));
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