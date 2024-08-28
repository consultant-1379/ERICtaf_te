package com.ericsson.cifwk.taf.executor.api;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * Used as a substitute for Eiffel's Arm (due to DCE-551)
 */
public class ArmInfo implements Serializable {

    private final String id;
    private String description;
    private String httpString;
    private String ftpString;
    private String nfsString;
    private String downloadRepoName;
    private String uploadRepoName;
    private Credentials credentials;

    public ArmInfo() {
        this("", null, null, null, null, null, null, null, null);
    }

    public ArmInfo(final String id, final String httpString, final String ftpString, final String nfsString,
                   final String downloadRepoName,
                   final String uploadRepoName, final String userName, final String password, final String description) {
        this.setHttp(httpString);
        this.setFtp(ftpString);
        this.setNfs(nfsString);
        this.setDownloadRepoName(downloadRepoName);
        this.setUploadRepoName(uploadRepoName);
        this.credentials = new Credentials(userName, password);
        this.id = id == null ? "" : id;
        this.description = description;
    }

    /**
     * @return A ID of this ARM. Never {@code null}.
     */
    public String getId() {
        return id;
    }

    /**
     * @return A description of this ARM. Can be {@code null} if ARM is missing description.
     */
    public String getDescription() {
        return description;
    }

    public String getHttpString() {
        return httpString;
    }

    public String getFtpString() {
        return ftpString;
    }

    public String getNfsString() {
        return nfsString;
    }

    public String getDownloadRepoName() {
        return downloadRepoName;
    }

    public String getUploadRepoName() {
        return uploadRepoName;
    }

    public String getUserName() {
        return credentials != null ? credentials.getUserName() : null;
    }

    public String getPassword() {
        return credentials != null ? credentials.getPassword() : null;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public final void setDownloadRepoName(final String s) {
        if (s == null || s.isEmpty()) {
            downloadRepoName = null;
        }
        else {
            downloadRepoName = s;
        }
    }

    public final void setUploadRepoName(final String s) {
        if (s == null || s.isEmpty()) {
            uploadRepoName = null;
        }
        else {
            uploadRepoName = s;
        }
    }

    public final ArmInfo setHttp(final String s) {
        final String[] validPrefix = { "http://", "https://" };
        this.httpString = format(s, validPrefix, "http://");
        return this;
    }

    public final ArmInfo setFtp(final String s) {
        final String[] validPrefix = { "ftp://" };
        this.ftpString = format(s, validPrefix, "ftp://");
        return this;
    }

    public final ArmInfo setNfs(final String s) {
        final String[] validPrefix = { "/" };
        this.nfsString = format(s, validPrefix, "/");
        return this;
    }

    private String format(final String s, final String[] validPrefix, final String defaultPrefix) {
        if (s == null || s.isEmpty()) {
            return null;
        }

        final String out = StringUtils.strip(s, "/");

        for (int i = 0; i < validPrefix.length; i++) {
            if (out.startsWith(validPrefix[i])) {
                return out;
            }
        }

        return defaultPrefix + out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArmInfo armInfo = (ArmInfo) o;

        if (credentials != null ? !credentials.equals(armInfo.credentials) : armInfo.credentials != null) return false;
        if (description != null ? !description.equals(armInfo.description) : armInfo.description != null) return false;
        if (downloadRepoName != null ? !downloadRepoName.equals(armInfo.downloadRepoName) : armInfo.downloadRepoName != null)
            return false;
        if (ftpString != null ? !ftpString.equals(armInfo.ftpString) : armInfo.ftpString != null) return false;
        if (httpString != null ? !httpString.equals(armInfo.httpString) : armInfo.httpString != null) return false;
        if (id != null ? !id.equals(armInfo.id) : armInfo.id != null) return false;
        if (nfsString != null ? !nfsString.equals(armInfo.nfsString) : armInfo.nfsString != null) return false;
        if (uploadRepoName != null ? !uploadRepoName.equals(armInfo.uploadRepoName) : armInfo.uploadRepoName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (httpString != null ? httpString.hashCode() : 0);
        result = 31 * result + (ftpString != null ? ftpString.hashCode() : 0);
        result = 31 * result + (nfsString != null ? nfsString.hashCode() : 0);
        result = 31 * result + (downloadRepoName != null ? downloadRepoName.hashCode() : 0);
        result = 31 * result + (uploadRepoName != null ? uploadRepoName.hashCode() : 0);
        result = 31 * result + (credentials != null ? credentials.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ArmInfo{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", httpString='" + httpString + '\'' +
                ", ftpString='" + ftpString + '\'' +
                ", nfsString='" + nfsString + '\'' +
                ", downloadRepoName='" + downloadRepoName + '\'' +
                ", uploadRepoName='" + uploadRepoName + '\'' +
                ", credentials=" + credentials +
                '}';
    }
}
