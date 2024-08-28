package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.api.TafTeBuildTriggerResponse;
import com.ericsson.cifwk.taf.executor.api.TriggeringTask;
import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;
import com.ericsson.duraci.datawrappers.EventId;
import com.ericsson.duraci.datawrappers.ExecutionId;
import com.ericsson.duraci.datawrappers.MessageBus;
import jenkins.model.Jenkins;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.StringWriter;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TeRestServiceTriggerTest extends AbstractTeRestServiceTest {

    private static final String JOB_URL = "schedulerJob/123";

    private static final String JENKINS_ROOT_URL = "http://jenkins/";

    @Mock
    private Jenkins jenkins;

    @Mock
    private TafScheduleProject tafScheduleProject;

    @Mock
    private TafScheduleBuild tafScheduleBuild;

    @Mock
    private GlobalTeSettings globalTeSettings;

    @Spy
    private TeRestService unit;

    @Before
    public void setUp() throws Exception {
        doReturn(jenkins).when(unit).getJenkinsInstance();
        doReturn(new MessageBus("amqpHostName", "amqpExchange")).when(globalTeSettings).getMessageBus();
        doReturn(globalTeSettings).when(unit).getGlobalTeSettings();
        doReturn(JENKINS_ROOT_URL).when(jenkins).getRootUrl();
        doReturn(new EventId()).
                when(unit).sendTriggeringEvent(any(MessageBus.class), anyString(), any(TriggeringTask.class));
        doReturn(tafScheduleProject).when(unit).getProjectOfType(eq(jenkins), eq(TafScheduleProject.class));
        when(tafScheduleBuild.getUrl()).thenReturn(JOB_URL);
        doReturn(tafScheduleBuild).when(unit).waitForSchedulerJob(eq(tafScheduleProject), any(ExecutionId.class));
    }

    @Test
    public void shouldProcessTriggerEvent() throws Exception {
        TriggeringTask triggeringTask = createTriggeringTask();
        TafTeBuildTriggerResponse response = unit.processTriggerTask(triggeringTask);

        verifySuccessResponse(response);
    }

    @Test
    public void doTrigger() throws Exception {
        StaplerRequest request = mock(StaplerRequest.class);
        StaplerResponse response = mock(StaplerResponse.class);
        StringWriter writer = new StringWriter();
        when(response.getCompressedWriter(eq(request))).thenReturn(writer);

        doReturn(createSerializedTriggeringTask()).when(unit).getRequestBody(eq(request));
        HttpResponse httpResponse = unit.doTrigger(request, response);
        httpResponse.generateResponse(request, response, null);

        String responseStr = writer.getBuffer().toString();
        TafTeBuildTriggerResponse tafTeBuildTriggerResponse = gson.fromJson(responseStr, TafTeBuildTriggerResponse.class);

        verifySuccessResponse(tafTeBuildTriggerResponse);
    }

    private void verifySuccessResponse(TafTeBuildTriggerResponse response) {
        Assert.assertEquals(response.getJobSchedulingStatus(), TafTeBuildTriggerResponse.Status.OK);
        DateTime dateTime = new DateTime(response.getGeneratedAt());
        Assert.assertTrue(dateTime.plus(new Period().withMinutes(2)).isAfterNow());
        String jobExecutionId = response.getJobExecutionId();
        Assert.assertNotNull(jobExecutionId);
        String triggeringEventId = response.getTriggeringEventId();
        Assert.assertNotNull(triggeringEventId);
        String jobUrl = response.getJobUrl();
        Assert.assertEquals("http://jenkins/schedulerJob/123", jobUrl);
    }

}