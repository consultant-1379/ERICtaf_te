package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.api.TafTeBuildDetails;
import com.ericsson.cifwk.taf.executor.api.TafTeBuildTriggerResponse;
import com.ericsson.cifwk.taf.executor.api.TriggeringTask;
import com.ericsson.cifwk.taf.executor.api.TriggeringTaskBuilder;
import com.ericsson.cifwk.taf.executor.helpers.TafScheduleProjectAdapter;
import com.ericsson.duraci.datawrappers.ArtifactGav;
import com.google.common.io.Resources;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.nio.charset.Charset;

import static org.junit.Assert.assertTrue;

public class ManualTestsITest extends WithSlaveTest {

    public static final String REPOSITORY_URL = "http://repositoryUrl";
    public static final String SCHEDULE_GROUP_ID = "schedule-groupId";
    public static final String SCHEDULE_ARTIFACT_ID = "schedule-artifactId";
    public static final String SCHEDULE_VERSION = "schedule-version";

    private TafScheduleProjectAdapter scheduleProjectAdapter;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        scheduleProjectAdapter = getScheduleProjectAdapterBuilder().build();

        URL scheduleUrl = this.getClass().getClassLoader().getResource("schedule/manual_parallel.xml");
        String scheduleXml = Resources.toString(scheduleUrl, Charset.defaultCharset());

        setUpSchedulerProjectForSchedule("manual_parallel.xml", scheduleXml);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        deleteAllJobs();
    }

    @Test
    public void shouldRunManualTests() throws Exception {
        setUpExecutionProject();
        TafTeBuildTriggerResponse triggerResponse = triggerBuildFor(testwareVersion, "", new String[] {"manual_parallel.xml"});
        String jobExecutionIdStr = triggerResponse.getJobExecutionId();
        TafTeBuildDetails buildDetails = waitUntilBuildFinished(jobExecutionIdStr);
        assertTrue(buildDetails.getSuccess());
    }

    private void setUpSchedulerProjectForSchedule(String schedulePath, String scheduleXmlToUse) {
        scheduleProjectAdapter.resolveScheduleAs(new ArtifactGav(SCHEDULE_GROUP_ID, SCHEDULE_ARTIFACT_ID, SCHEDULE_VERSION),
                schedulePath, scheduleXmlToUse);
    }

    @Override
    protected TriggeringTask createTriggeringTask(String packageVersion, String sutResource, String[] pathsToSchedules) {
        TriggeringTaskBuilder taskBuilder = new TriggeringTaskBuilder()
                .withCiFwkPackage("com.ericsson.cifwk.taf.executor", "te-taf-testware", packageVersion)
                .withNexusURI(REPOSITORY_URL)
                .withTestWare("com.ericsson.cifwk.taf.executor", "te-taf-testware", packageVersion)
                .withSutResource(sutResource);
        for (String pathToSchedule : pathsToSchedules) {
            taskBuilder.withSchedule(SCHEDULE_GROUP_ID, SCHEDULE_ARTIFACT_ID, SCHEDULE_VERSION, pathToSchedule);
        }
        return taskBuilder.build();
    }
}
