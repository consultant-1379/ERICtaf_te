package com.ericsson.cifwk.taf.execution;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.execution.operator.JenkinsOperator;
import com.ericsson.cifwk.taf.execution.operator.model.TestDataContext;
import com.ericsson.cifwk.taf.execution.operator.model.jenkins.Job;
import com.ericsson.cifwk.taf.executor.TAFExecutor;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class JobFactoryBase extends TafTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobFactoryBase.class);

    protected void recreateSchedulerJob(JenkinsOperator operator, String jobName, String xmlTemplatePath) {
        recreateJob(operator, jobName, xmlTemplatePath, scheduleTemplateParams());
    }

    protected void recreateExecutorJob(JenkinsOperator operator, String jobName) {
        recreateExecutorJob(operator, jobName, TAFExecutor.TAF_NODE_LABEL);
    }

    protected void recreateFullEnvExecutorJob(JenkinsOperator operator, String jobName) {
        recreateExecutorJob(operator, jobName, TAFExecutor.FULL_ENV_TAF_NODE_LABEL);
    }

    protected void recreateExecutorJob(JenkinsOperator operator, String jobName, String agentLabel) {
        Properties templateParameters = commonProperties();
        templateParameters.put("agentLabel", agentLabel);
        recreateJob(operator, jobName, TestDataContext.defaultExecutorJobConfigFilePath(), templateParameters);
    }

    private Properties scheduleTemplateParams() {
        Properties allProperties = commonProperties();

        allProperties.put("reportingMbHost", TestDataContext.getReportingMbHostAddress());
        allProperties.put("reportingMbPort", String.valueOf(TestDataContext.getReportingMbPort()));
        allProperties.put("reportingMbUsername", TestDataContext.getReportingMbUsername());
        allProperties.put("reportingMbPassword", TestDataContext.getReportingMbPassword());
        allProperties.put("mbReportExchange", TestDataContext.getReportingMbExchange());
        allProperties.put("mbReportDomainId", TestDataContext.getReportingMbDomain());

        allProperties.put("reportsHost", TestDataContext.getAllureReportsHttpBase());
        allProperties.put("localReportsStorage", TestDataContext.getLocalReportsStorage());
        allProperties.put("reportingScriptsFolder", TestDataContext.getReportingScriptsFolder());
        allProperties.put("allureServiceUrl", TestDataContext.getAllureServiceUrl());
        allProperties.put("allureVersion", TestDataContext.getAllureVersion());

        allProperties.put("minExecutorDiskSpaceGB", TestDataContext.getMinExecutorDiskSpaceGb());
        allProperties.put("minExecutorMemorySpaceGB", TestDataContext.getMinExecutorMemorySpaceGb());

        return allProperties;
    }

    private Properties commonProperties() {
        Properties allProperties = new Properties();
        allProperties.put("pluginVersion", TestDataContext.getPluginVersion());
        return allProperties;
    }

    private void recreateJob(JenkinsOperator operator, String jobName, String xmlTemplatePath, Properties templateParameters) {
        Host teMasterHost = TestDataContext.getTeMasterHost();
        Optional<Job> optional = operator.getJob(jobName, teMasterHost);
        if (optional.isPresent()) {
            LOGGER.info(String.format("Job '%s' already exists - deleting it", jobName));
            operator.deleteJob(jobName, teMasterHost);
        }
        operator.createJob(jobName, xmlTemplatePath, templateParameters, teMasterHost);
    }
}
