
package com.ericsson.cifwk.taf.execution.operator.model.jenkins;

import com.google.gson.annotations.Expose;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")
public class Job {

    @Expose
    private List<JobAction> actions = new ArrayList<>();
    @Expose
    private String description;
    @Expose
    private String displayName;
    @Expose
    private Object displayNameOrNull;
    @Expose
    private String name;
    @Expose
    private String url;
    @Expose
    private Boolean buildable;
    @Expose
    private List<Build> builds = new ArrayList<>();
    @Expose
    private String color;
    @Expose
    private BuildReference firstBuild;
    @Expose
    private List<HealthReport> healthReport = new ArrayList<>();
    @Expose
    private Boolean inQueue;
    @Expose
    private Boolean keepDependencies;
    @Expose
    private BuildReference lastBuild;
    @Expose
    private BuildReference lastCompletedBuild;
    @Expose
    private BuildReference lastFailedBuild;
    @Expose
    private BuildReference lastStableBuild;
    @Expose
    private BuildReference lastSuccessfulBuild;
    @Expose
    private BuildReference lastUnstableBuild;
    @Expose
    private BuildReference lastUnsuccessfulBuild;
    @Expose
    private Integer nextBuildNumber;
    @Expose
    private List<Property> property = new ArrayList<>();
    @Expose
    private Object queueItem;
    @Expose
    private Boolean concurrentBuild;
    @Expose
    private List<Object> downstreamProjects = new ArrayList<>();
    @Expose
    private Scm scm;
    @Expose
    private List<Object> upstreamProjects = new ArrayList<Object>();

    public List<JobAction> getActions() {
        return actions;
    }

    public void setActions(List<JobAction> actions) {
        this.actions = actions;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Object getDisplayNameOrNull() {
        return displayNameOrNull;
    }

    public void setDisplayNameOrNull(Object displayNameOrNull) {
        this.displayNameOrNull = displayNameOrNull;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getBuildable() {
        return buildable;
    }

    public void setBuildable(Boolean buildable) {
        this.buildable = buildable;
    }

    public List<Build> getBuilds() {
        return builds;
    }

    public void setBuilds(List<Build> builds) {
        this.builds = builds;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public BuildReference getFirstBuild() {
        return firstBuild;
    }

    public void setFirstBuild(BuildReference firstBuild) {
        this.firstBuild = firstBuild;
    }

    public List<HealthReport> getHealthReport() {
        return healthReport;
    }

    public void setHealthReport(List<HealthReport> healthReport) {
        this.healthReport = healthReport;
    }

    public Boolean getInQueue() {
        return inQueue;
    }

    public void setInQueue(Boolean inQueue) {
        this.inQueue = inQueue;
    }

    public Boolean getKeepDependencies() {
        return keepDependencies;
    }

    public void setKeepDependencies(Boolean keepDependencies) {
        this.keepDependencies = keepDependencies;
    }

    public BuildReference getLastBuild() {
        return lastBuild;
    }

    public void setLastBuild(BuildReference lastBuild) {
        this.lastBuild = lastBuild;
    }

    public BuildReference getLastCompletedBuild() {
        return lastCompletedBuild;
    }

    public void setLastCompletedBuild(BuildReference lastCompletedBuild) {
        this.lastCompletedBuild = lastCompletedBuild;
    }

    public BuildReference getLastFailedBuild() {
        return lastFailedBuild;
    }

    public void setLastFailedBuild(BuildReference lastFailedBuild) {
        this.lastFailedBuild = lastFailedBuild;
    }

    public BuildReference getLastStableBuild() {
        return lastStableBuild;
    }

    public void setLastStableBuild(BuildReference lastStableBuild) {
        this.lastStableBuild = lastStableBuild;
    }

    public BuildReference getLastSuccessfulBuild() {
        return lastSuccessfulBuild;
    }

    public void setLastSuccessfulBuild(BuildReference lastSuccessfulBuild) {
        this.lastSuccessfulBuild = lastSuccessfulBuild;
    }

    public BuildReference getLastUnstableBuild() {
        return lastUnstableBuild;
    }

    public void setLastUnstableBuild(BuildReference lastUnstableBuild) {
        this.lastUnstableBuild = lastUnstableBuild;
    }

    public BuildReference getLastUnsuccessfulBuild() {
        return lastUnsuccessfulBuild;
    }

    public void setLastUnsuccessfulBuild(BuildReference lastUnsuccessfulBuild) {
        this.lastUnsuccessfulBuild = lastUnsuccessfulBuild;
    }

    public Integer getNextBuildNumber() {
        return nextBuildNumber;
    }

    public void setNextBuildNumber(Integer nextBuildNumber) {
        this.nextBuildNumber = nextBuildNumber;
    }

    public List<Property> getProperty() {
        return property;
    }

    public void setProperty(List<Property> property) {
        this.property = property;
    }

    public Object getQueueItem() {
        return queueItem;
    }

    public void setQueueItem(Object queueItem) {
        this.queueItem = queueItem;
    }

    public Boolean getConcurrentBuild() {
        return concurrentBuild;
    }

    public void setConcurrentBuild(Boolean concurrentBuild) {
        this.concurrentBuild = concurrentBuild;
    }

    public List<Object> getDownstreamProjects() {
        return downstreamProjects;
    }

    public void setDownstreamProjects(List<Object> downstreamProjects) {
        this.downstreamProjects = downstreamProjects;
    }

    public Scm getScm() {
        return scm;
    }

    public void setScm(Scm scm) {
        this.scm = scm;
    }

    public List<Object> getUpstreamProjects() {
        return upstreamProjects;
    }

    public void setUpstreamProjects(List<Object> upstreamProjects) {
        this.upstreamProjects = upstreamProjects;
    }

    @Override
    public String toString() {
        return "Job{" +
                "actions=" + actions +
                ", description='" + description + '\'' +
                ", displayName='" + displayName + '\'' +
                ", displayNameOrNull=" + displayNameOrNull +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", buildable=" + buildable +
                ", builds=" + builds +
                ", color='" + color + '\'' +
                ", firstBuild=" + firstBuild +
                ", healthReport=" + healthReport +
                ", inQueue=" + inQueue +
                ", keepDependencies=" + keepDependencies +
                ", lastBuild=" + lastBuild +
                ", lastCompletedBuild=" + lastCompletedBuild +
                ", lastFailedBuild=" + lastFailedBuild +
                ", lastStableBuild=" + lastStableBuild +
                ", lastSuccessfulBuild=" + lastSuccessfulBuild +
                ", lastUnstableBuild=" + lastUnstableBuild +
                ", lastUnsuccessfulBuild=" + lastUnsuccessfulBuild +
                ", nextBuildNumber=" + nextBuildNumber +
                ", property=" + property +
                ", queueItem=" + queueItem +
                ", concurrentBuild=" + concurrentBuild +
                ", downstreamProjects=" + downstreamProjects +
                ", scm=" + scm +
                ", upstreamProjects=" + upstreamProjects +
                '}';
    }
}
