package com.ericsson.cifwk.taf.executor.api.schedule.model;

public class ScheduleGavLocation implements ScheduleLocation {

    private final ScheduleComponent component;
    private final String name;

    public ScheduleGavLocation(ScheduleComponent component, String name) {
        this.component = component;
        this.name = name;
    }

    public static ScheduleGavLocation of(String groupId, String artifactId, String name) {
        return new ScheduleGavLocation(new ScheduleComponent(groupId, artifactId), name);
    }

    public static ScheduleGavLocation ofArtifact(String artifact, String name) {
        ScheduleComponent component = ScheduleComponent.parseGav(artifact);
        return new ScheduleGavLocation(component, name);
    }

    public ScheduleComponent getComponent() {
        return component;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScheduleGavLocation that = (ScheduleGavLocation) o;

        if (!component.equals(that.component)) return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = component.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return component.toString() + '/' + name;
    }
}
