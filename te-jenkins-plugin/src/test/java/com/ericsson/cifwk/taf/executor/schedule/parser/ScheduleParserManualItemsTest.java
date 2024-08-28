package com.ericsson.cifwk.taf.executor.schedule.parser;

import com.ericsson.cifwk.taf.executor.ArtifactHelper;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ManualTestData;
import com.ericsson.cifwk.taf.executor.api.schedule.model.Schedule;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleChild;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleItemGroup;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleLocation;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleManualItem;
import com.ericsson.cifwk.taf.executor.schedule.ScheduleGavLoader;
import com.ericsson.cifwk.taf.executor.schedule.ScheduleItemGavResolver;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class ScheduleParserManualItemsTest extends AbstractScheduleParserTest {

    private ScheduleGavLoader loader;
    private ScheduleParser parser;

    @Before
    public void setUp() throws Exception {
        ScheduleItemGavResolver itemGavResolver = mock(ScheduleItemGavResolver.class);
        loader = new ScheduleGavLoader("REPOSITORY_URL", itemGavResolver, mock(ArtifactHelper.class));
        loader = spy(loader);
        parser = new ScheduleParserFactory().create(loader);
        autoResolveIncludes(loader);
    }

    @Test
    public void shouldParseWithManualItems() throws Exception {
        Schedule parsedSchedule = getSchedule();
        ScheduleItemGroup topGroup = (ScheduleItemGroup) parsedSchedule.getChildren().get(0);
        ScheduleChild manualTestItem = topGroup.getChildren().get(1);
        assertThat(manualTestItem, instanceOf(ScheduleManualItem.class));
        ManualTestData manualTestData = ((ScheduleManualItem) manualTestItem).getManualTestData();
        Set<String> testCampaignIds = manualTestData.getTestCampaignIds();
        assertThat(testCampaignIds, hasSize(3));
        assertThat(testCampaignIds, hasItems("1", "2", "3"));
    }

    @Test
    public void shouldBeAbleToSerializeParsedSchedule() throws Exception {
        // Make sure there are no StackOverflow or similar errors
        Schedule parsedSchedule = getSchedule();
        assertThat(new Gson().toJson(parsedSchedule), not(isEmptyString()));
    }

    private Schedule getSchedule() throws IOException {
        String xml = loadResource("schedule/xml/with_manual_items.xml");
        return parser.parse(xml, mock(ScheduleLocation.class));
    }

    @Override
    protected ScheduleParser getParser() {
        return parser;
    }

}
