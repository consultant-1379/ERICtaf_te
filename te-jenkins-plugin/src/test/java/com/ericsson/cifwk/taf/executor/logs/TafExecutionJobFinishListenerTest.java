package com.ericsson.cifwk.taf.executor.logs;

import com.ericsson.cifwk.taf.executor.TafExecutionBuild;
import com.ericsson.cifwk.taf.executor.model.ExecutorBuildParameters;
import com.ericsson.cifwk.taf.executor.model.ScheduleBuildParameters;
import com.ericsson.cifwk.taf.executor.model.TeBuildMainParameters;
import com.google.common.io.Files;
import hudson.model.AbstractBuild;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.UUID;

import static com.ericsson.cifwk.taf.executor.TafScheduleBuilder.TE_LOGS_DIR;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 29/10/2015
 */
@RunWith(MockitoJUnitRunner.class)
public class TafExecutionJobFinishListenerTest {

    public static final String EXPECTED_LOG_CONTENT = "Expected Log Content";

    @Mock
    private TafExecutionBuild tafExecutionBuild;

    @Mock
    private AbstractBuild abstractBuild;

    private TafExecutionJobFinishListener tafExecutionJobFinishListener;
    private String stepName;
    private String executionId;
    private File tempDir;
    private File tafTeBuildLogFile;
    private ExecutorBuildParameters executorBuildParameters = new ExecutorBuildParameters();
    private ScheduleBuildParameters scheduleBuildParameters = new ScheduleBuildParameters();
    private TeBuildMainParameters teBuildMainParameters = new TeBuildMainParameters(null, scheduleBuildParameters);

    @Before
    public void setUp() throws IOException {
        stepName = UUID.randomUUID().toString();
        executionId = UUID.randomUUID().toString();
        tempDir = Files.createTempDir();

        tafExecutionJobFinishListener = new TafExecutionJobFinishListener();
        tafExecutionJobFinishListener = spy(tafExecutionJobFinishListener);
        doReturn(executorBuildParameters).when(tafExecutionJobFinishListener).getBuildParameters(any(AbstractBuild.class));
        doReturn(teBuildMainParameters).when(tafExecutionJobFinishListener).getTeBuildMainParameters(anyString());

        scheduleBuildParameters.setAllureLogDir(tempDir.getAbsolutePath());
        executorBuildParameters.setTestStepName(stepName);
        executorBuildParameters.setExecutionId(executionId);

        tafTeBuildLogFile = File.createTempFile("te-build", ".log");
        FileUtils.write(tafTeBuildLogFile, EXPECTED_LOG_CONTENT);
        doReturn(tafTeBuildLogFile).when(tafExecutionBuild).getLogFile();
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(tempDir);
        assertTrue(FileUtils.deleteQuietly(tafTeBuildLogFile));
    }

    @Test
    public void shouldNotInteractWithNonExecutorBuilds() {
        tafExecutionJobFinishListener.onFinalized(abstractBuild);
        verifyZeroInteractions(abstractBuild);
    }

    @Test
    public void shouldCopyLogsInAllureDirectory() throws IOException {
        tafExecutionJobFinishListener.onFinalized(tafExecutionBuild);

        //assert that in log directory folder with TEJobFinishListener.TE_LOGS_DIR name is created
        File copiedLogsDir = findTeLogsFolderInAllureLogsDirectory();

        //assert folder has build log file inside with execution id in its name
        File[] files = copiedLogsDir.listFiles();
        assertThat(files.length, is(1));

        File copiedTeLog = files[0];
        assertThat(copiedTeLog.getName(), containsString(sanitize(stepName)));

        String content = Files.toString(copiedTeLog, Charset.defaultCharset());
        assertThat(content, is(EXPECTED_LOG_CONTENT));
    }

    @Test
    public void shouldHandleMultipleLogsCopyToAllureDirectory() throws IOException {
        //Call first
        tafExecutionJobFinishListener.onFinalized(tafExecutionBuild);

        File copiedLogsDir = findTeLogsFolderInAllureLogsDirectory();
        File copiedBuildLog = copiedLogsDir.listFiles()[0];

        File renamedBuildLog = new File(copiedLogsDir, "renamed.log");
        assertTrue("Failed to rename copied log file", copiedBuildLog.renameTo(renamedBuildLog));

        //Call second time
        tafExecutionJobFinishListener.onFinalized(tafExecutionBuild);

        //assert folder has build log file inside with execution id in its name
        File[] files = copiedLogsDir.listFiles();
        assertThat(files.length, is(2));

        for (File logFile : copiedLogsDir.listFiles()) {
            assertThat(logFile.getName(), anyOf(containsString(sanitize(stepName)), is("renamed.log")));
            String content = Files.toString(logFile, Charset.defaultCharset());
            assertThat(content, is(EXPECTED_LOG_CONTENT));
        }
    }

    private String sanitize(String name) {
        return tafExecutionJobFinishListener.sanitizeStepName(name);
    }

    private File findTeLogsFolderInAllureLogsDirectory() {
        File copiedLogsDir = new File(tempDir, TE_LOGS_DIR);
        assertThat(copiedLogsDir.exists(), is(true));
        assertThat(copiedLogsDir.isDirectory(), is(true));
        return copiedLogsDir;
    }
}
