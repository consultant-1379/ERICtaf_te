package com.ericsson.cifwk.taf.executor.schedule;

import com.ericsson.cifwk.taf.executor.BuildParameterNames;
import com.google.common.base.Throwables;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterDefinition;
import hudson.tasks.BatchFile;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;

import java.io.IOException;

public class TafJobBuilder {
    
    public static final String EXECUTION_JOB_NAME = "TEST_EXECUTOR";

    private static final String COMMAND =
            "echo \"" + EXECUTION_JOB_NAME + "(" +
                    BuildParameterNames.TAF_DEPENDENCIES +
                    "=%" + BuildParameterNames.TAF_DEPENDENCIES + "%; " +
                    BuildParameterNames.TAF_SUITES +
                    "=%" + BuildParameterNames.TAF_SUITES + "%)";

    private final Jenkins jenkins;

    public TafJobBuilder(Jenkins jenkins) {
        this.jenkins = jenkins;
    }

    public void setup() {
        BatchFile builder = new BatchFile(COMMAND);
        setup(builder, EXECUTION_JOB_NAME);
    }

    FreeStyleProject setup(Builder builder, String jobName) {
        FreeStyleProject tafJob = jenkins.getItemByFullName(EXECUTION_JOB_NAME, FreeStyleProject.class);
        deleteIfExists(tafJob);
        return createNewJob(builder, jobName);
    }

    private void deleteIfExists(FreeStyleProject tafJob) {
        if (tafJob != null) {
            try {
                tafJob.delete();
            } catch (IOException | InterruptedException e) {
                throw Throwables.propagate(e);
            }
        }
    }

    private FreeStyleProject createNewJob(Builder builder, String jobName) {
        FreeStyleProject tafJob;
        try {
            tafJob = createTafJob(builder, jobName);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
        return tafJob;
    }

    private FreeStyleProject createTafJob(Builder buildStep, String jobName) throws IOException {
        FreeStyleProject tafJob = jenkins.createProject(FreeStyleProject.class, jobName);
        ParametersDefinitionProperty paramsProp = new ParametersDefinitionProperty(
                new StringParameterDefinition(BuildParameterNames.TAF_DEPENDENCIES, ""),
                new StringParameterDefinition(BuildParameterNames.TAF_SUITES, "")
        );
        tafJob.addProperty(paramsProp);
        tafJob.setConcurrentBuild(true);
        tafJob.getBuildersList().add(buildStep);
        return tafJob;
    }

}
