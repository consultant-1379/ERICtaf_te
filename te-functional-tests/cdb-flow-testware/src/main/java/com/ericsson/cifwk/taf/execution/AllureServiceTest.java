package com.ericsson.cifwk.taf.execution;

import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.execution.operator.AllureReportOperator;
import com.ericsson.cifwk.taf.execution.operator.JenkinsOperator;
import com.ericsson.cifwk.taf.execution.operator.RestOperator;
import com.ericsson.cifwk.taf.execution.operator.impl.AllureReportOperatorImpl;
import com.ericsson.cifwk.taf.execution.operator.impl.JenkinsOperatorImpl;
import com.ericsson.cifwk.taf.execution.operator.impl.RestOperatorImpl;
import com.ericsson.cifwk.taf.execution.operator.model.TestDataContext;
import com.ericsson.cifwk.taf.execution.operator.model.allure.OverviewTabModel;
import com.ericsson.cifwk.taf.executor.api.ScheduleRequest;
import com.ericsson.cifwk.taf.executor.api.TafTeBuildDetails;
import com.ericsson.cifwk.taf.executor.api.TafTeBuildTriggerResponse;
import com.ericsson.cifwk.taf.executor.api.TafTeJenkinsJob;
import com.ericsson.cifwk.taf.executor.api.TeRestServiceClient;

import org.apache.log4j.Logger;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

public class AllureServiceTest extends JobFactoryBase {

    private static final Logger LOGGER = Logger.getLogger(AllureServiceTest.class);

    private static final String GROUP_ID = "com.ericsson.cifwk.taf.executor";
    private static final String ARTIFACT_ID = "te-taf-testware";

    @Inject
    private Provider<JenkinsOperatorImpl> jenkinsOperatorProvider;

    @Inject
    private Provider<RestOperatorImpl> restOperatorProvider;

    @Inject
    private Provider<AllureReportOperatorImpl> allureReportOperatorProvider;

    private RestOperator restOperator;

    private ScheduleRequest scheduleRequest;

    @BeforeTest
    public void init() throws UnknownHostException {
        restOperator = restOperatorProvider.get();
        restOperator.init(TestDataContext.getTeMasterHost());
        scheduleRequest = new ScheduleRequest("schedule/complex.xml", GROUP_ID, ARTIFACT_ID, "1.0.69");
    }

    @Test
    @TestId(id = "TAF_TE_19", title = "Create TE jobs for Allure Service required for test running")
    public void should_generate_allure_report_using_external_service() throws IOException, InterruptedException {
        JenkinsOperator operator = jenkinsOperatorProvider.get();
        recreateExecutorJob(operator, TestConstants.EXECUTOR_JOB_NAME);
        recreateSchedulerJob(operator, TestConstants.SCHEDULER_JOB_NAME, "data/templates/allure_ms_scheduler_job.xml.ftl");

        TafTeBuildTriggerResponse buildResponse = triggerBuild(GROUP_ID, ARTIFACT_ID, scheduleRequest);

        TafTeBuildDetails buildDetails = waitUntilCompletion(buildResponse.getJobExecutionId());
        List<TafTeJenkinsJob> schedulerJobs = buildDetails.getTafTeJenkinsJobs(TafTeJenkinsJob.Type.SCHEDULER);
        for (TafTeJenkinsJob scheduler : schedulerJobs) {
            assertThat(scheduler.getResult())
                .named("unsuccessful [" + scheduler.getName() + "] scheduler result")
                .isEqualTo(TafTeJenkinsJob.Result.SUCCESS);
        }
        verifyAllureReport(buildDetails.getAllureLogUrl());
        recreateSchedulerJob(operator, TestConstants.SCHEDULER_JOB_NAME, "data/templates/scheduler_job.xml.ftl");
    }

    private TafTeBuildTriggerResponse triggerBuild(String groupId, String artifactId, ScheduleRequest schedule) {
        return restOperator.triggerBuild(groupId, artifactId, schedule);
    }

    private TafTeBuildDetails waitUntilCompletion(String jobExecutionIdStr) throws InterruptedException {
        TafTeBuildDetails buildDetails;
        TeRestServiceClient client = restOperator.getTeRestServiceClient();
        do {
            buildDetails = client.getBuildDetails(jobExecutionIdStr);
            Thread.sleep(5000);
        } while (Boolean.FALSE.equals(buildDetails.getBuildComplete()));
        return buildDetails;
    }

    private void verifyAllureReport(String allureLogUrl) {
        LOGGER.info("Verifying Allure report (" + allureLogUrl + ")");
        AllureReportOperator allureOperator = allureReportOperatorProvider.get();
        allureOperator.init(allureLogUrl);

        OverviewTabModel overviewTab = allureOperator.getOverviewTab();
        assertThat(overviewTab.isEnvSectionVisible()).isTrue();
        Map<String, String> envData = overviewTab.getEnvironmentData();

        LOGGER.info("Verifying trigger environment data are present");
        assertThat(envData.get("ISO version"))
            .named("ISO Version must present")
            .isNotEmpty();
        assertThat(envData.get("Jenkins job name"))
            .named("Jenkins job name must present")
            .isNotEmpty();
        assertThat(envData.get("Link to Jenkins job"))
            .named("Link to Jenkins job must present")
            .isNotEmpty();
        assertThat(envData.get("Current sprint"))
            .named("Current sprint must present")
            .isNotEmpty();
    }
}
