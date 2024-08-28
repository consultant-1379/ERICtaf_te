package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.eiffel.EiffelMessageBus;
import com.ericsson.cifwk.taf.executor.eiffel.EiffelParent;
import com.ericsson.cifwk.taf.executor.helpers.TafScheduleProjectAdapter;
import com.ericsson.cifwk.taf.executor.utils.MessageBusUtils;
import com.ericsson.duraci.datawrappers.EventId;
import com.ericsson.duraci.datawrappers.ExecutionId;
import com.ericsson.duraci.eiffelmessage.messages.EiffelEvent;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import hudson.EnvVars;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import hudson.model.labels.LabelAtom;
import hudson.slaves.ComputerListener;
import hudson.slaves.DumbSlave;
import hudson.slaves.RetentionStrategy;
import hudson.tasks.Builder;
import hudson.util.DescribableList;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyVararg;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class JenkinsIntegrationTest {

    static final String TAF_SCHEDULE_JOB = "TAF_SCHEDULE";
    static final String TAF_EXECUTE_JOB = "TAF_EXECUTE";

    static final String SCHEDULE_GROUPS = "group1,group2";
    static final String TARGET_LOCAL_REPORTS_STORAGE = "target/localReportsStorage";
    static final String REPORTS_HOST = "reportsHost";

    static {
        System.setProperty("hudson.model.ParametersAction.keepUndefinedParameters", "true");
    }

    @Rule
    public transient JenkinsRule jenkinsContext = new JenkinsRule();

    protected String testwareVersion;


    protected JenkinsRule context() {
        return jenkinsContext;
    }

    protected Jenkins jenkins() {
        return jenkinsContext.getInstance();
    }

    public void setUp() throws Exception {
        URL url;
        try {
            url = jenkinsContext.getURL();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
        System.out.println("Embedded Jenkins started at " + url);

        deleteAllJobs();
        disableCSRFCheck();

        EiffelMessageBus eiffelMessageBus = mockMessageBus();
        MessageBusUtils.setGenericBus(eiffelMessageBus);

        testwareVersion = getTestwareVersion();
        Preconditions.checkArgument(testwareVersion != null);
    }

    protected String getTestwareVersion() throws IOException {
        // TODO: fix problem with testware version not loaded from file
        return "1.0.18";
//        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("testware.properties");
//        Properties properties = new Properties();
//        properties.load(resourceAsStream);
//        return properties.getProperty("testwareVersion");
    }

    @SuppressWarnings({"deprecation"})
    protected DumbSlave createSlaveNode(String name, int executorThreadCount) throws Exception {
        // 99% copy & paste from JenkinsRule class that doesn't allow to define executor amount...
        final CountDownLatch latch = new CountDownLatch(1);
        ComputerListener waiter = new ComputerListener() {
            @Override
            public void onOnline(Computer C, TaskListener t) {
                latch.countDown();
                unregister();
            }
        };
        waiter.register();

        DumbSlave onlineSlave = new DumbSlave(name, "TAF TE test slave",
                jenkinsContext.createTmpDir().getPath(), String.valueOf(executorThreadCount), Node.Mode.NORMAL,
                TAFExecutor.TAF_NODE_LABEL, jenkinsContext.createComputerLauncher(new EnvVars()),
                RetentionStrategy.NOOP, Collections.EMPTY_LIST);
        jenkins().addNode(onlineSlave);

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return onlineSlave;
    }

    protected void shutdownSlave(String slaveName) throws Exception {
        Node node = jenkins().getNode(slaveName);
        if (node == null) {
            return;
        }
        Computer computer = node.toComputer();
        if (computer.isOnline()) {
            computer.interrupt();
            Future<?> disconnectFuture = computer.disconnect(null);
            disconnectFuture.get();
            computer.waitUntilOffline();
        }
        jenkins().removeNode(node);
    }

    protected void deleteJob(String jobName) throws IOException, InterruptedException {
        Item item = jenkins().getItemByFullName(jobName);
        if (item != null) {
            item.delete();
        }
    }

    protected void deleteJobs() throws IOException, InterruptedException {
        deleteJob(TAF_SCHEDULE_JOB);
        deleteJob(TAF_EXECUTE_JOB);
    }

    protected void deleteAllJobs() {
        List<TopLevelItem> items = jenkins().getItems();
        try {
            for (TopLevelItem item : items) {
                System.out.println("--- Deleting " + item);
                item.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void disableCSRFCheck() {
//        jenkins().setSecurityRealm(null);
        jenkins().setCrumbIssuer(null);
    }

    protected EiffelMessageBus mockMessageBus() {
        EiffelMessageBus eiffelMessageBus = mock(EiffelMessageBus.class);

        EventId startEventId = new EventId("startEventId");
        when(
                eiffelMessageBus.sendStart(
                        (EiffelEvent) any(),
                        (ExecutionId) any(),
                        (EventId[]) any()
                )
        ).thenReturn(startEventId);
        ExecutionId executionId = new ExecutionId();
        when(eiffelMessageBus.getSentParent()).thenReturn(new EiffelParent(startEventId, executionId));
        doNothing().when(eiffelMessageBus).sendFinish(any(EiffelEvent.class));
        when(eiffelMessageBus.sendStart(any(EiffelEvent.class), any(ExecutionId.class))).thenReturn(startEventId);
        when(eiffelMessageBus.sendStart(any(EiffelEvent.class), any(ExecutionId.class), (EventId[]) anyVararg())).thenReturn(startEventId);

        return eiffelMessageBus;
    }

    protected TafExecutionProject setUpExecutionProject() throws IOException {
        Descriptor executorDescriptor = jenkins().getDescriptor(TafExecutionProject.class);
        TafExecutionProject executionProject = (TafExecutionProject) jenkins().createProject((TopLevelItemDescriptor) executorDescriptor, TAF_EXECUTE_JOB, true);
        executionProject.setAssignedLabel(new LabelAtom(TAFExecutor.TAF_NODE_LABEL));
        return executionProject;
    }

    protected TafScheduleProjectAdapterBuilder getScheduleProjectAdapterBuilder() {
        return new TafScheduleProjectAdapterBuilder(jenkins());
    }

    public class TafScheduleProjectAdapterBuilder {

        private final Jenkins jenkins;

        private TafScheduleProjectAdapterBuilder(Jenkins jenkins) {
            this.jenkins = jenkins;
        }

        public TafScheduleProjectAdapter build() {
            Descriptor scheduleProjectDescriptor = jenkins.getDescriptor(TafScheduleProject.class);
            TafScheduleProject project;
            try {
                project = (TafScheduleProject) jenkins.createProject(
                        (TopLevelItemDescriptor) scheduleProjectDescriptor, TAF_SCHEDULE_JOB, true);
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
            project.reportMbHost = CommonTestConstants.MB_HOST_NAME;
            project.reportMbPort = CommonTestConstants.MB_PORT;
            project.reportMbUsername = "project.username";
            project.reportMbPassword = Secret.fromString("project.password");
            project.reportMbExchange = CommonTestConstants.MB_EXCHANGE;
            project.reportMbDomainId = CommonTestConstants.MB_DOMAIN;
            project.setDeletableFlowsAgeInSeconds(3600 * 24);
            //
            project.reportsHost = REPORTS_HOST;
            project.localReportsStorage = TARGET_LOCAL_REPORTS_STORAGE;
            //

            project.artifactHelper = mock(ArtifactHelper.class);

            return new TafScheduleProjectAdapter(project);
        }
    }


    //
    protected String[][] components(String... names) {
        String[][] components = new String[names.length][];
        for (int i = 0; i < components.length; i++) {
            components[i] = new String[]{names[i] + "-groupId", names[i] + "-artifactId", (i+1) + ".0", names[i] + ".xml"};
        }
        return components;
    }

    //
    protected String schedule(String[][] components) {
        StringBuilder schedule = new StringBuilder();
        schedule.append("<schedule>");
        int i = 1;
        for (String[] component : components) {
            schedule.append("<item stop-on-fail=\"true\">")
                    .append("<name>test" + (i++) + "</name>")
                    .append("<component>")
                    .append(component[0]).append(':').append(component[1]).append(':').append(component[2])
                    .append("</component>")
                    .append("<suites>")
                    .append(component[3])
                    .append("</suites>")
                    .append("<groups>").append(SCHEDULE_GROUPS).append("</groups>")
                    .append("</item>");
        }
        schedule.append("</schedule>");
        return schedule.toString();
    }

    protected TafExecutionProject setUpExecutionProjectWithCustomBuilder(TestBuilder dummyBuilder) throws IOException {
        TafExecutionProject project = setUpExecutionProject();
        DescribableList<Builder, Descriptor<Builder>> buildersList = project.getBuildersList();
        // Remove the real TAF executor builder
        buildersList.clear();
        buildersList.add(dummyBuilder);

        return project;
    }
}
