package com.ericsson.cifwk.taf.executor.node;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessDestroyer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessDestroyer.class);

    public boolean kill(int pid, int timeout) {
        return kill(getSigIntCommand(pid), pid, timeout) ||
                kill(getSigTermCommand(pid), pid, timeout) ||
                kill(getSigKillCommand(pid), pid, timeout);
    }

    @VisibleForTesting
    boolean kill(String killCommand, int pid, int timeOut) {
        LOGGER.debug("Trying to terminate TAF Maven process with " + killCommand);
        long time = System.currentTimeMillis();
        long end  = time + timeOut;
        try {
            executeCommand(killCommand);
            while (isCurrentTimeBefore(end)) {
                if (isProcessRunning(pid)) {
                    Thread.sleep(300);
                } else {
                    LOGGER.info("Process killed using " + killCommand);
                    return true;
                }
            }
        }
        catch (IOException e) {
            LOGGER.error("Failed to kill process using " + killCommand, e);
        }
        catch (InterruptedException e) {
            LOGGER.error("Interrupted when checking if TAF process was killed", e);
        }

        return false;
    }

    @VisibleForTesting
    boolean isCurrentTimeBefore(long futureTimestamp) {
        return System.currentTimeMillis() < futureTimestamp;
    }

    @VisibleForTesting
    boolean isProcessRunning(int pid) throws IOException {
        String line;
        Process checkTafProcessIsRunning = executeCommand(getProcessListCommand(pid));
//        checkTafProcessIsRunning.waitFor();
        try (BufferedReader outputReader = new BufferedReader(new InputStreamReader(checkTafProcessIsRunning.getInputStream()))) {
            while ((line = outputReader.readLine()) != null) {
                if (line.contains(Integer.toString(pid))) {
                    return true;
                }
            }
        }
        return false;
    }

    @VisibleForTesting
    Process executeCommand(String command) throws IOException {
        Preconditions.checkState(StringUtils.isNotBlank(command), "Cannot accept empty commands");
        return new ProcessBuilder(command.split(" ")).start();
    }

    @VisibleForTesting
    String getProcessListCommand(int pid) {
        switch (OsInformation.getOsName()) {
            case WINDOWS:
                return "tasklist";
            case NIX:
                return "ls /proc/ | grep ^" + pid + "$";
            default:
                throw new UnsupportedOperationException("Task list command in undefined for this OS");
        }
    }

    @VisibleForTesting
    String getSigIntCommand(int pid) {
        switch (OsInformation.getOsName()) {
            case WINDOWS:
                return getWindowsKillCommand(pid);
            case NIX:
                return "kill -2 " + pid;
            default:
                LOGGER.warn("Process termination on this OS not supported yet");
                return null;
        }
    }

    @VisibleForTesting
    String getSigTermCommand(int pid) {
        switch (OsInformation.getOsName()) {
            case WINDOWS:
                return getWindowsKillCommand(pid);
            case NIX:
                return "kill -15 " + pid;
            default:
                LOGGER.warn("Process termination on this OS not supported yet");
                return null;
        }
    }

    @VisibleForTesting
    String getSigKillCommand(int pid) {
        switch (OsInformation.getOsName()) {
            case WINDOWS:
                return getWindowsKillCommand(pid);
            case NIX:
                return "kill -9 " + pid;
            case MAC:
                LOGGER.warn("Process termination on MAC not supported yet");
                return null;
            default:
                LOGGER.warn("Process termination on this OS not supported yet");
                return null;
        }
    }

    private String getWindowsKillCommand(int pid) {
        return "taskkill /F /PID " + pid;
    }

}
