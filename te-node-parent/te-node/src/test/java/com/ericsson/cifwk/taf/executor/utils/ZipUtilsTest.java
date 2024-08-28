package com.ericsson.cifwk.taf.executor.utils;

import com.google.common.io.Resources;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 11/08/2017
 */
public class ZipUtilsTest {

    @Rule
    public TemporaryFolder tmpFolderRule = new TemporaryFolder();

    @After
    public void tearDown() throws IOException {
        tmpFolderRule.delete();
    }

    @Test
    public void shouldCopyZipEntryFromArchive() throws Exception {
        URI jarUri = getSourceFileUri();
        File targetFile = copyZipToTarget(jarUri, tmpFolderRule.newFolder());
        assertThat(targetFile).exists();
    }

    @Test
    public void shouldCopyZipEntryFromArchive_inParallel() throws Exception {
        final int threadAmount = 10;
        List<UnzippingThread> threads = newArrayList();
        for (int i = 0; i < threadAmount; i++) {
            UnzippingThread thread = new UnzippingThread();
            threads.add(thread);
            thread.start();
        }
        for (UnzippingThread thread : threads) {
            thread.join();
            assertThat(thread.isPassed()).isTrue();
        }
    }

    private URI getSourceFileUri() throws URISyntaxException {
        URI srcUri = Resources.getResource("assembly.zip").toURI();
        return fileUriToJarUri(srcUri);
    }

    private File copyZipToTarget(URI jarUri, File targetFolder) throws IOException, URISyntaxException {
        Path pathToTargetFolder = targetFolder.toPath();
        Path targetFilePath = pathToTargetFolder.resolve("zip.xml");
        ZipUtils.copyZipEntry(jarUri, "assembly/zip.xml", targetFilePath);
        return targetFilePath.toFile();
    }

    private URI fileUriToJarUri(URI fileUri) {
        String uriAsString = fileUri.toString();
        return URI.create(JarUtils.JAR_URI_PREFIX + uriAsString);
    }


    private class UnzippingThread extends Thread {

        private final Logger LOGGER = LoggerFactory.getLogger(UnzippingThread.class);

        private boolean passed = false;

        @Override
        public void run() {
            try {
                File targetFile = copyZipToTarget(getSourceFileUri(), tmpFolderRule.newFolder());
                assertThat(targetFile).exists();
                passed = true;
            } catch (Exception e) {
                LOGGER.error("Error", e);
                fail("Unzipping/copying failed with error %s", e.getClass());
            }
        }

        boolean isPassed() {
            return passed;
        }
    }
}