package com.ericsson.cifwk.taf.executor.api;

import com.ericsson.cifwk.taf.executor.api.healthcheck.HealthCheckState;
import org.apache.http.client.HttpClient;

/**
 * <p>TAF TE REST service client API.</p>
 * <p>Please refer to <a href="http://confluence-oss.lmera.ericsson.se/display/TAF/TE+job+triggering+via+REST+call+and+new+event+model">REST service specification</a>
 *  for details</p>
 */
public interface TeRestServiceClient {

    /**
     * <p>Trigger the new TAF TE build based on the information included into the triggering task.</p>
     * @param triggeringTask
     * @return response containing the details of the triggered build or error description if triggering failed.
     */
    TafTeBuildTriggerResponse triggerBuild(TriggeringTask triggeringTask);

    /**
     * Returns TE build details, including overall result and the details of each Jenkins job.
     * @param primaryJobExecutionId execution ID available after the <code>triggerBuild</code> invocation
     */
    TafTeBuildDetails getBuildDetails(String primaryJobExecutionId);

    /**
     * <p>Abort TAF TE build based on the jobExecutionId.</p>
     * @param jobExecutionId
     * @return response containing the information whether jobs were aborted.
     */
    TafTeAbortBuildResponse abortBuild(String jobExecutionId);

    /**
     * Makes Health check request to TE
     * @return Health check state - negative or positive
     */
    HealthCheckState getTeHealthCheck();

    /**
     * Returns the address of the host that this REST service client is talking to
     * @return REST service host
     */
    String getHostAddress();

    /**
     * Returns the port of the host that this REST service client is talking to
     * @return REST service host port
     */
    int getHostPort();

    /**
     * Convenience method. Returns an instance of Apache HTTP client that can be used to send any HTTP request to
     * the REST service host.
     * @return Apache HTTP client talking to the REST service host
     */
    HttpClient getHttpClient();

    public static class Builder {

        private final String hostAddress;

        private final int port;

        public Builder(String hostAddress, int port) {
            this.hostAddress = hostAddress;
            this.port = port;
        }

        public static Builder forHost(String hostAddress, int port) {
            return new Builder(hostAddress, port);
        }

        public TeRestServiceClient build() {
            return new TeRestServiceClientImpl(hostAddress, port);
        }
    }
}
