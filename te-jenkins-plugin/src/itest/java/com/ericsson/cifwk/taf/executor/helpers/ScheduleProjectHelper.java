package com.ericsson.cifwk.taf.executor.helpers;

import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleComponent;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleItem;
import com.ericsson.cifwk.taf.executor.eiffel.EiffelMessageBus;
import com.ericsson.cifwk.taf.executor.eiffel.EiffelParent;
import com.ericsson.duraci.datawrappers.ArtifactGav;
import com.ericsson.duraci.datawrappers.EventId;
import com.ericsson.duraci.datawrappers.ExecutionId;
import com.ericsson.duraci.eiffelmessage.messages.EiffelEvent;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import hudson.model.Run;
import hudson.util.RunList;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public final class ScheduleProjectHelper {

    private final Collection<ScheduleItem> scheduleItems;
    private final String schedule;

    private ScheduleProjectHelper(Collection<ScheduleItem> itemGAVs, String schedule) {
        this.scheduleItems = itemGAVs;
        this.schedule = schedule;
    }

    public static ScheduleProjectHelper from(Collection<String> componentNames, String groups) {
        Collection<ArtifactGav> testWares = testWaresFrom(componentNames);
        return fromItemGAVs(testWares, groups);
    }

    public static ScheduleProjectHelper fromItemGAVs(Collection<ArtifactGav> itemGAVs, String groups) {
        Collection<ScheduleItem> scheduleItems = scheduleItemsFrom(itemGAVs, groups);
        String schedule = scheduleFrom(scheduleItems);
        return new ScheduleProjectHelper(scheduleItems, schedule);
    }

    private static ScheduleProjectHelper fromScheduleItems(Collection<ScheduleItem> scheduleItems) {
        String schedule = scheduleFrom(scheduleItems);
        return new ScheduleProjectHelper(scheduleItems, schedule);
    }

    private static Collection<ArtifactGav> testWaresFrom(Collection<String> componentNames) {
        ArrayList<ArtifactGav> artifactGavs = new ArrayList<>();
        for (String componentName : componentNames) {
            artifactGavs.add(new ArtifactGav(
                    componentName + "-groupId",
                    componentName + "-artifactId",
                    "1.0"
            ));
        }
        return artifactGavs;
    }

    private static Collection<ScheduleItem> scheduleItemsFrom(Collection<ArtifactGav> itemGAVs, String groups) {
        List<ScheduleItem> result = newArrayList();
        Splitter splitter = Splitter.on(",").trimResults();
        for (ArtifactGav itemGAV : itemGAVs) {
            ScheduleComponent component = new ScheduleComponent(
                    itemGAV.getGroupId(),
                    itemGAV.getArtifactId(),
                    itemGAV.getVersion());
            ScheduleItem scheduleItem = new ScheduleItem(null,
                    itemGAV.getArtifactId(),
                    component,
                    (itemGAV.getArtifactId() != null)
                                ? newArrayList(splitter.split(itemGAV.getArtifactId()))
                                : newArrayList(),
                    (groups != null)
                                ? newArrayList(splitter.split(groups))
                                : newArrayList(),
                    null,
                    false,
                    0, null);
            result.add(scheduleItem);
        }
        return result;
    }

    private static String scheduleFrom(Collection<ScheduleItem> scheduleItems) {
        StringBuilder schedule = new StringBuilder();
        schedule.append("<schedule>");
        for (ScheduleItem scheduleItem : scheduleItems) {
            schedule.append("<item stop-on-fail=\"true\"");
            ScheduleComponent scheduleItemComponent = scheduleItem.getComponent();
            String groupId = scheduleItemComponent.getGroupId();
            String artifactId = scheduleItemComponent.getArtifactId();
            String version = scheduleItemComponent.getVersion();
            schedule.append(">")
                    .append("<name>")
                    .append(artifactId)
                    .append("</name>")
                    .append("<component>")
                    .append(groupId)
                    .append(":")
                    .append(artifactId);
            if (StringUtils.isNotBlank(version)) {
                schedule.append(":")
                        .append(version);
            }
            schedule.append("</component>")
                    .append("<suites>")
                    .append(artifactId)
                    .append("</suites>");
            List<String> groups = scheduleItem.getGroups();
            String groupsStr = Joiner.on(",").join(groups);
            if (StringUtils.isNotBlank(groupsStr)) {
                schedule.append("<groups>").append(groupsStr).append("</groups>");
            }
            schedule.append("</item>");
        }
        schedule.append("</schedule>");
        return schedule.toString();
    }

    public String getSchedule() {
        return schedule;
    }

    public static <R extends Run> R waitForBuild(RunList<R> builds, int timeoutInSeconds) throws InterruptedException {
        R build = builds.getLastBuild();
        while ((build == null || build.isBuilding()) && timeoutInSeconds > 0) {
            Thread.sleep(1000);
            timeoutInSeconds--;
            build = builds.getLastBuild();
        }
        if (build.isBuilding()) fail("Didn't wait for build:" + build + " complete");
        return build;
    }

    public static EiffelMessageBus mockMessageBus() {
        EiffelMessageBus messageBus = mock(EiffelMessageBus.class);
        when(messageBus.getSentParent()).thenReturn(new EiffelParent(new EventId(), new ExecutionId()));
        when(messageBus.sendStart(any(EiffelEvent.class), any(ExecutionId.class))).thenReturn(new EventId());
        when(messageBus.sendStart(any(EiffelEvent.class), any(ExecutionId.class), (EventId[]) anyVararg())).thenReturn(new EventId());
        return messageBus;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String[] components;
        private String groups;
        private Collection<ArtifactGav> itemGAVs;
        private Collection<ScheduleItem> scheduleItems;

        public Builder withComponents(String... components) {
            this.components = components;
            return this;
        }

        public Builder withItemGAVs(Collection<ArtifactGav> itemGAVs) {
            this.itemGAVs = itemGAVs;
            return this;
        }

        public Builder withScheduleItems(Collection<ScheduleItem> scheduleItems) {
            this.scheduleItems = scheduleItems;
            return this;
        }

        public Builder withGroups(String... groups) {
            String joint = Joiner.on(", ").join(groups);
            this.groups = joint;
            return this;
        }

        public ScheduleProjectHelper build() {
            ScheduleProjectHelper helper;
            if (scheduleItems != null) {
                helper = ScheduleProjectHelper.fromScheduleItems(scheduleItems);
            } else {
                helper = ScheduleProjectHelper.from(newArrayList(components), groups);
            }

            return helper;
        }

    }


}
