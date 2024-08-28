package com.ericsson.cifwk.taf.executor.schedule;

import com.ericsson.cifwk.taf.executor.ArtifactHelper;
import com.ericsson.cifwk.taf.executor.api.schedule.model.Schedule;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleComponent;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleEnvironmentProperty;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleGavLocation;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleLocation;
import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;
import com.ericsson.cifwk.taf.executor.model.ScheduleBuildParameters;
import com.ericsson.cifwk.taf.executor.schedule.model.SampleSchedules;
import com.ericsson.cifwk.taf.executor.schedule.parser.ScheduleParser;
import com.ericsson.cifwk.taf.executor.schedule.parser.ScheduleParserFactory;
import com.ericsson.duraci.datawrappers.ExecutionId;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class FlowEmitterTest {

    private FlowEmitterFactory emitterFactory;
    private GlobalTeSettings globalTeSettings = new GlobalTeSettings();
    private ScheduleBuildParameters mainBuildParameters = new ScheduleBuildParameters();
    private static final ExecutionId executionId = new ExecutionId("scheduleItemExecutionId");

    @Before
    public void setUp() {
        mainBuildParameters.setExecutionId("executionId");
        globalTeSettings.setAgentJobsMap(getLabelToJobsMap());
        emitterFactory = new FlowEmitterFactory() {
            @Override
            public FlowEmitter create(Schedule schedule, ScheduleItemGavResolver scheduleItemGavResolver,
                                      GlobalTeSettings globalTeSettings, ScheduleBuildParameters mainBuildParameters) {
                return new FlowEmitter(schedule, scheduleItemGavResolver, globalTeSettings, mainBuildParameters) {
                    @Override
                    protected ExecutionId createScheduleItemExecutionId() {
                        return executionId;
                    }

                    @Override
                    String serializeToJson(Object pojo) {
                        if (pojo instanceof List) {
                            List<?> list = (List<?>) pojo;
                            if (!list.isEmpty() && list.get(0) instanceof ScheduleEnvironmentProperty) {
                                sortEnvironmentProperties((List<ScheduleEnvironmentProperty>) list);
                            }
                        }
                        return super.serializeToJson(pojo);
                    }

                    private void sortEnvironmentProperties(List<ScheduleEnvironmentProperty> environmentProperties) {
                        // Need to be sorted for flow text to always match the result
                        Collections.sort(environmentProperties, (o1, o2) -> {
                            int typesCompared = o1.getType().compareTo(o2.getType());
                            int keysCompared = o1.getKey().compareTo(o2.getKey());
                            return typesCompared != 0 ? typesCompared :
                                    (keysCompared != 0 ? keysCompared : o1.getValue().compareTo(o2.getValue()));
                        });
                    }
                };
            }
        };
    }

    @Test
    public void testEmitSchedule() throws Exception {
        URL resource = Resources.getResource("schedule/flow/cdb_full_no_include.txt");
        String expectedFlow = Resources.toString(resource, Charsets.UTF_8).replaceAll("\\s+", "");

        Schedule schedule = SampleSchedules.sampleCdbFull();
        ScheduleItemGavResolver resolver = new ScheduleItemGavResolver(new ScheduleComponent("g.r", "a1", "1.0"),
                testWareComponents());
        globalTeSettings.setReportsHost("reportsHost");
        globalTeSettings.setLocalReportsStorage("localReportsStorage");
        FlowEmitter flowEmitter = getFlowEmitter(schedule, resolver);
        String flow = flowEmitter.emit();

        assertEquals(expectedFlow, flow.replaceAll("\\s+", ""));
    }

    @Test
    public void testEmitScheduleWithMissingItem() throws Exception {
        URL resource = Resources.getResource("schedule/flow/simple_flow.txt");
        String expectedFlow = Resources.toString(resource, Charsets.UTF_8).replaceAll("\\s+", "");

        Schedule schedule = SampleSchedules.simpleFlow();
        ScheduleItemGavResolver resolver = new ScheduleItemGavResolver(new ScheduleComponent("g.r", "s1", "1.0"),
                lessTestWareGAVs());
        globalTeSettings.setReportsHost("reportsHost");
        globalTeSettings.setLocalReportsStorage("localReportsStorage");
        FlowEmitter flowEmitter = getFlowEmitter(schedule, resolver);
        String flow = flowEmitter.emit();

        assertEquals(expectedFlow, flow.replaceAll("\\s+", ""));
    }

    @Test
    public void testEmitScheduleWithNestedItemGroups() throws Exception {
        String scheduleXml = loadResource("schedule/xml/cdb_with_parallel_item_groups.xml");
        String expectedFlow = loadResource("schedule/flow/cdb_with_parallel_groups.txt").replaceAll("\\s+", "");

        ScheduleItemGavResolver resolver = new ScheduleItemGavResolver(new ScheduleComponent("g.r", "s1", "1.0"),
                moreTestWareComponents());
        ScheduleGavLoader loader = getScheduleGavLoaderMock(resolver);
        ScheduleParser parser = new ScheduleParserFactory().create(loader);
        Schedule schedule = parser.parse(scheduleXml, mock(ScheduleLocation.class));

        FlowEmitter flowEmitter = getFlowEmitter(schedule, resolver);
        String flow = flowEmitter.emit();

        assertEquals(expectedFlow, flow.replaceAll("\\s+", ""));
    }

    @Test
    public void testEmitScheduleWithEnvironmentProperties() throws Exception {
        String scheduleXml = loadResource("schedule/xml/with_env_properties.xml");
        String expectedFlow = loadResource("schedule/flow/with_env_properties.txt").replaceAll("\\s+", "");

        ScheduleItemGavResolver resolver = new ScheduleItemGavResolver(new ScheduleComponent("g.r", "s1", "1.0"),
                moreTestWareComponents());
        ScheduleGavLoader loader = getScheduleGavLoaderMock(resolver);
        ScheduleParser parser = new ScheduleParserFactory().create(loader);
        Schedule schedule = parser.parse(scheduleXml, mock(ScheduleLocation.class));

        FlowEmitter flowEmitter = getFlowEmitter(schedule, resolver);
        String flow = flowEmitter.emit();

        assertEquals(expectedFlow, flow.replaceAll("\\s+", ""));
    }

    @Test
    public void testEmitScheduleWithManualTestItems() throws Exception {
        String scheduleXml = loadResource("schedule/xml/with_manual_items.xml");
        String expectedFlow = loadResource("schedule/flow/with_manual_items.txt").replaceAll("\\s+", "");

        ScheduleItemGavResolver resolver = new ScheduleItemGavResolver(new ScheduleComponent("g.r", "s1", "1.0"),
                moreTestWareComponents());
        ScheduleGavLoader loader = getScheduleGavLoaderMock(resolver);
        ScheduleParser parser = new ScheduleParserFactory().create(loader);
        Schedule schedule = parser.parse(scheduleXml, mock(ScheduleLocation.class));

        FlowEmitter flowEmitter = getFlowEmitter(schedule, resolver);
        String flow = flowEmitter.emit();

        assertEquals(expectedFlow, flow.replaceAll("\\s+", ""));
    }

    @Test
    public void shouldEscapeFlowParamValue() {
        FlowEmitter unit = new FlowEmitter(null, null, null, null);
        assertEquals("Item\\'s No. 1", unit.escapeFlowParamValue("Item's No. 1"));
    }

    @Test
    public void shouldScheduleJobsBasedOnAgentLabel() throws IOException {
        String scheduleXml = loadResource("schedule/xml/with_agent_label.xml");
        String expectedFlow = loadResource("schedule/flow/with_agent_labels.txt").replaceAll("\\s+", "");

        ScheduleItemGavResolver resolver = new ScheduleItemGavResolver(new ScheduleComponent("g.r", "s1", "1.0"),
                moreTestWareComponents());
        ScheduleGavLoader loader = getScheduleGavLoaderMock(resolver);
        ScheduleParser parser = new ScheduleParserFactory().create(loader);
        Schedule schedule = parser.parse(scheduleXml, mock(ScheduleLocation.class));

        FlowEmitter flowEmitter = getFlowEmitter(schedule, resolver);
        String flow = flowEmitter.emit();

        assertEquals(expectedFlow, flow.replaceAll("\\s+", ""));
    }

    @Test
    public void shouldGetJobNameForLabel() throws Exception {
        FlowEmitter unit = new FlowEmitter(null, null, null, null);
        Map<String, String> labelToJobsMap = getLabelToJobsMap();
        assertThat(unit.getJobNameForLabel(labelToJobsMap, "taf")).isEqualTo("TEST_EXECUTOR");
        assertThat(unit.getJobNameForLabel(labelToJobsMap, "")).isEqualTo("TEST_EXECUTOR");
        assertThat(unit.getJobNameForLabel(labelToJobsMap, "uber")).isEqualTo("UBER_TEST_EXECUTOR");
    }

    private Map<String, String> getLabelToJobsMap() {
        Map<String, String> labelToJobsMap = newHashMap();
        labelToJobsMap.put("taf", "TEST_EXECUTOR");
        labelToJobsMap.put("uber", "UBER_TEST_EXECUTOR");
        return labelToJobsMap;
    }

    private ScheduleGavLoader getScheduleGavLoaderMock(ScheduleItemGavResolver resolver) {
        ScheduleGavLoader loader = new ScheduleGavLoader("REPOSITORY_URL", resolver, mock(ArtifactHelper.class));
        loader = spy(loader);
        doAnswer(invocation -> {
            Object[] arguments = invocation.getArguments();
            ScheduleGavLocation location = (ScheduleGavLocation) arguments[0];
            String xmlFileName = location.getName();
            return loadResource("schedule/xml/" + xmlFileName);
        }).when(loader).load(any(ScheduleGavLocation.class));
        return loader;
    }

    private FlowEmitter getFlowEmitter(Schedule schedule, ScheduleItemGavResolver resolver) {
        return emitterFactory.create(schedule, resolver, globalTeSettings, mainBuildParameters);
    }

    private String loadResource(String name) throws IOException {
        URL resource = Resources.getResource(name);
        return Resources.toString(resource, Charsets.UTF_8);
    }

    private List<String> lessTestWareGAVs() {
        return Arrays.asList("g.r:a1:1.0", "g.r:a3:3.1");
    }

    private Collection<String> testWareComponents() {
        return Arrays.asList(
                "g.r:a1:1.0",
                "g.r:a2:1.1",
                "g.r:a3:1.2"
        );
    }

    private Collection<String> moreTestWareComponents() {
        return Arrays.asList(
                "g.r:a1:1.0",
                "g.r:a2:1.1",
                "g.r:a3:1.2",
                "g.r:a4:1.3"
        );
    }

}

