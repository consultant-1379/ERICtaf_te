package com.ericsson.cifwk.taf.executor.schedule.parser;

import com.ericsson.cifwk.taf.executor.schedule.InvalidScheduleException;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ScheduleValidatorTest {

    private ScheduleValidator validator;

    @Before
    public void setUp() throws Exception {
        validator = ScheduleValidator.withDefaultSchema();
    }

    @Test
    public void testNoNamespace() throws Exception {
        assertTrue(isValid("cdb_full_no_include"));
        assertTrue(isValid("cdb_with_include"));
    }

    @Test
    public void testAcceptUnderscores() throws Exception {
        assertTrue(isValid("cdb_with_underscores"));
    }

    @Test
    public void testLatestVersion() throws Exception {
        assertTrue(isValid("cdb_with_latest"));
    }

    @Test
    public void testInvalid() throws Exception {
        assertFalse(isValid("cdb_invalid"));
    }

    @Test
    public void testFindDuplicateItems() throws Exception {
        assertFalse(isValid("cdb_with_duplicates"));
        assertFalse(isValid("cdb_with_deep_duplicates"));
    }

    @Test
    public void testTafSchedulerUrnUsageInInclude() throws Exception {
        assertTrue(isValid("taf_scheduler/with_simple_include"));
        assertFalse(isValid("taf_scheduler/with_invalid_include"));
    }

    @Test
    public void testEnvPropertiesAllowedInsideItemGroup() throws Exception {
        assertTrue(isValid("with_env_properties"));
    }

    @Test
    public void testSnapshotVersion() throws Exception {
        assertTrue(isValid("cdb_with_snapshot"));
    }

    private boolean isValid(String name) throws IOException {
        URL resource = Resources.getResource("schedule/xml/" + name + ".xml");
        String xml = Resources.toString(resource, Charsets.UTF_8);
        try {
            validator.validate(xml);
            return true;
        } catch (InvalidScheduleException e) {
            return false;
        }
    }

}
