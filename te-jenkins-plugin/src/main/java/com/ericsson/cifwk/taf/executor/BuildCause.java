package com.ericsson.cifwk.taf.executor;

import hudson.model.Cause;
import org.kohsuke.stapler.export.Exported;

public class BuildCause extends Cause {

    private final String msg;

    public BuildCause(String msg) {
        this.msg = msg;
    }

    @Override
    @Exported(visibility = 3)
    public String getShortDescription() {
        return msg;
    }

}
