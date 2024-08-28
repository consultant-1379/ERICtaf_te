package com.ericsson.cifwk.taf.executor.schedule.parser;

import com.ericsson.cifwk.taf.executor.schedule.ScheduleException;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleLocation;
import com.google.common.base.Preconditions;

import java.util.*;

public class ScheduleIncludeStack {

    private final Set<ScheduleLocation> index = new HashSet<>();
    private final Deque<ScheduleLocation> stack = new LinkedList<>();

    public ScheduleIncludeStack(ScheduleLocation root) {
        push(root);
    }

    public void push(ScheduleLocation location) {
        Preconditions.checkNotNull(location, "Location cannot be null");
        if (!index.add(location)) {
            String message = String.format("Recursive include: %s in %s", location, stack);
            throw new ScheduleException(message);
        }
        stack.push(location);
    }

    public ScheduleLocation pop() {
        if (stack.size() <= 1) {
            throw new ScheduleException("Removing root location is not allowed");
        }
        ScheduleLocation location = stack.pop();
        index.remove(location);
        return location;
    }

}
