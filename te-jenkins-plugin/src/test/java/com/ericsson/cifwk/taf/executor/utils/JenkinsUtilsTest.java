package com.ericsson.cifwk.taf.executor.utils;

import com.ericsson.cifwk.taf.executor.BuildParameterNames;
import com.ericsson.cifwk.taf.executor.TafScheduleBuild;
import com.ericsson.cifwk.taf.executor.annotations.Parameter;
import com.ericsson.cifwk.taf.executor.model.CommonBuildParameters;
import hudson.model.Build;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.StringParameterValue;
import hudson.util.VariableResolver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class JenkinsUtilsTest {

    private static final String JOB_URL = "schedulerJob/123";
    public static final String EXECUTION_ID = "executionId";
    public static final String JOB_STARTED_EVENT_ID = "jobStartedEventId";

    private final Map<String, String> paramMap = new HashMap<>();

    @Before
    public void setUp() {
        paramMap.put(BuildParameterNames.EIFFEL_JOB_EXECUTION_ID, EXECUTION_ID);
        paramMap.put(BuildParameterNames.EIFFEL_JOB_STARTED_EVENT_ID, JOB_STARTED_EVENT_ID);
    }

    @Test
    public void shouldPopulateParameterValues() throws Exception {
        TafScheduleBuild tafScheduleBuild = getTafScheduleBuildInstanceWithParams();
        TestBuildParameters buildParameters =
                JenkinsUtils.getBuildParameters(tafScheduleBuild, TestBuildParameters.class);
        assertThat(buildParameters.getExecutionId(), equalTo(EXECUTION_ID));
        assertThat(buildParameters.getJobStartedEventId(), equalTo(JOB_STARTED_EVENT_ID));
    }

    private TafScheduleBuild getTafScheduleBuildInstanceWithParams() {
        TafScheduleBuild tafScheduleBuild = mock(TafScheduleBuild.class);
        when(tafScheduleBuild.getUrl()).thenReturn(JOB_URL);
        ParametersAction parametersAction = mock(ParametersAction.class);
        when(parametersAction.getParameter(anyString())).thenAnswer(new Answer<ParameterValue>() {
            @Override
            public ParameterValue answer(InvocationOnMock invocation) throws Throwable {
                String paramName = (String) invocation.getArguments()[0];
                String valueStr = paramMap.get(paramName);
                ParameterValue parameterValue = new StringParameterValue(paramName, valueStr);
                parameterValue = spy(parameterValue);
                VariableResolver variableResolver = mock(VariableResolver.class);
                when(variableResolver.resolve(eq(paramName))).thenReturn(valueStr);
                doReturn(variableResolver).when(parameterValue).createVariableResolver(any(Build.class));
                return parameterValue;
            }
        });
        when(tafScheduleBuild.getActions(ParametersAction.class)).thenReturn(Arrays.asList(parametersAction));
        return tafScheduleBuild;
    }

    public static class TestBuildParameters extends CommonBuildParameters {

        @Parameter(name = BuildParameterNames.EIFFEL_JOB_STARTED_EVENT_ID)
        private String jobStartedEventId;

        public String getJobStartedEventId() {
            return jobStartedEventId;
        }

    }

}