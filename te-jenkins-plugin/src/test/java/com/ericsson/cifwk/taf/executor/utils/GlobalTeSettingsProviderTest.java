package com.ericsson.cifwk.taf.executor.utils;

import com.ericsson.cifwk.taf.executor.TafScheduleProject;
import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;
import com.ericsson.duraci.datawrappers.MessageBus;
import hudson.model.Project;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GlobalTeSettingsProviderTest {

    private final static String MB_HOST = "MB_HOST";
    private final static int MB_PORT = 5678;
    private final static String MB_EXCHANGE = "eiffel.taf.executor";
    private final static String MB_DOMAIN_ID = "test.domain";
    private final static String ALLURE_VERSION = "1.4.17";
    private final static String REPORTS_HOST = "https://oss-taf-logs.seli.wh.rnd.internal.ericsson.com";
    private final static String LOCAL_REPORTS_STORAGE = "/var/log/te_logs";
    private final static String LOG_UPLOAD_SCRIPT_PATH= "/opt/log_upload/";

    @Mock
    private TafScheduleProject scheduleProject;

    @Mock
    private Jenkins jenkins;

    @Spy
    @InjectMocks
    private GlobalTeSettingsProvider unit;

    @Before
    public void setUp() {
        when(scheduleProject.getReportMbHost()).thenReturn(MB_HOST);
        when(scheduleProject.getReportMbPort()).thenReturn(MB_PORT);
        when(scheduleProject.getReportMbExchange()).thenReturn(MB_EXCHANGE);
        when(scheduleProject.getReportMbDomainId()).thenReturn(MB_DOMAIN_ID);
        when(scheduleProject.getAllureVersion()).thenReturn(ALLURE_VERSION);
        when(scheduleProject.getReportsHost()).thenReturn(REPORTS_HOST);
        when(scheduleProject.getLocalReportsStorage()).thenReturn(LOCAL_REPORTS_STORAGE);
        when(scheduleProject.getReportingScriptsFolder()).thenReturn(LOG_UPLOAD_SCRIPT_PATH);
    }

    @Test
    public void shouldProvideEmptyGlobalTeSettingsOnMissingProject() {
        doReturn(null).when(unit).getSchedulerProject();
        GlobalTeSettings globalTeSettings = unit.provide();
        assertThat(globalTeSettings).isNotNull();
    }

    @Test
    public void shouldProvideGlobalTeSettings() {
        doReturn(scheduleProject).when(unit).getSchedulerProject();

        GlobalTeSettings globalTeSettings = unit.provide();
        assertThat(globalTeSettings.getAllureVersion()).isEqualTo(ALLURE_VERSION);
        assertThat(globalTeSettings.getReportsHost()).isEqualTo(REPORTS_HOST);
        assertThat(globalTeSettings.getLocalReportsStorage()).isEqualTo(LOCAL_REPORTS_STORAGE);
        assertThat(globalTeSettings.getReportingScriptsFolder()).isEqualTo(LOG_UPLOAD_SCRIPT_PATH);
        assertThat(globalTeSettings.getMbHostWithPort()).isEqualTo(MB_HOST + ":" + MB_PORT);
        assertThat(globalTeSettings.getMbExchange()).isEqualTo(MB_EXCHANGE);
        assertThat(globalTeSettings.getReportMbDomainId()).isEqualTo(MB_DOMAIN_ID);

        MessageBus messageBus = globalTeSettings.getMessageBus();
        assertThat(messageBus.getExchangeName()).isEqualTo(MB_EXCHANGE);
        assertThat(messageBus.getHostName()).isEqualTo(MB_HOST);
    }

    @Test
    public void getMessageBus() {
        MessageBus messageBus = GlobalTeSettingsProvider.getMessageBus(scheduleProject);
        assertThat(messageBus.getHostName()).isEqualTo(MB_HOST);
        assertThat(messageBus.getPort()).isEqualTo(MB_PORT);
        assertThat(messageBus.getExchangeName()).isEqualTo(MB_EXCHANGE);
    }

    @Test
    public void shouldReturnEmptystringIfMessageBusIsNull() throws Exception {
        String host = GlobalTeSettingsProvider.getHost(null);
        assertThat(host).isEqualTo(StringUtils.EMPTY);
    }

    @Test
    public void shouldReturnHost_WOPort_ifNullPointerException() throws Exception {
        MessageBus mb = mock(MessageBus.class);
        when(mb.getHostName()).thenReturn("host");
        when(mb.getPort()).thenThrow(NullPointerException.class);
        assertThat(GlobalTeSettingsProvider.getHost(mb)).isEqualTo("host");
    }

    @Test
    public void shouldReturnHostPort_ifPortGreate0() throws Exception {
        MessageBus mb = mock(MessageBus.class);
        when(mb.getHostName()).thenReturn("host");
        when(mb.getPort()).thenReturn(1);
        assertThat(GlobalTeSettingsProvider.getHost(mb)).isEqualTo("host:1");
    }

    @Test
    public void shouldReturnHost_WOPort_ifPort_LessOOrEquals0() throws Exception {
        MessageBus mb = mock(MessageBus.class);
        when(mb.getHostName()).thenReturn("host");
        when(mb.getPort()).thenReturn(-1);
        assertThat(GlobalTeSettingsProvider.getHost(mb)).isEqualTo("host");
    }

    @Test
    public void shouldGetAgentJobMap() throws Exception {
        Project project1 = getProject("Project 1", null);
        Project project2 = getProject("Project 2", "taf");
        Project project3 = getProject("Project 3", "uber");

        List<Project> allProjects = asList(project1, project2, project3);
        Map<String, String> map = GlobalTeSettingsProvider.getAgentJobsMap(allProjects);
        assertThat(map).hasSize(2);
        assertThat(map).containsEntry("taf", "Project 2");
        assertThat(map).containsEntry("uber", "Project 3");
    }

    private Project getProject(String name, String label) {
        Project project1 = mock(Project.class);
        when(project1.getName()).thenReturn(name);
        when(project1.getAssignedLabelString()).thenReturn(label);
        return project1;
    }
}
