package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.api.TafTeAbortBuildResponse;
import com.ericsson.cifwk.taf.executor.api.TafTeBuildDetails;
import com.ericsson.cifwk.taf.executor.api.TafTeBuildTriggerResponse;
import com.ericsson.cifwk.taf.executor.api.TeRestServiceClient;
import com.ericsson.cifwk.taf.executor.api.TriggeringTask;
import com.google.common.base.Throwables;
import org.junit.Assert;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class RestServiceAwareITest extends JenkinsIntegrationTest {

    private TeRestServiceClient restClient;

    public void setUp() throws Exception {
        super.setUp();
        this.restClient = createTeRestServiceClient();
    }

    protected TeRestServiceClient getTeRestServiceClient() {
        return restClient;
    }

    private TeRestServiceClient createTeRestServiceClient() {
        try {
            URL url = jenkinsContext.getURL();
            String host = url.getHost();
            int port = url.getPort();
            return TeRestServiceClient.Builder.forHost(host, port).build();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    protected String getValidResponseFromUrl(String configUrl) throws IOException {
        URL url = new URL(configUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        int responseCode = urlConnection.getResponseCode();
        Assert.assertEquals(urlConnection.getResponseMessage(), HttpServletResponse.SC_OK, responseCode);
        InputStream inputStream = urlConnection.getInputStream();

        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String inputLine;
        StringBuilder buf = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            buf.append(inputLine).append(System.getProperty("line.separator"));
        }
        in.close();

        return buf.toString();
    }

    protected TafTeBuildTriggerResponse triggerBuildFor(String testWareVersion, String sutResource, String[] pathsToSchedules) {
        return triggerBuildForTask(createTriggeringTask(testWareVersion, sutResource, pathsToSchedules));
}

    protected TafTeBuildTriggerResponse triggerBuildForTask(TriggeringTask event) {
        TeRestServiceClient teRestServiceClient = getTeRestServiceClient();
        return teRestServiceClient.triggerBuild(event);
    }

    protected TafTeAbortBuildResponse abortBuild(String executionId) {
        TeRestServiceClient teRestServiceClient = getTeRestServiceClient();
        return teRestServiceClient.abortBuild(executionId);
    }

    protected TafTeBuildDetails waitUntilBuildFinished(String jobExecutionIdStr) {
        TafTeBuildDetails buildDetails;
        do {
            buildDetails = getTeRestServiceClient().getBuildDetails(jobExecutionIdStr);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // ignore
            }
        } while(!Boolean.TRUE.equals(buildDetails.getBuildComplete()));
        return buildDetails;
    }

    protected abstract TriggeringTask createTriggeringTask(String packageVersion, String sutResource, String[] pathsToSchedules);

}
