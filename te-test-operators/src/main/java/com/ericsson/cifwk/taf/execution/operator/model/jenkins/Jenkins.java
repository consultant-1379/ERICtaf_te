
package com.ericsson.cifwk.taf.execution.operator.model.jenkins;

import com.google.gson.annotations.Expose;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")
public class Jenkins {

    @Expose
    private List<AssignedLabel> assignedLabels = new ArrayList<AssignedLabel>();
    @Expose
    private String mode;
    @Expose
    private String nodeDescription;
    @Expose
    private String nodeName;
    @Expose
    private Integer numExecutors;
    @Expose
    private Object description;
    @Expose
    private List<JobReference> jobs = new ArrayList<JobReference>();
    @Expose
    private Load overallLoad;
    @Expose
    private ViewReference primaryView;
    @Expose
    private Boolean quietingDown;
    @Expose
    private Integer slaveAgentPort;
    @Expose
    private Load unlabeledLoad;
    @Expose
    private Boolean useCrumbs;
    @Expose
    private Boolean useSecurity;
    @Expose
    private List<ViewReference> views = new ArrayList<ViewReference>();

    public List<AssignedLabel> getAssignedLabels() {
        return assignedLabels;
    }

    public void setAssignedLabels(List<AssignedLabel> assignedLabels) {
        this.assignedLabels = assignedLabels;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getNodeDescription() {
        return nodeDescription;
    }

    public void setNodeDescription(String nodeDescription) {
        this.nodeDescription = nodeDescription;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Integer getNumExecutors() {
        return numExecutors;
    }

    public void setNumExecutors(Integer numExecutors) {
        this.numExecutors = numExecutors;
    }

    public Object getDescription() {
        return description;
    }

    public void setDescription(Object description) {
        this.description = description;
    }

    public List<JobReference> getJobs() {
        return jobs;
    }

    public void setJobs(List<JobReference> jobs) {
        this.jobs = jobs;
    }

    public Load getOverallLoad() {
        return overallLoad;
    }

    public void setOverallLoad(Load overallLoad) {
        this.overallLoad = overallLoad;
    }

    public ViewReference getPrimaryView() {
        return primaryView;
    }

    public void setPrimaryView(ViewReference primaryView) {
        this.primaryView = primaryView;
    }

    public Boolean getQuietingDown() {
        return quietingDown;
    }

    public void setQuietingDown(Boolean quietingDown) {
        this.quietingDown = quietingDown;
    }

    public Integer getSlaveAgentPort() {
        return slaveAgentPort;
    }

    public void setSlaveAgentPort(Integer slaveAgentPort) {
        this.slaveAgentPort = slaveAgentPort;
    }

    public Load getUnlabeledLoad() {
        return unlabeledLoad;
    }

    public void setUnlabeledLoad(Load unlabeledLoad) {
        this.unlabeledLoad = unlabeledLoad;
    }

    public Boolean getUseCrumbs() {
        return useCrumbs;
    }

    public void setUseCrumbs(Boolean useCrumbs) {
        this.useCrumbs = useCrumbs;
    }

    public Boolean getUseSecurity() {
        return useSecurity;
    }

    public void setUseSecurity(Boolean useSecurity) {
        this.useSecurity = useSecurity;
    }

    public List<ViewReference> getViews() {
        return views;
    }

    public void setViews(List<ViewReference> views) {
        this.views = views;
    }

    @Override
    public String toString() {
        return "Jenkins{" +
                "assignedLabels=" + assignedLabels +
                ", mode='" + mode + '\'' +
                ", nodeDescription='" + nodeDescription + '\'' +
                ", nodeName='" + nodeName + '\'' +
                ", numExecutors=" + numExecutors +
                ", description=" + description +
                ", jobs=" + jobs +
                ", overallLoad=" + overallLoad +
                ", primaryView=" + primaryView +
                ", quietingDown=" + quietingDown +
                ", slaveAgentPort=" + slaveAgentPort +
                ", unlabeledLoad=" + unlabeledLoad +
                ", useCrumbs=" + useCrumbs +
                ", useSecurity=" + useSecurity +
                ", views=" + views +
                '}';
    }
}
