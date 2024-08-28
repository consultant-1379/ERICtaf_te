package com.ericsson.cifwk.taf.executor;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class ArtifactHelperITest {

    private static final String ARM_ADDRESS = "https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443";

    @Test
    public void shouldDownloadArtifactEndExtractEntry() throws Exception {
        String entry = new ArtifactHelper().getArtifact(
                ARM_ADDRESS + "/nexus/content/groups/public",
                "junit:junit:4.13"
        ).getArtifactEntry("META-INF/MANIFEST.MF");
        assertThat(entry, not(isEmptyString()));
    }

    @Test
    public void shouldReturnNull_whenZIPfile_empty() throws Exception {
        File file = File.createTempFile("taf", "");
        file.deleteOnExit();
        ArtifactHelper.Artifact artifact = new ArtifactHelper.Artifact(file);
        String emptyString = artifact.getArtifactEntry("any");
        assertThat(emptyString, nullValue());
    }

    @Test
    public void shouldReturnNull_whenEntryNotFound() throws Exception {
        File zip = createTempZip("schedule.xml", "<schedulde/>");
        ArtifactHelper.Artifact artifact = new ArtifactHelper.Artifact(zip);
        String emptyString = artifact.getArtifactEntry("not_existing.xml");
        assertThat(emptyString, nullValue());
    }

    @Test
    public void shouldReadEntryFromZip() throws Exception {
        File zip = createTempZip("schedule.xml", "<schedulde/>");
        ArtifactHelper.Artifact artifact = new ArtifactHelper.Artifact(zip);
        String emptyString = artifact.getArtifactEntry("schedule.xml");
        assertThat(emptyString, equalTo("<schedulde/>"));
    }

    @Test
    public void testCanDownloadArtifactEntry() throws Exception {
        //TODO: use stub or mock
        String entry = new ArtifactHelper().getArtifact(
                ARM_ADDRESS + "/nexus/content/repositories/releases",
                "com.ericsson.cifwk.taf.executor:te-taf-testware:2.0"
        ).getArtifactEntry("schedule/success.xml");
        assertThat(entry, not(isEmptyString()));
    }

    @Test
    public void shouldCopyArtifact() throws Exception {
        File zip = createTempZip("schedule.xml", "<schedulde/>");
        File dst = File.createTempFile("temp", ".zip");

        ArtifactHelper.Artifact artifact = new ArtifactHelper.Artifact(zip);
        artifact.copyToDestination(dst);

        ArtifactHelper.Artifact artifactCopy = new ArtifactHelper.Artifact(dst);
        String emptyString = artifactCopy.getArtifactEntry("schedule.xml");
        assertThat(emptyString, equalTo("<schedulde/>"));
    }

    private File createTempZip(String entryName, String entryText) throws Exception {
        File file = File.createTempFile("taf", ".zip");
        file.deleteOnExit();
        try (
                FileOutputStream fos = new FileOutputStream(file);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ZipOutputStream zip = new ZipOutputStream(os)
        ) {
            zip.putNextEntry(new ZipEntry(entryName));
            zip.write(entryText.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.close();
            fos.write(os.toByteArray());
        }
        return file;
    }


}
