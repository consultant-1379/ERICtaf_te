package com.ericsson.cifwk.taf.executor.schedule.parser;

import com.ericsson.cifwk.taf.executor.ArtifactHelper;
import com.ericsson.cifwk.taf.executor.api.schedule.model.Schedule;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleChild;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleGavLocation;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleItem;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleLocation;
import com.ericsson.cifwk.taf.executor.schedule.InvalidScheduleException;
import com.ericsson.cifwk.taf.executor.schedule.ScheduleGavLoader;
import com.ericsson.cifwk.taf.executor.schedule.ScheduleItemGavResolver;
import com.ericsson.cifwk.taf.executor.schedule.model.SampleSchedules;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class ScheduleParserGavScheduleTest extends AbstractScheduleParserTest {

    private ScheduleGavLoader loader;
    private ScheduleParser parser;

    @Before
    public void setUp() throws Exception {
        ScheduleItemGavResolver itemGavResolver = mock(ScheduleItemGavResolver.class);
        loader = new ScheduleGavLoader("REPOSITORY_URL", itemGavResolver, mock(ArtifactHelper.class));
        loader = spy(loader);
        parser = new ScheduleParserFactory().create(loader);
    }

    @Test
    public void testSimpleParse() throws Exception {
        String xml = loadResource("schedule/xml/cdb_full_no_include.xml");
        Schedule parsedSchedule = parser.parse(xml, mock(ScheduleLocation.class));
        Schedule expectedSchedule = SampleSchedules.sampleCdbFull();

        assertEquals(expectedSchedule, parsedSchedule);

        List<ScheduleChild> children = parsedSchedule.getChildren();
        ScheduleItem item1 = (ScheduleItem) children.get(0);
        assertEquals(123, item1.getTimeoutInSeconds());
        ScheduleItem item2 = (ScheduleItem) children.get(1);
        assertEquals(0, item2.getTimeoutInSeconds());
    }

    @Test(expected = InvalidScheduleException.class)
    public void shouldFailOnInvalidSchedule() throws Exception {
        parser.parse(loadResource("schedule/xml/cdb_invalid.xml"), mock(ScheduleLocation.class));
    }

    @Test(expected = InvalidScheduleException.class)
    public void shouldFailOnInvalidIncludedSchedule() throws Exception {
        doReturn(loadResource("schedule/xml/cdb_invalid.xml"))
                .when(loader).load(eq(ScheduleGavLocation.of("g.r", "s1", "cdb_invalid.xml")));
        parser.parse(loadResource("schedule/xml/cdb_with_invalid_include.xml"), mock(ScheduleLocation.class));
    }

    @Test
    public void testRecursiveGavIncludes() throws Exception {
        // Recursive includes are simulated by setting the root schedule location
        // to be equal to schedule location included inside.
        ScheduleLocation location = ScheduleGavLocation.of("g.r", "s1", "cdb_for_inclusion.xml");
        verifyRecursiveIncludeDenial("schedule/xml/cdb_with_include.xml", location, "Recursive include"); // includes cdb_for_inclusion.xml

        doReturn(loadResource("schedule/xml/cdb_with_include.xml"))
                .when(loader).load(eq(ScheduleGavLocation.of("g.r", "s1", "cdb_with_include.xml")));
        verifyRecursiveIncludeDenial("schedule/xml/cdb_with_deep_include.xml", location, "Recursive include");
    }

    @Override
    protected ScheduleParser getParser() {
        return parser;
    }
}
