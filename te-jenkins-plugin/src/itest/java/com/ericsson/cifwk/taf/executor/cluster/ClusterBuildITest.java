package com.ericsson.cifwk.taf.executor.cluster;

import com.ericsson.cifwk.taf.executor.JenkinsIntegrationTest;
import com.ericsson.cifwk.taf.executor.TAFExecutor;
import com.ericsson.cifwk.taf.executor.TafExecutionBuilder;
import com.ericsson.cifwk.taf.executor.TestExecution;
import com.ericsson.cifwk.taf.executor.TestExecutionResult;
import com.ericsson.cifwk.taf.executor.TestResult;
import com.ericsson.cifwk.taf.executor.schedule.TafTestExecutor;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.Cause;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.Result;
import hudson.model.Slave;
import hudson.model.queue.QueueTaskFuture;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hudson.test.FakeLauncher;
import org.jvnet.hudson.test.MockBuilder;
import org.jvnet.hudson.test.PretendSlave;

import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ClusterBuildITest extends JenkinsIntegrationTest {

    private static final String MASTER = TAFExecutor.TAF_MASTER_LABEL;
    private static final String SLAVE_1 = "first";
    private static final String SLAVE_2 = "second";

    @Before
    public void setUpBeforeTests() throws Exception {
        Slave slave1 = createSlave(SLAVE_1 + " " + TAFExecutor.TAF_NODE_LABEL);
        Slave slave2 = createSlave(SLAVE_2 + " " + TAFExecutor.TAF_NODE_LABEL);
        jenkins().addNode(slave1);
        jenkins().addNode(slave2);
    }

    @After
    public void tearDown() throws Exception {
        shutdownSlave(SLAVE_1);
        shutdownSlave(SLAVE_2);
    }

    @Test
    public void shouldRunTestOnPredefinedSlave() throws Exception {
        FreeStyleProject project = context().createFreeStyleProject();

        project.getBuildersList().add(new MockBuilder(Result.SUCCESS));

        QueueTaskFuture<FreeStyleBuild> future = startOnSlave(project, SLAVE_2);

        FreeStyleBuild build = future.get();
        assertThat(build.getBuiltOn().getLabelString(), containsString(SLAVE_2));
    }

    @Test
    @Ignore("Pass only run w/o other JENKINS tests")
    //TODO //FIX //INVESTIGATE
    public void shouldDuplicateOutputOnMaster() throws Exception {
        FreeStyleProject project = context().createFreeStyleProject(UUID.randomUUID().toString());
        PrintingTestExecutor executor = new PrintingTestExecutor("hello", "error", new TestExecutionResult(TestResult.Status.SUCCESS, null));
        TafExecutionBuilder builder = new TafExecutionBuilder();

        project.getBuildersList().add(builder);

        QueueTaskFuture<FreeStyleBuild> future = startOnSlave(project, SLAVE_1);

        FreeStyleBuild build = future.get();
        assertThat(build.getBuiltOn().getLabelString(), equalTo(SLAVE_1));

        String log = FileUtils.readFileToString(build.getLogFile());
        assertThat(log, containsString("hello"));
        assertThat(log, containsString("error"));
    }

    @Test
    public void shouldExecuteOnMaster() throws Exception {
        FreeStyleProject project = context().createFreeStyleProject();

        project.getBuildersList().add(new MockBuilder(Result.SUCCESS));

        QueueTaskFuture<FreeStyleBuild> future = startOnSlave(project, MASTER);

        FreeStyleBuild build = future.get();
        assertThat(build.getBuiltOn().getLabelString(), equalTo(""));
    }

    private Slave createSlave(String labels) throws Exception {
        PretendSlave slave = jenkinsContext.createPretendSlave(new TestLauncher(0));
        slave.setLabelString(labels);
        return slave;
    }

    private QueueTaskFuture<FreeStyleBuild> startOnSlave(FreeStyleProject project, String label) {
        ParametersAction action = new ParametersAction(new NodeLabelParameterValue(label));
        return project.scheduleBuild2(0, (Cause) null, action);
    }

    public static class PrintingTestExecutor implements TafTestExecutor {

        final String out;
        final String err;
        final TestExecutionResult result;

        public PrintingTestExecutor(String out, String err, TestExecutionResult result) {
            this.out = out;
            this.err = err;
            this.result = result;
        }

        @Override
        public TestExecutionResult runTests(TestExecution execution, PrintStream buildLog) {
            buildLog.println(out);
            buildLog.flush();
            buildLog.println(err);
            buildLog.flush();
            return result;
        }

    }

    public static class TestLauncher implements FakeLauncher {

        private int exitCode;

        public TestLauncher(int exitCode) {
            this.exitCode = exitCode;
        }

        @Override
        public Proc onLaunch(Launcher.ProcStarter p) throws IOException {
            return new FinishedProc(exitCode);
        }
    }

}
