package com.ericsson.cifwk.taf.executor.api;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class TriggeringTaskBuilderTest {

    private static final String SUT_RESOURCE = "host.ms1.ip=192.168.0.42\\nhost.ms1.user.root.pass=12shroot\\nhost.ms1.user.root.type=admin";
    private static final String GLOBAL_PROPERTIES = "myGlobalProperty=myGlobalValue";

    @Test
    public void testBuild() throws Exception {
        TriggeringTaskBuilder unit = new TriggeringTaskBuilder();

        Properties schedule1TestProperties = new Properties();
        schedule1TestProperties.put("myProperty", "myValue");

        Properties schedule2TestProperties = new Properties();
        schedule2TestProperties.put("myProperty2", "myValue2");
        schedule2TestProperties.put("myProperty3", "myValue3");

        Properties globalTestProperties = new Properties();
        globalTestProperties.put("myGlobalProperty", "myGlobalValue");

        TriggeringTask task = unit
                .withCiFwkPackage("ciPkg.groupId", "ciPkg.artifactId", "1.0.0")
                .withNexusURI("http://nexus")
                .withSchedule("schedule.groupId", "schedule.artifactId", "1.0.1", "schedules/success.xml")
                .withSchedule("schedule.groupId", "schedule.artifactId", "1.0.1", "schedules/success.xml", schedule1TestProperties)
                .withSchedule("schedule.groupId", "schedule.artifactId", "1.0.1", "schedules/complex.xml", schedule2TestProperties)
                .withSlaveHost(getSlaveHost("tafexe1", 22, "root", "shroot"))
                .withSlaveHost(getSlaveHost("jenkinss1", 22, "root", "shroot"))
                .withTestWare("testware.groupId", "testware.artifactId1", "2.0.0")
                .withTestWare("testware.groupId", "testware.artifactId2", "2.0.0")
                .withSutResource(SUT_RESOURCE)
                .withTestProperties(globalTestProperties)
                .withTestTriggerDetails("isoVersion", "1.4.1")
                .build();

        Assert.assertThat(task.getTestWare(), Matchers.hasSize(2));

        List<ScheduleRequest> schedules = task.getSchedules();
        Assert.assertThat(schedules, Matchers.hasSize(3));
        Assert.assertThat(schedules.get(0).getName(), equalTo("schedules/success.xml"));

        ScheduleRequest schedule2 = schedules.get(1);
        Assert.assertThat(schedule2.getName(), equalTo("schedules/success.xml"));
        Assert.assertEquals("myValue", schedule2.getTestProperties().getProperty("myProperty"));

        ScheduleRequest schedule3 = schedules.get(2);
        Assert.assertThat(schedule3.getName(), containsString("schedules/complex.xml"));
        Assert.assertEquals("myValue2", schedule3.getTestProperties().getProperty("myProperty2"));
        Assert.assertEquals("myValue3", schedule3.getTestProperties().getProperty("myProperty3"));

        Assert.assertThat(task.getCiFwkPackages(), Matchers.hasSize(1));

        ArmInfo armInfo = task.getArmInfo();
        Assert.assertThat(armInfo.getHttpString(), equalTo("http://nexus"));

        Assert.assertThat(task.getSlaveHosts(), Matchers.hasSize(2));

        Assert.assertThat(task.getSutResource(), equalTo(SUT_RESOURCE));
        Assert.assertThat(TriggeringTaskBuilder.propertyMapToString(task.getGlobalTestProperties()), equalTo(GLOBAL_PROPERTIES));

        Assert.assertThat(task.getTestTriggerDetails().size(), equalTo(1));
        Assert.assertThat(task.getTestTriggerDetails().getProperty("isoVersion"), equalTo("1.4.1"));
    }

    private Host getSlaveHost(String ip, int port, String userName, String password) {
        return new Host(ip, ip, port, userName, password);
    }
}