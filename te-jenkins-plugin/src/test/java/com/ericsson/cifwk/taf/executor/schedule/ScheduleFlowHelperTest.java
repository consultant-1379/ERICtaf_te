package com.ericsson.cifwk.taf.executor.schedule;

import com.ericsson.cifwk.taf.executor.ArtifactHelper;
import com.ericsson.cifwk.taf.executor.api.ScheduleSource;
import com.ericsson.cifwk.taf.executor.api.schedule.model.Schedule;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleComponent;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleGavLocation;
import com.ericsson.cifwk.taf.executor.api.schedule.model.TafScheduleLocation;
import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;
import com.ericsson.cifwk.taf.executor.model.ScheduleBuildParameters;
import com.ericsson.cifwk.taf.executor.schedule.model.SampleSchedules;
import com.ericsson.cifwk.taf.executor.schedule.parser.ScheduleParser;
import com.ericsson.cifwk.taf.executor.schedule.parser.ScheduleParserFactory;
import com.ericsson.duraci.datawrappers.ExecutionId;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.simpleframework.xml.core.Persister;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScheduleFlowHelperTest {

    private static final String REPOSITORY_URL = "repositoryUrl";
    private static final String CONFIG_URL = "configUrl";
    private static final String SCHEDULE_ARTIFACT = "g.r:s1:1.2";

    private static final String REPORTS_HOST = "reportsHost";
    private static final String LOCAL_REPORTS_STORAGE = "localReportsStorage";
    private static final ExecutionId scheduleItemExecutionId = new ExecutionId("scheduleItemExecutionId");
    private static final String SCHEDULE_NAME = "schedule.xml";

    private ScheduleGavLoader loader;
    private ScheduleFlowHelper helper;
    private GlobalTeSettings globalTeSettings = new GlobalTeSettings();
    private ScheduleBuildParameters mainBuildParameters = new ScheduleBuildParameters();

    @Before
    public void setUp() {
        loader = new ScheduleGavLoader(REPOSITORY_URL, mock(ScheduleItemGavResolver.class), mock(ArtifactHelper.class));
        loader = spy(loader);

        ScheduleLoaderFactory loaderFactory = mock(ScheduleLoaderFactory.class);
        when(loaderFactory.createForGavSchedule(eq(REPOSITORY_URL), any(ScheduleItemGavResolver.class))).thenReturn(loader);

        ScheduleParserFactory parserFactory = mock(ScheduleParserFactory.class);
        when(parserFactory.create(same(loader))).thenReturn(new ScheduleParser(new Persister(), loader));

        FlowEmitterFactory emitterFactory = mock(FlowEmitterFactory.class);
        when(emitterFactory.create(
                any(Schedule.class),
                any(ScheduleItemGavResolver.class),
                any(GlobalTeSettings.class),
                any(ScheduleBuildParameters.class)
        )).thenAnswer(new Answer<FlowEmitter>() {
            @Override
            public FlowEmitter answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                return new FlowEmitter(
                        (Schedule) arguments[0],
                        (ScheduleItemGavResolver) arguments[1],
                        (GlobalTeSettings) arguments[2],
                        (ScheduleBuildParameters) arguments[3]
                ) {
                    @Override
                    protected ExecutionId createScheduleItemExecutionId() {
                        return scheduleItemExecutionId;
                    }

                };
            }
        });
        globalTeSettings.setMbExchange("mbExchange");
        globalTeSettings.setMbHostWithPort("mbHost");
        globalTeSettings.setReportMbDomainId("mbDomain");
        globalTeSettings.setReportsHost(REPORTS_HOST);
        globalTeSettings.setLocalReportsStorage(LOCAL_REPORTS_STORAGE);
        globalTeSettings.setAgentJobsMap(getLabelToJobsMap());

        mainBuildParameters.setExecutionId("executionId");
        mainBuildParameters.setRepositoryUrl(REPOSITORY_URL);
        mainBuildParameters.setScheduleArtifact(SCHEDULE_ARTIFACT);
        mainBuildParameters.setRepositoryUrl(REPOSITORY_URL);
        mainBuildParameters.setRepositoryUrl(REPOSITORY_URL);
        mainBuildParameters.setSlaveHosts("slaveHosts");
        mainBuildParameters.setConfigUrl(CONFIG_URL);
        mainBuildParameters.setAllureLogDir("allureLogDir");
        mainBuildParameters.setScheduleSource(ScheduleSource.MAVEN_GAV.toString());
        mainBuildParameters.setScheduleName(SCHEDULE_NAME);

        helper = new ScheduleFlowHelper(parserFactory, loaderFactory, emitterFactory,
                globalTeSettings, mainBuildParameters);
    }

    @Test
    public void testSimple() throws Exception {
        String gavs = "g.r:a1:1.0";
        String xml = loadResource("schedule/xml/cdb_single.xml");
        String expectedFlow = loadResource("schedule/flow/cdb_single.txt").replaceAll("\\s+", "");
        String flow = getFlow(gavs, xml, "cdb_single.xml");
        assertEquals(expectedFlow, flow.replaceAll("\\s+", ""));
    }

    @Test
    public void testGavInclude() throws Exception {
        String gavs = "g.r:a1:1.0,g.r:a2:1.1," + SCHEDULE_ARTIFACT;
        String xml = loadResource("schedule/xml/cdb_with_include.xml");
        String includedXml = loadResource("schedule/xml/cdb_for_inclusion.xml");
        String expectedFlow = loadResource("schedule/flow/cdb_with_include.txt").replaceAll("\\s+", "");
        doReturn(includedXml).when(loader).load(ScheduleGavLocation.of("g.r", "s1", "cdb_for_inclusion.xml"));
        String flow = getFlow(gavs, xml, "cdb_with_include.xml");

        assertEquals(expectedFlow, flow.replaceAll("\\s+", ""));
    }

    @Test
    public void shouldGetSuiteCount() {
        Schedule schedule = SampleSchedules.sampleCdbFull();
        Assert.assertEquals(9, ScheduleFlowHelper.getSuiteCount(schedule));
    }

    @Test
    public void shouldConsiderManualTestsSuiteCount() {
        Schedule schedule = SampleSchedules.withManualItems();
        Assert.assertEquals(11, ScheduleFlowHelper.getSuiteCount(schedule));
    }

    @Test
    public void shouldGetGavSchedule() {
        ScheduleLoaderFactory loaderFactory = mock(ScheduleLoaderFactory.class);
        when(loaderFactory.createForGavSchedule(eq(REPOSITORY_URL), any(ScheduleItemGavResolver.class))).thenReturn(loader);

        ScheduleParserFactory parserFactory = mock(ScheduleParserFactory.class);
        ScheduleParser scheduleParser = mock(ScheduleParser.class);
        when(parserFactory.create(same(loader))).thenReturn(scheduleParser);

        helper = new ScheduleFlowHelper(parserFactory, loaderFactory, mock(FlowEmitterFactory.class),
                globalTeSettings, mainBuildParameters);

        String primaryScheduleXml = "<xml/>";

        helper.getSchedule(mock(ScheduleItemGavResolver.class), primaryScheduleXml);

        verify(loaderFactory).createForGavSchedule(eq(REPOSITORY_URL), any(ScheduleItemGavResolver.class));
        verify(scheduleParser).parse(eq(primaryScheduleXml),
                eq(new ScheduleGavLocation(new ScheduleComponent("g.r", "s1", "1.2"), SCHEDULE_NAME)));
    }

    @Test
    public void shouldGetTafSchedule() {
        String tafSchedulerUrl = "http://tafScheduler/";
        mainBuildParameters.setTafSchedulerUrl(tafSchedulerUrl);
        mainBuildParameters.setScheduleSource(ScheduleSource.TAF_SCHEDULER.toString());

        ScheduleLoaderFactory loaderFactory = mock(ScheduleLoaderFactory.class);
        when(loaderFactory.createForTafSchedule(tafSchedulerUrl)).thenReturn(loader);

        ScheduleParserFactory parserFactory = mock(ScheduleParserFactory.class);
        ScheduleParser scheduleParser = mock(ScheduleParser.class);
        when(parserFactory.create(same(loader))).thenReturn(scheduleParser);

        helper = new ScheduleFlowHelper(parserFactory, loaderFactory, mock(FlowEmitterFactory.class),
                globalTeSettings, mainBuildParameters);

        String primaryScheduleXml = "<xml/>";

        helper.getSchedule(mock(ScheduleItemGavResolver.class), primaryScheduleXml);

        verify(loaderFactory).createForTafSchedule(eq(tafSchedulerUrl));
        verify(scheduleParser).parse(eq(primaryScheduleXml), eq(new TafScheduleLocation(0)));
    }

    private String getFlow(String testware, String xml, String name) {
        mainBuildParameters.setScheduleName(name);
        mainBuildParameters.setTestware(testware);
        return helper.getFlow(xml);
    }

    private String loadResource(String name) throws IOException {
        URL resource = Resources.getResource(name);
        return Resources.toString(resource, Charsets.UTF_8);
    }

    private Map<String, String> getLabelToJobsMap() {
        Map<String, String> labelToJobsMap = newHashMap();
        labelToJobsMap.put("taf", "TEST_EXECUTOR");
        labelToJobsMap.put("uber", "UBER_TEST_EXECUTOR");
        return labelToJobsMap;
    }
}
