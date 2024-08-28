package com.ericsson.cifwk.taf.executor.cleanup;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 30/01/2017
 */
public class FileCleanupUtils {

    private FileCleanupUtils() {}

    public static void deleteFiles(Logger logger, File[] filesToDelete) {
        if (filesToDelete != null && filesToDelete.length > 0) {
            logger.log(Level.INFO, "Found {0} files eligible for deletion", filesToDelete.length);
            for (File fileToDelete : filesToDelete) {
                try {
                    logger.log(Level.INFO, "Deleting {0}", fileToDelete);
                    // Should check whether directory was deleted or not, and it's done by 'forceDelete'
                    FileUtils.forceDelete(fileToDelete);
                } catch (IOException e) {
                    logger.severe(format("Failed to delete file '%s'%n%s", fileToDelete, ExceptionUtils.getStackTrace(e)));
                }
            }
        }

    }
}
