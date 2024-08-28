package com.ericsson.cifwk.taf.executor.schedule.parser.xml;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;

import java.util.List;

abstract class BaseItemGroupNode extends EnvironmentPropertiesHolder {

    @ElementListUnion({
            @ElementList(type = ItemNode.class, inline = true),
            @ElementList(type = ItemGroupNode.class, inline = true),
            @ElementList(type = IncludeNode.class, inline = true)
    })
    protected List<ScheduleChildNode> children;

    public List<ScheduleChildNode> getChildren() {
        return children;
    }

    public void setChildren(List<ScheduleChildNode> children) {
        this.children = children;
    }

}
