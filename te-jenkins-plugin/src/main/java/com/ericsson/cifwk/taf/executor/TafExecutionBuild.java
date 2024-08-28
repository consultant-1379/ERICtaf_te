package com.ericsson.cifwk.taf.executor;

import hudson.model.Build;
import hudson.model.Executor;

import java.io.File;
import java.io.IOException;

public class TafExecutionBuild extends Build<TafExecutionProject, TafExecutionBuild> {

    public TafExecutionBuild(TafExecutionProject project) throws IOException {
        super(project);
    }

    public TafExecutionBuild(TafExecutionProject project, File buildDir) throws IOException {
        super(project, buildDir);
    }

    @Override
    public void run() {
        this.execute(new TafExecutionRunner());
    }

    protected class TafExecutionRunner extends BuildExecution {
    }

    public void abort() {
        Executor executor = this.getExecutor();
        if (executor != null) {
            executor.interrupt();
        }
    }
}
