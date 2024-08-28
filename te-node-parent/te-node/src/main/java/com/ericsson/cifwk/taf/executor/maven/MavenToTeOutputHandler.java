package com.ericsson.cifwk.taf.executor.maven;

import org.apache.maven.shared.invoker.InvocationOutputHandler;

import java.io.PrintStream;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 16/03/2016
 */
public class MavenToTeOutputHandler implements InvocationOutputHandler {

    private final PrintStream logger;

    public MavenToTeOutputHandler(PrintStream logger) {
        this.logger = logger;
    }

    @Override
    public void consumeLine(String line) {
        logger.println(line);
    }

}