package com.ericsson.cifwk.taf.executor.schedule.parser;

import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleGavLocation;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleLocation;
import com.ericsson.cifwk.taf.executor.schedule.ScheduleException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ScheduleIncludeStackTest {

    private ScheduleIncludeStack includeStack;

    @Before
    public void setUp() throws Exception {
        includeStack = new ScheduleIncludeStack(ScheduleGavLocation.of("g", "a1", "f1"));
    }

    @Test
    public void testPushNull() throws Exception {
        try {
            includeStack.push(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    @Test
    public void testPushPop() throws Exception {
        checkInvalidPush("g", "a1", "f1");
        checkPush("g", "a1", "f2");
        checkPush("g", "a2", "f1");
        checkInvalidPush("g", "a1", "f1");
        checkInvalidPush("g", "a1", "f2");
        checkInvalidPush("g", "a2", "f1");
        checkPop("g", "a2", "f1");
        checkPop("g", "a1", "f2");
        checkInvalidPush("g", "a1", "f1");
        checkPush("g", "a1", "f2");
        checkPop("g", "a1", "f2");
        checkInvalidPop();
    }

    private void checkPush(String groupId, String artifactId, String name) {
        includeStack.push(ScheduleGavLocation.of(groupId, artifactId, name));
    }

    private void checkInvalidPush(String groupId, String artifactId, String name) {
        try {
            checkPush(groupId, artifactId, name);
            fail();
        } catch (ScheduleException expected) {
        }
    }

    private void checkPop(String groupId, String artifactId, String name) {
        ScheduleLocation include = includeStack.pop();
        assertEquals(ScheduleGavLocation.of(groupId, artifactId, name), include);
    }

    private void checkInvalidPop() {
        try {
            includeStack.pop();
            fail();
        } catch (ScheduleException expected) {
        }
    }

}
