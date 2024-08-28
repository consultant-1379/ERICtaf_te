package com.ericsson.cifwk.taf.executor.healthcheck;

import com.ericsson.cifwk.taf.executor.TAFExecutor;

import java.util.ServiceLoader;

public class TeNodeResponseCheckCallable extends AbstractTeNodeHealthCheckCallable {

    private final String rootUrl;

    public TeNodeResponseCheckCallable(String rootUrl, String nodeName) {
        super(nodeName);
        this.rootUrl = rootUrl;
    }

    @Override
    public String doCheck(HealthParam check) {
        boolean executorExists = ServiceLoader.load(TAFExecutor.class).iterator().hasNext();
        if (!executorExists) {
            return failCheck(check, "Unable to load TAFExecutor. Most likely master and node has different versions.");
        }

        Check.Result result = Ping.checkUrl(rootUrl);
        if (!result.isSuccess()) {
            return failCheck(check, "Unable to connect to Jenkins master. Configured URL is not valid: " + rootUrl);
        }

        return toJson(check);
    }

    @Override
    public String getCheckName(String nodeName) {
        return "Jenkins Node is responding";
    }
}
