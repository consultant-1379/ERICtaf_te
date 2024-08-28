package com.ericsson.cifwk.taf.executor.healthcheck;

import com.ericsson.cifwk.taf.executor.http.client.HttpClientFactory;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static java.lang.String.format;
import static org.apache.http.HttpStatus.SC_OK;

public class TeNodeAllureServiceAccessibilityCheckCallable extends AbstractTeNodeHealthCheckCallable {

    private final String allureServiceUrl;
    private final String allureServiceComponent;

    private final static Logger LOGGER = LoggerFactory.getLogger(TeNodeAllureServiceAccessibilityCheckCallable.class);

    public TeNodeAllureServiceAccessibilityCheckCallable(String nodeName, String allureServiceUrl, String allureServiceComponent) {
        super(nodeName);
        this.allureServiceComponent = allureServiceComponent;
        if(allureServiceComponent.equals("Nginx")) {
            this.allureServiceUrl = extractBaseAddress(allureServiceUrl) + "/api/";
        } else {
            this.allureServiceUrl = extractBaseAddress(allureServiceUrl);
        }
    }

    @Override
    public String doCheck(HealthParam check) {
        int statusCode;
        try {
            statusCode = queryAllureServiceUrl(allureServiceUrl);
        } catch (IOException e) {
            LOGGER.error("Failed to get status code from Allure service %s", allureServiceComponent, e);
            return failCheck(check, format("Failed to check Allure service %s %s accessibility: %s", allureServiceComponent, allureServiceUrl, e.getMessage()));
        }
        if(statusCode == SC_OK || statusCode ==401)
        return toJson(check);
        else
            return failCheck(check, format("edited Allure service %s %s is not accessible, statusCode: %s", allureServiceComponent, allureServiceUrl, statusCode));
    }

    @VisibleForTesting
    static String extractBaseAddress(String allureServiceUrl) {
        try {
            URL url = new URL(allureServiceUrl);
            return StringUtils.remove(allureServiceUrl, url.getPath());
        } catch (MalformedURLException e) {
            LOGGER.error("Could not extract base address from %s, defaulting to use full address", allureServiceUrl, e);
            return allureServiceUrl;
        }
    }

    @VisibleForTesting
    int queryAllureServiceUrl(String allureServiceUrl) throws IOException {
        final HttpResponse response;
        try (CloseableHttpClient httpClient = HttpClientFactory.createInstance()) {
            HttpGet request = new HttpGet(allureServiceUrl);
            response = httpClient.execute(request);
        }
        return response.getStatusLine().getStatusCode();
    }

    @Override
    public String getCheckName(String nodeName) {
        return format("Allure service %s is accessible from %s", allureServiceComponent, nodeName);
    }

}
