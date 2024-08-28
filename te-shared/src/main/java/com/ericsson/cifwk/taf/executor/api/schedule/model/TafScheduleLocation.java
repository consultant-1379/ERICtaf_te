package com.ericsson.cifwk.taf.executor.api.schedule.model;

public class TafScheduleLocation implements ScheduleLocation {

    private final long scheduleId;

    public TafScheduleLocation(long scheduleId) {
        this.scheduleId = scheduleId;
    }

    @Override
    public String toString() {
        return "TAF Schedule, ID=" + scheduleId;
    }

    public long getScheduleId() {
        return scheduleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TafScheduleLocation that = (TafScheduleLocation) o;

        if (scheduleId != that.scheduleId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (scheduleId ^ (scheduleId >>> 32));
    }
}
