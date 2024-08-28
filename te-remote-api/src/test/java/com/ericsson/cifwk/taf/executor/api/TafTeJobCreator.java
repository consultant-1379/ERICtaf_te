package com.ericsson.cifwk.taf.executor.api;

public final class TafTeJobCreator {

    public static TafTeJenkinsJob createJob(TafTeJenkinsJob.Type type, int number,
                                                TafTeJenkinsJob.RunStatus runStatus, TafTeJenkinsJob.Result result) {
        String name = type.toString();
        return new TafTeJenkinsJobImpl(type, name, name, number,
                "http://" + name + "/" + number,
                "http://" + name + "/" + number + "/log",
                runStatus, result);
    }
}
