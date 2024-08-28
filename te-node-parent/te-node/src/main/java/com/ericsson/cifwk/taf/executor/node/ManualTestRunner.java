package com.ericsson.cifwk.taf.executor.node;

import com.ericsson.cifwk.taf.executor.TestExecution;
import com.ericsson.cifwk.taf.executor.TestResult;
import com.ericsson.cifwk.taf.executor.maven.ManualTestPomGenerator;
import com.ericsson.cifwk.taf.executor.maven.Pom;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.apache.maven.shared.invoker.InvocationRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import static com.ericsson.cifwk.taf.executor.maven.InvocationRequestHelper.invokeRequest;
import static com.ericsson.cifwk.taf.executor.maven.InvocationRequestHelper.mavenInvocationRequest;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 16/03/2016
 */
public class ManualTestRunner extends AbstractTafTestRunner {

    private final ManualTestPomGenerator pomGenerator;

    public ManualTestRunner(PrintStream buildLog) {
        super(buildLog);
        pomGenerator = new ManualTestPomGenerator();
    }

    @VisibleForTesting
    ManualTestRunner(ManualTestPomGenerator pomGenerator, PrintStream buildLog) {
        super(buildLog);
        this.pomGenerator = pomGenerator;
    }

    @Override
    public void setUp(TestExecution execution) {
        super.setUp(execution);
        createPom();
        copyAllureArchiveDescriptor(execution);
    }

    @Override
    protected void createPom(FileOutputStream outputStream) {
        pomGenerator.emit(executionDetails, outputStream);
    }

    @Override
    protected TestResult processFailedExecution() {
        return new TestResult(TestResult.Status.FAILURE);
    }

    @Override
    protected int runMavenTestBuild(File workingDir) {
        InvocationRequest request = mavenInvocationRequest();

        Pom pom = getPomForRun();

        request.setPomFile(pom.getFile());
        request.setGoals(Lists.newArrayList("test"));
        setMavenOutputHandlers(request);

        return invokeRequest(workingDir, request, buildLog);
    }

    @Override
    public void tearDown() {
    }
}
