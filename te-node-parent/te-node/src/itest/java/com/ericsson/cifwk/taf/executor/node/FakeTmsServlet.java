package com.ericsson.cifwk.taf.executor.node;

import org.mortbay.util.ajax.JSON;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 21/03/2016
 */
public class FakeTmsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HashMap<String, String> result = new HashMap<>();
        writeJson(resp, result);
    }

    private void writeJson(HttpServletResponse resp, Object object) throws IOException {
        resp.getOutputStream().print(new JSON().toJSON(object));
        resp.setContentType("application/json");
        resp.setStatus(200);
        resp.flushBuffer();
    }

}
