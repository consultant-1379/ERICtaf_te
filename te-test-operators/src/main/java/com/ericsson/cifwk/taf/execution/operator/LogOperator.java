package com.ericsson.cifwk.taf.execution.operator;

import com.ericsson.cifwk.taf.executor.api.TafTeJenkinsJob;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelJobStartedEvent;

public interface LogOperator {

    String getAllureLogUrl(EiffelJobStartedEvent jobStartedEvent);

    String[] getTeConsoleLogs(String logDirectory);

    boolean isDirectory(String directory);

    String[] getAllureLogXmls(String logDirectory);

    boolean allureLogIndexExists(String logDirectory);

    void closeShell();

    void verifyTeBuildLog(TafTeJenkinsJob build, TeLogVisitor visitor);
}
