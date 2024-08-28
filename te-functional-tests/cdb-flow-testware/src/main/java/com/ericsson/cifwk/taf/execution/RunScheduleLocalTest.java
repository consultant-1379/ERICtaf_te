package com.ericsson.cifwk.taf.execution;

import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.execution.operator.LogOperator;
import com.ericsson.cifwk.taf.execution.operator.impl.LocalLogOperatorImpl;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * <p>Triggers the build on local Jenkins, but uses external infrastructure
 * (MB, VE, ER)</p>
 * <p>Prerequisites:</p>
 * <p><ol>
 * <li>Start up local Jenkins instance by running te-jenkins-plugin via <code>mvn hpi:run</code> Note: you need to be in the te-jenkins-plugin folder </li>
 * <li>Start up local Jenkins slave by running LocalTestBootstrap.java main()</li>
 * <li>Create mandatory projects by running LocalTeJenkinsJobCreatingTest</li>
 * </ol></p>
 */
public class RunScheduleLocalTest extends RunScheduleCITest {

    @Inject
    private Provider<LocalLogOperatorImpl> localLogOperatorProvider;

    @BeforeClass
    public static void beforeTests() {
        // Forcibly tied to local tests profile, as the test logic is customized for local run
        loadTafProfileData(TafProfiles.LOCAL);
    }

    @Override
    public void shouldTriggerBuildViaRestCall() throws Exception {
        super.shouldTriggerBuildViaRestCall();
    }

    @Override
    public void shouldTriggerLegacyScheduleBuild() {
        super.shouldTriggerLegacyScheduleBuild();
    }

    @Override
    public void shouldTriggerBuildXmlSchedule() throws Exception {
        super.shouldTriggerBuildXmlSchedule();
    }

    @Override
    public void shouldTriggerWithDependancyMgtUpdates() throws Exception {
        super.shouldTriggerWithDependancyMgtUpdates();
    }

    @Override
    public void shouldUseTafSurefireProviderToDetectFailures() throws Exception {
        super.shouldUseTafSurefireProviderToDetectFailures();
    }

    @Override
    public void shouldRunIncludedSchedule() throws Exception {
        super.shouldRunIncludedSchedule();
    }

    @Override
    public void shouldRunManualTests() throws Exception {
        super.shouldRunManualTests();
    }

    @Override
    public void shouldUseArbitraryJavaVersion() throws Exception {
        super.shouldUseArbitraryJavaVersion();
    }

    @Override
    public void shouldUseCustomRuntimeSettings() throws Exception {
        super.shouldUseCustomRuntimeSettings();
    }

    @Override
    public void shouldUseDefaultSettingsForThreadLimitation() throws Exception {
        super.shouldUseDefaultSettingsForThreadLimitation();
    }

    @Override
    public void shouldUseScheduleSettingsForThreadLimitation() throws Exception {
        super.shouldUseScheduleSettingsForThreadLimitation();
    }

    @Override
    protected void verifyAllureReport(String allureLogUrl) {
        // Allure HTML report is not generated for local acceptance test
    }

    @Override
    @Test
    public void shouldGenerateMissingSuiteAllureXml() throws Exception {
        // Allure HTML report is not generated for local acceptance test
    }

    @Override
    @Test
    public void shouldGenerateAllureReportWhenJvmCrashForSuiteHappened() throws Exception {
        // Allure HTML report is not generated for local acceptance test
    }

    @Override
    protected LogOperator getLogOperator() {
        return localLogOperatorProvider.get();
    }

    protected static void loadTafProfileData(TafProfiles profile) {
        System.setProperty("taf.profiles", profile.getName());
        TafConfigurationProvider.provide().reload();
    }

}
