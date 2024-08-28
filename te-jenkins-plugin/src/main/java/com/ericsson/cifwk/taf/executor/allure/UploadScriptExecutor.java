package com.ericsson.cifwk.taf.executor.allure;

import com.google.common.annotations.VisibleForTesting;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class UploadScriptExecutor {

    private static final String REPORTS_DELIVERY_SCRIPT = "{cliDirPath}upload {localReportsStorage} {logFolder} {expectedSuiteCount} {uploadToRemoteHost} {hasAllureService}";

    private final String reportsUploadDir;
    private final PrintStream buildLogger;

    public UploadScriptExecutor(String logUploadScriptDir, PrintStream buildLogger) throws IOException {
        this(logUploadScriptDir, buildLogger, true);
    }

    @VisibleForTesting
    UploadScriptExecutor(String reportsUploadScriptDir, PrintStream buildLogger, boolean checkDirectory) throws IOException {
        this.buildLogger = buildLogger;

        if (checkDirectory) {
            File scriptDirectory = new File(reportsUploadScriptDir);
            if (!scriptDirectory.exists() || !scriptDirectory.isDirectory()) {
                throw new IllegalArgumentException(
                        String.format("Invalid path specified. Path to upload script directory is required, but '%s' specified",
                                reportsUploadScriptDir));
            }
        }
        this.reportsUploadDir = reportsUploadScriptDir;
    }

    public boolean runScript(UploadScript uploadScript) throws IOException {
        String command = getCommandToRun(uploadScript);
        int exitValue;
        int count = 1;
        do {

            buildLogger.println("Executing upload script command: " + command);

            String[] exec = {"/bin/sh", "-c", command};

            ProcessBuilder pb = new ProcessBuilder(exec);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            logScriptOutput(process.getInputStream());
            count++;
            try {
                exitValue = process.waitFor();
            } catch (InterruptedException e) {
                buildLogger.println("ERROR: log upload process was interrupted");
                return false;
            }
            if (exitValue == 0) {
                buildLogger.println("INFO: log upload success");
                break;
            }
        }while (count <= 3);

        if (exitValue != 0) {
            buildLogger.println("ERROR: log upload process returned code " + exitValue);
            return false;
        }
        return true;
    }

    @VisibleForTesting
    String getCommandToRun(UploadScript uploadScript) {
        return REPORTS_DELIVERY_SCRIPT
            .replace("{cliDirPath}", reportsUploadDir)
            .replace("{localReportsStorage}", uploadScript.getLocalReportsStorage())
            .replace("{logFolder}", uploadScript.getLogSubDir())
            .replace("{expectedSuiteCount}", String.valueOf(uploadScript.getExpectedSuiteCount()))
            .replace("{uploadToRemoteHost}", String.valueOf(uploadScript.shouldUpload()))
            .replace("{hasAllureService}", String.valueOf(uploadScript.hasAllureService()));
    }

    private void logScriptOutput(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;

        while ((line = reader.readLine()) != null) {
            buildLogger.append(line);
            buildLogger.println();
        }
        reader.close();
    }
}
