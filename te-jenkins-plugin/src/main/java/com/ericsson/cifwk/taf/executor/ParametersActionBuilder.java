package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.api.ArmInfo;
import com.ericsson.cifwk.taf.executor.api.ArtifactInfo;
import com.ericsson.cifwk.taf.executor.api.ScheduleRequest;
import com.ericsson.cifwk.taf.executor.api.ScheduleSource;
import com.ericsson.cifwk.taf.executor.api.TriggeringTaskBuilder;
import com.ericsson.cifwk.taf.executor.eiffel.EiffelMessageHelper;
import com.ericsson.duraci.datawrappers.ArtifactGav;
import com.ericsson.duraci.datawrappers.EventId;
import com.ericsson.duraci.datawrappers.ExecutionId;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Project;
import hudson.model.StringParameterValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class ParametersActionBuilder<B extends ParametersActionBuilder, P extends Project<?, ?>> {

    List<ParameterValue> parameters = new ArrayList<>();
    P project;

    public ParametersActionBuilder(P project) {
        this.project = project;
    }

    protected B add(String name, String value) {
        parameters.add(new StringParameterValue(name, value));
        return (B) this;
    }

    public ParametersAction build() {
        ParametersAction parametersAction = new ParametersAction(parameters);
        ParametersAction actions = project.getAction(ParametersAction.class);
        if (actions != null) {
            actions = actions.merge(parametersAction);
        } else {
            actions = parametersAction;
        }
        return actions;
    }

}

class TafScheduleParametersActionBuilder extends ParametersActionBuilder<TafScheduleParametersActionBuilder, TafScheduleProject> {

    static final String EMPTY_STRING = "";

    public TafScheduleParametersActionBuilder(TafScheduleProject project) {
        super(project);
    }

    @VisibleForTesting
    TafScheduleParametersActionBuilder withSchedule(ScheduleRequest schedule) {
        ScheduleSource scheduleSource = schedule.getSource();
        Preconditions.checkArgument(ScheduleSource.UNKNOWN != scheduleSource, "Unknown schedule type");
        Preconditions.checkArgument(schedule.isComplete(), "Schedule information is incomplete");

        add(BuildParameterNames.SCHEDULE_SOURCE, schedule.getSource().toString());
        switch(scheduleSource) {
            case MAVEN_GAV:
                add(BuildParameterNames.SCHEDULE_NAME, schedule.getName());
                ArtifactInfo artifact = schedule.getArtifact();
                add(BuildParameterNames.SCHEDULE_ARTIFACT, artifact.getGavString());
                break;
            case PLAIN_XML:
            case TAF_SCHEDULER:
                add(BuildParameterNames.SCHEDULE_NAME, "Custom schedule");
                add(BuildParameterNames.SCHEDULE_ARTIFACT,
                        new ArtifactGav("custom-groupId", "custom-artifactId", "custom-version").toString());
                add(BuildParameterNames.SCHEDULE, schedule.getXml());
                break;
            default:
                throw new UnsupportedOperationException(String.format("Schedule source '%s' is not supported", scheduleSource));

        }
        return this;
    }

    TafScheduleParametersActionBuilder withEnableLdap(String enableLdap) {
        return add(BuildParameterNames.ENABLE_LDAP, enableLdap);
    }

    TafScheduleParametersActionBuilder withTeUsername(String teUsername) {
        return add(BuildParameterNames.TE_USERNAME, teUsername);
    }

    TafScheduleParametersActionBuilder withTePassword(String tePassword) {
        return add(BuildParameterNames.TE_PASSWORD, tePassword);
    }

    TafScheduleParametersActionBuilder withTestWare(Collection<ArtifactInfo> testWares) {
        String gav = EiffelMessageHelper.parseTestWares(testWares);
        return add(BuildParameterNames.TESTWARE, gav);
    }

    TafScheduleParametersActionBuilder withJobTriggeringEventId(EventId eventId) {
        add(BuildParameterNames.EIFFEL_JOB_TRIGGER_EVENT_ID, eventId.toString());
        return this;
    }

    TafScheduleParametersActionBuilder withSutResource(String sutResource) {
        return add(BuildParameterNames.SUT_RESOURCE, sutResource);
    }

    TafScheduleParametersActionBuilder withMinTafVersion(String minTafVersion) {
        return add(BuildParameterNames.MIN_TAF_VERSION, minTafVersion);
    }

    TafScheduleParametersActionBuilder withUserDefinedGAVs(String userDefinedGAVs) {
        return add(BuildParameterNames.USER_DEFINED_GAVS, userDefinedGAVs);
    }

    TafScheduleParametersActionBuilder withSlaveHosts(Collection<com.ericsson.cifwk.taf.executor.api.Host> testResources) {
        String hosts = testResources.isEmpty() ? TAFExecutor.TAF_NODE_LABEL : EiffelMessageHelper.parseTestResource(testResources);
        return add(BuildParameterNames.SLAVE_HOSTS, hosts);
    }

    TafScheduleParametersActionBuilder withRepositoryUrl(ArmInfo arm) {
        return add(BuildParameterNames.REPOSITORY_URL, arm == null ? EMPTY_STRING : arm.getHttpString());
    }

    TafScheduleParametersActionBuilder withExecutionId(ExecutionId executionId) {
        return add(BuildParameterNames.EIFFEL_JOB_EXECUTION_ID, executionId.toString());
    }

    public TafScheduleParametersActionBuilder withCommonTestProperties(Properties commonTestProperties) {
        add(BuildParameterNames.COMMON_TEST_PROPERTIES, TriggeringTaskBuilder.propertyMapToString(commonTestProperties));
        return this;
    }

    public TafScheduleParametersActionBuilder withTestTriggerDetails(String testTriggerDetailsJson) {
        add(BuildParameterNames.TEST_TRIGGER_DETAILS_JSON, testTriggerDetailsJson);
        return this;
    }

    public TafScheduleParametersActionBuilder withMiscProperties(String miscProperties) {
        add(BuildParameterNames.MISC_PROPERTIES, miscProperties);
        return this;
    }

    public TafScheduleParametersActionBuilder withJobType(String jobType) {
        add(BuildParameterNames.JOB_TYPE, jobType);
        return this;
    }

    public TafScheduleParametersActionBuilder withTafSchedulerAddress(String tafSchedulerAddress) {
        add(BuildParameterNames.TAF_SCHEDULER_ADDRESS, tafSchedulerAddress);
        return this;
    }
}
