package com.ericsson.cifwk.taf.executor.maintenance;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import hudson.FilePath;

import java.io.FileFilter;
import java.util.List;

/**
 * Needed for test purposes, as FilePath is not mockable
 */
public class FilePathWrapper {

    private final FilePath filePath;

    public FilePathWrapper(FilePath filePath) {
        this.filePath = filePath;
    }

    public FilePathWrapper child(String relOrAbsolute) {
        return new FilePathWrapper(filePath.child(relOrAbsolute));
    }

    public void deleteRecursive() {
        try {
            filePath.deleteRecursive();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public boolean exists() {
        try {
            return filePath.exists();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public String toString() {
        return filePath.toString();
    }

    public List<FilePathWrapper> list(FileFilter fileFilter) {
        try {
            List<FilePath> list = filePath.list(fileFilter);
            if (list == null) {
                return null;
            }
            return Lists.newArrayList(Iterables.transform(list, new Function<FilePath, FilePathWrapper>() {
                @Override
                public FilePathWrapper apply(FilePath filePath) {
                    return new FilePathWrapper(filePath);
                }
            }));
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
