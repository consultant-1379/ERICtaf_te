package com.ericsson.cifwk.taf.executor.utils;

import com.ericsson.cifwk.taf.executor.api.ArmInfo;
import com.ericsson.cifwk.taf.executor.api.ArtifactInfo;
import com.ericsson.cifwk.taf.executor.api.ScheduleRequest;
import com.ericsson.cifwk.taf.executor.api.ScheduleSource;
import com.ericsson.cifwk.taf.executor.api.TriggeringTask;
import com.ericsson.duraci.datawrappers.Arm;
import com.ericsson.duraci.datawrappers.ArtifactGav;
import com.ericsson.duraci.datawrappers.BaselineContext;
import com.ericsson.duraci.datawrappers.BaselinePart;
import com.ericsson.duraci.datawrappers.Environment;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelBaselineDefinedEvent;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Collection;

public class TriggeringEventBuilder {

    private static final String SCHEDULE_TAG_PATTERN = "Schedule:%s";
    static final String TEST_PROPERTIES_KEY = "TestProperties";

    private final Gson gson = new GsonBuilder().create();

    public TriggeringEventBuilder() {}

    public EiffelBaselineDefinedEvent build(TriggeringTask triggeringTask) {
        Collection<Environment> environments = Lists.newArrayList();
        Collection<BaselinePart> baselineParts = Lists.newArrayList();

        addBaselineParts(baselineParts, triggeringTask.getTestWare(), "TestWare:");
        addBaselineParts(baselineParts, triggeringTask.getCiFwkPackages(), "TestPackage:");
        for (ScheduleRequest schedule : triggeringTask.getSchedules()) {
            addSchedule(baselineParts, schedule);
        }

        Arm arm = toArm(triggeringTask.getArmInfo());
        environments.add(new Environment("ARM", toJson(arm)));
        environments.add(new Environment("TestGridResources", toJson(triggeringTask.getSlaveHosts())));
        environments.add(new Environment("SUTResource", triggeringTask.getSutResource()));

        EiffelBaselineDefinedEvent event = EiffelBaselineDefinedEvent.Factory.create("TestScheduleBaseline", baselineParts,
                new BaselineContext(environments, null));
        event.setOptionalParameter(TEST_PROPERTIES_KEY, toJson(triggeringTask.getGlobalTestProperties()));

        return event;
    }

    @VisibleForTesting
    void addSchedule(Collection<BaselinePart> baselineParts, ScheduleRequest schedule) {
        ScheduleSource scheduleSource = schedule.getSource();
        switch (scheduleSource) {
            case MAVEN_GAV:
                addSchedule(baselineParts, schedule.getArtifact(), schedule);
                break;
            default:
                addSchedule(baselineParts, nonGavScheduleArtifact(), schedule);
                break;
        }
    }

    private void addSchedule(Collection<BaselinePart> baselineParts, ArtifactInfo scheduleArtifact, ScheduleRequest schedule) {
        addBaselinePart(baselineParts, scheduleArtifact, String.format(SCHEDULE_TAG_PATTERN, toJson(schedule)));
    }

    private ArtifactInfo nonGavScheduleArtifact() {
        return new ArtifactInfo("custom-groupId", "custom-artifactId", "custom-version");
    }

    Arm toArm(ArmInfo armInfo) {
        return new Arm(armInfo.getId(), armInfo.getHttpString(), armInfo.getFtpString(), armInfo.getNfsString(),
                armInfo.getDownloadRepoName(), armInfo.getUploadRepoName(), armInfo.getUserName(), armInfo.getPassword(),
                armInfo.getDescription());
    }

    void addBaselineParts(Collection<BaselinePart> baselineParts, Collection<ArtifactInfo> artifactInfos, String tag) {
        for (ArtifactInfo artifactInfo : artifactInfos) {
            addBaselinePart(baselineParts, artifactInfo, tag);
        }
    }

    private void addBaselinePart(Collection<BaselinePart> baselineParts, ArtifactInfo artifactInfo, String tag) {
        ArtifactGav gav = new ArtifactGav(artifactInfo.getGroupId(), artifactInfo.getArtifactId(), artifactInfo.getVersion());
        baselineParts.add(new BaselinePart(gav, tag));
    }

    @VisibleForTesting
    String toJson(Object object) {
        return (object == null) ? "" : gson.toJson(object);
    }

}
