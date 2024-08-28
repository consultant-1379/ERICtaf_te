package com.ericsson.cifwk.taf.executor.schedule.parser.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "item-group")
public final class ItemGroupNode extends BaseItemGroupNode implements ScheduleChildNode {

    @Attribute(required = false)
    private boolean parallel;

    public boolean isParallel() {
        return parallel;
    }

    public void setParallel(boolean parallel) {
        this.parallel = parallel;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitItemGroup(children, parallel, environmentProperties);
    }
}
