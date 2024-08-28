package com.ericsson.cifwk.taf.executor.healthcheck;

import com.ericsson.cifwk.taf.executor.TafScheduleProject;
import jenkins.model.Jenkins;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static com.ericsson.cifwk.taf.executor.healthcheck.CheckJenkinsJobs.LOCAL_REPORT_STORAGE_FOLDER_IS_UNDEFINED_ERROR;
import static com.ericsson.cifwk.taf.executor.healthcheck.CheckJenkinsJobs.PATH_TO_THE_REPORTING_SCRIPT_IS_UNDEFINED_ERROR;
import static com.ericsson.cifwk.taf.executor.healthcheck.CheckJenkinsJobs.REPORTS_HOST_IS_NOT_CONFIGURED_ERROR;
import static com.ericsson.cifwk.taf.executor.healthcheck.CheckJenkinsJobs.REPORTS_STORAGE_CONFIGURATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 20/06/2017
 */
@RunWith(MockitoJUnitRunner.class)
public class CheckJenkinsJobsTest {

    @Mock
    private Jenkins jenkins;

    @Mock
    private TafScheduleProject scheduleProject;

    @InjectMocks
    private CheckJenkinsJobs unit;

    @Test
    public void checkLocalReportsStorage_fewFailures() throws Exception {
        DefaultHealthCheckContext context = new DefaultHealthCheckContext();
        unit.checkLocalReportsStorage(context, scheduleProject);
        List<HealthParam> results = context.health();
        assertHavingFailure(results, REPORTS_STORAGE_CONFIGURATION, REPORTS_HOST_IS_NOT_CONFIGURED_ERROR);
        assertHavingFailure(results, REPORTS_STORAGE_CONFIGURATION, PATH_TO_THE_REPORTING_SCRIPT_IS_UNDEFINED_ERROR);
        assertHavingFailure(results, REPORTS_STORAGE_CONFIGURATION, LOCAL_REPORT_STORAGE_FOLDER_IS_UNDEFINED_ERROR);
    }

    private void assertHavingFailure(List<HealthParam> results, String checkName, String error) {
        assertThat(results).extracting("name", "description").contains(tuple(checkName, error));
    }
}