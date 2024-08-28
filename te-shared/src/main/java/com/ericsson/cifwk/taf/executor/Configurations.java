package com.ericsson.cifwk.taf.executor;

public interface Configurations {

    String PLUGIN_NAME = "TAF Executor";
    String FLOW_JOB_PREFIX = "TAF_Execution_";
    String LOG_STORAGE = "LOG_STORAGE";

    interface RuntimeLimitations {
        String MAX_THREAD_COUNT = "maxThreadCountInTestware";
    }

}
