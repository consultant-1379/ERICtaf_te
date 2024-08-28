package com.ericsson.cifwk.taf.executor.utils;

import com.ericsson.cifwk.taf.executor.TafScheduleProject;
import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;
import com.ericsson.duraci.datawrappers.MessageBus;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import hudson.model.Project;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 22/01/2016
 */
public class GlobalTeSettingsProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalTeSettingsProvider.class);

    private static final String DEFAULT_REPORT_MB_DOMAIN_ID = "test.execution";

    private static GlobalTeSettingsProvider INSTANCE;

    private Jenkins jenkins;

    @VisibleForTesting
    GlobalTeSettingsProvider(Jenkins jenkins) {
        this.jenkins = jenkins;
    }

    public synchronized static GlobalTeSettingsProvider getInstance() {
        // Terminating Jenkins instance is a case in itests
        if (INSTANCE == null || INSTANCE.jenkins.isTerminating()) {
            INSTANCE = new GlobalTeSettingsProvider(JenkinsUtils.getJenkinsInstance());
        }
        return INSTANCE;
    }

    public GlobalTeSettings provide() {
        return extractAllSettings();
    }

    private GlobalTeSettings extractAllSettings() {
        TafScheduleProject scheduleProject = getSchedulerProject();
        GlobalTeSettings settings = extractTeProjectSettings(scheduleProject);
        settings.setAgentJobsMap(getAgentJobsMap(getAllProjects()));
        return settings;
    }

    private List<Project> getAllProjects() {
        return JenkinsUtils.getAllProjects(jenkins);
    }

    private GlobalTeSettings extractTeProjectSettings(TafScheduleProject scheduleProject) {
        GlobalTeSettings result = new GlobalTeSettings();
        if (scheduleProject == null) {
            LOGGER.warn("Instance of TafScheduleProject is not found");
            return result;
        }
        MessageBus messageBus = getMessageBus(scheduleProject);
        result.setMessageBus(messageBus);
        result.setMbHostWithPort(getHost(messageBus));
        result.setMbExchange(messageBus.getExchangeName());
        String reportMbDomainId = scheduleProject.getReportMbDomainId();
        if (reportMbDomainId == null) {
            LOGGER.warn("DomainId for outgoing Eiffel test events is undefined in TAF_SCHEDULER configuration " +
                    "- using the default one ('" + DEFAULT_REPORT_MB_DOMAIN_ID + "')");
            reportMbDomainId = DEFAULT_REPORT_MB_DOMAIN_ID;
        }
        result.setReportMbDomainId(reportMbDomainId);
        result.setAllureVersion(scheduleProject.getAllureVersion());
        result.setMinExecutorDiskSpaceGB(scheduleProject.getMinExecutorDiskSpaceGB());
        result.setMinExecutorMemorySpaceGB(scheduleProject.getMinExecutorMemorySpaceGB());
        result.setReportsHost(scheduleProject.getReportsHost());
        result.setAllureServiceUrl(scheduleProject.getAllureServiceUrl());
        result.setAllureServiceBackendUrl(scheduleProject.getAllureServiceBackendUrl());
        result.setShouldUploadToOssLogs(scheduleProject.isUploadToOssLogs());
        result.setLocalReportsStorage(scheduleProject.getLocalReportsStorage());
        result.setReportingScriptsFolder(scheduleProject.getReportingScriptsFolder());
        result.setDefaultRuntimeLimitations(scheduleProject.getRuntimeLimitations());

        return result;
    }

    @VisibleForTesting
    TafScheduleProject getSchedulerProject() {
        return JenkinsUtils.getProjectOfType(jenkins, TafScheduleProject.class);
    }

    @VisibleForTesting
    static Map<String, String> getAgentJobsMap(List<Project> allProjects) {
        return allProjects.stream()
                .filter(project -> isNotBlank(project.getAssignedLabelString()))
                .collect(Collectors.toMap(Project::getAssignedLabelString, Project::getName));
    }

    @VisibleForTesting
    static MessageBus getMessageBus(TafScheduleProject scheduleProject) {
        Preconditions.checkArgument(scheduleProject != null, "Schedule project cannot be null");
        return new MessageBus(
                scheduleProject.getReportMbHost() + ":" + scheduleProject.getReportMbPort(),
                scheduleProject.getReportMbExchange()
        );
    }

    static String getHost(MessageBus messageBus) {
        if (messageBus == null) {
            return StringUtils.EMPTY;
        }
        return messageBus.getHostName() + getPort(messageBus);
    }

    private static String getPort(MessageBus messageBus) {
        Preconditions.checkArgument(messageBus != null, "Reporting MB is undefined");
        try {
            int port = messageBus.getPort();
            // Port 0 is used in integration tests
            if (port >= 0) {
                return ":" + Integer.toString(port);
            }
        } catch (NullPointerException ignore) { // NOSONAR
            // Can get a NPE due to unboxing in MessageBus.getPort()
        }
        return StringUtils.EMPTY;
    }

}
