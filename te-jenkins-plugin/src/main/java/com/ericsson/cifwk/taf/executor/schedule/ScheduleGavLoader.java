package com.ericsson.cifwk.taf.executor.schedule;

import com.ericsson.cifwk.taf.executor.ArtifactHelper;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleComponent;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleGavLocation;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleLoader;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleLocation;
import com.google.common.base.Preconditions;

public class ScheduleGavLoader implements ScheduleLoader {

    private final String repositoryUrl;
    private final ScheduleItemGavResolver scheduleItemGavResolver;
    private final ArtifactHelper artifactHelper;

    public ScheduleGavLoader(String repositoryUrl, ScheduleItemGavResolver scheduleItemGavResolver, ArtifactHelper artifactHelper) {
        this.artifactHelper = artifactHelper;
        this.scheduleItemGavResolver = scheduleItemGavResolver;
        this.repositoryUrl = repositoryUrl;
    }

    public String load(ScheduleLocation location) {
        Preconditions.checkArgument(location instanceof ScheduleGavLocation, "expected instance of " + ScheduleGavLocation.class.getName());

        ScheduleGavLocation scheduleGavLocation = (ScheduleGavLocation) location;
        ScheduleComponent component = scheduleGavLocation.getComponent();
        String name = scheduleGavLocation.getName();
        ScheduleComponent resolvedComponent = scheduleItemGavResolver.resolve(component);
        String artifactStr = resolvedComponent.toString();
        ArtifactHelper.Artifact artifact = artifactHelper.getArtifact(repositoryUrl, artifactStr);

        return artifact.getArtifactEntry(name);
    }

    @Override
    public ScheduleLocation getScheduleIncludeLocation(String address) {
        int slash = address.indexOf('/');
        String gav = address.substring(0, slash);
        String name = address.substring(slash + 1);
        ScheduleComponent component = getScheduleComponent(gav);
        return new ScheduleGavLocation(component, name);
    }

    public static ScheduleComponent getScheduleComponent(String component) {
        if (ScheduleComponent.isGav(component)) {
            return ScheduleComponent.parseGav(component);
        }
        int dot = component.lastIndexOf(':');
        String groupId = component.substring(0, dot);
        String artifactId = component.substring(dot + 1);
        return new ScheduleComponent(groupId, artifactId);
    }

}
