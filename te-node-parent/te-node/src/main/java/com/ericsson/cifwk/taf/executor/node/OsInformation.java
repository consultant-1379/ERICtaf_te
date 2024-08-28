package com.ericsson.cifwk.taf.executor.node;

import com.google.common.annotations.VisibleForTesting;

/**
 * Information about current node's OS
 */
public class OsInformation {

    private static String OS = System.getProperty("os.name").toLowerCase();

    public static OsName getOsName() {
        if (OsInformation.isWindows()) {
            return OsName.WINDOWS;
        } else if (OsInformation.isNix()) {
            return OsName.NIX;
        } else if (OsInformation.isMac()) {
            return OsName.MAC;
        } else {
            return null;
        }
    }

    public static boolean isWindows() {
        return (OS.contains("win"));
    }

    public static boolean isMac() {
        return (OS.contains("mac"));
    }

    @VisibleForTesting
    public static boolean isNix() {
        return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix") || OS.contains("sunos"));
    }


    public static enum OsName {
        NIX, WINDOWS, MAC
    }
}
