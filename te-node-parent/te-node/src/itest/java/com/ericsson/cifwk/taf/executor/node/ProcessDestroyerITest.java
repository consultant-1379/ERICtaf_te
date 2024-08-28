package com.ericsson.cifwk.taf.executor.node;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class ProcessDestroyerITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessDestroyerITest.class);

    @Test
    public void killProcessTest() throws IOException, NoSuchFieldException, IllegalAccessException, InterruptedException {
        if (!OsInformation.isNix()) {
            LOGGER.info("Test bypassed, as intended to be run only on *nix machines");
            return;
        }
        String scriptText = "#!/bin/bash\n" +
                "\n" +
                "trap \"echo SIGINT\" SIGINT\n" +
                "trap \"echo SIGTERM\" SIGTERM\n" +
                "\n" +
                "ps aux | grep ProcessDestroyerTestScript\n" +
                "\n" +
                "echo \"Print file? [y/n]: \"\n" +
                "read\n" +
                "if [ \"$REPLY\" = \"y\" ]; then\n" +
                "        echo \"ok\"\n" +
                "fi";
        String line;
        Path filePath = Paths.get(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "ProcessDestroyerTestScript");
        Files.write(filePath, scriptText.getBytes(), StandardOpenOption.CREATE);
        Process chmod = Runtime.getRuntime().exec(String.format("chmod 755 %s", filePath.toString()));
        chmod.waitFor();
        Process bashProcess = Runtime.getRuntime().exec(filePath.toString());
        ProcessWithTimeOut processWithTimeOut = new ProcessWithTimeOut(bashProcess);
        Field field = bashProcess.getClass().getDeclaredField("pid");
        field.setAccessible(true);
        int pid = Integer.parseInt(field.get(bashProcess).toString());
        LOGGER.debug(String.valueOf(pid));
        new ProcessDestroyer().kill(pid, 5000);
        int exitCode = processWithTimeOut.waitForProcess(100);
        BufferedReader outputReader = new BufferedReader(new InputStreamReader(bashProcess.getInputStream()));
        List<String> output = new ArrayList<>();
        while ((line = outputReader.readLine()) != null) {
            output.add(line);
        }
        outputReader.close();
        LOGGER.info("Exit code: {}", exitCode);
        LOGGER.info("Output");
        for (String outLine : output) {
            LOGGER.info(outLine);
        }
        Files.delete(filePath);
        //Check for Kill -9 (exit code 137),  kill -5 (exit code 145), kill -2 (exit code 130)
        Assert.assertTrue(exitCode == 137 || exitCode == 145 || exitCode == 130);
    }
}
