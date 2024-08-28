package com.ericsson.cifwk.taf.executor.healthcheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


public final class Ping {

    private static final Logger LOGGER = LoggerFactory.getLogger(Ping.class);

    private Ping() {
    }

    public static Check.Result checkUrl(String url) {
        try {
            URL u = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) u.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            int code = urlConnection.getResponseCode();
            urlConnection.disconnect();
            return new Check.Result(200 == code, "");
        } catch (Exception e) {
            LOGGER.error("Failed to connect to Url: " + url, e);
            return new Check.Result(false, e.getMessage());
        }
    }

}
