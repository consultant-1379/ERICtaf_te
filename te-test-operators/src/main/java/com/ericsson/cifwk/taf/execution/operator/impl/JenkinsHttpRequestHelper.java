package com.ericsson.cifwk.taf.execution.operator.impl;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.cifwk.taf.tools.http.HttpToolBuilder;
import com.ericsson.cifwk.taf.tools.http.RequestBuilder;
import com.ericsson.cifwk.taf.tools.http.constants.HttpStatus;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;

import java.util.Map;

public class JenkinsHttpRequestHelper {

    private static final Logger LOGGER = Logger.getLogger(JenkinsHttpRequestHelper.class);
    private static final String AUTH_PROTECTED_JENKINS_URL = "/jenkins/configure";

    private final HttpTool httpTool;
    private final Host jenkinsHost;
    private final boolean authenticationRequired;

    public JenkinsHttpRequestHelper(Host jenkinsHost) {
        this.jenkinsHost = jenkinsHost;
        this.httpTool = createHttpTool(jenkinsHost);
        this.authenticationRequired = isAuthenticationRequired();
        if (authenticationRequired) {
            // Need to login first to be able to upload plugin
            String userName = jenkinsHost.getUser(UserType.ADMIN);
            String password = jenkinsHost.getPass(UserType.ADMIN);
            Preconditions.checkArgument(userName != null && password != null,
                    "Jenkins requires authentication which Host definition lacks (for admin user)");
            LOGGER.info(String.format("Jenkins requires authentication - using '%s' admin user's credentials", userName));
        }
    }

    @VisibleForTesting
    JenkinsHttpRequestHelper(Host jenkinsHost, HttpTool httpTool, boolean authenticationRequired) {
        this.jenkinsHost = jenkinsHost;
        this.httpTool = httpTool;
        this.authenticationRequired = authenticationRequired;
    }

    public HttpTool getHttpTool() {
        return httpTool;
    }

    @VisibleForTesting
    HttpTool createHttpTool(Host host) {
        HttpToolBuilder httpToolBuilder = getHttpToolBuilder(host);
        // Workaround for CIS-14145
        Map<Ports, String> port = host.getPort();
        if (port.containsKey(Ports.HTTPS)) {
            httpToolBuilder.useHttpsIfProvided(true);
        }
        return httpToolBuilder.trustSslCertificates(true).build();
    }

    @VisibleForTesting
    HttpToolBuilder getHttpToolBuilder(Host host) {
        return HttpToolBuilder.newBuilder(host)
                .followRedirect(false);
    }

    public HttpResponse doRequest(Function<RequestBuilder, HttpResponse> function) {
        RequestBuilder requestBuilder = getRequestBuilder();
        return function.apply(requestBuilder);
    }

    public RequestBuilder getRequestBuilder() {
        RequestBuilder requestBuilder = httpTool.request();
        if (authenticationRequired) {
            // Need to login first to be able to upload plugin
            String userName = jenkinsHost.getUser(UserType.ADMIN);
            String password = jenkinsHost.getPass(UserType.ADMIN);
            requestBuilder = requestBuilder.authenticate(userName, password);
        }
        return requestBuilder;
    }

    private boolean isAuthenticationRequired() {
        RequestBuilder requestBuilder = httpTool.request();
        HttpResponse response = requestBuilder.get(AUTH_PROTECTED_JENKINS_URL);
        return isAuthorizationNeeded(response);
    }

    public static boolean isAuthorizationNeeded(HttpResponse response) {
        HttpStatus code = response.getResponseCode();
        return HttpStatus.FORBIDDEN.equals(code) || HttpStatus.UNAUTHORIZED.equals(code);
    }

}
