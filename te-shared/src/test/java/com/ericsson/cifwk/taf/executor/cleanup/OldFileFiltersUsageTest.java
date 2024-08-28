package com.ericsson.cifwk.taf.executor.cleanup;

import com.ericsson.cifwk.taf.executor.TAFExecutor;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 30/01/2017
 */
public class OldFileFiltersUsageTest {

    private File tempDir;

    private File workspaceDir;

    @Before
    public void setUp() {
        tempDir = Files.createTempDir();
        workspaceDir = new File(tempDir, TAFExecutor.TEST_RUN_WORKSPACE_SUBDIR);
        workspaceDir.mkdir();
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(tempDir);
    }

    @Test
    public void shouldFindOldDirectories() throws Exception {
        createDirectory(workspaceDir, "dir1-0", 5);
        createDirectory(workspaceDir, "dir2-0", 15);
        createDirectory(workspaceDir, "dir3-0", 25);
        OldTestRunDirectoryFilter oldTestRunDirectoryFilter = new OldTestRunDirectoryFilter(12);
        String[] list = workspaceDir.list(oldTestRunDirectoryFilter);
        Preconditions.checkState(list != null);
        Set<String> dirsToDelete = Sets.newHashSet(list);
        assertEquals(2, dirsToDelete.size());
        assertThat(dirsToDelete, hasItem("dir2-0"));
        assertThat(dirsToDelete, hasItem("dir3-0"));

    }

    @Test
    public void shouldFindOldTmpFiles() throws Exception {
        createFile(tempDir, "file1.xml.finish", 5);
        createFile(tempDir, "file2.xml.finish", 7);
        createDirectory(tempDir, "dir1-0", 5);
        createDirectory(tempDir, "dir2-0", 7);

        OldTmpFileFilter oldTmpFileFilter = new OldTmpFileFilter(6);
        String[] list = tempDir.list(oldTmpFileFilter);
        Preconditions.checkState(list != null);
        Set<String> filesToDelete = Sets.newHashSet(list);
        assertEquals(2, filesToDelete.size());
        assertThat(filesToDelete, hasItem("file2.xml.finish"));
        assertThat(filesToDelete, hasItem("dir2-0"));
    }

    private File createFile(File parentDir, String fileName, int ageInHours) throws IOException {
        File file = new File(parentDir, fileName);
        file.createNewFile();
        Files.write("data", file, Charset.defaultCharset());
        makeFileOld(file, ageInHours);
        return file;
    }

    private void makeFileOld(File file, int ageInHours) {
        DateTime now = new DateTime();
        DateTime then = now.minusHours(ageInHours);
        if (!file.setLastModified(then.toDate().getTime())) {
            throw new IllegalStateException("Failed to update 'modified' attribute for file " + file);
        }
    }

    private File createDirectory(File parentDir, String name, int ageInHours) throws IOException {
        File dir = new File(parentDir, name);
        dir.mkdir();
        // Make sure directory is not empty
        createFile(dir, "pom.xml.finish", ageInHours);
        makeFileOld(dir, ageInHours);
        return dir;
    }
}