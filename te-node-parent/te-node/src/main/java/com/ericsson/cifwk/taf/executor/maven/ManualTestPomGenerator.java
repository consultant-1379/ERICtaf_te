package com.ericsson.cifwk.taf.executor.maven;

import com.ericsson.cifwk.taf.executor.TestExecution;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ManualTestData;
import com.google.common.base.Preconditions;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 16/03/2016
 */
public class ManualTestPomGenerator extends AbstractPomGenerator {

    public void emit(TestExecution testExecution, OutputStream outputStream) {
        emit(testExecution, outputStream, null);
    }

    public void emit(TestExecution testExecution, OutputStream outputStream, String additionalMavenPlugins) {
        ManualTestData manualTestData = testExecution.getManualTestData();
        Preconditions.checkArgument(manualTestData != null, "Manual test items are undefined");
        Map<String, Object> parameterMap = new HashMap<>();

        parameterMap.put("repositoryUrl", testExecution.getRepositoryUrl());
        parameterMap.put("allureLogDir", testExecution.getAllureLogDir());
        parameterMap.put("manual_test_plugin_version", nodeConfigProvider().getManualTestsPluginVersion());
        parameterMap.put("testPlanIdsCsv", manualTestData.getTestCampaignIdsAsCsv());
        parameterMap.put("GENERAL_EXECUTION_ID", testExecution.getGeneralExecutionId());
        parameterMap.put("ALLURE_SERVICE_URL", testExecution.getAllureServiceUrl());

        if (isNotBlank(additionalMavenPlugins)) {
            parameterMap.put("additionalMavenPlugins", additionalMavenPlugins);
        }
        emit("taf_manual_tests.xml.ftl", parameterMap, outputStream);
    }

}
