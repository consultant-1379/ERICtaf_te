package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.api.TriggeringTask;
import hudson.model.Build;
import org.hamcrest.collection.IsMapContaining;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class TeRestServiceTest extends AbstractTeRestServiceTest {

    private TeRestService unit = new TeRestService();

    @Test
    public void shouldDeserializeTriggeringEvent() throws Exception {
        TriggeringTask triggeringTask = createTriggeringTask();
        String eventStr = gson.toJson(triggeringTask);
        assertNotNull(unit.deserializeTriggeringTask(eventStr));
    }

    @Test
    public void getCommonTestProperties() {
        TriggeringTask triggeringTask = createTriggeringTaskWithProperties(new Properties(), new Properties());
        Properties deserializedTestProperties = unit.getCommonTestProperties(triggeringTask);
        assertEquals(0, deserializedTestProperties.size());

        Properties commonTestProperties = new Properties();
        commonTestProperties.put("myProperty1", "myValue1");
        commonTestProperties.put("myProperty2", "oldValue");

        triggeringTask = createTriggeringTaskWithProperties(commonTestProperties, new Properties());
        deserializedTestProperties = unit.getCommonTestProperties(triggeringTask);
        assertEquals(2, deserializedTestProperties.size());
        assertThat(deserializedTestProperties,
                IsMapContaining.<Object, Object>hasEntry("myProperty1", "myValue1"));
        assertThat(deserializedTestProperties,
                IsMapContaining.<Object, Object>hasEntry("myProperty2", "oldValue"));
    }

    @Test
    public void mergeTestProperties() {
        Properties commonProperties = new Properties();
        commonProperties.put("a", "b");
        commonProperties.put("b", "c");
        Properties scheduleProperties = new Properties();
        scheduleProperties.put("b", "d");
        scheduleProperties.put("c", "e");
        Properties mergedTestProperties = unit.mergeTestProperties(commonProperties, scheduleProperties);
        assertEquals(3, mergedTestProperties.size());
        assertThat(mergedTestProperties,
                IsMapContaining.<Object, Object>hasEntry("a", "b"));
        assertThat(mergedTestProperties,
                IsMapContaining.<Object, Object>hasEntry("b", "d"));
        assertThat(mergedTestProperties,
                IsMapContaining.<Object, Object>hasEntry("c", "e"));
    }

    @Test
    public void shouldGetItemName() {
        unit = spy(unit);
        doReturn("itemName").doReturn(null).when(unit).getBuildParameter(any(Build.class), anyString());
        TafExecutionBuild build = mock(TafExecutionBuild.class);
        when(build.getDescription()).thenReturn("jobDesc").thenReturn("");
        when(build.getFullDisplayName()).thenReturn("jobDisplayName");
        
        assertEquals("itemName", unit.getItemName(build));
        assertEquals("jobDesc", unit.getItemName(build));
        assertEquals("jobDisplayName", unit.getItemName(build));
    }
}