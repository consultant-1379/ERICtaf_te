package com.ericsson.cifwk.taf.executor.utils;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;

public final class ZipUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(ZipUtils.class);

    private ZipUtils() {
    }

    public static void copyZipEntry(URI zipSrcUri, String zipSrcEntry, Path targetFilePath)
            throws IOException, URISyntaxException {
        FileSystem zipFileSystem = getZipFileSystem(zipSrcUri);
        Path zipEntryPath = zipFileSystem.getPath(zipSrcEntry);
        Files.copy(zipEntryPath, targetFilePath);
    }

    /**
     * See reference: http://docs.oracle.com/javase/7/docs/technotes/guides/io/fsp/zipfilesystemprovider.html
     */
    private static FileSystem getZipFileSystem(URI zipUri) throws IOException {
        try {
            return FileSystems.getFileSystem(zipUri);
        } catch (FileSystemNotFoundException e) {
            try {
                return FileSystems.newFileSystem(zipUri, ImmutableMap.of("create", "true"));
            }catch (FileSystemAlreadyExistsException fsaee) {
                LOGGER.warn("zip file system already created", fsaee);
                return FileSystems.getFileSystem(zipUri);
            }
        }
    }
}
