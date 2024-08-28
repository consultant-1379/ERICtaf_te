package com.ericsson.cifwk.taf.executor.node;

import com.ericsson.cifwk.taf.executor.NodeConfigurationProvider;
import com.ericsson.cifwk.taf.executor.TestExecution;
import com.ericsson.cifwk.taf.executor.TestResult;
import com.ericsson.cifwk.taf.executor.TestwareRuntimeLimitations;
import com.ericsson.cifwk.taf.executor.commons.MultilinePropertiesConverter;
import com.ericsson.cifwk.taf.executor.maven.GAV;
import com.ericsson.cifwk.taf.executor.maven.InvocationRequestHelper;
import com.ericsson.cifwk.taf.executor.maven.MavenToTeOutputHandler;
import com.ericsson.cifwk.taf.executor.maven.Pom;
import com.ericsson.cifwk.taf.executor.maven.PomValues;
import com.ericsson.cifwk.taf.executor.maven.TestPomGenerator;
import com.ericsson.cifwk.taf.executor.security.TestRuntimeSecurityManager;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ericsson.cifwk.taf.executor.maven.InvocationRequestHelper.mavenInvocationRequest;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

class TafTestRunner extends AbstractTafTestRunner {

    // The list of properties that should not be permitted for user to define, otherwise they will override
    // the system properties that are TAF-specific
    private final static Set<String> EXCLUDED_SYSTEM_PROPERTY_NAMES =
            Sets.newHashSet("groups", "suites", "taf.http_config.url", "suitethreadpoolsize",
                    "testng.test.classpath", "dir", "fakeSuitePath");
    private static final String GLOBAL_TEST_GROUPS_MISC_PROPERTY = "globalTestGroups";
    private static final String SKIP_TESTS = "skipTests";

    private final Pattern tafMavenPidPattern = Pattern.compile(".*Current process ID is (?<tafMavenPid>\\d+).*");

    protected PomValues values = new PomValues();
    private volatile Integer tafMavenPid = 0;
    private volatile boolean timedOut = false;
    private Timer timeoutTimer;
    private TestPomGenerator testPomGenerator;
    private ScheduleEnvironmentPropertyProvider environmentPropertyProvider;

    TafTestRunner(PrintStream buildLog) {
        super(buildLog);
        testPomGenerator = new TestPomGenerator();
    }

    @Override
    public void setUp(TestExecution execution) {
        super.setUp(execution);
        environmentPropertyProvider = new ScheduleEnvironmentPropertyProvider(buildLog, execution.getEnvPropertyJson());
        environmentPropertyProvider.setExcludedSystemSettings(EXCLUDED_SYSTEM_PROPERTY_NAMES);
        StringBuilder suites = new StringBuilder();
        for (String s : execution.getSuites()) {
            if (StringUtils.isNotBlank(s)) {
                suites.append(suites.length() > 0 ? "," : "").append(s);
            }
        }
        GAV testware = new GAV(execution.getTestware());
        values.setTestware(testware);
        values.setSuites(suites.toString());
        values.setGeneralExecutionId(execution.getGeneralExecutionId());
        values.setParentEventId(execution.getParentEventId());
        values.setParentExecutionId(execution.getParentExecutionId());
        values.setTestExecutionId(execution.getTestExecutionId());
        values.setMbExchange(execution.getMbExchange());
        values.setMbHost(execution.getMbHost());
        values.setMbDomain(execution.getMbDomain());
        values.setRepositoryUrl(execution.getRepositoryUrl());
        values.setConfigUrl(execution.getConfigUrl());
        String allureServiceUrl = execution.getAllureServiceUrl();
        if (isNotBlank(allureServiceUrl)) {
            values.setAllureServiceUrl(allureServiceUrl);
        }
        values.setAllureLogDir(execution.getAllureLogDir());
        values.setAllureVersion(execution.getAllureVersion());
        values.setMinTafVersion(execution.getMinTafVersion());
        values.setSkipTests(execution.getSkipTests());

        populateMiscPropertiesAndGroups(execution);
        populateSystemSettings(environmentPropertyProvider);

        List<GAV> additionalDependencies = getAdditionalDependencies(execution);
        values.setAdditionalDependencies(additionalDependencies);

        addUserDefinedGAVs(execution);

        createPom();
        copyAllureArchiveDescriptor(execution);
        //Override version of TAF specified in trigger if higher version of TAF if specified in the testware
        if (StringUtils.isNotBlank(values.getMinTafVersion())) {
            Pom pom = getPomForRun();
            testPomGenerator.updateTafVersion(pom, values, workingDir, buildLog);
        }
    }

    private void populateSystemSettings(ScheduleEnvironmentPropertyProvider environmentPropertyProvider) {
        Map<String, String> allSystemProperties = environmentPropertyProvider.getAllSystemProperties();
        Integer maxThreads = getMaxThreads(environmentPropertyProvider, executionDetails.getRuntimeLimitations());
        if (maxThreads != null) {
            buildLog.printf("Max number of threads for this test run is limited to %d%n", maxThreads);
            allSystemProperties.put(TestRuntimeSecurityManager.THREADS_LIMIT_PROPERTY, String.valueOf(maxThreads));
        }
        values.setSystemProperties(allSystemProperties);
    }

    @VisibleForTesting
    Integer getMaxThreads(ScheduleEnvironmentPropertyProvider environmentPropertyProvider, TestwareRuntimeLimitations runtimeLimitations) {
        Integer maxThreads = null;
        Integer maxThreadsEnvProperty = environmentPropertyProvider.getMaxThreads();
        if (maxThreadsEnvProperty != null) {
            maxThreads = maxThreadsEnvProperty;
        } else if (runtimeLimitations != null && runtimeLimitations.getMaxThreadCount() != null) {
            maxThreads = runtimeLimitations.getMaxThreadCount();
        }
        return maxThreads;
    }

    @VisibleForTesting
    void populateMiscPropertiesAndGroups(TestExecution execution) {
        Map<?, ?> miscProperties = MultilinePropertiesConverter.stringToPropertyMap(execution.getMiscProperties());
        String globalTestGroups = (String) miscProperties.get(GLOBAL_TEST_GROUPS_MISC_PROPERTY);
        String testGroups = execution.getGroups();
        if (StringUtils.isNotBlank(globalTestGroups)) {
            values.setGroups(globalTestGroups);
            if (StringUtils.isNotBlank(testGroups)) {
                buildLog.println(String.format("WARN: schedule item's group(s) '%s' will be overridden by '%s' groups",
                        testGroups, globalTestGroups));
            }
        } else {
            values.setGroups(testGroups);
        }
    }

    @Override
    protected void createPom(FileOutputStream outputStream) {
        testPomGenerator.emit(values, outputStream);
    }

    protected TestResult processFailedExecution() {
        FailedTestRunAnalyser failedTestRunAnalyser = new FailedTestRunAnalyser(
                workingDir,
                executionDetails.getSuites(),
                executionDetails.getAllureLogDir(),
                buildLog);
        failedTestRunAnalyser.writeSuiteWithResults();

        buildLog.println("Test finished with error or was aborted");
        if (tafMavenPid != 0) {
            buildLog.println(String.format("Killing TAF Maven process (PID=%d) if it's still alive", tafMavenPid));
            killTafMavenProcess();
        }
        if (timedOut) {
            return new TestResult(TestResult.Status.FAILURE);
        } else {
            return new TestResult(TestResult.Status.ERROR);
        }
    }

    private void killTafMavenProcess() {
        if (tafMavenPid != 0) {
            int gracePeriodMilli = Integer.parseInt(System.getProperty("killGracePeriod", "30000"));
            new ProcessDestroyer().kill(tafMavenPid, gracePeriodMilli);
            tafMavenPid = 0;
        } else {
            buildLog.println("Failed to kill TAF Maven Process - no PID");
        }
    }

    @Override
    protected int runMavenTestBuild(File workingDir) {
        InvocationRequest request = mavenInvocationRequest();
        request.setShowErrors(true);
        setJavaOptionsIfNeeded(request);
        String[] suites = executionDetails.getSuites();
        Joiner joiner = Joiner.on(", ");

        Pom pom = getPomForRun();

        request.setPomFile(pom.getFile());
        List<String> goals = Lists.newArrayList("test");

        buildLog.println("Settings goals to execute: " + goals.toString());
        request.setGoals(goals);

        String separator = "/";
        String fileExtension = ".xml";
        if (suites.length != 0) {
            for (int i=0; i<suites.length; i++)
            {
                String name = suites[i];
                buildLog.println("Test suite :" + suites[i]);
                if (name.contains(separator)) {
                    File sourceFile = new File(suites[i]);
                    Path sourcePath = sourceFile.toPath();
                    int pos = name.lastIndexOf(separator);
                    String fileName = name.substring(pos + separator.length());
                    int posNew = fileName.indexOf(fileExtension);
                    String newName = fileName.substring(0,posNew);
                    suites[i] = newName.concat("_new.xml");
                    buildLog.println("Final Test suite:" + suites[i]);
                    String copyPath = workingDir.getPath() + "/target/taf/suites/";
                    String copyFile = copyPath + suites[i];
                    File destFileNew = new File(copyFile);
                    Path destPathNew = destFileNew.toPath();
                    File destFile = new File(copyPath);
                    Path destPath = destFile.toPath();
                    if (! destFile.exists()) {
                        try {
                            Files.createDirectories(destPath);
                            buildLog.println("File created successfully in" + destPath);
                        } catch( IOException e){
                            buildLog.println("Failed to create file" + e);
                        }
                    }
                    try {
                        Files.copy(sourcePath, destPathNew, StandardCopyOption.REPLACE_EXISTING);
                        buildLog.println("File copied successfully in" + destPathNew);
                    } catch(IOException e) {
                        buildLog.println("Failed to copy file" + e);
                    }
                }
            }
            String value = joiner.join(suites);
            Properties properties = new Properties();
            properties.setProperty("suites", value);
            request.setProperties(properties);
        }

        setCustomJavaIfRequired(request);

        setMavenOutputHandlers(request);
        if (StringUtils.isNotBlank(executionDetails.getTimeoutInSeconds())) {
            monitorProcessTimeout(executionDetails);
        }

        return InvocationRequestHelper.invokeRequest(workingDir, request, buildLog);
    }

    @VisibleForTesting
    void setJavaOptionsIfNeeded(InvocationRequest request) {
        String javaOpts = environmentPropertyProvider.getJavaOpts();
        if (StringUtils.isNotBlank(javaOpts)) {
            buildLog.println(String.format("Applying custom JVM settings: '%s'", javaOpts));
            request.setMavenOpts(javaOpts);
        }
    }

    @VisibleForTesting
    void setCustomJavaIfRequired(InvocationRequest request) {
        Integer requiredJavaVersion = environmentPropertyProvider.getRequiredJavaVersion();
        if (requiredJavaVersion != null) {
            String javaHomeEnvPropertyName = String.format("JAVA%d_HOME", requiredJavaVersion);
            String requiredJavaHome = getSystemEnvProperty(javaHomeEnvPropertyName);
            if (StringUtils.isBlank(requiredJavaHome)) {
                buildLog.println(String.format(
                        "WARNING: the OS environment setting required for " +
                        "required Java version %d ('%s') is not defined, so it's impossible to locate the required Java. " +
                                "Build will proceed with default JAVA_HOME (%s)",
                        requiredJavaVersion, javaHomeEnvPropertyName, getSystemEnvProperty("JAVA_HOME")));
            } else {
                buildLog.println("Tests will be run using Java from " + requiredJavaHome);
                request.setJavaHome(new File(requiredJavaHome));
            }
        }
    }

    @VisibleForTesting
    String getSystemEnvProperty(String javaHomeEnvPropertyName) {
        return System.getenv(javaHomeEnvPropertyName);
    }

    @Override
    protected boolean reportingIsEnabled(TestExecution execution, Pom pomFile) {
        return !pomFile.isLegacy() && StringUtils.isNotBlank(execution.getAllureLogDir());
    }

    @Override
    protected Pom getPomForRun() {
        File pomFile = new File(this.workingDir, "pom.xml");
        if (pomFile.exists() && pomFile.isFile()) {
            return new Pom(pomFile, false);
        } else {
            return new Pom(new File(this.workingDir, "legacy-pom.xml"), true);
        }
    }

    @Override
    protected void setMavenOutputHandlers(final InvocationRequest request) {
        request.setOutputHandler(new InvocationOutputHandler() {
            @Override
            public void consumeLine(String line) {
                buildLog.println(line);
                // Search only when it's not found yet
                if (tafMavenPid == 0 && request.getGoals().contains("test")) {
                    findTafMavenPid(line);
                }
            }
        });
        request.setErrorHandler(new MavenToTeOutputHandler(buildLog));
    }

    // Current process ID is 8320
    boolean findTafMavenPid(String line) {
        Matcher matcher = tafMavenPidPattern.matcher(line);
        if (matcher.matches()) {
            String group = matcher.group("tafMavenPid");
            this.tafMavenPid = Integer.parseInt(group);
            return true;
        }
        return false;
    }

    void monitorProcessTimeout(TestExecution execution) {
        int timeoutInSeconds;
        try {
            timeoutInSeconds = Integer.parseInt(execution.getTimeoutInSeconds());
        } catch (NumberFormatException e) {
            timeoutInSeconds = 0;
        }

        if (timeoutInSeconds > 0) {
            timeoutTimer = new Timer(true);
            TimerTask timerTask = new HungBuildMonitoringTask(timeoutInSeconds);
            int pollPeriodInSeconds = nodeConfigProvider().getTimeoutTimerPollInSeconds();
            timeoutTimer.schedule(timerTask, 0, pollPeriodInSeconds * 1000);
            buildLog.println("Timeout timer started at " + new Date());
            buildLog.println(String.format("Timeout is defined for %s suite set: %d seconds", values.getSuites(), timeoutInSeconds));
        }
    }

    @VisibleForTesting
    NodeConfigurationProvider nodeConfigProvider() {
        return NodeConfigurationProvider.getInstance();
    }

    @VisibleForTesting
    PomValues getValues() {
        return values;
    }

    @Override
    public void tearDown() {
        if (timeoutTimer != null) {
            buildLog.println("Cancelling hung state monitoring timer");
            timeoutTimer.cancel();
        }
    }

    public Integer getTafMavenProcessId() {
        return tafMavenPid;
    }

    @VisibleForTesting
    protected void addUserDefinedGAVs(TestExecution execution) {
        List<String> userDefinedGavsAsString = execution.getUserDefinedGAVs();
        if(userDefinedGavsAsString == null) {
            return;
        }
        List<GAV> userDefinedPoms = new ArrayList<>();
        List<GAV> userDefinedBoms = new ArrayList<>();

        for(String singleGAV : userDefinedGavsAsString) {
            String[] splitGAV = singleGAV.split(":");
            if(splitGAV.length>2 && splitGAV.length<5) {
                buildLog.println(String.format("Setting %s to be added to DependencyManagement", singleGAV));
                GAV gav = new GAV(splitGAV[0], splitGAV[1], splitGAV[2]);
                if(splitGAV.length == 4) {
                    if(splitGAV[3].equals("bom")) {
                        buildLog.println(String.format("Setting %s as a bom with a scope of import", singleGAV));
                        gav.setIsBom(true);
                    } else {
                        throw new GavParsingException(String.format("Last parameter of '%s' not equal to bom", singleGAV));
                    }
                }
                if(gav.isBom()) {
                    userDefinedBoms.add(gav);
                } else {
                    userDefinedPoms.add(gav);
                }
            } else {
                throw new GavParsingException(String.format("Could not parse GAV from string '%s' using delimiter ':'", singleGAV));
            }
        }
        values.setUserDefinedBOMs(userDefinedBoms);
        values.setUserDefinedPOMs(userDefinedPoms);
    }

    private class HungBuildMonitoringTask extends TimerTask {

        private long startedAt;
        private int timeoutInSeconds;

        public HungBuildMonitoringTask(int timeoutInSeconds) {
            Preconditions.checkArgument(timeoutInSeconds > 0, "timeoutInSeconds must be a positive value");
            this.timeoutInSeconds = timeoutInSeconds;
            this.startedAt = System.currentTimeMillis();
        }

        @Override
        public void run() {
            if ((System.currentTimeMillis() - startedAt) / 1000 > timeoutInSeconds) {
                buildLog.println(new Date() + " Suite set [" + values.getSuites() + "] execution has timed out - terminating TAF Maven process");
                timedOut = true;
                killTafMavenProcess();
                cancel();
            }
        }
    }
}
