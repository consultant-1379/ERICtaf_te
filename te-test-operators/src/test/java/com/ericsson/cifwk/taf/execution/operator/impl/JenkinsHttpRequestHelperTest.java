package com.ericsson.cifwk.taf.execution.operator.impl;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.cifwk.taf.tools.http.RequestBuilder;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JenkinsHttpRequestHelperTest {

    @Test
    public void getRequestBuilder_authentication() throws Exception {
        String username = "tafuser";
        String password = "password";
        Host host = getJenkinsHost(username, password);
        HttpTool httpTool = mock(HttpTool.class);
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(httpTool.request()).thenReturn(requestBuilder);

        JenkinsHttpRequestHelper unit = new JenkinsHttpRequestHelper(host, httpTool, true);

        unit.getRequestBuilder();

        verify(requestBuilder).authenticate(username, password);
    }

    @Test
    public void getRequestBuilder_noAuthentication() throws Exception {
        Host host = getJenkinsHost(null, null);
        HttpTool httpTool = mock(HttpTool.class);
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(httpTool.request()).thenReturn(requestBuilder);

        JenkinsHttpRequestHelper unit = new JenkinsHttpRequestHelper(host, httpTool, false);

        unit.getRequestBuilder();

        verify(requestBuilder, never()).authenticate(anyString(), anyString());
    }

    private Host getJenkinsHost(String username, String password) {
        Host host = mock(Host.class);
        when(host.getIp()).thenReturn("fem118-eiffel004.lmera.ericsson.se");
        HashMap<Ports, String> ports = new HashMap<>();
        ports.put(Ports.HTTP, "8080");
        if (username != null && password != null) {
            ports.put(Ports.HTTPS, "8443");
        }
        when(host.getPort()).thenReturn(ports);
        when(host.getUser(eq(UserType.ADMIN))).thenReturn(username);
        when(host.getPass(eq(UserType.ADMIN))).thenReturn(password);
        return host;
    }
}