package com.ericsson.cifwk.taf.executor.allure;

import org.junit.Test;

import static com.ericsson.cifwk.taf.executor.allure.UploadScript.UploadScriptBuilder.anUploadScript;
import static org.assertj.core.api.Assertions.assertThat;

public class UploadScriptExecutorTest {

    @Test
    public void getCommandToRun() throws Exception {
        UploadScriptExecutor unit = new UploadScriptExecutor("logUploadScriptDir/", null, false);

        UploadScript uploadScript = anUploadScript()
            .withLocalReportsStorage("localStore")
            .withLogSubDir("logSubFolder")
            .withExpectedSuiteCount(5)
            .shouldUpload(true)
            .hasAllureService(true)
            .build();

        assertThat(unit.getCommandToRun(uploadScript))
            .isEqualTo("logUploadScriptDir/upload localStore logSubFolder 5 true true");
    }
}
