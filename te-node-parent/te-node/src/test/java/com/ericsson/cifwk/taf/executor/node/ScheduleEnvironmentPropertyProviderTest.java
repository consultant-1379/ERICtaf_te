package com.ericsson.cifwk.taf.executor.node;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.io.PrintStream;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class ScheduleEnvironmentPropertyProviderTest {

    private PrintStream buildLog = mock(PrintStream.class);

    private static final String DEFAULT_PROPERTY_JSON = "[" +
            "{\"type\":\"type\",\"key\":\"opts\",\"value\":\"blah\"}," +
            "{\"type\":\"type\",\"key\":\"int\",\"value\":\"8\"}," +
            "{\"type\":\"jvm\",\"key\":\"version\",\"value\":\"8\"}" +
            "]";

    @Test
    public void testFindProperty() throws Exception {
        ScheduleEnvironmentPropertyProvider unit = new ScheduleEnvironmentPropertyProvider(buildLog, DEFAULT_PROPERTY_JSON);
        assertEquals(8, unit.findProperty("type", "int", Integer.class).intValue());
        assertEquals("blah", unit.findProperty("type", "opts"));
        assertNull(unit.findProperty("type", "noSuchProperty"));
        assertNull(unit.findProperty("type", "noSuchProperty", Integer.class));

        unit = new ScheduleEnvironmentPropertyProvider(buildLog, "");
        assertNull(unit.findProperty("type", "noSuchProperty"));
    }

    @Test
    public void testFindPropertiesWithExclusions() throws Exception {
        ScheduleEnvironmentPropertyProvider unit = new ScheduleEnvironmentPropertyProvider(buildLog, DEFAULT_PROPERTY_JSON);
        unit.setExcludedSettings("type", Sets.newHashSet("opts"));
        unit.setExcludedSettings("jvm", Sets.newHashSet("version"));

        assertEquals(8, unit.findProperty("type", "int", Integer.class).intValue());
        assertNull(unit.findProperty("type", "opts"));
        assertNull(unit.findProperty("jvm", "version"));
    }

    @Test
    public void shouldGetRequiredJavaVersion() throws Exception {
        ScheduleEnvironmentPropertyProvider unit = new ScheduleEnvironmentPropertyProvider(buildLog, DEFAULT_PROPERTY_JSON);
        assertEquals(8, unit.getRequiredJavaVersion().intValue());
    }

    @Test
    public void shouldGetAllPropertiesOfType() throws Exception {
        ScheduleEnvironmentPropertyProvider unit = new ScheduleEnvironmentPropertyProvider(buildLog, DEFAULT_PROPERTY_JSON);
        Map<String, String> systemProperties = unit.getAllPropertiesOfType("type");
        assertEquals(2, systemProperties.size());
    }

    @Test
    public void shouldGetAllPropertiesOfTypeWithExclusions() throws Exception {
        ScheduleEnvironmentPropertyProvider unit = new ScheduleEnvironmentPropertyProvider(buildLog, DEFAULT_PROPERTY_JSON);
        unit.setExcludedSettings("type", Sets.newHashSet("opts"));
        Map<String, String> systemProperties = unit.getAllPropertiesOfType("type");
        assertEquals(1, systemProperties.size());
        assertNotNull(systemProperties.get("int"));
    }
}