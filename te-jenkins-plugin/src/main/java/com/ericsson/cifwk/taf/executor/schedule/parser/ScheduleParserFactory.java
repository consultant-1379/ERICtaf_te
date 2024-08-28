package com.ericsson.cifwk.taf.executor.schedule.parser;

import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleLoader;
import org.simpleframework.xml.core.Persister;

public class ScheduleParserFactory {

    public ScheduleParser create(ScheduleLoader loader) {
        return new ScheduleParser(new Persister(), loader);
    }

}
