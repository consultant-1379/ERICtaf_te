package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.healthcheck.Check;
import com.ericsson.cifwk.taf.executor.healthcheck.RabbitMqChecker;
import com.ericsson.cifwk.taf.executor.schedule.FlowExecutor;
import com.ericsson.cifwk.taf.executor.utils.JenkinsUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.ItemGroup;
import hudson.model.Project;
import hudson.model.TopLevelItem;
import hudson.triggers.Trigger;
import hudson.util.FormValidation;
import hudson.util.Secret;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.ObjectStreamException;


public class TafScheduleProject extends Project<TafScheduleProject, TafScheduleBuild>
        implements TopLevelItem, TafScheduleProjectConfigurable {

    public static final String NAME = "TAF Executor scheduler project";

    String reportMbHost;
    Integer reportMbPort;
    String reportMbUsername;
    Secret reportMbPassword;
    String reportMbExchange;
    String reportMbDomainId;
    String reportsHost;
    String localReportsStorage;
    String reportingScriptsFolder;
    Integer minExecutorDiskSpaceGB;
    Integer minExecutorMemorySpaceGB;
    String allureVersion;
    Integer deletableFlowsAgeInDays;
    String allureServiceUrl;
    String allureServiceBackendUrl;
    // TODO: temporary solution, introduce a block for upload parameters definition (currently hardcoded in upload script)
    boolean uploadToOssLogs;

    // Introduced for test purposes
    Integer deletableFlowsAgeInSeconds;

    transient ArtifactHelper artifactHelper;
    transient FlowExecutor flowExecutor;
    TestwareRuntimeLimitations runtimeLimitations = new TestwareRuntimeLimitations();

    @DataBoundConstructor
    public TafScheduleProject(ItemGroup parent, String name,
                              String reportMbHost, Integer reportMbPort, String reportMbUsername, Secret reportMbPassword,
                              String reportMbExchange, String reportMbDomainId,
                              String reportsHost, String allureServiceUrl, String allureServiceBackendUrl, String localReportsStorage, String reportingScriptsFolder, boolean uploadToOssLogs,
                              Integer minExecutorDiskSpaceGB, String allureVersion, Integer deletableFlowsAgeInDays,
                              Integer minExecutorMemorySpaceGB,TestwareRuntimeLimitations runtimeLimitations) {
        super(parent, name);
        this.reportMbHost = reportMbHost;
        this.reportMbPort = reportMbPort;
        this.reportMbUsername = reportMbUsername;
        this.reportMbPassword = reportMbPassword;
        this.reportMbExchange = reportMbExchange;
        this.reportMbDomainId = reportMbDomainId;
        this.reportsHost = reportsHost;
        this.allureServiceUrl = allureServiceUrl;
        this.allureServiceBackendUrl = allureServiceBackendUrl;
        this.localReportsStorage = localReportsStorage;
        this.reportingScriptsFolder = reportingScriptsFolder;
        this.uploadToOssLogs = uploadToOssLogs;
        this.minExecutorDiskSpaceGB = minExecutorDiskSpaceGB;
        this.minExecutorMemorySpaceGB = minExecutorMemorySpaceGB;
        this.allureVersion = allureVersion;
        this.deletableFlowsAgeInDays = deletableFlowsAgeInDays;
        //
        this.artifactHelper = new ArtifactHelper();
        this.flowExecutor = new FlowExecutor(JenkinsUtils.getJenkinsInstance());
        this.runtimeLimitations = runtimeLimitations;
    }


    @Override
    public String getReportMbHost() {
        return reportMbHost;
    }

    @Override
    public Integer getReportMbPort() {
        return reportMbPort;
    }

    @Override
    public String getReportMbUsername() {
        return reportMbUsername;
    }

    @Override
    public String getReportMbPassword() {
        return reportMbPassword == null ? null : reportMbPassword.getPlainText();
    }

    @Override
    public String getReportMbExchange() {
        return reportMbExchange;
    }

    @Override
    public String getReportMbDomainId() {
        return reportMbDomainId;
    }

    public String getReportsHost() {
        return reportsHost;
    }

    @Override
    public String getAllureServiceUrl() {
        return allureServiceUrl;
    }

    @Override
    public String getAllureServiceBackendUrl() {
        return allureServiceBackendUrl;
    }

    public String getLocalReportsStorage() {
        return localReportsStorage;
    }

    @Override
    public String getReportingScriptsFolder() {
        return reportingScriptsFolder;
    }

    public Integer getMinExecutorDiskSpaceGB() {
        return minExecutorDiskSpaceGB;
    }
    public Integer getMinExecutorMemorySpaceGB() {
        return minExecutorMemorySpaceGB;
    }

    @Override
    public String getAllureVersion() {
        return allureVersion;
    }

    @Override
    public Integer getDeletableFlowsAgeInDays() {
        return deletableFlowsAgeInDays;
    }

    @Override
    public boolean isUploadToOssLogs() {
        return uploadToOssLogs;
    }

    @Override
    public TestwareRuntimeLimitations getRuntimeLimitations() {
        return runtimeLimitations;
    }

    public Integer getDeletableFlowsAgeInSeconds() {
        if (deletableFlowsAgeInSeconds == null) {
            // Have to do it here, because seconds are not a part of project's config XML, and will be null on unmarshalling
            this.deletableFlowsAgeInSeconds = deletableFlowsAgeInDays == null ? null : deletableFlowsAgeInDays * (24 * 3600);
        }
        return deletableFlowsAgeInSeconds;
    }

    @VisibleForTesting
    void setDeletableFlowsAgeInSeconds(int deletableFlowsAgeInSeconds) {
        this.deletableFlowsAgeInSeconds = deletableFlowsAgeInSeconds;
        this.deletableFlowsAgeInDays = deletableFlowsAgeInSeconds / (24 * 60 * 60);
    }

    public ArtifactHelper getArtifactHelper() {
        return artifactHelper;
    }

    public FlowExecutor getFlowExecutor() {
        return flowExecutor;
    }

    /**
     * Called when Job Configuration is saved
     *
     * @param req
     * @param rsp
     * @throws IOException
     * @throws ServletException
     * @throws Descriptor.FormException
     */
    @Override
    protected void submit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
        JSONObject submittedForm = req.getSubmittedForm();
        TafScheduleProjectConfiguration config = TafScheduleProjectConfiguration.from(submittedForm);

        this.reportMbHost = config.getReportMbHost();
        this.reportMbPort = config.getReportMbPort();
        this.reportMbUsername = config.getReportMbUsername();
        this.reportMbPassword = config.getReportMbPassword();
        this.reportMbExchange = config.getReportMbExchange();
        this.reportMbDomainId = config.getReportMbDomainId();
        this.reportsHost = config.getReportsHost();
        this.allureServiceUrl = config.getAllureServiceUrl();
        this.allureServiceBackendUrl = config.getAllureServiceBackendUrl();
        this.localReportsStorage = config.getLocalReportsStorage();
        this.reportingScriptsFolder = config.getReportingScriptsFolder();
        this.uploadToOssLogs = config.isUploadToOssLogs();
        this.minExecutorDiskSpaceGB = config.getMinExecutorDiskSpaceGB();
        this.minExecutorMemorySpaceGB = config.getMinExecutorMemorySpaceGB();
        this.deletableFlowsAgeInDays = config.getDeletableFlowsAgeInDays();
        this.allureVersion = config.getAllureVersion();
        this.runtimeLimitations = config.getRuntimeLimitations();

        super.submit(req, rsp);
    }

    @Override
    public void onCreatedFromScratch() {
        super.onCreatedFromScratch();
        this.getBuildersList().add(new TafScheduleBuilder());
        try {
            this.setConcurrentBuild(true);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void makeDisabled(boolean disabled) throws IOException {
        if (disabled && !this.disabled) {
            for (Trigger t : this.triggers()) t.stop();
        } else if (this.disabled && !disabled) {
            for (Trigger t : this.triggers()) t.start(this, true);
        }
        super.makeDisabled(disabled);
    }

    @Override
    protected void performDelete() throws IOException, InterruptedException {
        super.performDelete();
    }

    @Override
    protected Class<TafScheduleBuild> getBuildClass() {
        return TafScheduleBuild.class;
    }

    /**
     * @return TafScheduleProject
     * @see java.io.Serializable
     */
    protected Object readResolve() throws ObjectStreamException {
        this.artifactHelper = new ArtifactHelper();
        this.flowExecutor = new FlowExecutor(JenkinsUtils.getJenkinsInstance());
        return this;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) JenkinsUtils.getJenkinsInstance().getDescriptorOrDie(getClass());
    }

    /**
     * POJO representing the global TE configuration in Jenkins
     */
    @Extension
    public static class DescriptorImpl extends AbstractProjectDescriptor implements TafScheduleProjectConfigurable {

        private String reportMbHost;
        private Integer reportMbPort;
        private String username;
        private Secret password;
        private String reportMbExchange;
        private String reportMbDomainId;
        private String reportsHost;
        private String allureServiceUrl;
        private String allureServiceBackendUrl;
        private String localReportsStorage;
        private String reportingScriptsFolder;
        private Integer minExecutorDiskSpaceGB;
        private Integer minExecutorMemorySpaceGB;
        private String allureVersion;
        private Integer deletableFlowsAgeInDays;
        private boolean uploadToOssLogs;
        private TestwareRuntimeLimitations runtimeLimitations;

        public DescriptorImpl() {
            load();
        }

        public FormValidation doTestConnection(@QueryParameter("reportMbHost") final String host,
                                               @QueryParameter("reportMbPort") final int port,
                                               @QueryParameter("reportMbExchange") final String reportMbExchange,
                                               @QueryParameter("reportMbUsername") final String username,
                                               @QueryParameter("reportMbPassword") final String password)
                throws IOException, ServletException {
            Check.Result result = new RabbitMqChecker().checkExchange(host, port, reportMbExchange, username, password, false);
            if (result.isSuccess()) {
                return FormValidation.ok("Success");
            } else {
                return FormValidation.error(result.getMessage());
            }
        }

        public FormValidation doCheckMaxThreadCount(@QueryParameter String value) {
            int maxThreadCount;
            try {
                maxThreadCount = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return FormValidation.error(TestwareRuntimeLimitations.REQUIRED_MAX_THREAD_COUNT_MSG);
            }
            return (maxThreadCount > 0) ? FormValidation.ok() : FormValidation.error(TestwareRuntimeLimitations.REQUIRED_MAX_THREAD_COUNT_MSG);
        }

        @Override
        public String getReportMbHost() {
            return reportMbHost;
        }

        public void setReportMbHost(String reportMbHost) {
            this.reportMbHost = reportMbHost;
        }

        @Override
        public Integer getReportMbPort() {
            return reportMbPort;
        }

        @Override
        public String getReportMbUsername() {
            return username;
        }

        @Override
        public String getReportMbPassword() {
            return password == null ? null : password.getPlainText();
        }

        @Override
        public String getReportMbExchange() {
            return reportMbExchange;
        }

        @Override
        public String getReportMbDomainId() {
            return reportMbDomainId;
        }

        @Override
        public String getReportsHost() {
            return reportsHost;
        }

        @Override
        public String getAllureServiceUrl() {
            return allureServiceUrl;
        }

        @Override
        public String getAllureServiceBackendUrl() {
            return allureServiceBackendUrl;
        }

        public String getLocalReportsStorage() {
            return localReportsStorage;
        }

        @Override
        public String getReportingScriptsFolder() {
            return reportingScriptsFolder;
        }

        @Override
        public Integer getMinExecutorDiskSpaceGB() {
            return minExecutorDiskSpaceGB;
        }

        @Override
        public Integer getMinExecutorMemorySpaceGB() {
            return minExecutorMemorySpaceGB;
        }


        @Override
        public String getAllureVersion() {
            return allureVersion;
        }

        @Override
        public Integer getDeletableFlowsAgeInDays() {
            return deletableFlowsAgeInDays;
        }

        @Override
        public boolean isUploadToOssLogs() {
            return uploadToOssLogs;
        }

        @Override
        public TestwareRuntimeLimitations getRuntimeLimitations() {
            return runtimeLimitations;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            TafScheduleProjectConfiguration config = TafScheduleProjectConfiguration.from(json);

            this.reportMbHost = config.getReportMbHost();
            this.reportMbPort = config.getReportMbPort();
            this.username = config.getReportMbUsername();
            this.password = config.getReportMbPassword();
            this.reportMbExchange = config.getReportMbExchange();
            this.reportMbDomainId = config.getReportMbDomainId();
            this.reportsHost = config.getReportsHost();
            this.allureServiceUrl = config.getAllureServiceUrl();
            this.allureServiceBackendUrl = config.getAllureServiceBackendUrl();
            this.localReportsStorage = config.getLocalReportsStorage();
            this.reportingScriptsFolder = config.getReportingScriptsFolder();
            this.minExecutorDiskSpaceGB = config.getMinExecutorDiskSpaceGB();
            this.minExecutorMemorySpaceGB = config.getMinExecutorMemorySpaceGB();
            this.allureVersion = config.getAllureVersion();
            this.deletableFlowsAgeInDays = config.getDeletableFlowsAgeInDays();
            this.uploadToOssLogs = config.isUploadToOssLogs();
            this.runtimeLimitations = config.getRuntimeLimitations();

            save();
            return true;
        }

        @Override
        public String getDisplayName() {
            return NAME;
        }

        @Override
        public TafScheduleProject newInstance(ItemGroup parent, String name) {
            JenkinsUtils.checkIfCanCreateNewProject(JenkinsUtils.getJenkinsInstance(), TafScheduleProject.class, 1);
            return new TafScheduleProject(parent, name,
                    reportMbHost, reportMbPort, username, password, reportMbExchange, reportMbDomainId, reportsHost,
                    allureServiceUrl, allureServiceBackendUrl, localReportsStorage, reportingScriptsFolder, uploadToOssLogs,
                    minExecutorDiskSpaceGB, allureVersion, deletableFlowsAgeInDays, minExecutorMemorySpaceGB, runtimeLimitations);
        }

        public static DescriptorImpl get() {
            return (DescriptorImpl) JenkinsUtils.getJenkinsInstance().getDescriptor(TafScheduleProject.class);
        }
    }

}
