package com.ericsson.cifwk.taf.executor;

import org.junit.Test;

import java.io.File;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ArtifactHelperTest {


    @Test
    public void shouldBuildArtifactHelper_completeGAV() throws Exception {
        String url = new ArtifactHelper().buildURL(
                "http://repo.com",
                "groupId:artifactId:extension:classifier:version"
        );
        assertThat(url, equalTo("http://repo.com/groupId/artifactId/version/artifactId-version-classifier.extension"));
    }

    @Test
    public void shouldBuildArtifactHelper_GAV() throws Exception {
        String url = new ArtifactHelper().buildURL(
                "http://repo.com",
                "groupId:artifactId:version"
        );
        assertThat(url, equalTo("http://repo.com/groupId/artifactId/version/artifactId-version.jar"));
    }

    @Test
    public void shouldBuildArtifactHelper_GAV_and_extension() throws Exception {
        String url = new ArtifactHelper().buildURL(
                "http://repo.com",
                "groupId:artifactId:extension:version"
        );
        assertThat(url, equalTo("http://repo.com/groupId/artifactId/version/artifactId-version.extension"));
    }


    @Test
    public void shouldThrowException_when_repository_null() throws Exception {
        try {
            new ArtifactHelper().buildURL(
                    null,
                    "groupId:artifactId:extension:version"
            );
            fail("Can't throw IllegalArgumentException");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void shouldThrowException_when_artifact_null() throws Exception {
        try {
            new ArtifactHelper().buildURL(
                    "http://repo.com",
                    null
            );
            fail("Can't throw IllegalArgumentException");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void shouldThrowException_when_artifact_empty() throws Exception {
        try {
            new ArtifactHelper().buildURL(
                    "http://repo.com",
                    ""
            );
            fail("Can't throw IllegalArgumentException");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void shouldThrowException_when_artifact_small() throws Exception {
        try {
            new ArtifactHelper().buildURL(
                    "http://repo.com",
                    "groupId:artifactId"
            );
            fail("Can't throw IllegalArgumentException");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void shouldThrowException_when_artifact_biggest() throws Exception {
        try {
            new ArtifactHelper().buildURL(
                    "http://repo.com",
                    "groupId:artifactId:extension:classifier:version:INCORRECT"
            );
            fail("Can't throw IllegalArgumentException");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void shouldReturnNull_when_not_found() throws Exception {
        ArtifactHelper.Artifact artifact = new ArtifactHelper.Artifact(new File("new"));
        String emptyString = artifact.getArtifactEntry("any");
        assertThat(emptyString, nullValue());
    }

    @Test
    public void shouldReturnNull_when_isDirectory() throws Exception {
        ArtifactHelper.Artifact artifact = new ArtifactHelper.Artifact(new File("."));
        String emptyString = artifact.getArtifactEntry("any");
        assertThat(emptyString, nullValue());
    }

    @Test
    public void shouldReturnTrue_IsVersion_SNAPSHOT() throws Exception {
        boolean isSnapshot = ArtifactHelper.GAV.parseGAV("groupId:artifactId:extension:classifier:version-SNAPSHOT").isShapshot();
        assertThat(isSnapshot, is(true));
    }

    @Test
    public void shouldRemoveFromStringSimbols() throws Exception {
        assertThat(new ArtifactHelper().removeLast("a/b\\c"), equalTo("a/b\\c"));
        assertThat(new ArtifactHelper().removeLast("a/b\\c//"), equalTo("a/b\\c"));
        assertThat(new ArtifactHelper().removeLast("a/b\\c\\\\"), equalTo("a/b\\c"));
        assertThat(new ArtifactHelper().removeLast("a/b\\c\\\\//"), equalTo("a/b\\c"));
        assertThat(new ArtifactHelper().removeLast("a/b\\c//\\\\"), equalTo("a/b\\c"));
        assertThat(new ArtifactHelper().removeLast("a/b\\c//\\//\\"), equalTo("a/b\\c"));
    }

}
