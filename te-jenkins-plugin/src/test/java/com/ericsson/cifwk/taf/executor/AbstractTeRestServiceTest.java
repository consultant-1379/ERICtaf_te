package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.api.Host;
import com.ericsson.cifwk.taf.executor.api.TriggeringTask;
import com.ericsson.cifwk.taf.executor.api.TriggeringTaskBuilder;
import com.ericsson.duraci.datawrappers.ArtifactGav;
import com.ericsson.duraci.datawrappers.BaselinePart;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Properties;

public abstract class AbstractTeRestServiceTest {

    protected final Gson gson = new GsonBuilder().create();

    protected TriggeringTask createTriggeringTask() {
        TriggeringTaskBuilder eventBuilder = new TriggeringTaskBuilder();
        return eventBuilder
                .withCiFwkPackage("ciPkg.groupId", "ciPkg.artifactId", "1.0.0")
                .withNexusURI("http://nexus")
                .withSchedule("schedule.groupId", "schedule.artifactId", "1.0.1", "schedules/success.xml")
                .withSchedule("schedule.groupId", "schedule.artifactId", "1.0.1", "schedules/complex.xml")
                .withSlaveHost(createSlaveHost("tafexe1", 22, "root", "shroot"))
                .withSlaveHost(createSlaveHost("jenkinss1", 22, "root", "shroot"))
                .withTestWare("testware.groupId", "testware.artifactId1", "2.0.0")
                .withTestWare("testware.groupId", "testware.artifactId2", "2.0.0")
                .withSutResource("host.ms1.ip=192.168.0.42")
                .build();
    }

    protected TriggeringTask createTriggeringTaskWithProperties(Properties commonTestProperties,
                                                                Properties schedule2TestProperties) {
        TriggeringTaskBuilder taskBuilder = new TriggeringTaskBuilder();

        return taskBuilder
                .withCiFwkPackage("ciPkg.groupId", "ciPkg.artifactId", "1.0.0")
                .withNexusURI("http://nexus")
                .withSchedule("schedule.groupId", "schedule.artifactId", "1.0.1", "schedules/success.xml")
                .withSchedule("schedule.groupId", "schedule.artifactId", "1.0.1", "schedules/complex.xml", schedule2TestProperties)
                .withSlaveHost(createSlaveHost("tafexe1", 22, "root", "shroot"))
                .withSlaveHost(createSlaveHost("jenkinss1", 22, "root", "shroot"))
                .withTestWare("testware.groupId", "testware.artifactId1", "2.0.0")
                .withTestWare("testware.groupId", "testware.artifactId2", "2.0.0")
                .withSutResource("host.ms1.ip=192.168.0.42")
                .withTestProperties(commonTestProperties)
                .build();
    }

    protected BaselinePart createTestWare(String groupId, String artifactId, String version) {
        return createBaselinePart(groupId, artifactId, version, "TestWare:");
    }

    protected BaselinePart createBaselinePart(String groupId, String artifactId, String version, String tag) {
        ArtifactGav gav = new ArtifactGav(groupId, artifactId, version);
        return new BaselinePart(gav, tag);
    }

    protected Host createSlaveHost(String ip, int port, String userName, String password) {
        return new Host(ip, ip, port, userName, password);
    }

    protected String createSerializedTriggeringTask() {
        return gson.toJson(createTriggeringTask());
    }
}
