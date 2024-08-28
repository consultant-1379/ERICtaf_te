package com.ericsson.cifwk.taf.execution;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.execution.operator.impl.JenkinsOperatorImpl;
import com.ericsson.cifwk.taf.execution.operator.model.TestDataContext;
import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.RequestBuilder;
import com.ericsson.cifwk.taf.tools.http.constants.HttpStatus;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static com.google.common.truth.Truth.assertThat;
import static java.lang.String.format;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 07/06/2017
 */
public class JobFactoryBaseTest {

    private static final String TAF_PROFILES = "taf.profiles";

    private RequestBuilder requestBuilder = mock(RequestBuilder.class, Mockito.RETURNS_DEEP_STUBS);

    private ArgumentCaptor<String> jobConfigXmlCaptor = ArgumentCaptor.forClass(String.class);

    @Test
    public void shouldCreateProperSchedulerJob_ciProfile() {
        String configXml = createAndVerifySchedulerJob(TafProfiles.DEFAULT);
        verifyDefaultProfileSettings(configXml);
    }

    @Test
    public void shouldCreateProperSchedulerJob_localProfile() {
        String configXml = createAndVerifySchedulerJob(TafProfiles.LOCAL);
        verifyLocalProfileSettings(configXml);
    }

    private String createAndVerifySchedulerJob(TafProfiles tafProfile) {
        System.setProperty(TAF_PROFILES, tafProfile.getName());
        DataHandler.getConfiguration().reload();

        createJob();
        String configXml = getCreatedJobConfigXml();

        verifyCommonSettings(configXml);
        return configXml;
    }

    private void createJob() {
        new JobFactoryBase().recreateSchedulerJob(jenkinsOperatorStub(), TestConstants.SCHEDULER_JOB_NAME,
                TestDataContext.defaultSchedulerJobConfigFilePath());
    }

    private void verifyCommonSettings(String configXml) {
        assertThat(configXml).containsMatch("plugin=\"te-jenkins-plugin@[0-9]\\.[0-9].*\"");

        assertThat(configXml).contains(format("<reportMbHost>%s</reportMbHost>", DataHandler.getHostByName("reporting_message_bus").getIp()));
        assertThat(configXml).contains("<reportMbPort>5672</reportMbPort>");
        assertThat(configXml).contains("<reportMbUsername>guest</reportMbUsername>");
        assertThat(configXml).contains("<reportMbPassword>guest</reportMbPassword>");
        assertThat(configXml).contains("<reportMbExchange>eiffel.taf.executor</reportMbExchange>");
        assertThat(configXml).contains("<reportMbDomainId>test.execution</reportMbDomainId>");

        assertThat(configXml).contains("<reportsHost>https://oss-taf-logs.seli.wh.rnd.internal.ericsson.com</reportsHost>");
        assertThat(configXml).contains("<minExecutorDiskSpaceGB>2</minExecutorDiskSpaceGB>");
        assertThat(configXml).contains("<minExecutorMemorySpaceGB>15</minExecutorMemorySpaceGB>");
        assertThat(configXml).contains("<allureVersion>1.4.13</allureVersion>");
    }

    private void verifyDefaultProfileSettings(String configXml) {
        assertThat(configXml).contains("<localReportsStorage>/var/log/te_logs</localReportsStorage>");
        assertThat(configXml).contains("<reportingScriptsFolder>/opt/log_upload/</reportingScriptsFolder>");
    }

    private void verifyLocalProfileSettings(String configXml) {
        assertThat(configXml).contains("<localReportsStorage>" + System.getProperty("java.io.tmpdir") + "/LOG_STORAGE</localReportsStorage>");
        assertThat(configXml).contains("<reportingScriptsFolder>/opt/log_upload/</reportingScriptsFolder>");
    }

    private JenkinsOperatorImpl jenkinsOperatorStub() {
        when(requestBuilder.body(jobConfigXmlCaptor.capture())).thenReturn(requestBuilder);
        JenkinsOperatorImpl jenkinsOperator = new JenkinsOperatorImpl();
        jenkinsOperator = spy(jenkinsOperator);
        doReturn(requestBuilder).when(jenkinsOperator).getRequestBuilder(any(Host.class));
        doNothing().when(jenkinsOperator).checkResponse(any(HttpResponse.class), any(HttpStatus.class));
        return jenkinsOperator;
    }

    private String getCreatedJobConfigXml() {
        return jobConfigXmlCaptor.getValue();
    }

}