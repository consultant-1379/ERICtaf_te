package com.ericsson.cifwk.taf.executor.healthcheck;

import com.google.common.annotations.VisibleForTesting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TeNodeLogMountAccessCheckCallable extends AbstractTeNodeHealthCheckCallable {

    private final String localReportsStorage;
    private final int nodeCount;

    public TeNodeLogMountAccessCheckCallable(String nodeName, String localReportsStorage, int nodeCount) {
        super(nodeName);
        this.localReportsStorage = localReportsStorage;
        this.nodeCount = nodeCount;
    }

    @Override
    public String doCheck(HealthParam check) {
        try {
            // Not validating log storage definition here, as it's done in CheckJenkinsJobs
            boolean mounted = checkMount();

            if (!mounted) {
                return failCheck(check, String.format("Shared log mount %s is not mounted.", localReportsStorage));
            }
            else {
                String fileName = "healthCheck_" + System.currentTimeMillis();
                Path file = createTmpFile(fileName);
                if (Files.notExists(file)) {
                    return failCheck(check, "Unable to create a tmp file to test write permissions on the log mount");
                }
                deleteTmpFile(file);
                if (Files.exists(file)) {
                    return failCheck(check, "Unable to delete a tmp file on the log mount");
                }
            }
        } catch (Exception e) {  // NOSONAR
            return failCheck(check, "Error while checking the log mount: " + e.toString());
        }
        return toJson(check);
    }

    @VisibleForTesting
    void deleteTmpFile(Path file) throws IOException {
        Files.delete(file);
    }

    @VisibleForTesting
    Path createTmpFile(String fileName) throws IOException {
        return Files.createFile(Paths.get(localReportsStorage, fileName));
    }

    @VisibleForTesting
    boolean checkMount() throws IOException, InterruptedException {
        if (nodeCount == 1) { //return true if there is only one node i.e. KGB TE
            return true;
        }
        Process mounts = Runtime.getRuntime().exec("mount");
        mounts.waitFor();
        BufferedReader outputReader = new BufferedReader(new InputStreamReader(mounts.getInputStream(), StandardCharsets.UTF_8));
        String line;

        while ((line = outputReader.readLine()) != null) {
            if (line.contains(localReportsStorage)) {
                outputReader.close();
                return true;
            }
        }
        outputReader.close();
        return false;
    }

    @Override
    public String getCheckName(String nodeName) {
        return "Node '" + nodeName + "' has access to log mount";
    }

}
