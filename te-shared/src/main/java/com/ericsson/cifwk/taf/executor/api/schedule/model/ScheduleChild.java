package com.ericsson.cifwk.taf.executor.api.schedule.model;

import java.util.List;

public interface ScheduleChild {

    <T> T accept(Visitor<T> visitor);

    ScheduleChild getParent();

    public interface Visitor<T> {

        T visitManualTestItem(ManualTestData manualTestData);

        T visitItem(String name, ScheduleComponent component,
                    List<String> suites,
                    List<String> groups,
                    String agentLabel,
                    boolean stopOnFail,
                    Integer timeoutInSeconds,
                    List<ScheduleEnvironmentProperty> environmentProperties);

        T visitItemGroup(List<ScheduleChild> children, boolean parallel);

    }
}
