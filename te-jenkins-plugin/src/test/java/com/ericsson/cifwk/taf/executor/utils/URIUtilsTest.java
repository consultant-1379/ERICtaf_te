package com.ericsson.cifwk.taf.executor.utils;

import org.junit.Test;

import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

public class URIUtilsTest {

    @Test
    public void build_uri_without_trailing_slash() throws URISyntaxException {
        String url = "http://localhost:8080/api/reports";
        assertThat(URIUtils.buildUri(url, "path-param"))
            .isEqualTo("http://localhost:8080/api/reports/path-param");
    }

    @Test
    public void build_uri_with_trailing_slash() throws URISyntaxException {
        String url = "http://localhost:8080/api/reports/";
        assertThat(URIUtils.buildUri(url, "path-param"))
            .isEqualTo("http://localhost:8080/api/reports/path-param");
    }
}
