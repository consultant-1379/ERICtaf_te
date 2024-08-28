package com.ericsson.cifwk.taf.executor;

import com.cloudbees.plugins.flow.FlowRun;
import com.ericsson.cifwk.taf.executor.allure.UploadScript;
import com.ericsson.cifwk.taf.executor.allure.UploadScriptExecutor;
import com.ericsson.cifwk.taf.executor.api.schedule.model.Schedule;
import com.ericsson.cifwk.taf.executor.eiffel.EiffelMessageBus;
import com.ericsson.cifwk.taf.executor.http.client.FileDownloadClient;
import com.ericsson.cifwk.taf.executor.http.client.FileDownloadException;
import com.ericsson.cifwk.taf.executor.http.client.FileUploadClient;
import com.ericsson.cifwk.taf.executor.http.client.HttpClientFactory;
import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;
import com.ericsson.cifwk.taf.executor.model.ScheduleBuildParameters;
import com.ericsson.cifwk.taf.executor.schedule.ScheduleFlowHelper;
import com.ericsson.cifwk.taf.executor.schedule.ScheduleItemGavResolver;
import com.ericsson.cifwk.taf.executor.utils.GlobalTeSettingsProvider;
import com.ericsson.cifwk.taf.executor.utils.JenkinsUtils;
import com.ericsson.cifwk.taf.executor.utils.MessageBusUtils;
import com.ericsson.duraci.datawrappers.EventId;
import com.ericsson.duraci.datawrappers.ExecutionId;
import com.ericsson.duraci.datawrappers.LogReference;
import com.ericsson.duraci.datawrappers.ResultCode;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelJobFinishedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelJobStartedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelTestSuiteFinishedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelTestSuiteStartedEvent;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.TextParameterValue;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.zip.ZipUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.ericsson.cifwk.taf.executor.allure.UploadScript.UploadScriptBuilder.anUploadScript;
import static com.ericsson.cifwk.taf.executor.schedule.ScheduleFlowHelper.getSuiteCount;
import static com.ericsson.cifwk.taf.executor.utils.URIUtils.buildUri;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class TafScheduleBuilder extends Builder {

    private static final Logger LOGGER = LoggerFactory.getLogger(TafScheduleBuilder.class);

    public static final String NAME = Configurations.PLUGIN_NAME + ": Schedule Invocation";

    public static final String TE_LOGS_DIR = "te-console-logs";
    public static final String DATA = "data";
    public static final String TOTAL = "total.json";
    public static final String SKIP_PROPERTY = "taf.allure.skipCount.enable=true";

    private static final String ERROR_EMPTY_SCHEDULE = "Schedule can't be empty";
    private static final String ERROR_SCHEDULE_NOT_FOUND = "Schedule '%s!%s' was not found";
    private static final String ERROR_EMPTY_DSL = "Schedule parse result can't be empty";
    private static final String ERROR_EMPTY_FLOWJOBNAME = "Can't create flow job";

    private static final String LOG_MSG_BEGIN = "Beginning to execute build %s with parameters: %s";
    private static final String LOG_MSG_COMPLETED = "Build %s completed with parameters: %s";
    private static final String LOG_MSG_UNCOMPLETED = "Can't complete build %s with parameters: %s because of the error :%s";
    static final String TP_ENVIRONMENT_PROPERTIES_FILE_NAME = "tp-environment.properties";

    private static final String ERROR_REPOSITORY_EMPTY = "Repository url can't be empty for %s:%s";
    private static final String ERROR_SCHEDULE_ARTIFACT_EMPTY = "Schedule artifact can't be empty for %s:%s";
    private static final String ERROR_SCHEDULE_NAME_EMPTY = "Schedule name can't be empty for %s:%s";
    private static final String ERROR_BUILD_PARAMETERS = "Incorrect build parameters %s:%s";
    private static final String FAILED_TO_GET_PARENT_FOLDER_MSG_PATTERN = "Failed to get parent folder of '%s'";
    static final int TRIGGER_PROPS_WRITE_RETRY_COUNT = 5;

    @SuppressWarnings("WeakerAccess")
    @DataBoundConstructor
    public TafScheduleBuilder() {
        //TODO: this constructor never called on LocalTestBootstrap
    }

    @Override
    public boolean prebuild(Build build, BuildListener listener) {
        ScheduleBuildParameters params = JenkinsUtils.getBuildParameters(build, ScheduleBuildParameters.class);
        Preconditions.checkState(isParametersValid(build, params), ERROR_BUILD_PARAMETERS, build.getProject().getName(), build.getNumber());
        return true;
    }

    private boolean isParametersValid(Build build, ScheduleBuildParameters params) {
        String projectName = build.getProject().getName();
        int buildNumber = build.getNumber();
        checkArgument(isNotBlank(params.getRepositoryUrl()), ERROR_REPOSITORY_EMPTY, projectName, buildNumber);
        if (StringUtils.isBlank(params.getScheduleXml())) {
            checkArgument(isNotBlank(params.getScheduleArtifact()), ERROR_SCHEDULE_ARTIFACT_EMPTY, projectName, buildNumber);
            checkArgument(isNotBlank(params.getScheduleName()), ERROR_SCHEDULE_NAME_EMPTY, projectName, buildNumber);
        }
        return true;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {
        return perform((TafScheduleBuild) build, listener);
    }

    @SuppressFBWarnings
    boolean perform(final TafScheduleBuild build, final BuildListener listener)
            throws InterruptedException, IOException {
        GlobalTeSettings settings = GlobalTeSettingsProvider.getInstance().provide();
        ScheduleBuildParameters buildParameters = getBuildParameters(build);

        String jobInstance = buildParameters.getJobType();
        final String jobExecutionId = buildParameters.getExecutionId();
        final int jobExecutionNumber = build.getNumber();

        final String scheduleName = buildParameters.getScheduleName();

        final String enableLdap = buildParameters.getEnableLdap();

        final String teUsername = buildParameters.getTeUsername();

        final String tePassword = buildParameters.getTePassword();

        final String reportDirPath = getBuildParameters(build).getAllureLogDir();

        EiffelMessageBus messageBus = getEiffelMessageBus(settings);
        Boolean logUploadResult = null;
        // TODO: move all parameter calculation one level up, to TafScheduleBuild. Leave just Flow kick-off here.
        try {
            ExecutionId testExecutionId = new ExecutionId();
            String testTriggerPropertiesJson = buildParameters.getTestTriggerDetailsJson();
            Gson gson = new GsonBuilder().create();
            Properties testTriggerProperties = gson.fromJson(testTriggerPropertiesJson, Properties.class);

            EventId startEventId = sendJobStartedEvent(testTriggerProperties, jobInstance, jobExecutionId, jobExecutionNumber, testExecutionId,
                    messageBus, buildParameters);
            buildParameters.setEiffelJobStartedEventId(startEventId.toString());

            ExecutionId scheduleExecutionId = new ExecutionId();
            EventId scheduleStartedEventId = sendScheduleStartedEvent(new ExecutionId(jobExecutionId), scheduleExecutionId, messageBus,
                    testExecutionId, testTriggerProperties, startEventId, scheduleName);
            buildParameters.setEiffelScheduleStartedEventId(scheduleStartedEventId.toString());
            buildParameters.setEiffelScheduleStartedExecutionId(scheduleExecutionId.toString());
            buildParameters.setEiffelTestExecutionId(testExecutionId.toString());
            buildParameters.setEnableLdap(enableLdap);
            buildParameters.setTeUsername(teUsername);
            buildParameters.setTePassword(tePassword);

            build.addToParameters(buildParameters.getCommonEiffelParameters());

            PrintStream buildLogger = listener.getLogger();
          //  buildLogger.println(format(LOG_MSG_BEGIN, build, buildParameters.getAllParameters()));
            //
            String scheduleXml = buildParameters.getScheduleXml();
            if (StringUtils.isBlank(scheduleXml)) {
                String scheduleArtifact = buildParameters.getScheduleArtifact();
                scheduleXml = build.resolveSchedule(buildParameters.getRepositoryUrl(), scheduleArtifact, scheduleName);
                if (scheduleXml == null) {
                    String error = format(ERROR_SCHEDULE_NOT_FOUND, scheduleArtifact, scheduleName);
                    LOGGER.error(error);
                    throw new IllegalArgumentException(error);
                }
                checkArgument(isNotBlank(scheduleXml), ERROR_EMPTY_SCHEDULE);
                build.addToParameters(new TextParameterValue(BuildParameterNames.SCHEDULE, scheduleXml));
            }
            //
            ScheduleFlowHelper scheduleFlowHelper = new ScheduleFlowHelper(settings, buildParameters);
            ScheduleItemGavResolver gavResolver = scheduleFlowHelper.scheduleItemResolverFor(buildParameters);

            Schedule scheduleObject = scheduleFlowHelper.getSchedule(gavResolver, scheduleXml);
            String dsl = scheduleFlowHelper.getFlow(gavResolver, scheduleObject);

            checkArgument(isNotBlank(dsl), ERROR_EMPTY_DSL);
            build.setSchedule(scheduleObject);
            //
            String flowJobName = build.createFlowJob(dsl);
            checkArgument(isNotBlank(flowJobName), ERROR_EMPTY_FLOWJOBNAME);
            //
            Future<FlowRun> flowJob = build.scheduleFlowJob(flowJobName);
            buildLogger.println("Initiating the Flow build");
            FlowRun flowRun = flowJob.get();
            buildLogger.println("Flow build finished");
            Result result = flowRun.getResult();
            ResultCode resultCode = EiffelMessageBus.buildResultCode(result);

            boolean hasAllureService = isNotBlank(settings.getAllureServiceUrl());
            if (hasAllureService || isNotBlank(settings.getReportingScriptsFolder())) {
                boolean triggerDetailsResult = writeTriggerDetailsToAllureLogs(buildLogger, buildParameters);

                if (hasAllureService) {
                    uploadResource(settings, build, buildLogger);
                    try {
                        downloadReport(settings, build, buildLogger);
                    } catch (FileDownloadException e) {
                        listener.error("Error while downloading report archive");
                        throw e;
                    }
                }
                Properties testTriggerDetails = stringToProperties(testTriggerPropertiesJson);
                buildLogger.println("[INFO] The Skip Property in Allure is enabled:"+testTriggerDetails.toString().contains(SKIP_PROPERTY));
                if(testTriggerDetails.toString().contains(SKIP_PROPERTY)) {
                    long startTime = System.nanoTime();
                    editTotalJson(reportDirPath, buildLogger);
                    long endTime = System.nanoTime();
                    long time_ns = endTime - startTime;
                    long time_sec = TimeUnit.NANOSECONDS.toSeconds(time_ns);
                    buildLogger.println("[INFO] Time to edit total.json file(in secs) is:"+time_sec);

                }
                logUploadResult = triggerDetailsResult & uploadReports(build, scheduleObject, buildLogger, settings); // NOSONAR
            }

            sendScheduleFinishedEvent(messageBus, scheduleExecutionId, testExecutionId, resultCode);
            sendJobFinishedEvent(jobInstance, jobExecutionId, jobExecutionNumber, messageBus, resultCode);
            if (enableLdap != null && enableLdap.equalsIgnoreCase("true")) {
                    buildLogger.println("Build completed  with the provided parameters!");
                    String allureUrl = buildParameters.getAllureLogUrl();
                    if (allureUrl.contains("http:") && allureUrl.contains(":8088/")) {
                        allureUrl = allureUrl.replace("http:","https:").replace(":8088/",":443/");
                    }
                    buildLogger.println("ALLURE_LOG_URL="+allureUrl);
            }
            else{
                String buildCompleteMsg = format(LOG_MSG_COMPLETED, build, buildParameters);
                LOGGER.info(buildCompleteMsg);
            }
        } catch (Exception e) {
            sendScheduleFinishedEvent(messageBus, new ExecutionId(buildParameters.getEiffelScheduleStartedExecutionId()),
                    new ExecutionId(buildParameters.getEiffelTestExecutionId()), ResultCode.ABORTED);
            sendJobFinishedEvent(jobInstance, jobExecutionId, jobExecutionNumber, messageBus, ResultCode.ABORTED);
            if (enableLdap != null && enableLdap.equalsIgnoreCase("true")) {
                    listener.error("Build is not complete and failed with exception: " + e.getMessage());
            }
            else{
                listener.error(LOG_MSG_UNCOMPLETED, build, buildParameters, e.getMessage());
                LOGGER.error(format(LOG_MSG_UNCOMPLETED, build, buildParameters, e.getMessage()), e);
            }
            throw new RuntimeException(e);
        } finally {
            messageBus.disconnect();
        }
        return logUploadResult == null || Boolean.TRUE.equals(logUploadResult);
    }

    /**
     *  The method editTotalJson(..) is in reference to CIS-108838 and is used to update total.json file.
     *  Internally it calls in getSkippedCount(..) to get the count of skipped TC's.
     * @param reportDirectory-the path to that particular TE execution Allure logs which stores .xml files
     *
     */

    private void editTotalJson(String reportDirectory,PrintStream buildLog)  {
        Path dataFilePath = Paths.get(reportDirectory, DATA, TOTAL);
        int count = getSkippedCount(dataFilePath,buildLog);
        JSONParser parser = new JSONParser();
        try {
            Reader fileReader =new InputStreamReader(new FileInputStream(dataFilePath.toString()), "UTF-8");
            JSONObject jsonObject = (JSONObject)parser.parse(fileReader);
            JSONObject statistic = (JSONObject)jsonObject.get("statistic");
            Long lCount = (Long) statistic.get("broken");
            int brokenCount = lCount.intValue();
            int countNew = brokenCount-count;
            fileReader.close();
            statistic.put("broken",countNew);
            statistic.put("skipped",count);
            jsonObject.put("statistic",statistic);
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(dataFilePath.toString()), "UTF-8");
            fileWriter.write(jsonObject.toJSONString());
            fileWriter.close();

        }catch (IOException e) {
            buildLog.println("Problem in accessing the json file while reading/writing");
        }catch (ParseException e) {
            buildLog.println("Problem in parsing the file");
        }
    }

    private int getSkippedCount(Path filePath,PrintStream buildLog)  {
        int count=0;
        try {
            File dir =new File(filePath.getParent().getParent().toString());
            for (File file : dir.listFiles()) {
                String fileName = file.getName();
                if (fileName.contains(".xml")) {
                    Scanner scanner = new Scanner(file, "UTF-8");
                    while (scanner.hasNextLine()) {
                        String lineFromFile = scanner.nextLine();
                        if (lineFromFile.contains("org.testng.SkipException")) {
                            count++;
                        }
                    }
                    scanner.close();
                }
            }
        }catch (FileNotFoundException e) {
            buildLog.println("Problem in reading the xml files");
            e.printStackTrace();
        }catch (NullPointerException e) {
            buildLog.println("Count is zero as there is a problem in reading the xml files");
            e.printStackTrace();
        }
        return count;
    }


    @VisibleForTesting
    ScheduleBuildParameters getBuildParameters(TafScheduleBuild build) {
        return JenkinsUtils.getBuildParameters(build, ScheduleBuildParameters.class);
    }

    private EventId sendJobStartedEvent(Properties testTriggerProperties, String jobInstance, String jobExecutionId, int jobExecutionNumber,
            ExecutionId testExecutionId, EiffelMessageBus messageBus, ScheduleBuildParameters buildParameters) {
        EiffelJobStartedEvent startedEvent = EiffelJobStartedEvent.Factory.create(jobInstance, jobExecutionId, jobExecutionNumber);
        //
        startedEvent.setLogReference("allure_log_url", new LogReference(buildParameters.getAllureLogUrl()));
        startedEvent.setOptionalParameter("testExecutionId", testExecutionId.toString());

        for (Map.Entry<Object, Object> property : testTriggerProperties.entrySet()) {
            String key = (String) property.getKey();
            String value = (String) property.getValue();
            startedEvent.setOptionalParameter(key, value);
        }

        EventId eiffelJobScheduleEventId = new EventId(buildParameters.getEiffelJobTriggerEventId());
        return messageBus.sendStart(startedEvent, startedEvent.getJobExecutionId(), eiffelJobScheduleEventId);
    }

    private void sendJobFinishedEvent(String jobInstance, String jobExecutionId, int jobExecutionNumber, EiffelMessageBus messageBus,
            ResultCode resultCode) {
        EiffelJobFinishedEvent finishedEvent = EiffelJobFinishedEvent.Factory.create(jobInstance, jobExecutionId, jobExecutionNumber, resultCode);
        messageBus.sendFinish(finishedEvent);
    }

    private EventId sendScheduleStartedEvent(ExecutionId jobStartedExecutionId, ExecutionId sceduleExecutionId, EiffelMessageBus messageBus,
            ExecutionId testExecutionId, Properties testTriggerProperties, EventId startEventId, String scheduleName) {
        if (StringUtils.isBlank(scheduleName)) {
            scheduleName = "custom schedule";
        }
        EiffelTestSuiteStartedEvent suiteStartedEvent = EiffelTestSuiteStartedEvent.Factory
                .create(jobStartedExecutionId, "schedule", scheduleName, sceduleExecutionId);
        for (Map.Entry<Object, Object> property : testTriggerProperties.entrySet()) {
            String key = (String) property.getKey();
            String value = (String) property.getValue();
            suiteStartedEvent.setOptionalParameter(key, value);
        }
        suiteStartedEvent.setTestExecutionId(testExecutionId);
        return messageBus.sendStart(suiteStartedEvent, sceduleExecutionId, startEventId);
    }

    private void sendScheduleFinishedEvent(EiffelMessageBus messageBus, ExecutionId sceduleExecutionId, ExecutionId testExecutionId,
            ResultCode resultCode) {
        EiffelTestSuiteFinishedEvent suiteFinishedEvent = EiffelTestSuiteFinishedEvent.Factory.create(resultCode, null, sceduleExecutionId);
        suiteFinishedEvent.setTestExecutionId(testExecutionId);
        messageBus.sendFinish(suiteFinishedEvent);
    }

    @VisibleForTesting
    Properties stringToProperties(String string) {
        if (StringUtils.isBlank(string)) {
            return new Properties();
        }
        return new Gson().fromJson(string, Properties.class);
    }

    private boolean writeTriggerDetailsToAllureLogs(PrintStream buildLog, ScheduleBuildParameters buildParameters) {
        String triggerDetailsJson = buildParameters.getTestTriggerDetailsJson();
        Properties testTriggerDetails = stringToProperties(triggerDetailsJson);
        boolean result = true;
        if (testTriggerDetails.isEmpty()) {
            buildLog.println("WARN: no test trigger details were received; no TP environment relevant " +
                    "information will be added to the Allure report");
        } else {
            result = writeTriggerDetailsToAllureLogs(testTriggerDetails, buildLog, buildParameters);
        }
        return result;
    }

    @VisibleForTesting
    boolean writeTriggerDetailsToAllureLogs(Properties testTriggerDetails, PrintStream buildLog, ScheduleBuildParameters buildParameters) {
        String allureLogDir = buildParameters.getAllureLogDir();
        // TODO: impl a more reliable way to check for 100% legacy build
        if (!new File(allureLogDir).exists()) {
            buildLog.println(format("WARN: Not adding test trigger details %s to Allure logs %s, as the directory doesn't exist. Legacy build?",
                    testTriggerDetails, allureLogDir));
            return true;
        }

        Path tpEnvPropFilePath = getTpEnvPropFilePath(allureLogDir);
        buildLog.println(format("Writing test trigger properties %s to %s", testTriggerDetails, tpEnvPropFilePath));

        int counter = 0;
        do {
            if (writeTriggerPropsToFile(testTriggerDetails, tpEnvPropFilePath, buildLog)) {
                return true;
            } else
                counter++;
        } while (counter < TRIGGER_PROPS_WRITE_RETRY_COUNT);

        buildLog.println(format("ERROR: Failed to write trigger environment data into %s in %d attempts",
                allureLogDir + "/" + TP_ENVIRONMENT_PROPERTIES_FILE_NAME, TRIGGER_PROPS_WRITE_RETRY_COUNT));
        return false;
    }

    private Path getTpEnvPropFilePath(String allureLogDir) {
        return Paths.get(allureLogDir, TP_ENVIRONMENT_PROPERTIES_FILE_NAME);
    }

    @VisibleForTesting
    boolean writeTriggerPropsToFile(Properties testTriggerDetails, Path tpEnvPropFilePath, PrintStream buildLog) {
        try (FileOutputStream outputStream = new FileOutputStream(tpEnvPropFilePath.toFile())) {
            testTriggerDetails.store(outputStream, "TE trigger environment properties");
            return true;
        } catch (IOException e) { // NOSONAR
            buildLog.println(format("WARN: Failed to write trigger environment data into %s: %s", TP_ENVIRONMENT_PROPERTIES_FILE_NAME, e.getMessage()));
            return false;
        }
    }

    private boolean uploadReports(TafScheduleBuild build, Schedule scheduleObject, PrintStream buildLogger, GlobalTeSettings settings)
    throws IOException, URISyntaxException {
        UploadScriptExecutor logUploadExec = new UploadScriptExecutor(settings.getReportingScriptsFolder(), buildLogger);
        UploadScript uploadScript = anUploadScript()
                .withLocalReportsStorage(settings.getLocalReportsStorage())
                .withLogSubDir(build.getExecutionId())
                .withExpectedSuiteCount(getSuiteCount(scheduleObject))
                .shouldUpload(settings.isShouldUploadToOssLogs())
                .hasAllureService(!isNullOrEmpty(settings.getAllureServiceUrl()))
                .build();
        return logUploadExec.runScript(uploadScript);
    }

    private void uploadResource(GlobalTeSettings settings, TafScheduleBuild build, PrintStream buildLogger) throws IOException, URISyntaxException {
        String reportSourcePath = getBuildParameters(build).getAllureLogDir();
        Path consoleLogPath = Paths.get(reportSourcePath, TE_LOGS_DIR);
        Path tpEnvPropFilePath = getTpEnvPropFilePath(reportSourcePath);
        String enableLdap = getBuildParameters(build).getEnableLdap();
        String serviceUrl = format("%s.zip", buildUri(settings.getAllureServiceUrl(), build.getExecutionId()));
        FileUploadClient httpClient;

        if (exists(consoleLogPath) || exists(tpEnvPropFilePath)) {
            if (enableLdap != null && enableLdap.equalsIgnoreCase("true")) {
                    String teUsername = getBuildParameters(build).getTeUsername();
                    String tePassword = getBuildParameters(build).getTePassword();
                    httpClient = new FileUploadClient(HttpClientFactory.createSecureInstance(teUsername, tePassword));
            }
            else
                httpClient = new FileUploadClient(HttpClientFactory.createInstance());
            File archiveFile = pack(consoleLogPath, tpEnvPropFilePath);
            buildLogger.println(String.format("Uploading file %s to %s", archiveFile, serviceUrl));
            httpClient.upload(serviceUrl, archiveFile);
            if (!archiveFile.delete()) {
                buildLogger.println(String.format("WARN. Failed to delete archive %s", archiveFile));
            }
        } else {
            buildLogger.println(String.format("ERROR. No console logs found at %s", consoleLogPath));
        }
    }

    @SuppressFBWarnings
    @VisibleForTesting
    static File pack(Path sourcePath, Path... otherPaths) {
        Path sourcePathParent = sourcePath.getParent();
        Preconditions.checkArgument(sourcePathParent != null, FAILED_TO_GET_PARENT_FOLDER_MSG_PATTERN, sourcePath);

        File archiveFile = Paths.get(sourcePathParent.toString(), UUID.randomUUID().toString()).toFile();
        ZipUtil.pack(sourcePath.toFile(), archiveFile, true);
        for (Path anotherPath : otherPaths) {
            File anotherFile = anotherPath.toFile();
            ZipUtil.addEntry(archiveFile, anotherFile.getName(), anotherFile);
        }
        return archiveFile;
    }

    @SuppressFBWarnings
    private void downloadReport(GlobalTeSettings settings, TafScheduleBuild build, PrintStream buildLogger)
    throws URISyntaxException, IOException, FileDownloadException {
        String generalExecutionId = build.getExecutionId();
        String enableLdap = getBuildParameters(build).getEnableLdap();
        String serviceUrl = settings.getAllureServiceUrl();

        String requestUrl = buildUri(serviceUrl, generalExecutionId) + ".zip";
        Path reportPath = Paths.get(settings.getLocalReportsStorage(), generalExecutionId, randomUUID().toString());
        buildLogger.println("Downloading Allure report from " + requestUrl + " to " + reportPath.toString());
        FileDownloadClient httpClient;
        if (enableLdap != null && enableLdap.equalsIgnoreCase("true")) {
            String teUsername = getBuildParameters(build).getTeUsername();
            String tePassword = getBuildParameters(build).getTePassword();
            httpClient = new FileDownloadClient(HttpClientFactory.createSecureInstance(teUsername, tePassword));
        }
        else
            httpClient = new FileDownloadClient(HttpClientFactory.createInstance());
        File reportArchive = httpClient.download(requestUrl, reportPath);
        Path parentPath = reportPath.getParent();

        Preconditions.checkState(parentPath != null, FAILED_TO_GET_PARENT_FOLDER_MSG_PATTERN, reportPath);

        ZipUtil.unpack(reportArchive, parentPath.toFile());
        if (!reportArchive.delete()) {
            buildLogger.println(String.format("WARN. Failed to delete archive %s", reportArchive));
        }
    }

    EiffelMessageBus getEiffelMessageBus(GlobalTeSettings globalTeSettings) {
        return MessageBusUtils.initializeAndConnect(globalTeSettings);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public DescriptorImpl() {
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return TafScheduleProject.class.isAssignableFrom(jobType);
        }

        @Override
        public String getDisplayName() {
            return NAME;
        }

        public static DescriptorImpl get() {
            return (DescriptorImpl) JenkinsUtils.getJenkinsInstance().getDescriptor(TafScheduleBuilder.class);
        }

    }

}
