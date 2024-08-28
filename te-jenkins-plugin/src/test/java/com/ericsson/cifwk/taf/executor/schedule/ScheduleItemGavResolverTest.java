package com.ericsson.cifwk.taf.executor.schedule;

import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleComponent;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ScheduleItemGavResolverTest {

    @Test
    public void shouldResolveItems() throws Exception {
        ScheduleComponent scheduleArtifact = new ScheduleComponent("schedule-groupId", "schedule.artifactId", "1.2.3");
        String testWareArtifact = "testware-groupId:testware.artifactId:4.5.6";

        ScheduleItemGavResolver gavResolver = new ScheduleItemGavResolver(scheduleArtifact,
                Collections.singleton(testWareArtifact));

        ScheduleComponent component = gavResolver.resolve(new ScheduleComponent("schedule-groupId",
                "schedule.artifactId", "9.8.7"));
        assertEquals("9.8.7", component.getVersion());

        component = gavResolver.resolve(new ScheduleComponent("testware-groupId", "testware.artifactId"));
        assertEquals("4.5.6", component.getVersion());

        component = gavResolver.resolve(new ScheduleComponent("schedule-groupId", "schedule.artifactId"));
        assertEquals("1.2.3", component.getVersion());

        component = gavResolver.resolve(new ScheduleComponent("schedule-groupId", "schedule.anotherArtifactId"));
        assertNull(component);

    }

}
