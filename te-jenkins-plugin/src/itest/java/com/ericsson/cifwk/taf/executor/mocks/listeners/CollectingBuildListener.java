package com.ericsson.cifwk.taf.executor.mocks.listeners;

import com.ericsson.cifwk.taf.executor.mocks.MockBuilderListener;
import hudson.model.AbstractBuild;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CollectingBuildListener implements MockBuilderListener {

    private final Map<Integer, AbstractBuild<?, ?>> builds = new ConcurrentHashMap<>();

    @Override
    public void onPerform(AbstractBuild<?, ?> build) {
        builds.put(build.getNumber(), build);
    }

    public Map<Integer, AbstractBuild<?, ?>> getBuilds() {
        return builds;
    }
}
