package com.ericsson.cifwk.taf.executor.healthcheck;

import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 02/06/2017
 */
@RunWith(MockitoJUnitRunner.class)
public class HealthCheckTest {

    @Mock
    private Jenkins jenkins;

    @Mock
    private CheckJenkinsJobs checkJenkinsJobs;

    @Mock
    private CheckJenkinsMaster checkJenkinsMaster;

    @Mock
    private CheckJenkinsNodes checkJenkinsNodes;

    @Mock
    private DefaultHealthCheckContext healthCheckContext;

    @Spy
    private HealthCheck unit;

    @Before
    public void init() {
        doReturn(jenkins).when(unit).jenkins();
        doReturn(checkJenkinsJobs).when(unit).jenkinsJobsCheck(jenkins);
        doReturn(checkJenkinsMaster).when(unit).jenkinsMasterCheck(jenkins);
        doReturn(checkJenkinsNodes).when(unit).jenkinsNodesCheck(jenkins);
        doReturn(healthCheckContext).when(unit).healthCheckContext();
    }

    @Test
    public void healthCheck_happyPath() throws Exception {
        doReturn(checkResults(true)).when(healthCheckContext).health();
        unit.healthCheck();
        verify(checkJenkinsJobs).check(healthCheckContext);
        verify(checkJenkinsMaster).check(healthCheckContext);
        verify(checkJenkinsNodes).check(healthCheckContext);
    }

    @Test
    public void healthCheck_schedulerJobCheckFails() throws Exception {
        doReturn(checkResults(false)).when(healthCheckContext).health();
        unit.healthCheck();
        verify(checkJenkinsJobs).check(healthCheckContext);
        verify(checkJenkinsMaster, never()).check(healthCheckContext);
        verify(checkJenkinsNodes, never()).check(healthCheckContext);
    }

    @Test
    public void shouldFindSuccessfulMainProjectHealthCheck() throws Exception {
        assertThat(unit.findSuccessfulMainProjectHealthCheck(checkResults(true)).isPresent()).isTrue();
        assertThat(!unit.findSuccessfulMainProjectHealthCheck(checkResults(false)).isPresent()).isTrue();
    }

    private List<HealthParam> checkResults(boolean mainJobIsOk) {
        return asList(
                new CheckJenkinsJobs.TafScheduleJobHealthParam(mainJobIsOk),
                new HealthParam("one more check", "scope"));
    }

}