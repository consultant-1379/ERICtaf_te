package com.ericsson.cifwk.taf.executor.api;

import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class TriggeringTask implements Serializable {

    private static final long serialVersionUID = -6461031962485962864L;

    private Set<Host> slaveHosts = Sets.newHashSet();
    private Set<ArtifactInfo> ciFwkPackages;
    private Set<ArtifactInfo> testWare;
    private String sutResource;
    private ArmInfo armInfo;
    private Properties globalTestProperties;
    private List<ScheduleRequest> schedules;
    private Properties testTriggerDetails;
    private String minTafVersion;
    private String userDefinedGAVs;
    private String miscProperties;
    private String jobType;
    private String tafSchedulerAddress;
    private String enableLdap;
    private String teUsername;
    private String tePassword;

    public Set<ArtifactInfo> getCiFwkPackages() {
        return ciFwkPackages;
    }

    public void setCiFwkPackages(Set<ArtifactInfo> ciFwkPackages) {
        this.ciFwkPackages = ciFwkPackages;
    }

    public String getSutResource() {
        return sutResource;
    }

    public void setSutResource(String sutResource) {
        this.sutResource = sutResource;
    }

    public void setEnableLdap(String enableLdap){
        this.enableLdap = enableLdap;
    }

    public String getEnableLdap(){
        return enableLdap;
    }

    public void setTeUsername(String teUsername){
        this.teUsername = teUsername;
    }

    public String getTeUsername(){
        return teUsername;
    }

    public void setTePassword(String tePassword){
        this.tePassword = tePassword;
    }

    public String getTePassword(){
        return tePassword;
    }
    public ArmInfo getArmInfo() {
        return armInfo;
    }

    public void setArmInfo(ArmInfo armInfo) {
        this.armInfo = armInfo;
    }

    public Properties getGlobalTestProperties() {
        return globalTestProperties;
    }

    public void setGlobalTestProperties(Properties globalTestProperties) {
        this.globalTestProperties = globalTestProperties;
    }

    public Set<ArtifactInfo> getTestWare() {
        return testWare;
    }

    public void setTestWare(Set<ArtifactInfo> testWare) {
        this.testWare = testWare;
    }

    public Set<Host> getSlaveHosts() {
        return slaveHosts;
    }

    public void setSlaveHosts(Set<Host> slaveHosts) {
        this.slaveHosts = slaveHosts;
    }

    public List<ScheduleRequest> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<ScheduleRequest> schedules) {
        this.schedules = schedules;
    }

    public Properties getTestTriggerDetails() {
        return testTriggerDetails;
    }

    public void setTestTriggerDetails(Properties testTriggerDetails) {
        this.testTriggerDetails = testTriggerDetails;
    }

    public void setMinTafVersion(String minTafVersion) {
        this.minTafVersion = minTafVersion;
    }

    public void setUserDefinedGAVs(String userDefinedGAVs) {
        this.userDefinedGAVs = userDefinedGAVs;
    }

    public String getMinTafVersion() {
        return minTafVersion;
    }

    public String getUserDefinedGAVs() {
        return userDefinedGAVs;
    }

    public String getMiscProperties() {
        return miscProperties;
    }

    public void setMiscProperties(String miscProperties) {
        this.miscProperties = miscProperties;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public void setTafSchedulerAddress(String tafSchedulerAddress) {
        this.tafSchedulerAddress = tafSchedulerAddress;
    }

    public String getTafSchedulerAddress() {
        return tafSchedulerAddress;
    }
}
