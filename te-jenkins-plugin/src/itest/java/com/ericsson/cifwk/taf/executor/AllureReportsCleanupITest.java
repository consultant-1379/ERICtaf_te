package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.maintenance.AllureReportsCleanup;
import com.google.common.io.Files;
import hudson.ExtensionList;
import hudson.model.PeriodicWork;
import hudson.model.TopLevelItemDescriptor;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class AllureReportsCleanupITest extends JenkinsIntegrationTest {

    private File localReportsDir;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        TopLevelItemDescriptor descriptor =
                (TopLevelItemDescriptor) jenkins().getDescriptor(TafScheduleProject.class);
        TafScheduleProject scheduleProject = (TafScheduleProject) jenkins().createProject(descriptor, TAF_SCHEDULE_JOB, true);
        scheduleProject.reportMbDomainId = CommonTestConstants.MB_DOMAIN;

        // Reports don't exist, because master itests do not start real slaves - so creating manually
        localReportsDir = Files.createTempDir();
        scheduleProject.localReportsStorage = localReportsDir.getAbsolutePath();
        createDirectory(localReportsDir, "allure-results1", 20);
        createDirectory(localReportsDir, "allure-results2", 25);
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(localReportsDir);
    }

    @Test
    public void shouldDeleteOldReports() throws Exception {

        assertThat(localReportsDir.list().length, equalTo(2));

        ExtensionList<PeriodicWork> periodicWorks = PeriodicWork.all();
        AllureReportsCleanup cleanupWork = periodicWorks.get(AllureReportsCleanup.class);
        assertNotNull(cleanupWork);

        cleanupWork.run();

        // It takes some time to delete the reports - so need to wait
        Thread.sleep(7000);

        assertThat(localReportsDir.list().length, equalTo(2));
    }

    private File createFile(File parentDir, String fileName, int ageInHours) throws IOException {
        File file = new File(parentDir, fileName);
        file.createNewFile();
        Files.write("<attachment/>", file, Charset.defaultCharset());
        makeFileOld(file, ageInHours);
        return file;
    }

    private void makeFileOld(File file, int ageInHours) {
        DateTime now = new DateTime();
        DateTime then = now.minusHours(ageInHours);
        if (!file.setLastModified(then.toDate().getTime())) {
            throw new IllegalStateException("Failed to update 'modified' attribute for file " + file);
        }
    }

    private File createDirectory(File parentDir, String name, int ageInHours) throws IOException {
        File dir = new File(parentDir, name);
        dir.mkdir();
        // Make sure directory is not empty
        createFile(dir, "suite-result.xml", ageInHours);
        makeFileOld(dir, ageInHours);
        return dir;
    }
}
