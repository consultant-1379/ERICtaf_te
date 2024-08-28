package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.utils.JenkinsUtils;
import com.ericsson.cifwk.taf.ui.Browser;
import com.ericsson.cifwk.taf.ui.BrowserTab;
import com.ericsson.cifwk.taf.ui.BrowserType;
import com.ericsson.cifwk.taf.ui.UI;
import com.ericsson.cifwk.taf.ui.sdk.Button;
import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.common.base.Throwables;
import hudson.model.Descriptor;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.TopLevelItemDescriptor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockBuilder;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class PluginConfigurationTest extends JenkinsIntegrationTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() {
        deleteAllJobs();
    }

    @Test
    public void shouldRunSimpleJob() throws Exception {
        FreeStyleProject project = createMockProject();

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertThat(build.getResult(), equalTo(Result.SUCCESS));
    }

    @Test
    public void shouldHaveConsistentGlobalConfigUI() throws Exception {
        JenkinsRule.WebClient client = jenkinsContext.createWebClient();
        client.setJavaScriptEnabled(false);
        HtmlPage configPage = client.goTo("configure");

        verifyConfigPageConsistency(configPage);
    }

    @Test
    public void shouldHaveConsistentProjectConfigUI() throws Exception {
        createTafScheduleProject();
        JenkinsRule.WebClient client = jenkinsContext.createWebClient();
        client.setJavaScriptEnabled(false);
        HtmlPage configPage = client.goTo("job/" + TAF_SCHEDULE_JOB + "/configure");

        verifyConfigPageConsistency(configPage);
    }

    private void verifyConfigPageConsistency(HtmlPage configPage) {
        WebAssert.assertTextPresent(configPage, Configurations.PLUGIN_NAME);
        WebAssert.assertInputPresent(configPage, "_.reportMbUsername");
        WebAssert.assertInputPresent(configPage, "_.reportMbPassword");

        final String reportMbHostFieldName = "_.reportMbHost";
        final String reportMbPortFieldName = "_.reportMbPort";
        final String reportMbExchangeFieldName = "_.reportMbExchange";
        final String reportMbDomainIdFieldName = "_.reportMbDomainId";
        final String flowsAgeInDays = "_.deletableFlowsAgeInDays";

        // Check default config values
        WebAssert.assertInputContainsValue(configPage, reportMbHostFieldName, "mb1");
        WebAssert.assertInputContainsValue(configPage, reportMbPortFieldName, "5672");
        WebAssert.assertInputContainsValue(configPage, reportMbExchangeFieldName, "eiffel.taf.executor");
        WebAssert.assertInputContainsValue(configPage, reportMbDomainIdFieldName, "test.execution");
        WebAssert.assertInputContainsValue(configPage, flowsAgeInDays, "2");
    }

    @Test
    public void shouldSaveConfiguration() throws Exception {
        createTafScheduleProject();
        JenkinsRule.WebClient client = jenkinsContext.createWebClient();
        String configPath = client.getContextPath() + "job/" + TAF_SCHEDULE_JOB + "/configure";
        Browser browser = UI.newBrowser(BrowserType.HEADLESS);

        try {
            BrowserTab tab = browser.open(configPath);
            Button submitButton = tab.getGenericView().getButton("span[name='Submit']");
            submitButton.click();
            Thread.sleep(5000);
        } finally {
            browser.close();
        }

        TafScheduleProject project = JenkinsUtils.getProjectOfType(jenkins(), TafScheduleProject.class);
        Assert.assertEquals("mb1", project.getReportMbHost());
        Assert.assertEquals(new Integer(5672), project.getReportMbPort());
        Assert.assertEquals("eiffel.taf.executor", project.getReportMbExchange());
    }

    private TafScheduleProject createTafScheduleProject() {
        Descriptor scheduleProjectDescriptor = jenkins().getDescriptor(TafScheduleProject.class);
        TafScheduleProject project;
        try {
            project = (TafScheduleProject) jenkins().createProject(
                    (TopLevelItemDescriptor) scheduleProjectDescriptor, TAF_SCHEDULE_JOB, false);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
        return project;
    }

    private FreeStyleProject createMockProject() throws IOException {
        FreeStyleProject project = jenkinsContext.createFreeStyleProject();
        project.getBuildersList().add(new MockBuilder(Result.SUCCESS));
        return project;
    }

}
