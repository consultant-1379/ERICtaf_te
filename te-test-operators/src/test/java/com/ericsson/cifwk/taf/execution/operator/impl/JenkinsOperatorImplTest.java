package com.ericsson.cifwk.taf.execution.operator.impl;

import org.testng.annotations.Test;

import java.util.Properties;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.testng.AssertJUnit.assertEquals;

public class JenkinsOperatorImplTest {

    private JenkinsOperatorImpl unit = new JenkinsOperatorImpl();

    @Test
    public void testPreprocessAsTemplate() throws Exception {
        final String jobName = "MY_JOB";
        final String jobParameterValue = "1";

        Properties projectProperties = new Properties();
        projectProperties.put("jobName", jobName);
        projectProperties.put("jobParameter", jobParameterValue);

        String processedTemplate = unit.preProcessAsTemplate("data/templates/test_job.xml.ftl", projectProperties);

        assertNotNull(processedTemplate);
        assertThat(processedTemplate, containsString(format("<jobName>%s</jobName>", jobName)));
        assertThat(processedTemplate, containsString(format("<jobParameter>%s</jobParameter>", jobParameterValue)));
    }

    @Test
    public void testExtractRequestUri() {
        assertEquals("/jenkins/job/TAF_Execution_te-taf-testware_1.0.37-SNAPSHOT_legacy_2015-07-29_11-42-42/1/stop",
                unit.extractRequestUri("http://localhost:8091/jenkins/job/TAF_Execution_te-taf-testware_1.0.37-SNAPSHOT_legacy_2015-07-29_11-42-42/1/stop"));
    }
}
