package com.ericsson.cifwk.taf.executor.healthcheck;

import com.ericsson.cifwk.taf.executor.TAFExecutor;
import com.ericsson.cifwk.taf.executor.TafExecutionProject;
import com.ericsson.cifwk.taf.executor.TafScheduleProject;
import com.ericsson.cifwk.taf.executor.utils.JenkinsUtils;
import com.google.common.annotations.VisibleForTesting;
import jenkins.model.Jenkins;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isBlank;

public class CheckJenkinsJobs implements Check {

    static final String REPORTS_HOST_IS_NOT_CONFIGURED_ERROR = "Reports host is not configured.";

    static final String PATH_TO_THE_REPORTING_SCRIPT_IS_UNDEFINED_ERROR = "Path to the reporting scripts is undefined.";

    static final String LOCAL_REPORT_STORAGE_FOLDER_IS_UNDEFINED_ERROR = "Local report storage folder is undefined.";

    @VisibleForTesting
    static final String REPORTS_STORAGE_CONFIGURATION = "Reports storage configuration";

    private final Jenkins jenkins;

    public CheckJenkinsJobs(Jenkins jenkins) {
        this.jenkins = jenkins;
    }

    @Override
    public void check(HealthCheckContext context) {
        TafScheduleProject scheduleProject = JenkinsUtils.getProjectOfType(jenkins, TafScheduleProject.class);
        HealthParam mainJobSetUpParam = new TafScheduleJobHealthParam();
        if (scheduleProject == null) {
            context.fail(mainJobSetUpParam, "Scheduler project is missing");
            return;
        }
        context.ok(mainJobSetUpParam);
        checkExecutorReference(context);
        checkMessaging(context, scheduleProject);
        if (isBlank(scheduleProject.getAllureServiceUrl())) {
            checkLocalReportsStorage(context, scheduleProject);
        }
    }

    private void checkMessaging(HealthCheckContext context, TafScheduleProject scheduleProject) {
        HealthParam param = jobHealthParam("Reporting RabbitMQ is accessible", scheduleProject);
        Check.Result result = new RabbitMqChecker().checkExchange(
                scheduleProject.getReportMbHost(),
                scheduleProject.getReportMbPort(),
                scheduleProject.getReportMbExchange(),
                scheduleProject.getReportMbUsername(),
                scheduleProject.getReportMbPassword(), true);
        if (result.isSuccess()) {
            context.ok(param);
        } else {
            context.fail(param, "");
        }
    }

    public void checkLocalReportsStorage(HealthCheckContext context, TafScheduleProject scheduleProject) {
        boolean failed = false;

        if (StringUtils.isBlank(scheduleProject.getReportsHost())) {
            context.fail(storageHealthParam(scheduleProject), REPORTS_HOST_IS_NOT_CONFIGURED_ERROR);
            failed = true;
        }

        if (StringUtils.isBlank(scheduleProject.getReportingScriptsFolder())) {
            context.fail(storageHealthParam(scheduleProject), PATH_TO_THE_REPORTING_SCRIPT_IS_UNDEFINED_ERROR);
            failed = true;
        }

        String localReportsStorage = scheduleProject.getLocalReportsStorage();
        boolean localReportsStorageDefined = StringUtils.isNotBlank(localReportsStorage);
        if (!localReportsStorageDefined) {
            context.fail(storageHealthParam(scheduleProject), LOCAL_REPORT_STORAGE_FOLDER_IS_UNDEFINED_ERROR);
            failed = true;
        }
        if (localReportsStorageDefined && !new File(localReportsStorage).exists()) {
            context.fail(storageHealthParam(scheduleProject),
                    format("Reports storage folder '%s' doesn't exist on Jenkins master", localReportsStorage));
            failed = true;
        }
        if (!failed) {
            context.ok(storageHealthParam(scheduleProject));
        }
    }

    private HealthParam storageHealthParam(TafScheduleProject scheduleProject) {
        return jobHealthParam(REPORTS_STORAGE_CONFIGURATION, scheduleProject);
    }

    private HealthParam jobHealthParam(String name, TafScheduleProject scheduleProject) {
        return new HealthParam(name, scheduleProject.getDisplayName());
    }

    private void checkExecutorReference(HealthCheckContext context) {
        HealthParam check = new HealthParam("Test Executor jobs exist", CheckJenkinsMaster.SCOPE);

        List<TafExecutionProject> executionProjects = JenkinsUtils.getProjectsOfType(jenkins, TafExecutionProject.class);
        final String mandatoryLabel = TAFExecutor.TAF_NODE_LABEL;
        Optional<TafExecutionProject> mandatoryExecutionProject = executionProjects.stream()
                .filter(project -> mandatoryLabel.equals(project.getAssignedLabelString()))
                .findAny();

        if (mandatoryExecutionProject.isPresent()) {
            context.ok(check);
        } else {
            context.fail(check, format("Haven't found any jobs of type %s with label '%s'", TafExecutionProject.class, mandatoryLabel));
        }
    }

    public static class TafScheduleJobHealthParam extends HealthParam {

        public TafScheduleJobHealthParam() {
            super("TAF scheduler job exists", "master");
        }

        public TafScheduleJobHealthParam(boolean passed) {
            this();
            setPassed(passed);
        }
    }
}
