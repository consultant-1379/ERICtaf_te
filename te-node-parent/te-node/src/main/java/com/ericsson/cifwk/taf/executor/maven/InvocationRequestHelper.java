package com.ericsson.cifwk.taf.executor.maven;

import com.google.common.base.Throwables;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.io.PrintStream;

/**
 * Created by ekellmi on 7/2/15.
 */
public class InvocationRequestHelper {

    public static int invokeRequest(File workingDir, InvocationRequest request, PrintStream buildLog) {
        Invoker invoker = new DefaultInvoker();
        invoker.setWorkingDirectory(workingDir);

        InvocationResult result;
        try {
            result = invoker.execute(request);
        } catch (Exception e) {
            buildLog.println("Got an exception when executing current Maven request: " + e.getMessage());
            buildLog.println(ExceptionUtils.getStackTrace(e));
            throw Throwables.propagate(e);
        }

        return result.getExitCode();
    }

    public static DefaultInvocationRequest mavenInvocationRequest() {
        DefaultInvocationRequest request = new DefaultInvocationRequest();
        request.setBatchMode(true);
        return request;
    }

}
