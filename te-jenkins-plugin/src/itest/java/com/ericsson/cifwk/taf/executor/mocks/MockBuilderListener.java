package com.ericsson.cifwk.taf.executor.mocks;

import hudson.model.AbstractBuild;

public interface MockBuilderListener {
    void onPerform(AbstractBuild<?, ?> build);
}
