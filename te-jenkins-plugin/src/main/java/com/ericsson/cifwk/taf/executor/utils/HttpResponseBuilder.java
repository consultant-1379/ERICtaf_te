package com.ericsson.cifwk.taf.executor.utils;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Writer;

public class HttpResponseBuilder {

    public static HttpResponse json(final Object json) {
        return new HttpResponse() {
            @Override
            public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                rsp.setContentType("application/json;charset=UTF-8");
                Writer w = rsp.getCompressedWriter(req);
                w.write(json.toString());
                w.close();
            }
        };
    }

    public static HttpResponse properties(final String properties) {
        return new HttpResponse() {
            @Override
            public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                rsp.setContentType("text/plain;charset=UTF-8");
                Writer w = rsp.getCompressedWriter(req);
                w.write(properties);
                w.close();
            }
        };
    }

}
