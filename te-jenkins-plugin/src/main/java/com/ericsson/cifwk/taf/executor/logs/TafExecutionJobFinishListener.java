package com.ericsson.cifwk.taf.executor.logs;

import com.ericsson.cifwk.taf.executor.TafExecutionBuild;
import com.ericsson.cifwk.taf.executor.model.ExecutorBuildParameters;
import com.ericsson.cifwk.taf.executor.model.ScheduleBuildParameters;
import com.ericsson.cifwk.taf.executor.model.TeBuildMainParameters;
import com.ericsson.cifwk.taf.executor.utils.JenkinsUtils;
import com.google.common.annotations.VisibleForTesting;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.listeners.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import static com.ericsson.cifwk.taf.executor.TafScheduleBuilder.TE_LOGS_DIR;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 28/10/2015
 */

@Extension
public class TafExecutionJobFinishListener extends RunListener<AbstractBuild<?, ?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TafExecutionJobFinishListener.class);

    @Override
    public void onFinalized(AbstractBuild<?, ?> abstractBuild) {
        if (abstractBuild instanceof TafExecutionBuild) {
            ExecutorBuildParameters currentExecutionParams = getBuildParameters(abstractBuild);
            TeBuildMainParameters mainParameters = getTeBuildMainParameters(currentExecutionParams.getExecutionId());
            ScheduleBuildParameters scheduleBuildParameters = mainParameters.getScheduleBuildParameters();

            String logDirectoryParameter = scheduleBuildParameters.getAllureLogDir();
            if (logDirectoryParameter == null) {
                LOGGER.error("Allure log directory is not found in parameters!");
            }
            String stepName = currentExecutionParams.getTestStepName();
            if (stepName == null) {
                LOGGER.error("Test step name (schedule item name) is not found in parameters!");
            }
            if (logDirectoryParameter != null && stepName != null) {
                copyBuildLogFile(abstractBuild.getLogFile(), logDirectoryParameter, stepName);
            }
        }
    }

    @VisibleForTesting
    ExecutorBuildParameters getBuildParameters(AbstractBuild<?, ?> abstractBuild) {
        return JenkinsUtils.getBuildParameters(abstractBuild, ExecutorBuildParameters.class);
    }

    @VisibleForTesting
    TeBuildMainParameters getTeBuildMainParameters(String executionId) {
        return TeBuildMainParameters.lookup(executionId);
    }

    private void copyBuildLogFile(File logFile, String directoryPath, String stepName) {
        String destPath = String.format("%s/%s/%s.log", directoryPath, TE_LOGS_DIR, sanitizeStepName(stepName));

        LOGGER.info(String.format("Copying %s to destination path %s", logFile.getAbsolutePath(), destPath));

        File destinationFile = new File(destPath);
        boolean dirCreated = destinationFile.getParentFile().mkdirs();
        LOGGER.info(String.format("Destination directory created %b", dirCreated));

        try (FileInputStream fileInputStream = new FileInputStream(logFile);
             FileChannel src = fileInputStream.getChannel();
             FileOutputStream fileOutputStream = new FileOutputStream(destinationFile);
             FileChannel dest = fileOutputStream.getChannel()) {
            dest.transferFrom(src, 0, src.size());
        } catch (IOException e) {
            LOGGER.error("Exception when copying TE Log file ", e);
            return;
        }
        LOGGER.info("Copied log file to " + destinationFile.getAbsolutePath());
    }

    protected String sanitizeStepName(String stepName) {
        return stepName.replaceAll("\\W+", "");
    }

}
