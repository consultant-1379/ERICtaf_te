package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.utils.JenkinsUtils;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.ItemGroup;
import hudson.model.Project;
import hudson.model.TopLevelItem;
import hudson.tasks.Builder;
import hudson.util.DescribableList;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Writer;


public class TafExecutionProject extends Project<TafExecutionProject, TafExecutionBuild> implements TopLevelItem {

    public static final String TAF_TE_EXEC_PROJECT_NAME = "TAF Executor test runner project";

    String postProcessingScript;

    @DataBoundConstructor
    public TafExecutionProject(ItemGroup parent, String name, String postProcessingScript) {
        super(parent, name);
        this.postProcessingScript = postProcessingScript;
    }

    /**
     * Call this Stapler method by GET to url: /jenkins/{build_name}/script
     */
    public HttpResponse doScript(StaplerRequest req, StaplerResponse rsp) throws IOException {
        return new HttpResponse() {
            @Override
            public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node)
                    throws IOException, ServletException {
                rsp.setContentType("text/plain;charset=UTF-8");
                if (!Strings.isNullOrEmpty(postProcessingScript)) {
                    Writer writer = rsp.getCompressedWriter(req);
                    writer.write(postProcessingScript);
                    writer.close();
                }
            }
        };
    }

    @Override
    protected void submit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
        JSONObject json = req.getSubmittedForm().getJSONObject("taf-executor-project");
        this.postProcessingScript = json.getString("postProcessingScript");
        super.submit(req, rsp);
    }

    public String getPostProcessingScript() {
        return postProcessingScript;
    }

    @Override
    public void onCreatedFromScratch() {
        super.onCreatedFromScratch();
        DescribableList<Builder, Descriptor<Builder>> buildersList = this.getBuildersList();
        buildersList.add(new TafExecutionBuilder());
        buildersList.add(new TafManualTestExecutionBuilder());
        try {
            this.setConcurrentBuild(true);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    protected Class<TafExecutionBuild> getBuildClass() {
        return TafExecutionBuild.class;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) JenkinsUtils.getJenkinsInstance().getDescriptorOrDie(getClass());
    }

    @Extension
    public static class DescriptorImpl extends AbstractProjectDescriptor {

        String postProcessingScript;

        public DescriptorImpl() {
            load();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws Descriptor.FormException {
            JSONObject projectConfig = json.getJSONObject("taf-execution-project");
            this.postProcessingScript = projectConfig.getString("postProcessingScript");
            save();
            return true;
        }

        @Override
        public String getDisplayName() {
            return TAF_TE_EXEC_PROJECT_NAME;
        }

        @Override
        public TafExecutionProject newInstance(ItemGroup parent, String name) {
            return new TafExecutionProject(parent, name, postProcessingScript);
        }

        public static DescriptorImpl get() {
            return (DescriptorImpl) JenkinsUtils.getJenkinsInstance().getDescriptor(TafExecutionProject.class);
        }

        public String getPostProcessingScript() {
            return postProcessingScript;
        }

        public void setPostProcessingScript(String postProcessingScript) {
            this.postProcessingScript = postProcessingScript;
        }

    }

}
