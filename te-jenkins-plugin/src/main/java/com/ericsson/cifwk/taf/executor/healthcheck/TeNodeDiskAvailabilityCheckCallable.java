package com.ericsson.cifwk.taf.executor.healthcheck;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TeNodeDiskAvailabilityCheckCallable extends AbstractTeNodeHealthCheckCallable {

    private static final String ROOT_PATH = "/";
    private int minDiskSpaceGB;

    public TeNodeDiskAvailabilityCheckCallable(String nodeName, int minDiskSpaceGB) {
        super(nodeName);
        this.minDiskSpaceGB = minDiskSpaceGB;
    }

    @SuppressFBWarnings
    @Override
    public String doCheck(HealthParam check) {
        Path root = Paths.get(ROOT_PATH);   //
        try {
            FileStore store = Files.getFileStore(root);
            long availableDiskSpace = store.getUsableSpace() / 1024 / 1024 / 1024;
            if (availableDiskSpace < minDiskSpaceGB) {
                return failCheck(check, "Insufficient Disk Space on Node , current disk space is:" + availableDiskSpace
                        + "GB, required disk space is: " + minDiskSpaceGB + "GB");
            }
        } catch (IOException e) { // NOSONAR
            return failCheck(check, "error querying disk space: " + e.toString());
        }
        return toJson(check);
    }

    @Override
    public String getCheckName(String nodeName) {
        return "Node " + nodeName + " has adequate disk space";
    }

}
