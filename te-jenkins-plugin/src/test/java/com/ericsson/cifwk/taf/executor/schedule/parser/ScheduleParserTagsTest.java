package com.ericsson.cifwk.taf.executor.schedule.parser;

import com.ericsson.cifwk.taf.executor.ArtifactHelper;
import com.ericsson.cifwk.taf.executor.api.schedule.model.Schedule;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleChild;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleItem;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleLocation;
import com.ericsson.cifwk.taf.executor.schedule.InvalidScheduleException;
import com.ericsson.cifwk.taf.executor.schedule.ScheduleGavLoader;
import com.ericsson.cifwk.taf.executor.schedule.ScheduleItemGavResolver;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

public class ScheduleParserTagsTest extends AbstractScheduleParserTest {

    private ScheduleParser unit;

    @Before
    public void setUp() throws Exception {
        ScheduleItemGavResolver itemGavResolver = mock(ScheduleItemGavResolver.class);
        ScheduleGavLoader loader = new ScheduleGavLoader("REPOSITORY_URL", itemGavResolver, mock(ArtifactHelper.class));
        unit = new ScheduleParserFactory().create(loader);
    }

    @Test
    public void shouldGetGroupsFromTags() throws Exception {
        String xml = loadResource("schedule/xml/with_tags.xml");
        Schedule schedule = unit.parse(xml, mock(ScheduleLocation.class));
        List<ScheduleChild> items = schedule.getChildren();

        assertThat(((ScheduleItem) items.get(0)).getGroups()).contains("rfa250", "java");
        assertThat(((ScheduleItem) items.get(1)).getGroups()).contains("long");
    }

    @Test
    public void shouldNotHaveGroupsAndTags() throws Exception {
        String xml = loadResource("schedule/xml/with_tags_and_groups.xml");
        assertThatExceptionOfType(InvalidScheduleException.class)
                .isThrownBy(() -> unit.parse(xml, mock(ScheduleLocation.class)))
                .withMessageContaining("Provided schedule is malformed");
    }

    @Override
    protected ScheduleParser getParser() {
        return unit;
    }
}
