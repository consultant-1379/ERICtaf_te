package com.ericsson.cifwk.taf.executor.mbean;

import com.ericsson.cifwk.taf.executor.TAFExecutor;

public interface TafAgentMBean extends TAFExecutor {

    public static final String MBEAN_NAME = "com.ericsson:type=TafAgent";

    String exec(String[] testware, String[] suites);

}
