package com.ericsson.cifwk.taf.execution.operator;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.execution.operator.model.jenkins.Build;
import com.ericsson.cifwk.taf.execution.operator.model.jenkins.Jenkins;
import com.ericsson.cifwk.taf.execution.operator.model.jenkins.Job;
import com.ericsson.cifwk.taf.execution.operator.model.jenkins.SchedulerJobConfig;
import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.constants.HttpStatus;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;

import java.util.Map;

/**
 *
 */
public interface JenkinsOperator {

    Jenkins jenkins(Host jenkinsHost);

    Optional<SchedulerJobConfig> getMainJobConfig(Host jenkinsHost) throws Exception;

    Optional<Job> getJob(String jobName, Host jenkinsHost);

    void createJob(String jobName, String xmlResource, Map<Object, Object> testProperties, Host jenkinsHost);

    void deleteJob(String jobName, Host jenkinsHost);

    Build getBuild(String jobName, Host jenkinsHost, int buildNumber);

    Build lastBuild(String jobName, Host jenkinsHost);

    Build lastBuild(String jobName, Host jenkinsHost, int maxSecondsToTolerate404);

    HttpResponse requestWithToleranceToErrorCodes(Supplier<HttpResponse> responseSupplier,
                                                  String operationDescription, String totalFailureMsg,
                                                  int maxSecondsToTolerate, int timeToWaitBeforeNewAttemptInMillis,
                                                  HttpStatus... httpStatusesToAvoid);

    void stopBuild(Build build, Host jenkinsHost);

    String extractRequestUri(String buildUrl);

    String getBuildConsoleLogText(Build build, Host jenkinsHost);
}
