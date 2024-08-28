package com.ericsson.cifwk.taf.executor.schedule.parser;

import com.ericsson.cifwk.taf.executor.ArtifactHelper;
import com.ericsson.cifwk.taf.executor.api.schedule.model.EnvironmentPropertiesAwareItem;
import com.ericsson.cifwk.taf.executor.api.schedule.model.Schedule;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleChild;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleEnvironmentProperty;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleItemGroup;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleLocation;
import com.ericsson.cifwk.taf.executor.schedule.ScheduleGavLoader;
import com.ericsson.cifwk.taf.executor.schedule.ScheduleItemGavResolver;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.ericsson.cifwk.taf.executor.schedule.model.ScheduleHelper.envProperties;
import static com.ericsson.cifwk.taf.executor.schedule.model.ScheduleHelper.envProperty;
import static com.ericsson.cifwk.taf.executor.schedule.model.ScheduleHelper.schedule;
import static com.ericsson.cifwk.taf.executor.schedule.model.ScheduleHelper.scheduleItem;
import static com.ericsson.cifwk.taf.executor.schedule.model.ScheduleHelper.scheduleItemGroup;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class ScheduleParserEnvPropsTest extends AbstractScheduleParserTest {

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
    public void shouldParseWithEnvProperties() throws Exception {
        Schedule parsedSchedule = getSchedule();
        Schedule expectedSchedule = scheduleWithEnvProperties();
        assertEquals(expectedSchedule, parsedSchedule);
        verifyEffectiveEnvProperties(parsedSchedule);
    }

    @Test
    public void shouldBeAbleToSerializeParsedSchedule() throws Exception {
        // Make sure there are no StackOverflow errors
        Schedule parsedSchedule = getSchedule();
        assertThat(new Gson().toJson(parsedSchedule), not(isEmptyString()));
    }

    private Schedule getSchedule() throws IOException {
        String xml = loadResource("schedule/xml/with_env_properties.xml");
        return parser.parse(xml, mock(ScheduleLocation.class));
    }

    private void verifyEffectiveEnvProperties(Schedule parsedSchedule) {
        List<ScheduleChild> children = parsedSchedule.getChildren();

        ScheduleItemGroup group1 = (ScheduleItemGroup) children.get(0);
        List<ScheduleChild> group1Children = group1.getChildren();
        assertHavingEffectiveEnvProperties(group1Children.get(0), 3,
                envProperty("system", "systemOption1", "systemOption1ItemGroupValue"),
                envProperty("maven", "maven_opts", "-Xms1024m -Xmx4096m -XX:PermSize=1024m"),
                envProperty("jvm", "version", "7")
        );
        assertHavingEffectiveEnvProperties(group1Children.get(1), 3,
                envProperty("system", "systemOption1", "systemOption1ItemGroupValue"),
                envProperty("jvm", "version", "8")
        );
        ScheduleItemGroup group11 = (ScheduleItemGroup)group1Children.get(2);
        assertHavingEffectiveEnvProperties(group11.getChildren().get(0), 3,
                envProperty("system", "systemOption1", "systemOption1Include1GlobalValue"),
                envProperty("jvm", "version", "7")
        );

        ScheduleItemGroup group2 = (ScheduleItemGroup) children.get(1);
        List<ScheduleChild> group2Children = group2.getChildren();
        assertHavingEffectiveEnvProperties(group2Children.get(0), 2,
                envProperty("system", "systemOption1", "systemOption1Include2GlobalValue"),
                envProperty("maven", "maven_opts", "-Xms1024m -Xmx4096m -XX:PermSize=1024m")
        );
        ScheduleItemGroup group21 = (ScheduleItemGroup)group2Children.get(1);
        List<ScheduleChild> group21Children = group21.getChildren();
        assertHavingEffectiveEnvProperties(group21Children.get(0), 4,
                envProperty("system", "systemOption1", "systemOption1Include2GlobalValue"),
                envProperty("system", "systemOption2", "systemOption2Value"),
                envProperty("maven", "maven_opts", "-Xms1024m -Xmx4096m -XX:PermSize=1024m"),
                envProperty("maven", "defaultFlags", "-O -U")
        );
        assertHavingEffectiveEnvProperties(group21Children.get(1), 4,
                envProperty("system", "systemOption1", "systemOption1Include2GlobalValue"),
                envProperty("system", "systemOption2", "systemOption2Value"),
                envProperty("maven", "maven_opts", "-Xms1024m -Xmx4096m -XX:PermSize=1024m"),
                envProperty("maven", "defaultFlags", "skipTests")
        );

        assertHavingEffectiveEnvProperties(children.get(2), 2,
                envProperty("system", "systemOption1", "systemOption1GlobalValue"),
                envProperty("maven", "maven_opts", "-Xms1024m -Xmx4096m -XX:PermSize=1024m")
        );
    }

    private void assertHavingEffectiveEnvProperties(ScheduleChild scheduleItemOrGroup, int expectedPropertyAmount,
                                                    ScheduleEnvironmentProperty... expectedEffectiveEnvironmentProperties) {
        assertNotNull(scheduleItemOrGroup.getParent());
        List<ScheduleEnvironmentProperty> effectiveEnvironmentProperties =
                ((EnvironmentPropertiesAwareItem)scheduleItemOrGroup).getEffectiveEnvironmentProperties();
        assertThat(effectiveEnvironmentProperties.size(), equalTo(expectedPropertyAmount));
        for (ScheduleEnvironmentProperty environmentProperty : expectedEffectiveEnvironmentProperties) {
            assertThat(effectiveEnvironmentProperties, hasItem(environmentProperty));
        }
    }

    private Schedule scheduleWithEnvProperties() {
        return schedule(
                envProperties(
                        envProperty("system", "systemOption1", "systemOption1GlobalValue"),
                        envProperty("maven", "maven_opts", "-Xms1024m -Xmx4096m -XX:PermSize=1024m")
                ),
                scheduleItemGroup(
                        false,
                        envProperties(
                                envProperty("system", "systemOption1", "systemOption1ItemGroupValue"),
                                envProperty("jvm", "version", "7")
                        ),
                        scheduleItem("1", "g.r", "a1", "1.0", asList("1.xml"), emptyList(), false, null),
                        scheduleItem("2", "g.r", "a1", "1.0", asList("2.xml"), emptyList(), null, false, null,
                                envProperties(
                                        envProperty("jvm", "version", "8")
                                )
                        ),
                        scheduleItemGroup(
                                false,
                                envProperties(
                                        envProperty("system", "systemOption1", "systemOption1Include1GlobalValue")
                                ),
                                scheduleItem("4", "g.r", "a1", "1.0", asList("4.xml"), emptyList(), false, null)

                        )),
                scheduleItemGroup(
                        false,
                        envProperties(
                                envProperty("system", "systemOption1", "systemOption1Include2GlobalValue")
                        ),
                        scheduleItem("5", "g.r", "a1", "1.0", asList("5.xml"), emptyList(), false, null),
                        scheduleItemGroup(
                                false,
                                envProperties(
                                        envProperty("system", "systemOption2", "systemOption2Value"),
                                        envProperty("maven", "defaultFlags", "-O -U")
                                ),
                                scheduleItem("6", "g.r", "a1", "1.0", asList("6.xml"), emptyList(), false, null),
                                scheduleItem("7", "g.r", "a1", "1.0", asList("7.xml"), emptyList(), null, false, null,
                                        envProperties(
                                                envProperty("maven", "defaultFlags", "skipTests")
                                        )
                                )

                        )
                ),
                scheduleItem("3", "g.r", "a1", "1.0", asList("3.xml"), emptyList(), false, null)
        );
    }

    @Override
    protected ScheduleParser getParser() {
        return parser;
    }
}
