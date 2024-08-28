package com.ericsson.cifwk.taf.executor.node;

import com.ericsson.cifwk.taf.executor.TestExecution;
import com.ericsson.cifwk.taf.executor.TestResult;
import com.ericsson.cifwk.taf.executor.maven.GAV;
import com.ericsson.cifwk.taf.executor.maven.InvocationRequestHelper;
import com.ericsson.cifwk.taf.executor.maven.MavenToTeOutputHandler;
import com.ericsson.cifwk.taf.executor.maven.Pom;
import com.ericsson.cifwk.taf.executor.utils.JarUtils;
import com.ericsson.cifwk.taf.executor.utils.ZipUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.shared.invoker.InvocationRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ericsson.cifwk.taf.executor.maven.InvocationRequestHelper.mavenInvocationRequest;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 15/03/2016
 */
public abstract class AbstractTafTestRunner implements TestRunner {

    private static final int TEMP_DIR_ATTEMPTS = 1000;

    private static final String FAILED_TO_CREATE_WORK_DIR_MSG_PATTERN = "Failed to create working directory in '%s'";

    private static final String ASSEMBLY_ZIP_XML = "zip.xml";

    private static final String PATH_TO_ASSEMBLY_ZIP_XML_SRC = format("assembly/%s", ASSEMBLY_ZIP_XML);

    protected final PrintStream buildLog;

    @VisibleForTesting
    File workingDir;

    TestExecution executionDetails;

    public AbstractTafTestRunner(PrintStream buildLog) {
        this.buildLog = buildLog;
    }

    @Override
    public void setUp(TestExecution execution) {
        this.executionDetails = execution;
        buildLog.println("Current user: " + getCurrentUser());
        createWorkDir();
    }

    protected String getCurrentUser() {
        return System.getProperty("user.name");
    }

    protected List<GAV> getAdditionalDependencies(TestExecution execution) {
        List<String> additionalDependencies = execution.getAdditionalDependencies();
        return additionalDependencies == null ?
                Lists.<GAV>newArrayList() :
                Lists.newArrayList(Iterables.transform(additionalDependencies, new Function<String, GAV>() {
                    @Override
                    public GAV apply(String s) {
                        return new GAV(s);
                    }
                }));
    }

    void createPom() {
        File projectDescriptor = new File(workingDir, "pom.xml");
        try (FileOutputStream outputStream = new FileOutputStream(projectDescriptor)) {
            createPom(outputStream);
            executionDetails.setTestPomLocation(projectDescriptor.toString());
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    protected abstract void createPom(FileOutputStream outputStream);

    @VisibleForTesting
    void createWorkDir() {
        String workspaceDirName = getWorkspaceDirectoryName();
        Path workspaceDir = Paths.get(workspaceDirName);
        if (!workspaceDir.toFile().exists()) {
            try {
                Files.createDirectories(workspaceDir);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create a directory for Maven test runs: " + workspaceDirName, e);
            }
        }
        String baseName = System.currentTimeMillis() + "-";
        try {
            for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
                Path tempDir = Paths.get(workspaceDirName, baseName + counter);
                try {
                    tempDir = Files.createDirectory(tempDir);
                    File tempDirFile = tempDir.toFile();
                    buildLog.println("Working directory for the current test run is " + tempDir);
                    this.workingDir = tempDirFile;
                    return;
                } catch (FileAlreadyExistsException e) {  // NOSONAR
                    // ignore
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(format(FAILED_TO_CREATE_WORK_DIR_MSG_PATTERN, workspaceDirName), e);
        }
        throw new RuntimeException(format(FAILED_TO_CREATE_WORK_DIR_MSG_PATTERN, workspaceDirName));
    }

    @VisibleForTesting
    String getWorkspaceDirectoryName() {
        return WorkspaceDataProvider.getWorkspaceDirectoryName();
    }

    @Override
    public TestResult runTest() {
        buildLog.println("Running Dependency Resolution");
        runMavenGoal(workingDir, "dependency:resolve", buildLog);
        runMavenGoal(workingDir, "dependency:resolve-plugins", buildLog);
        buildLog.println("Starting Tests Process");
        int exitCode = runMavenTestBuild(workingDir);
        buildLog.println("Test process finished with exit code : " + exitCode);

        Pom pom = getPomForRun();

        if (reportingIsEnabled(executionDetails, pom)) {
            int siteExitCode = runMavenGoal(workingDir, "site-deploy", buildLog);
            buildLog.println("Site Deploy finished with exit code : " + siteExitCode);
        }

        // Checking OS to avoid running this in local itests on Windows machines
        if (OsInformation.isNix()) {
            //Make allureLogDir writeable so env properties can be added to allure report
            String command = "chmod 777 " + executionDetails.getAllureLogDir();
            String[] chmod = {"/bin/sh", "-c", command};
            executeCommands(chmod);
        }

        if (0 == exitCode) {
            return new TestResult(TestResult.Status.SUCCESS);
        } else {
            return processFailedExecution();
        }
    }

    protected abstract TestResult processFailedExecution();

    protected void executeCommands(String[] commands) {
        try {
            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.redirectErrorStream(true);
            pb.start();
        } catch (IOException e) {  // NOSONAR
            buildLog.println("ERROR: IO Exception happened while executing commands '" + Arrays.asList(commands)
                    + "': " + e.getMessage());
        }
    }

    protected abstract int runMavenTestBuild(File workingDir);

    protected int runMavenGoal(File workingDir, String goal, final PrintStream buildLog) {
        InvocationRequest siteRequest = mavenInvocationRequest();
        Pom pom = getPomForRun();

        siteRequest.setPomFile(pom.getFile());
        List<String> goals = new ArrayList<>();
        goals.add(goal);

        buildLog.println("Settings goals to execute: " + goals.toString());
        siteRequest.setGoals(goals);

        setMavenOutputHandlers(siteRequest);

        return InvocationRequestHelper.invokeRequest(workingDir, siteRequest, buildLog);
    }

    protected void setMavenOutputHandlers(final InvocationRequest request) {
        PrintStream teLogger = getTeLogger();
        request.setOutputHandler(new MavenToTeOutputHandler(teLogger));
        request.setErrorHandler(new MavenToTeOutputHandler(teLogger));
    }

    protected PrintStream getTeLogger() {
        return buildLog;
    }

    protected boolean reportingIsEnabled(TestExecution execution, Pom pomFile) {
        return StringUtils.isNotBlank(execution.getAllureLogDir());
    }

    @VisibleForTesting
    protected Pom getPomForRun() {
        File pomFile = new File(this.workingDir, "pom.xml");
        return new Pom(pomFile, false);
    }

    public File getWorkingDir() {
        return workingDir;
    }

    protected void copyAllureArchiveDescriptor(TestExecution execution) {
        if (isNullOrEmpty(execution.getAllureServiceUrl())) {
            buildLog.println("Allure service URL is not defined - Allure CLI tool will be used");
            return;
        }
        try {
            Path targetDirPath = workingDir.toPath();
            Path targetFilePath = Paths.get(targetDirPath.toString(), ASSEMBLY_ZIP_XML);
            Optional<URI> thisJarUri = JarUtils.getThisJarUri();
            if (thisJarUri.isPresent()) {
                buildLog.println(format("Copying Allure '%s' of '%s' to '%s'",
                        PATH_TO_ASSEMBLY_ZIP_XML_SRC, thisJarUri.get(), targetFilePath));
                ZipUtils.copyZipEntry(thisJarUri.get(), PATH_TO_ASSEMBLY_ZIP_XML_SRC, targetFilePath);
            } else {
                Path zipPathSource = Paths.get(ClassLoader.getSystemResource(PATH_TO_ASSEMBLY_ZIP_XML_SRC).toURI());
                buildLog.println(format("Copying Allure archive descriptor from %s to %s", zipPathSource, targetFilePath));
                Files.copy(zipPathSource, targetFilePath);
            }
        } catch (URISyntaxException | IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
