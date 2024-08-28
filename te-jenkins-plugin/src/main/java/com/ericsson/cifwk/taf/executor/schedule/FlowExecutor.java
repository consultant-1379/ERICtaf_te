package com.ericsson.cifwk.taf.executor.schedule;

import com.cloudbees.plugins.flow.BuildFlow;
import com.cloudbees.plugins.flow.FlowRun;
import com.ericsson.cifwk.taf.executor.Configurations;
import com.ericsson.cifwk.taf.executor.TAFExecutor;
import com.ericsson.cifwk.taf.executor.model.ScheduleBuildParameters;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import hudson.model.Action;
import hudson.model.Project;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlowExecutor {

    private static final int DEFAULT_QUIET_PERIOD = 0;
    private static final Pattern SCHEDULE_NAME_PATTERN = Pattern.compile("([a-zA-Z_0-9]*)\\.xml");

    private final Jenkins jenkins;

    public FlowExecutor(Jenkins jenkins) {
        this.jenkins = jenkins;
    }

    @SuppressWarnings("unchecked")
    public Future<FlowRun> scheduleBuild(String jobName, Action... actions) {
        Project project = jenkins.getItemByFullName(jobName, Project.class);
        Preconditions.checkState(project != null, "Failed to find project '%s'", jobName);
        return project.scheduleBuild2(DEFAULT_QUIET_PERIOD, null, actions); // async
    }

    public String createProject(String dsl, ScheduleBuildParameters params) throws IOException {
        try {
            String jobName = generateFlowName(params);
            BuildFlow job = jenkins.createProject(BuildFlow.class, jobName);
            job.setDsl(dsl);
            // Make sure the flow is executed only on master
            job.setAssignedLabel(jenkins.getLabel(TAFExecutor.TAF_MASTER_LABEL));
            job.save();
            return job.getName();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    protected Date getDate() {
        return Calendar.getInstance().getTime();
    }

    protected String generateFlowName(ScheduleBuildParameters params) {
        StringBuilder result = new StringBuilder(Configurations.FLOW_JOB_PREFIX);
        String testware = Strings.nullToEmpty(params.getTestware());
        String scheduleName = Strings.nullToEmpty(params.getScheduleName());

        String[] parts = testware.split(":");
        if (parts.length < 2) {
            result.append(testware);
        } else {
            result.append(parts[1]).append("_").append(parts[2]);
        }
        result.append("_");

        Matcher matcher = SCHEDULE_NAME_PATTERN.matcher(scheduleName);
        if (matcher.find()) {
            result.append(matcher.group(1));
        } else {
            result.append(scheduleName);
        }

        result.append(new SimpleDateFormat("_yyyy-MM-dd_HH-mm-ss.SSS").format(getDate()));

        if (result.length()>255) {
            result.setLength(255);
        }

        return result.toString().replaceAll("[^\\sa-zA-Z0-9.\\-_]", "");
    }

}
