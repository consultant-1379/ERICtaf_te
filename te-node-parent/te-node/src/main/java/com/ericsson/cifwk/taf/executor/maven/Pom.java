package com.ericsson.cifwk.taf.executor.maven;

import java.io.File;

/**
 * Created by ekellmi on 7/2/15.
 */
public class Pom {

    File file;
    boolean legacy;

    public Pom(File file, boolean legacy) {
        this.file = file;
        this.legacy = legacy;
    }

    public File getFile() {
        return file;
    }

    public boolean isLegacy() {
        return legacy;
    }
}
