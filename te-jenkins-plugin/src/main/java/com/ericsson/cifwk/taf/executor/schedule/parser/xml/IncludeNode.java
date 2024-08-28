package com.ericsson.cifwk.taf.executor.schedule.parser.xml;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "include")
public final class IncludeNode implements ScheduleChildNode {

    @Text
    private String schedule;

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitInclude(schedule);
    }
}
