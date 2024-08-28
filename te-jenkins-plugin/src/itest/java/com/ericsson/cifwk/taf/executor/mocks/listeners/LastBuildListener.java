package com.ericsson.cifwk.taf.executor.mocks.listeners;

import com.ericsson.cifwk.taf.executor.mocks.MockBuilderListener;
import hudson.model.AbstractBuild;

public class LastBuildListener implements MockBuilderListener {

    private AbstractBuild<?, ?> lastBuild;

    @Override
    public void onPerform(AbstractBuild<?, ?> build) {
        lastBuild = build;
    }

    public AbstractBuild<?, ?> getLastBuild() {
        return lastBuild;
    }
}
