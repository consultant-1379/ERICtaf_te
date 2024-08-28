package com.ericsson.cifwk.taf.executor;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ArtifactHelper {

    final static Logger LOGGER = LoggerFactory.getLogger(ArtifactHelper.class);

    private static final String MAVEN_METADATA_XML = "maven-metadata.xml";
    private static final String MAVEN_SNAPSHOT_SUFFIX = "-SNAPSHOT";
    private static final String MAVEN_DEFAULT_EXTENSION = "jar";

    private static final String MSG_REPOSITORY_URL_MUST_BE_NOT_EMPTY = "Repository URL must be not empty";
    private static final String MSG_REPOSITORY_URL_INCORRECT = "Repository URL :%s incorrect";
    private static final String MSG_ARTIFACT_GAV_MUST_BE_NOT_EMPTY = "Artifact GAV must be not empty";
    private static final String MSG_ILLEGAL_ARTIFACT_GAV_FORMAT = "Illegal artifact GAV format :%s";
    private static final String MSG_CAN_NOT_RESOLVE_ARTIFACT = "Can't resolve artifact :%s from repository :%s because of the error :%s";
    private static final String MSG_CAN_NOT_RESOLVE_SNAPSHOT = "Can't resolve SNAPSHOT for :%s because of the error :%s";

    public static class Artifact implements Closeable {

        File file;

        Artifact(File file) {
            this.file = file;
        }

        public String getArtifactEntry(String name) {
            String entry = null;
            try (ZipFile zip = new ZipFile(file)) {
                ZipEntry artifactEntryInZip = zip.getEntry(name);
                if (artifactEntryInZip == null) {
                    LOGGER.error(String.format("File '%s' is not found in archive '%s'", name, file));
                    return null;
                }
                InputStream is = zip.getInputStream(artifactEntryInZip);
                entry = IOUtils.toString(is, "UTF-8");
            } catch (Exception e) {
                LOGGER.warn("Can't resolve artifact '" + name + "' because of the error " + e.getMessage(), e);
            }
            return entry;
        }

        public void copyToDestination(File dst) {
            try {
                FileUtils.copyFile(file, dst);
            } catch (IOException e) {
                LOGGER.error("Can't copy file to destination " + e.getMessage(), e);
                throw Throwables.propagate(e);
            }
        }

        @Override
        public void close() {
            try {
                if (file != null) file.delete();
            } catch (Exception e) { // NOSONAR
                try {
                    file.deleteOnExit();
                } catch (Exception ignore) { // NOSONAR
                }
            }
        }
    }

    public String resolveArtefact(String repository, String artifact, String name) {
        try (
                ArtifactHelper.Artifact artefact = this.getArtifact(repository, artifact);
        ) {
            return artefact.getArtifactEntry(name);
        }

    }

    public Artifact getArtifact(String repository, String artifact) {
        try {
            String url = buildURL(repository, artifact);
            File file = getArtifactAsFile(url);
            return new Artifact(file);
        } catch (Exception e) {
            LOGGER.error(String.format(MSG_CAN_NOT_RESOLVE_ARTIFACT, artifact, repository, e.getMessage()));
            throw Throwables.propagate(e);
        }
    }

    public static class GAV {
        final String groupId;
        final String artifactId;
        final String extension;
        final String classifier;
        final String version;
        Maven.Metadata metadata;

        GAV(String groupId, String artifactId, String extension, String classifier, String version) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.extension = extension;
            this.classifier = classifier;
            this.version = version;
        }

        public static final int GROUPID = 0;
        public static final int ARTIFACTID = 1;
        public static final int EXTENSION = 2;
        public static final int CLASSIFIER = 3;

        public static GAV parseGAV(String artifact) {
            String[] gav = Iterables.toArray(
                    Splitter.on(':')
                            .trimResults()
                            .omitEmptyStrings()
                            .split(Strings.nullToEmpty(artifact)),
                    String.class
            );
            Preconditions.checkArgument(gav.length >= 3 && gav.length <= 5, MSG_ILLEGAL_ARTIFACT_GAV_FORMAT, artifact);
            return new GAV(gav[GROUPID], gav[ARTIFACTID], gav.length > 3 ? gav[EXTENSION] : MAVEN_DEFAULT_EXTENSION, gav.length > 4 ? gav[CLASSIFIER] : null, gav[gav.length - 1]);
        }

        String repositoryPath() {
            return groupId.replace('.', '/') + '/' + this.artifactId + '/' + this.version;
        }

        String repositoryFile() {
            StringBuilder repositoryFile = new StringBuilder();
            repositoryFile.append(this.artifactId);
            // version
            if (metadata != null && metadata.versioning != null && metadata.versioning.snapshot != null) {
                Maven.Metadata.Snapshot snapshot = metadata.versioning.snapshot;
                repositoryFile
                        .append('-')
                        .append(metadata.version.substring(0, metadata.version.indexOf(MAVEN_SNAPSHOT_SUFFIX)));
                if (StringUtils.isNotBlank(snapshot.timestamp)) repositoryFile.append('-').append(snapshot.timestamp);
                if (StringUtils.isNotBlank(snapshot.buildNumber)) repositoryFile.append('-').append(snapshot.buildNumber);
            } else {
                repositoryFile.append('-').append(this.version);
            }
            repositoryFile.append(this.classifier != null ? '-' + this.classifier : "");
            repositoryFile.append('.').append(this.extension);
            return repositoryFile.toString();
        }

        boolean isShapshot() {
            return version.endsWith(MAVEN_SNAPSHOT_SUFFIX);
        }

        @Override
        public String toString() {
            return groupId + ':' +
                    artifactId + ':' +
                    (classifier != null ? classifier + ':' : "") +
                    version + ':' +
                    (!extension.equals(MAVEN_DEFAULT_EXTENSION) ? extension : "");
        }
    }

    String buildURL(String repository, String artifact) {
        Preconditions.checkArgument(StringUtils.isNotBlank(repository), MSG_REPOSITORY_URL_MUST_BE_NOT_EMPTY);
        Preconditions.checkArgument(StringUtils.isNotBlank(artifact), MSG_ARTIFACT_GAV_MUST_BE_NOT_EMPTY);
        String original = repository;
        repository = removeLast(repository.trim());
        Preconditions.checkArgument(StringUtils.isNotBlank(repository), MSG_REPOSITORY_URL_INCORRECT, original);
        GAV gav = GAV.parseGAV(artifact);
        if (gav.isShapshot()) resolveSnapshotVersion(repository, gav);
        return repository + '/' + gav.repositoryPath() + '/' + gav.repositoryFile();
    }

    private static final String REMOVE_LAST_IN = "\\/";

    String removeLast(String value) {
        int pos = value.length();
        while (pos > 0 && (REMOVE_LAST_IN.indexOf(value.charAt(pos - 1)) != -1)) pos--;
        return pos < value.length() ? value.substring(0, pos) : value;
    }

    public static class Maven {
        @Root(name = "metadata", strict = false)
        public static class Metadata {
            @Element
            String groupId;
            @Element
            String artifactId;
            @Element
            String version;
            @Element
            Versioning versioning;

            @Root(strict = false)
            public static class Versioning {
                @Element
                Snapshot snapshot;
            }

            @Root(strict = false)
            public static class Snapshot {
                @Element
                String timestamp;
                @Element
                String buildNumber;
            }
        }
    }

    void resolveSnapshotVersion(String repository, GAV gav) {
        String urlMavenMetadataXml = repository + '/' + gav.repositoryPath() + '/' + MAVEN_METADATA_XML;
        try {
            byte[] bytes = getArtifact(urlMavenMetadataXml);
            String xml = new String(bytes);
            gav.metadata = new Persister().read(Maven.Metadata.class, xml);
        } catch (Exception e) {
            LOGGER.warn(String.format(MSG_CAN_NOT_RESOLVE_SNAPSHOT, gav, e.getMessage()), e);
        }
    }

    File getArtifactAsFile(String url) throws URISyntaxException, IOException {
        Path tempFile = Files.createTempFile("taf", "");
        File file = tempFile.toFile();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(getArtifact(url));
        } finally {
            try {
                file.deleteOnExit();
            } catch (Exception ignore) { // NOSONAR
            }
        }
        return file;
    }

    byte[] getArtifact(String url) throws URISyntaxException, IOException {
        HttpGet httpget = new HttpGet(url);
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setRedirectStrategy(new DefaultRedirectStrategy());
        try (CloseableHttpClient client = httpClientBuilder.build()) {
            HttpResponse response = client.execute(httpget);
            int statusCode = response.getStatusLine().getStatusCode();
            if (HttpStatus.SC_OK != statusCode) {
                throw new RuntimeException(String.format("'%s' URL request finished with %d status", url, statusCode));
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream((int) response.getEntity().getContentLength());
            response.getEntity().writeTo(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

}
