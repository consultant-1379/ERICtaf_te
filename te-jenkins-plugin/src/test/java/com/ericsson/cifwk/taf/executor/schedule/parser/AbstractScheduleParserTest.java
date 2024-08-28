package com.ericsson.cifwk.taf.executor.schedule.parser;

import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleGavLocation;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleLocation;
import com.ericsson.cifwk.taf.executor.schedule.ScheduleException;
import com.ericsson.cifwk.taf.executor.schedule.ScheduleGavLoader;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 02/02/2016
 */
public abstract class AbstractScheduleParserTest {

    protected String loadResource(String name) throws IOException {
        URL resource = Resources.getResource(name);
        return Resources.toString(resource, Charsets.UTF_8);
    }

    protected void verifyRecursiveIncludeDenial(String pathToScheduleXml, ScheduleLocation location, String expectedChuck) throws IOException {
        String xml = loadResource(pathToScheduleXml);
        try {
            getParser().parse(xml, location);
            fail();
        } catch (ScheduleException expected) {
            assertThat(expected.getMessage(), containsString(expectedChuck));
        }
    }

    protected void autoResolveIncludes(ScheduleGavLoader loader) {
        doAnswer(invocation -> {
            Object[] arguments = invocation.getArguments();
            ScheduleGavLocation location = (ScheduleGavLocation) arguments[0];
            String xmlFileName = location.getName();
            return loadResource("schedule/xml/" + xmlFileName);
        }).when(loader).load(any(ScheduleGavLocation.class));
    }

    protected abstract ScheduleParser getParser();

}
