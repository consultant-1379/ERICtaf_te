package com.ericsson.cifwk.taf.executor.api.schedule.model;

import java.util.List;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 17/02/2016
 */
public interface EnvironmentPropertiesAwareItem {

    List<ScheduleEnvironmentProperty> getDefinedEnvironmentProperties();

    List<ScheduleEnvironmentProperty> getEffectiveEnvironmentProperties();

}
