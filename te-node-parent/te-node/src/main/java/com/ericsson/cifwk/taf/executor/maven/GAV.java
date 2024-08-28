package com.ericsson.cifwk.taf.executor.maven;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

public class GAV {

    private String groupId;
    private String artifactId;
    private String version;
    private boolean isBom = false;

    public GAV(String descriptor) {
        String[] tokens = Iterables.toArray(Splitter.on(':')
                                                     .trimResults()
                                                     .omitEmptyStrings()
                                                     .split(Strings.nullToEmpty(descriptor)), String.class);
        this.groupId = tokens[0];
        this.artifactId = tokens[1];
        this.version = tokens[2];
    }

    public GAV(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public boolean isBom() {
        return isBom;
    }

    public void setIsBom(boolean isBom) {
        this.isBom = isBom;
    }

}
