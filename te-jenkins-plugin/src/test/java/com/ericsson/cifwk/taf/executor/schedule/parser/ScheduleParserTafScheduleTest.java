package com.ericsson.cifwk.taf.executor.schedule.parser;

import com.ericsson.cifwk.taf.executor.api.schedule.model.Schedule;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleChild;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleItem;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleItemGroup;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleLoader;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleLocation;
import com.ericsson.cifwk.taf.executor.schedule.InvalidScheduleException;
import com.ericsson.cifwk.taf.executor.schedule.TafScheduleLoader;
import com.ericsson.oss.axis.interfaces.scheduler.TafScheduleInfo;
import com.ericsson.oss.axis.interfaces.scheduler.TafSchedulerService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ScheduleParserTafScheduleTest extends AbstractScheduleParserTest {

    private ScheduleParser parser;

    @Before
    public void setUp() throws Exception {
        TafSchedulerService tafSchedulerService = mock(TafSchedulerService.class);
        when(tafSchedulerService.getScheduleById(eq(123L))).thenReturn(withXmlFrom("schedule/xml/taf_scheduler/simple_include.xml"));
        when(tafSchedulerService.getScheduleById(eq(234L))).thenReturn(withXmlFrom("schedule/xml/taf_scheduler/1st_level_include.xml"));
        when(tafSchedulerService.getScheduleById(eq(345L))).thenReturn(withXmlFrom("schedule/xml/taf_scheduler/2nd_level_include.xml"));
        when(tafSchedulerService.getScheduleById(eq(777L))).thenReturn(withXmlFrom("schedule/xml/taf_scheduler/recursive_include.xml"));
        ScheduleLoader loader = new TafScheduleLoader(tafSchedulerService);
        loader = spy(loader);
        parser = new ScheduleParserFactory().create(loader);
    }

    private TafScheduleInfo withXmlFrom(String pathToScheduleXml) throws Exception {
        String scheduleXml = loadResource(pathToScheduleXml);
        TafScheduleInfo tafScheduleInfo = new TafScheduleInfo();
        tafScheduleInfo.setXml(scheduleXml);
        return tafScheduleInfo;
    }

    @Test
    @Ignore("temporarily until validation is productified")
    public void shouldParseEmbeddedTafSchedules() throws Exception {
        String xml = loadResource("schedule/xml/taf_scheduler/with_simple_include.xml");
        ScheduleLocation scheduleLocation = mock(ScheduleLocation.class);
        Schedule parsedSchedule = parser.parse(xml, scheduleLocation);

        String expectedScheduleXml = loadResource("schedule/xml/taf_scheduler/with_simple_include_resolved.xml");
        assertEquals(parser.parse(expectedScheduleXml, scheduleLocation), parsedSchedule);

        List<ScheduleChild> children = parsedSchedule.getChildren();

        ScheduleItem primaryItem = (ScheduleItem) children.get(0);
        assertEquals("Primary", primaryItem.getName());
        List<String> suites = primaryItem.getSuites();
        assertThat(suites, hasSize(2));
        assertThat(suites, hasItem("one.xml"));
        assertThat(suites, hasItem("two.xml"));

        ScheduleItemGroup includedItemGroup = (ScheduleItemGroup) children.get(1);
        List<ScheduleChild> includedItemGroupChildren = includedItemGroup.getChildren();
        assertThat(includedItemGroupChildren, hasSize(1));
        ScheduleItem includedItem = (ScheduleItem) includedItemGroupChildren.get(0);

        assertEquals("Included", includedItem.getName());
        List<String> includedItemSuites = includedItem.getSuites();
        assertThat(includedItemSuites, hasSize(1));
        assertThat(includedItemSuites, hasItem("three.xml"));
    }

    @Test
    public void shouldParseMultipleEmbeddedTafSchedules() throws Exception {
        String xml = loadResource("schedule/xml/taf_scheduler/with_few_levels_of_includes.xml");
        ScheduleLocation scheduleLocation = mock(ScheduleLocation.class);
        Schedule parsedSchedule = parser.parse(xml, scheduleLocation);

        String expectedScheduleXml = loadResource("schedule/xml/taf_scheduler/with_few_levels_of_includes_resolved.xml");
        assertEquals(parser.parse(expectedScheduleXml, scheduleLocation), parsedSchedule);
    }

    @Test
    public void shouldFailOnInvalidIncludedSchedule() throws Exception {
        assertThatExceptionOfType(InvalidScheduleException.class)
                .isThrownBy(() -> parser.parse(loadResource("schedule/xml/taf_scheduler/with_invalid_include.xml"), mock(ScheduleLocation.class)))
                .withMessageContaining("Provided schedule is malformed");
    }

    @Test
    public void shouldFailOnRecursiveIncludes() throws Exception {
        ScheduleLocation location = mock(ScheduleLocation.class);
        verifyRecursiveIncludeDenial(
                "schedule/xml/taf_scheduler/with_recursive_includes.xml",
                location,
                "Recursive include: TAF Schedule, ID=777");
    }

    @Override
    protected ScheduleParser getParser() {
        return parser;
    }
}
