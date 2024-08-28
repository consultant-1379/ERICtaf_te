package com.ericsson.cifwk.taf.executor.schedule.parser.xml;

import java.util.List;

public interface ScheduleChildNode {

    <T> T accept(Visitor<T> visitor);

    interface Visitor<T> {

        T visitItem(String name, String component, String suites,
                    String groups, String agentLabel, boolean stopOnFail, Integer timeoutInSeconds,
                    List<EnvironmentProperty> environmentProperties);

        T visitItemGroup(List<ScheduleChildNode> children, boolean parallel, List<EnvironmentProperty> environmentProperties);

        T visitInclude(String include);

    }
}
