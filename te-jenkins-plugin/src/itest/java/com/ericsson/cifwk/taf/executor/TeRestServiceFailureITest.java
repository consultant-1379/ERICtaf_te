package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.api.TafTeBuildDetails;
import com.ericsson.cifwk.taf.executor.api.TafTeBuildTriggerResponse;
import com.ericsson.cifwk.taf.executor.api.TeRestServiceClient;
import com.ericsson.cifwk.taf.executor.api.TriggeringTask;
import com.ericsson.cifwk.taf.executor.api.TriggeringTaskBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TeRestServiceFailureITest extends RestServiceAwareITest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        disableCSRFCheck();
        deleteAllJobs();
    }

    @Test
    public void shouldNotFailWithTeProjectsMissing() throws Exception {
        TeRestServiceClient restServiceClient = getTeRestServiceClient();
        TriggeringTask triggeringTask = createTriggeringTask(getTestwareVersion(), "sutResource", new String[]{"success.xml"});
        TafTeBuildTriggerResponse result = restServiceClient.triggerBuild(triggeringTask);
        Assert.assertEquals(TafTeBuildTriggerResponse.Status.FAILURE, result.getJobSchedulingStatus());
        Assert.assertNotNull(result.getErrorTxt());

        TafTeBuildDetails buildDetails = restServiceClient.getBuildDetails("aaa-bbb-ccc");
        Assert.assertNull(buildDetails.getSuccess());
        Assert.assertNull(buildDetails.getBuildComplete());
        Assert.assertNotNull(buildDetails.getErrorTxt());
    }

    protected TriggeringTask createTriggeringTask(String packageVersion, String sutResource, String[] pathsToSchedules) {
        TriggeringTaskBuilder taskBuilder = new TriggeringTaskBuilder()
                .withCiFwkPackage("com.ericsson.cifwk.taf.executor", "te-taf-testware", packageVersion)
                .withNexusURI("REPOSITORY_URL")
                .withSlaveHost("SLAVE_1")
                .withTestWare("com.ericsson.cifwk.taf.executor", "te-taf-testware", packageVersion)
                .withSutResource(sutResource);
        for (String pathToSchedule : pathsToSchedules) {
            taskBuilder.withSchedule("SCHEDULE_GROUP_ID", "SCHEDULE_ARTIFACT_ID", "SCHEDULE_VERSION", pathToSchedule);
        }
        return taskBuilder.build();
    }

}
