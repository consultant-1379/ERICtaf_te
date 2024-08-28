package com.ericsson.cifwk.taf.executor.api.schedule.model;

import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 15/02/2016
 */
public abstract class AbstractEnvironmentPropertiesAwareChild implements ScheduleChild, EnvironmentPropertiesAwareItem {

    @Override
    public List<ScheduleEnvironmentProperty> getEffectiveEnvironmentProperties() {
        Stack<ScheduleChild> propertyHolderStack = new Stack<>();
        populatePropertyHolderStack(this, propertyHolderStack);

        Map<String, ScheduleEnvironmentProperty> buffer = new HashMap<>();
        while (!propertyHolderStack.empty()) {
            ScheduleChild node = propertyHolderStack.pop();
            if (node instanceof EnvironmentPropertiesAwareItem) {
                List<ScheduleEnvironmentProperty> properties = ((EnvironmentPropertiesAwareItem)node).getDefinedEnvironmentProperties();
                if (properties != null) {
                    for (ScheduleEnvironmentProperty property : properties) {
                        buffer.put(property.getType() + "_" + property.getKey(), property);
                    }
                }
            }
        }

        return Lists.newArrayList(buffer.values());
    }

    private void populatePropertyHolderStack(ScheduleChild currentNode, Stack<ScheduleChild> propertyHolderStack) {
        if (currentNode != null && currentNode instanceof EnvironmentPropertiesAwareItem) {
            propertyHolderStack.push(currentNode);
            ScheduleChild parent = currentNode.getParent();
            populatePropertyHolderStack(parent, propertyHolderStack);
        }
    }
}
