package com.ericsson.cifwk.taf.executor.utils;

import java.net.URISyntaxException;

public final class URIUtils {

    private URIUtils() {
    }

    public static String buildUri(String url, String path) throws URISyntaxException {
        return url.replaceAll("/$", "") + "/" + path;
    }
}
