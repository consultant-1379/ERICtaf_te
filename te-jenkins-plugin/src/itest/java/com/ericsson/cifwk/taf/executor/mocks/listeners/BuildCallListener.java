package com.ericsson.cifwk.taf.executor.mocks.listeners;

import com.ericsson.cifwk.taf.executor.mocks.MockBuilderListener;
import hudson.model.AbstractBuild;

public class BuildCallListener implements MockBuilderListener {

    private boolean called = false;

    @Override
    public void onPerform(AbstractBuild<?, ?> build) {
        called = true;
    }

    public boolean wasCalled() {
        return called;
    }
}
