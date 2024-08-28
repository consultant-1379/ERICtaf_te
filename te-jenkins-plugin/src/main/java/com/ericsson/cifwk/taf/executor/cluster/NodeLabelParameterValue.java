package com.ericsson.cifwk.taf.executor.cluster;

import com.ericsson.cifwk.taf.executor.utils.JenkinsUtils;
import hudson.model.Label;
import hudson.model.ParameterValue;
import hudson.model.queue.SubTask;

public class NodeLabelParameterValue extends ParameterValue {

    private String label;

    public NodeLabelParameterValue(String label) {
        super("", "");
        this.label = label;
    }

    @Override
    public Label getAssignedLabel(SubTask task) {
        return JenkinsUtils.getJenkinsInstance().getLabel(label);
    }

}
