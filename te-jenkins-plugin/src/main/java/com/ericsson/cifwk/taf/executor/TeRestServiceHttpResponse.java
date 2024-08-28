package com.ericsson.cifwk.taf.executor;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Writer;

abstract class TeRestServiceHttpResponse implements HttpResponse {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeRestServiceHttpResponse.class);

    private Gson gson = new GsonBuilder().create();
    private StaplerRequest req;
    private StaplerResponse rsp;

    TeRestServiceHttpResponse(StaplerRequest req, StaplerResponse rsp) {
        this.req = req;
        this.rsp = rsp;
    }

    @Override
    public void generateResponse(StaplerRequest staplerRequest, StaplerResponse staplerResponse, Object node) throws IOException, ServletException {
        Object response = createResponse();
        rsp.setContentType("application/json");
        String responseStr;
        try (Writer writer = rsp.getCompressedWriter(req)) {
            responseStr = gson.toJson(response);
            writer.write(responseStr);
        } catch (Exception e) {
            LOGGER.error("Failed to serialize response", e);
            throw Throwables.propagate(e);
        }
    }

    protected abstract Object createResponse();
}
