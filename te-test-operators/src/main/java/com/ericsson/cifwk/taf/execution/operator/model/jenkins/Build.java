package com.ericsson.cifwk.taf.execution.operator.model.jenkins;

import com.google.gson.annotations.Expose;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Generated("org.jsonschema2pojo")
public class Build {

    @Expose
    private List<BuildAction> actions = new ArrayList<BuildAction>();
    @Expose
    private List<Object> artifacts = new ArrayList<Object>();
    @Expose
    private Boolean building;
    @Expose
    private Object description;
    @Expose
    private Integer duration;
    @Expose
    private Integer estimatedDuration;
    @Expose
    private Object executor;
    @Expose
    private String fullDisplayName;
    @Expose
    private String id;
    @Expose
    private Boolean keepLog;
    @Expose
    private Integer number;
    @Expose
    private String result;
    @Expose
    private Date timestamp;
    @Expose
    private String url;
    @Expose
    private String builtOn;
    @Expose
    private ChangeSet changeSet;
    @Expose
    private List<Object> culprits = new ArrayList<Object>();

    public List<BuildAction> getActions() {
        return actions;
    }

    public void setActions(List<BuildAction> actions) {
        this.actions = actions;
    }

    public List<Object> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<Object> artifacts) {
        this.artifacts = artifacts;
    }

    public Boolean getBuilding() {
        return building;
    }

    public void setBuilding(Boolean building) {
        this.building = building;
    }

    public Object getDescription() {
        return description;
    }

    public void setDescription(Object description) {
        this.description = description;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(Integer estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public Object getExecutor() {
        return executor;
    }

    public void setExecutor(Object executor) {
        this.executor = executor;
    }

    public String getFullDisplayName() {
        return fullDisplayName;
    }

    public void setFullDisplayName(String fullDisplayName) {
        this.fullDisplayName = fullDisplayName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getKeepLog() {
        return keepLog;
    }

    public void setKeepLog(Boolean keepLog) {
        this.keepLog = keepLog;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getUrl() {
        return url;
    }

    public String getFullConsoleTextLogUrl() {
        return url + "consoleText";
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBuiltOn() {
        return builtOn;
    }

    public void setBuiltOn(String builtOn) {
        this.builtOn = builtOn;
    }

    public ChangeSet getChangeSet() {
        return changeSet;
    }

    public void setChangeSet(ChangeSet changeSet) {
        this.changeSet = changeSet;
    }

    public List<Object> getCulprits() {
        return culprits;
    }

    public void setCulprits(List<Object> culprits) {
        this.culprits = culprits;
    }

    @Override
    public String toString() {
        return "Build{" +
                "actions=" + actions +
                ", artifacts=" + artifacts +
                ", building=" + building +
                ", description=" + description +
                ", duration=" + duration +
                ", estimatedDuration=" + estimatedDuration +
                ", executor=" + executor +
                ", fullDisplayName='" + fullDisplayName + '\'' +
                ", id='" + id + '\'' +
                ", keepLog=" + keepLog +
                ", number=" + number +
                ", result='" + result + '\'' +
                ", timestamp=" + timestamp +
                ", url='" + url + '\'' +
                ", builtOn='" + builtOn + '\'' +
                ", changeSet=" + changeSet +
                ", culprits=" + culprits +
                '}';
    }
}
