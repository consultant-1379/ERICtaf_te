package com.ericsson.cifwk.taf.executor.model;

import com.ericsson.cifwk.taf.executor.BuildParameterNames;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ScheduleBuildParametersTest {

    @Test
    public void shouldProvideCommonEiffelParameters() throws Exception {
        ScheduleBuildParameters scheduleBuildParameters = new ScheduleBuildParameters();
        scheduleBuildParameters.setAllureLogDir("allureLogDir");
        Map<String, String> commonEiffelParameters = scheduleBuildParameters.getCommonEiffelParameters();
        assertTrue(commonEiffelParameters.isEmpty());

        scheduleBuildParameters.setExecutionId("ExecutionId");
        scheduleBuildParameters.setEiffelJobStartedEventId("JobStartedEventId");
        scheduleBuildParameters.setEiffelScheduleStartedEventId("ScheduleStartedEventId");
        scheduleBuildParameters.setEiffelScheduleStartedExecutionId("ScheduleStartedExecutionId");
        scheduleBuildParameters.setEiffelTestExecutionId("TestExecutionId");

        commonEiffelParameters = scheduleBuildParameters.getCommonEiffelParameters();
        assertEquals(5, commonEiffelParameters.size());
        assertThat(commonEiffelParameters, hasEntry(BuildParameterNames.EIFFEL_JOB_EXECUTION_ID, "ExecutionId"));
        assertThat(commonEiffelParameters, hasEntry(BuildParameterNames.EIFFEL_JOB_STARTED_EVENT_ID, "JobStartedEventId"));
        assertThat(commonEiffelParameters, hasEntry(BuildParameterNames.EIFFEL_SCHEDULE_STARTED_EVENT_ID, "ScheduleStartedEventId"));
        assertThat(commonEiffelParameters, hasEntry(BuildParameterNames.EIFFEL_SCHEDULE_STARTED_EXECUTION_ID, "ScheduleStartedExecutionId"));
        assertThat(commonEiffelParameters, hasEntry(BuildParameterNames.EIFFEL_TEST_EXECUTION_ID, "TestExecutionId"));
    }
}