package com.ericsson.cifwk.taf.executor.utils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import java.net.URI;

import static java.lang.String.format;


public final class JarUtils {

    @VisibleForTesting
    static final String JAR_URI_PREFIX = "jar:";

    private static final String FILE_URI_PREFIX = "file:";

    private static final String JAR_PATH_PATTERN = format("^%s.*\\.jar$", FILE_URI_PREFIX);

    private JarUtils() {
    }

    public static Optional<URI> getThisJarUri()  {
        String jarUrl = JarUtils.class.getProtectionDomain().getCodeSource().getLocation().toString();

        if (jarUrl.matches(JAR_PATH_PATTERN)) {
            return Optional.of(URI.create(JAR_URI_PREFIX + jarUrl));
        }
        return Optional.absent();
    }
}
