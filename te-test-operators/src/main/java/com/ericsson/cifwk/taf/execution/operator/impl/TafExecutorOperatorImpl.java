package com.ericsson.cifwk.taf.execution.operator.impl;

import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.execution.operator.TafExecutorOperator;
import com.ericsson.cifwk.taf.executor.api.healthcheck.HealthCheck;
import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.cifwk.taf.tools.http.HttpToolBuilder;
import com.ericsson.cifwk.taf.tools.http.constants.HttpStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Operator
public class TafExecutorOperatorImpl implements TafExecutorOperator {

    private static Logger LOGGER = Logger.getLogger(TafExecutorOperatorImpl.class);

    @Override
    public List<HealthCheck> healthCheck(Host teMasterHost) {
        LOGGER.info("Running TAF TE health check for host " + teMasterHost.getIp());
        HttpTool httpTool = createHttpTool(teMasterHost);
        HttpResponse httpResponse = httpTool
                .get("/jenkins/descriptorByName/com.ericsson.cifwk.taf.executor.healthcheck.HealthCheck/healthCheck");
        String body = httpResponse.getBody();
        LOGGER.info("Raw response: '" + body + "'");
        if (HttpStatus.OK.equals(httpResponse.getResponseCode())) {
            Gson gson = new Gson();
            return gson.fromJson(body, new TypeToken<List<HealthCheck>>() {
            }.getType());
        } else {
            LOGGER.error("TE health check request returned " + httpResponse.getResponseCode() + " response code");
            return new ArrayList<>();
        }
    }

    private HttpTool createHttpTool(Host host) {
        return HttpToolBuilder.newBuilder(host).build();
    }

}
