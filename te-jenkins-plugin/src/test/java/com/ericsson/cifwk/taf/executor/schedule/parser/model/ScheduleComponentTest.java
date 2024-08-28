package com.ericsson.cifwk.taf.executor.schedule.parser.model;

import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleComponent;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ScheduleComponentTest {

    @Test
    public void testIsGav() throws Exception {
        assertTrue(ScheduleComponent.isGav("groupId:artifactId:1.2.3"));
        assertFalse(ScheduleComponent.isGav("groupId:artifactId"));
    }
}
