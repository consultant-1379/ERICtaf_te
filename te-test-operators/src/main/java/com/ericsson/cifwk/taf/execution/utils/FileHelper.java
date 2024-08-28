package com.ericsson.cifwk.taf.execution.utils;

import com.google.common.base.Throwables;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 25/04/2016
 */
public class FileHelper {

    private FileHelper() {}

    public static Path getPath(String path) {
        FileSystem fileSystem = FileSystems.getDefault(); // NOSONAR
        Path result = fileSystem.getPath(path);
        try {
            fileSystem.close();
        } catch (UnsupportedOperationException e) { // NOSONAR
            // ignore
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
        return result;
    }

}
