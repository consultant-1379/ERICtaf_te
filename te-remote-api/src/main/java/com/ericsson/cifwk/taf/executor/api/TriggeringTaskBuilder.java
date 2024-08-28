package com.ericsson.cifwk.taf.executor.api;

import com.ericsson.cifwk.taf.executor.commons.MultilinePropertiesConverter;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class TriggeringTaskBuilder {

    private Set<Host> slaves = Sets.newLinkedHashSet();
    private Set<ArtifactInfo> testWares = Sets.newLinkedHashSet();
    private Set<ArtifactInfo> ciFwkPackages = Sets.newLinkedHashSet();
    private String nexusURI;
    private String sutResource;
    private ArmInfo armInfo;
    private Properties testProperties;
    private List<ScheduleRequest> schedules = Lists.newArrayList();
    private Properties testTriggerDetails = new Properties();
    private String minTafVersion;
    private String userDefinedGAVs;
    private String miscProperties;
    private String jobType;
    private String enableLdap;
    private String teUsername;
    private String tePassword;
    private String tafSchedulerAddress;

    public TriggeringTaskBuilder() {
    }

    public TriggeringTaskBuilder withNexusURI(String nexusURI) {
        this.nexusURI = nexusURI;
        return this;
    }

    public TriggeringTaskBuilder withArm(ArmInfo armInfo) {
        this.armInfo = armInfo;
        return this;
    }

    public TriggeringTaskBuilder withSlaveHosts(Collection<Host> hosts) {
        slaves.addAll(hosts);
        return this;
    }

    public TriggeringTaskBuilder withSlaveHost(Host host) {
        slaves.add(host);
        return this;
    }

    public TriggeringTaskBuilder withSlaveHost(String slave) {
        slaves.add(new Host(slave, slave, 22, "", ""));
        return this;
    }

    public TriggeringTaskBuilder withCiFwkPackages(Collection<ArtifactInfo> artifactGavs) {
        for (ArtifactInfo artifactGav : artifactGavs) {
            withCiFwkPackage(artifactGav);
        }
        return this;
    }

    public TriggeringTaskBuilder withCiFwkPackage(String groupId, String artifactId, String packageVersion) {
        return withCiFwkPackage(new ArtifactInfo(groupId, artifactId, packageVersion));
    }

    public TriggeringTaskBuilder withCiFwkPackage(ArtifactInfo artifactGav) {
        ciFwkPackages.add(artifactGav);
        return this;
    }

    public TriggeringTaskBuilder withTestWares(Collection<ArtifactInfo> artifactGavs) {
        for (ArtifactInfo artifactGav : artifactGavs) {
            withTestWare(artifactGav);
        }
        return this;
    }

    public TriggeringTaskBuilder withTestWare(String groupId, String artifactId, String version) {
        return withTestWare(new ArtifactInfo(groupId, artifactId, version));
    }

    public TriggeringTaskBuilder withTestWare(ArtifactInfo artifactGav) {
        testWares.add(artifactGav);
        return this;
    }

    public TriggeringTaskBuilder withSutResource(String sutResource) {
        this.sutResource = sutResource;
        return this;
    }

    public TriggeringTaskBuilder withTestProperties(Properties testProperties) {
        this.testProperties = testProperties;
        return this;
    }

    public TriggeringTaskBuilder withMinTafVersion(String minTafVersion) {
        this.minTafVersion = minTafVersion;
        return this;
    }

    public TriggeringTaskBuilder withEnableLdap(String enableLdap){
        this.enableLdap = enableLdap;
        return this;
    }

    public TriggeringTaskBuilder withTeUsername(String teUsername){
        this.teUsername = teUsername;
        return this;
    }

    public TriggeringTaskBuilder withTePassword(String tePassword){
        this.tePassword = tePassword;
        return this;
    }


    public TriggeringTaskBuilder withUserDefinedGAVs(String userDefinedGAVs) {
        this.userDefinedGAVs = userDefinedGAVs;
        return this;
    }

    public TriggeringTaskBuilder withSchedules(Collection<ScheduleRequest> schedules) {
        for (ScheduleRequest schedule : schedules) {
            withSchedule(schedule);
        }
        return this;
    }

    public TriggeringTaskBuilder withSchedule(ScheduleRequest schedule) {
        Preconditions.checkArgument(schedule.isComplete(), "Schedule information is incomplete");
        this.schedules.add(schedule);
        return this;
    }

    public TriggeringTaskBuilder withSchedule(ArtifactInfo artifactGav, String internalPathToSchedule,
                                              Properties scheduleTestProperties) {
        return withSchedule(new ScheduleRequest(internalPathToSchedule, artifactGav.getGroupId(),
                artifactGav.getArtifactId(), artifactGav.getVersion(), scheduleTestProperties));
    }

    public TriggeringTaskBuilder withSchedule(String groupId, String artifactId, String version,
                                              String internalPathToSchedule) {
        return withSchedule(new ScheduleRequest(internalPathToSchedule, groupId, artifactId, version, new Properties()));
    }

    public TriggeringTaskBuilder withSchedule(String groupId, String artifactId, String version,
                                              String internalPathToSchedule, Properties scheduleTestProperties) {
        return withSchedule(new ScheduleRequest(internalPathToSchedule, groupId, artifactId, version, scheduleTestProperties));
    }

    public TriggeringTaskBuilder withTestTriggerDetails(Properties properties) {
        this.testTriggerDetails.putAll(properties);
        return this;
    }

    public TriggeringTaskBuilder withTestTriggerDetails(String key, String value) {
        this.testTriggerDetails.put(key, value);
        return this;
    }

    public static String propertyMapToString(Map<?, ?> propertyMap) {
        return MultilinePropertiesConverter.propertyMapToString(propertyMap);
    }

    public static Map<?, ?> stringToPropertyMap(String propertiesAsString) {
        return MultilinePropertiesConverter.stringToPropertyMap(propertiesAsString);
    }

    public TriggeringTaskBuilder withMiscProperties(Map<String, String> miscProperties) {
        this.miscProperties = propertyMapToString(miscProperties);
        return this;
    }

    public TriggeringTaskBuilder withJobType(String jobType) {
        this.jobType = jobType;
        return this;
    }

    public TriggeringTaskBuilder withTafSchedulerAddress(String tafSchedulerAddress) {
        this.tafSchedulerAddress = tafSchedulerAddress;
        return this;
    }

    public TriggeringTask build() {
        TriggeringTask result = new TriggeringTask();
        if (armInfo == null) {
            armInfo = new ArmInfo();
            armInfo.setHttp(nexusURI);
        }
        result.setArmInfo(armInfo);
        result.setSlaveHosts(slaves);
        result.setSutResource(sutResource);
        result.setGlobalTestProperties(testProperties);
        result.setTestWare(testWares);
        result.setCiFwkPackages(ciFwkPackages);
        result.setSchedules(schedules);
        result.setTestTriggerDetails(testTriggerDetails);
        result.setMinTafVersion(minTafVersion);
        result.setUserDefinedGAVs(userDefinedGAVs);
        result.setMiscProperties(miscProperties);
        result.setJobType(jobType);
        result.setTafSchedulerAddress(tafSchedulerAddress);
        result.setEnableLdap(enableLdap);
        result.setTeUsername(teUsername);
        result.setTePassword(tePassword);

        return result;
    }

}
