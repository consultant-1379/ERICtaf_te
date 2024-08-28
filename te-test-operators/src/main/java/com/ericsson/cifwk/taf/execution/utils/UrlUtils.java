package com.ericsson.cifwk.taf.execution.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtils {

    public static String replaceHost(String url, String host) {
        if (url == null) return null;

        Pattern compile = Pattern.compile("(http:\\/\\/)([^:]*)(:[0-9]+|)(\\/jenkins.*)");

        Matcher matcher = compile.matcher(url);
        if (matcher.matches()) {
            StringBuilder sb = new StringBuilder(matcher.group(1))
                    .append(host)
                    .append(matcher.group(3))
                    .append(matcher.group(4));
            return sb.toString();
        }
        return url;
    }
}
