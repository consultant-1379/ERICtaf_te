package com.ericsson.cifwk.taf.executor.healthcheck;

import com.ericsson.cifwk.taf.executor.utils.JenkinsUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hudson.Extension;
import hudson.model.PageDecorator;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Optional;

@Extension
public class HealthCheck extends PageDecorator {

    public static HealthCheck getInstance(Jenkins jenkins) {
        return jenkins.getExtensionList(HealthCheck.class).stream().findAny().orElseGet(HealthCheck::new);
    }

    //accessible via jenkins_url/jenkins/descriptorByName/com.ericsson.cifwk.taf.executor.healthcheck.HealthCheck/healthCheck
    @SuppressWarnings("unused")
    public HttpResponse doHealthCheck(StaplerRequest req, StaplerResponse rsp) throws IOException {
        return (req1, rsp1, node) -> {
            rsp1.setContentType("application/json;charset=UTF-8");
            Writer writer = rsp1.getCompressedWriter(req1);
            // For pretty print in Jenkins UI
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(healthCheck()));
            writer.close();
        };
    }

    public List<HealthParam> healthCheck() {
        Jenkins jenkins = jenkins();

        DefaultHealthCheckContext context = healthCheckContext();

        // Check the existence of the TE jobs first, since other checks rely on config data from TafSchedulerProject
        jenkinsJobsCheck(jenkins).check(context);
        Optional<HealthParam> successfulMainProjectHealthCheck = findSuccessfulMainProjectHealthCheck(context.health());
        if (successfulMainProjectHealthCheck.isPresent()) {
            jenkinsMasterCheck(jenkins).check(context);
            jenkinsNodesCheck(jenkins).check(context);
        }

        return context.health();
    }

    @VisibleForTesting
    Optional<HealthParam> findSuccessfulMainProjectHealthCheck(List<HealthParam> health) {
        return health.stream()
                .filter(item -> item.getClass().equals(CheckJenkinsJobs.TafScheduleJobHealthParam.class))
                .filter(HealthParam::isPassed)
                .findAny();
    }

    @VisibleForTesting
    DefaultHealthCheckContext healthCheckContext() {
        return new DefaultHealthCheckContext();
    }

    @VisibleForTesting
    CheckJenkinsNodes jenkinsNodesCheck(Jenkins jenkins) {
        return new CheckJenkinsNodes(jenkins);
    }

    @VisibleForTesting
    CheckJenkinsMaster jenkinsMasterCheck(Jenkins jenkins) {
        return new CheckJenkinsMaster(jenkins);
    }

    @VisibleForTesting
    CheckJenkinsJobs jenkinsJobsCheck(Jenkins jenkins) {
        return new CheckJenkinsJobs(jenkins);
    }

    @VisibleForTesting
    Jenkins jenkins() {
        return JenkinsUtils.getJenkinsInstance();
    }

}
