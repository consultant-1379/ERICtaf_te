package com.ericsson.cifwk.taf.executor.api.schedule.model;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

public final class ScheduleComponent {

    private final String groupId;
    private final String artifactId;
    private String version;

    public ScheduleComponent(String groupId, String artifactId) {
        this(groupId, artifactId, null);
    }

    public ScheduleComponent(String groupId, String artifactId, String version) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduleComponent that = (ScheduleComponent) o;
        if (!artifactId.equals(that.artifactId)) return false;
        if (!groupId.equals(that.groupId)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = groupId.hashCode();
        result = 31 * result + artifactId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return groupId + ':' + artifactId + (version != null ? ':' + version : "");
    }


    public static boolean isGav(String gav) {
        String[] artifact = Iterables.toArray(
                Splitter.on(':')
                        .trimResults()
                        .omitEmptyStrings()
                        .split(Strings.nullToEmpty(gav)),
                String.class);
        return (artifact.length == 3);
    }

    public static ScheduleComponent parseGav(String gav) {
        String[] artifact = Iterables.toArray(
                Splitter.on(':')
                        .trimResults()
                        .omitEmptyStrings()
                        .split(Strings.nullToEmpty(gav)),
                String.class);
        if (artifact.length != 3)
            throw new IllegalArgumentException("Illegal artifact GAV format: " + gav);
        String groupId = artifact[0];
        String artifactId = artifact[1];
        String version = artifact[2];
        return new ScheduleComponent(groupId, artifactId, version);
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
