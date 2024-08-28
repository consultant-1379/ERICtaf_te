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

public class ScheduleParserAgentLabelTest extends AbstractScheduleParserTest {

    private ScheduleParser unit;

    @Before
    public void setUp() throws Exception {
        ScheduleItemGavResolver itemGavResolver = mock(ScheduleItemGavResolver.class);
        ScheduleGavLoader loader = new ScheduleGavLoader("REPOSITORY_URL", itemGavResolver, mock(ArtifactHelper.class));
        unit = new ScheduleParserFactory().create(loader);
    }

    @Test
    public void shouldGetAgentLabelFromXml() throws Exception {
        String xml = loadResource("schedule/xml/with_agent_label.xml");
        Schedule schedule = unit.parse(xml, mock(ScheduleLocation.class));
        List<ScheduleChild> items = schedule.getChildren();

        ScheduleItem item1 = (ScheduleItem) items.get(0);
        assertThat(item1.getAgentLabel()).isEqualTo("taf");

        ScheduleItem item2 = (ScheduleItem) items.get(1);
        assertThat(item2.getAgentLabel()).isEqualTo("uber");

        ScheduleItem item3 = (ScheduleItem) items.get(2);
        assertThat(item3.getAgentLabel()).isNull();
    }

    @Test
    public void shouldFailOnWrongLabel() throws Exception {
        String xml = loadResource("schedule/xml/with_wrong_agent_label.xml");
        assertThatExceptionOfType(InvalidScheduleException.class)
                .isThrownBy(() -> unit.parse(xml, mock(ScheduleLocation.class)))
                .withMessageContaining("Provided schedule is malformed")
                .withMessageContaining("Value 'tough' is not facet-valid");
    }

    @Override
    protected ScheduleParser getParser() {
        return unit;
    }
}
