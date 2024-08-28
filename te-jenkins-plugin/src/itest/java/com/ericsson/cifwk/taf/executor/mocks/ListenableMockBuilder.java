package com.ericsson.cifwk.taf.executor.mocks;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.tasks.Builder;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.Iterator;

public class ListenableMockBuilder extends Builder {

    private final Iterator<Result> resultIterator;
    private final MockBuilderListener builderListener;
    private final Function<AbstractBuild<?, ?>, Result> resultFunction;

    public ListenableMockBuilder(MockBuilderListener listener) {
        this.resultFunction = null;
        this.resultIterator = null;
        this.builderListener = listener;
    }

    public ListenableMockBuilder(Result result, MockBuilderListener listener) {
        this.resultFunction = null;
        this.resultIterator = Iterators.singletonIterator(result);
        this.builderListener = listener;
    }

    public ListenableMockBuilder(Iterator<Result> resultIterator, MockBuilderListener listener) {
        this.resultFunction = null;
        this.resultIterator = resultIterator;
        this.builderListener = listener;
    }

    public ListenableMockBuilder(Function<AbstractBuild<?, ?>, Result> resultFunction,
                                 MockBuilderListener listener) {
        this.resultFunction = resultFunction;
        this.resultIterator = null;
        this.builderListener = listener;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        Result result;
        if (resultFunction != null) {
            result = resultFunction.apply(build);
        } else if (resultIterator != null) {
            result = resultIterator.next();
        } else {
            result = Result.SUCCESS;
        }
        listener.getLogger().println("Simulating a specific result code " + result);
        build.setResult(result);
        if (builderListener != null) {
            builderListener.onPerform(build);
        }
        return true;
    }

    @Override
    public Descriptor<Builder> getDescriptor() {
        return new DescriptorImpl();
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<Builder> {
        public Builder newInstance(StaplerRequest req, JSONObject data) {
            throw new UnsupportedOperationException();
        }

        public String getDisplayName() {
            return "Force the build result";
        }
    }
}
