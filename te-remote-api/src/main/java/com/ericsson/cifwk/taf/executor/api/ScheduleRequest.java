package com.ericsson.cifwk.taf.executor.api;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Properties;

import static org.apache.commons.lang3.StringUtils.isNoneBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ScheduleRequest implements Serializable {

    private static final long serialVersionUID = -7750505246722515091L;
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * Path/to/the/schedule/xml/file inside the JAR (if schedule is stored in GAV)
     */
    private String name;
    // Left until the multiple schedules support
    private Properties testProperties = new Properties();
    /**
     * Schedule GAV. <code>null</code> if schedule is taken from TAF Scheduler or defined as plain XML.
     */
    private ArtifactInfo gav;
    /**
     * XML content. <code>null</code> if schedule is defined as GAV.
     */
    private String xml;
    /**
     * URI that uniquely depicts the schedule in TAF scheduler. Defined only when TAF scheduler is the schedule source,
     * <code>null</code> otherwise.
     */
    private String tafSchedulerSourceUri;

    public ScheduleRequest() {
    }

    public ScheduleRequest(final String name, final String groupId, final String artifactId, final String version) {
        this.name = name;
        gav = new ArtifactInfo(groupId, artifactId, version);
    }

    public ScheduleRequest(String name, String groupId, String artifactId, String version, String testPropertiesAsString) {
        this(name, groupId, artifactId, version);
        setTestPropertiesAsString(testPropertiesAsString);
    }

    public ScheduleRequest(String name, String groupId, String artifactId, String version, Properties testProperties) {
        this(name, groupId, artifactId, version);
        setTestProperties(testProperties);
    }

    public ScheduleRequest(String xml, String testPropertiesAsString) {
        this(null, null, null, null, testPropertiesAsString);
        this.xml = xml;
    }

    public ScheduleRequest(String xml, Properties testProperties) {
        this(null, null, null, null, testProperties);
        this.xml = xml;
    }

    public ScheduleRequest(String xml) {
        this(null, null, null, null);
        this.xml = xml;
    }

    public ScheduleRequest(ArtifactInfo ArtifactInfo, String schedulePath, Properties testProperties) {
        this(schedulePath, ArtifactInfo.getGroupId(), ArtifactInfo.getArtifactId(), ArtifactInfo.getVersion());
        setTestProperties(testProperties);
    }

    public ScheduleRequest(ArtifactInfo ArtifactInfo, String schedulePath) {
        this(schedulePath, ArtifactInfo.getGroupId(), ArtifactInfo.getArtifactId(), ArtifactInfo.getVersion());
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setArtifact(final ArtifactInfo gav) {
        this.gav = gav;
    }

    public ArtifactInfo getArtifact() {
        return gav;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public String getGroupId() {
        return gav.getGroupId();
    }

    public String getArtifactId() {
        return gav.getArtifactId();
    }

    public String getVersion() {
        return gav.getVersion();
    }

    public String getTestPropertiesAsString() {
        return Joiner.on(LINE_SEPARATOR).withKeyValueSeparator("=").join(testProperties);
    }

    public void setTestPropertiesAsString(String testPropertiesAsString) {
        Preconditions.checkArgument(testPropertiesAsString != null, "test properties should not be null");

        try {
            testProperties.load(new StringReader(testPropertiesAsString));
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public Properties getTestProperties() {
        return testProperties;
    }

    public void setTestProperties(Properties testProperties) {
        this.testProperties = testProperties;
    }

    public boolean isComplete() {
        switch (getSource()) {
            case MAVEN_GAV:
                return gav != null && isNoneBlank(name, gav.getArtifactId(), gav.getGroupId(), gav.getVersion());
            case TAF_SCHEDULER:
                return isNotBlank(xml);
            case PLAIN_XML:
                // If we're here, plain XML is not empty
                return true;
            default:
                return false;
        }
    }

    public boolean isGav() {
        return isNotBlank(name) || (gav != null && isGavPartiallyDefined(gav));
    }

    @VisibleForTesting
    static boolean isGavPartiallyDefined(ArtifactInfo gav) {
        return isNotBlank(gav.getArtifactId()) || isNotBlank(gav.getGroupId()) || isNotBlank(gav.getVersion());
    }

    public boolean isPlainXml() {
        return !isFromTafScheduler() && xml != null;
    }

    public boolean isFromTafScheduler() {
        return tafSchedulerSourceUri != null;
    }

    public String getTafSchedulerSourceUri() {
        return tafSchedulerSourceUri;
    }

    public void setTafSchedulerSourceUri(String tafSchedulerSourceUri) {
        this.tafSchedulerSourceUri = tafSchedulerSourceUri;
    }

    public ScheduleSource getSource() {
        if (isGav()) {
            return ScheduleSource.MAVEN_GAV;
        } else if (isFromTafScheduler()) {
            return ScheduleSource.TAF_SCHEDULER;
        } else if (isPlainXml()) {
            return ScheduleSource.PLAIN_XML;
        }
        return ScheduleSource.UNKNOWN;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(this.getClass().getSimpleName()).append("[");
        ScheduleSource scheduleSource = getSource();
        switch (scheduleSource) {
            case MAVEN_GAV:
                if (gav != null) {
                    result.append(gav.getGroupId()).append(":").append(gav.getArtifactId()).append(":").append(gav.getVersion());
                } else {
                    result.append("<null>");
                }
                result.append("!/").append(name);
                break;
            case TAF_SCHEDULER: // NOSONAR
                // Absence of break is on purpose here
                result.append("tafSchedulerSourceUri:").append(tafSchedulerSourceUri).append(",");
            case PLAIN_XML:
                result.append("xml:").append(xml);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported schedule source: " + scheduleSource);
        }
        return result.append("]").toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScheduleRequest that = (ScheduleRequest) o;

        if (gav != null ? !gav.equals(that.gav) : that.gav != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (tafSchedulerSourceUri != null ? !tafSchedulerSourceUri.equals(that.tafSchedulerSourceUri) : that.tafSchedulerSourceUri != null)
            return false;
        if (testProperties != null ? !testProperties.equals(that.testProperties) : that.testProperties != null)
            return false;
        if (xml != null ? !xml.equals(that.xml) : that.xml != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (testProperties != null ? testProperties.hashCode() : 0);
        result = 31 * result + (gav != null ? gav.hashCode() : 0);
        result = 31 * result + (xml != null ? xml.hashCode() : 0);
        result = 31 * result + (tafSchedulerSourceUri != null ? tafSchedulerSourceUri.hashCode() : 0);
        return result;
    }
}
