package com.ericsson.cifwk.taf.executor.schedule;

import com.cloudbees.plugins.flow.FlowRun;
import com.ericsson.cifwk.taf.executor.BuildParameterNames;
import com.ericsson.cifwk.taf.executor.JenkinsIntegrationTest;
import com.ericsson.cifwk.taf.executor.api.ScheduleSource;
import com.ericsson.cifwk.taf.executor.api.schedule.model.Schedule;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleGavLocation;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleLocation;
import com.ericsson.cifwk.taf.executor.mocks.ListenableMockBuilder;
import com.ericsson.cifwk.taf.executor.mocks.listeners.CollectingBuildListener;
import com.ericsson.cifwk.taf.executor.mocks.listeners.LastBuildListener;
import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;
import com.ericsson.cifwk.taf.executor.model.ScheduleBuildParameters;
import com.ericsson.cifwk.taf.executor.schedule.parser.ScheduleParser;
import com.ericsson.cifwk.taf.executor.schedule.parser.ScheduleParserFactory;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScheduleIntegrationTest extends JenkinsIntegrationTest {

    private static final String REPOSITORY_URL = "repository_url";
    private static final String MOCK_SCHEDULE = "<schedule/>";
    private static final String MOCK_GAVS = "";
    private static final String SCHEDULE_ARTIFACT = "com.ericsson.cifwk.taf:te-schedule:0.1.0";
    private static final String SCHEDULE_NAME = "schedule.xml";

    private TafJobBuilder tafJobBuilder;
    private ScheduleFlowHelper scheduleFlowHelper;
    private FlowExecutor flowExecutor;
    private FlowEmitter flowEmitter;

    private GlobalTeSettings globalTeSettings = new GlobalTeSettings();
    private ScheduleBuildParameters mainParams = new ScheduleBuildParameters();

    @Before
    public void setUp() throws Exception {
        super.setUp();

        ScheduleGavLoader scheduleGavLoader = mock(ScheduleGavLoader.class);
        ScheduleLoaderFactory scheduleLoaderFactory = mock(ScheduleLoaderFactory.class);
        when(scheduleLoaderFactory.createForGavSchedule(same(REPOSITORY_URL), any(ScheduleItemGavResolver.class))).thenReturn(scheduleGavLoader);

        Schedule schedule = new Schedule(null, null);
        ScheduleParser scheduleParser = mock(ScheduleParser.class);
        ScheduleLocation scheduleLocation = ScheduleGavLocation.ofArtifact(SCHEDULE_ARTIFACT, SCHEDULE_NAME);
        when(scheduleParser.parse(same(MOCK_SCHEDULE), eq(scheduleLocation))).thenReturn(schedule);
        ScheduleParserFactory scheduleParserFactory = mock(ScheduleParserFactory.class);
        when(scheduleParserFactory.create(same(scheduleGavLoader))).thenReturn(scheduleParser);

        FlowEmitterFactory flowEmitterFactory = mock(FlowEmitterFactory.class);
        flowEmitter = mock(FlowEmitter.class);
        when(flowEmitterFactory.create(
                same(schedule),
                any(ScheduleItemGavResolver.class),
                any(GlobalTeSettings.class),
                any(ScheduleBuildParameters.class)

        )).thenReturn(flowEmitter);

        Jenkins jenkinsInstance = jenkins();
        jenkinsInstance.setNumExecutors(3);

        mainParams.setScheduleSource(ScheduleSource.MAVEN_GAV.toString());
        scheduleFlowHelper = new ScheduleFlowHelper(
                scheduleParserFactory,
                scheduleLoaderFactory,
                flowEmitterFactory,
                globalTeSettings,
                mainParams
        );
        flowExecutor = new FlowExecutor(jenkinsInstance);
        tafJobBuilder = new TafJobBuilder(jenkinsInstance);
    }

    private void setEmitterResult(String mockFlow) {
        when(flowEmitter.emit()).thenReturn(mockFlow);
    }

    @Test
    public void testSchedule() throws Exception {
        String mockFlow = loadDsl("schedule/flow/cdb_integration.txt");
        setEmitterResult(mockFlow);

        LastBuildListener listener = new LastBuildListener();
        runBuild(new ListenableMockBuilder(listener), TafJobBuilder.EXECUTION_JOB_NAME);
        Map<String, String> buildVariables = listener.getLastBuild().getBuildVariables();

        assertEquals("com.ericsson.cifwk.taf:te-test:0.1.0", buildVariables.get(BuildParameterNames.TAF_DEPENDENCIES));
        assertEquals("install.xml", buildVariables.get(BuildParameterNames.TAF_SUITES));
    }

    @Test
    public void testParallel() throws Exception {
        String mockFlow = loadDsl("schedule/flow/cdb_parallel.txt");
        setEmitterResult(mockFlow);

        CollectingBuildListener listener = new CollectingBuildListener();
        final int timeout = 1500;
        Function<AbstractBuild<?, ?>, Result> resultFunction =
                new Function<AbstractBuild<?, ?>, Result>() {
                    @Override
                    public Result apply(AbstractBuild<?, ?> build) {
                        try {
                            Thread.sleep(timeout);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return Result.SUCCESS;
                    }
                };
        ListenableMockBuilder builder = new ListenableMockBuilder(resultFunction, listener);
        runBuild(builder, TafJobBuilder.EXECUTION_JOB_NAME + "_PAR");
        Collection<AbstractBuild<?, ?>> builds = listener.getBuilds().values();

        long build1aTime = findBuildWithSuites(builds, "1a.xml").getStartTimeInMillis();
        long build1bTime = findBuildWithSuites(builds, "1b.xml").getStartTimeInMillis();
        long build2Time = findBuildWithSuites(builds, "2.xml").getStartTimeInMillis();

        assertTrue(Math.abs(build1aTime - build1bTime) <= timeout); // both started within build timeout
        assertTrue(Math.abs(build1aTime - build2Time) > timeout);
        assertTrue(Math.abs(build1bTime - build2Time) > timeout);
    }

    private static AbstractBuild<?, ?> findBuildWithSuites(Collection<AbstractBuild<?, ?>> builds,
                                                           final String suites) {
        return Iterables.find(builds, new Predicate<AbstractBuild<?, ?>>() {
            @Override
            public boolean apply(AbstractBuild<?, ?> build) {
                return suites.equals(build.getBuildVariables().get("TAF_SUITES"));
            }
        });
    }


    private void runBuild(ListenableMockBuilder builder, String tafJob)
            throws IOException, ExecutionException, InterruptedException {
        tafJobBuilder.setup(builder, tafJob);

        mainParams.setRepositoryUrl(REPOSITORY_URL);
        mainParams.setScheduleArtifact(SCHEDULE_ARTIFACT);
        mainParams.setScheduleName(SCHEDULE_NAME);
        mainParams.setTestware(MOCK_GAVS);

        String dsl = scheduleFlowHelper.getFlow(MOCK_SCHEDULE);
        String jobName = flowExecutor.createProject(dsl, mainParams);
        Future<FlowRun> flowRunFuture = flowExecutor.scheduleBuild(jobName);
        flowRunFuture.get();
    }

    private String loadDsl(String name) throws IOException {
        URL resource = Resources.getResource(name);
        return Resources.toString(resource, Charsets.UTF_8).replaceAll("\\s+", "");
    }

}
