package com.ericsson.cifwk.taf.executor.utils;

import com.ericsson.cifwk.taf.executor.api.ArtifactInfo;
import com.ericsson.cifwk.taf.executor.api.Host;
import com.ericsson.cifwk.taf.executor.api.ScheduleRequest;
import com.ericsson.cifwk.taf.executor.api.ScheduleSource;
import com.ericsson.cifwk.taf.executor.api.TriggeringTask;
import com.ericsson.cifwk.taf.executor.api.TriggeringTaskBuilder;
import com.ericsson.duraci.datawrappers.BaselineContext;
import com.ericsson.duraci.datawrappers.BaselinePart;
import com.ericsson.duraci.datawrappers.Environment;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelBaselineDefinedEvent;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class TriggeringEventBuilderTest {

    private static final String SUT_RESOURCE = "host.ms1.ip=192.168.0.42\\nhost.ms1.user.root.pass=12shroot\\nhost.ms1.user.root.type=admin";
    private static final String GLOBAL_PROPERTIES = "{\"myGlobalProperty\":\"myGlobalValue\"}";

    private TriggeringEventBuilder unit = new TriggeringEventBuilder();

    @Test
    public void testBuild() throws Exception {

        TriggeringTask triggeringTask = getTriggeringTask();

        EiffelBaselineDefinedEvent event = unit.build(triggeringTask);

        assertEquals("TestScheduleBaseline", event.getBaselineName());
        Collection<BaselinePart> baselineParts = event.getConsistsOf();

        assertThat(baselineParts, Matchers.hasSize(6));
        assertThat(getTestWares(baselineParts), Matchers.hasSize(2));

        List<BaselinePart> schedules = getSchedules(baselineParts);
        assertThat(schedules, Matchers.hasSize(3));

        assertThat(getScheduleFromTag(schedules.get(0).getTag()).getName(), equalTo("schedules/success.xml"));

        ScheduleRequest scheduleFromTag2 = getScheduleFromTag(schedules.get(1).getTag());
        assertThat(scheduleFromTag2.getName(), containsString("schedules/success.xml"));
        assertEquals("myValue", scheduleFromTag2.getTestProperties().getProperty("myProperty"));

        ScheduleRequest scheduleFromTag3 = getScheduleFromTag(schedules.get(2).getTag());
        assertThat(scheduleFromTag3.getName(), containsString("schedules/complex.xml"));
        assertEquals("myValue2", scheduleFromTag3.getTestProperties().getProperty("myProperty2"));
        assertEquals("myValue3", scheduleFromTag3.getTestProperties().getProperty("myProperty3"));

        assertThat(getCiFwkPackages(baselineParts), Matchers.hasSize(1));

        BaselineContext baselineContext = event.getContext();
        Collection<Environment> environments = baselineContext.getEnvironment();

        assertThat(environments, Matchers.hasSize(3));

        String arm = getArm(environments);
        assertThat(arm, containsString("\"httpString\":\"http://nexus\""));

        assertThat(getSlaves(environments), containsString("\"ipAddress\":\"tafexe1\",\"sshPort\":22,\"credentials\":{\"username\":\"root\",\"password\":\"shroot\"}"));
        assertThat(getSlaves(environments), containsString("\"ipAddress\":\"jenkinss1\",\"sshPort\":22,\"credentials\":{\"username\":\"root\",\"password\":\"shroot\"}"));

        assertThat(getSUTDefinition(environments), equalTo(SUT_RESOURCE));
        assertThat(getCommonTestProperties(event), equalTo(GLOBAL_PROPERTIES));
    }

    @Test
    public void testScheduleDescription_tafScheduler() {
        ScheduleRequest extractedSchedule = createAndVerifyEventWithSchedule(getScheduleRequest(ScheduleSource.TAF_SCHEDULER));
        assertThat(extractedSchedule.getTafSchedulerSourceUri(), equalTo("http://tafScheduler/1/2"));
    }

    @Test
    public void testScheduleDescription_gav() {
        ScheduleRequest extractedSchedule = createAndVerifyEventWithSchedule(getScheduleRequest(ScheduleSource.MAVEN_GAV));
        assertThat(extractedSchedule.getName(), equalTo("schedule.xml"));
    }

    @Test
    public void testScheduleDescription_plainXml() {
        ScheduleRequest extractedSchedule = createAndVerifyEventWithSchedule(getScheduleRequest(ScheduleSource.PLAIN_XML));
        assertThat(extractedSchedule.getXml(), equalTo("<plain_xml/>"));
    }

    private ScheduleRequest createAndVerifyEventWithSchedule(ScheduleRequest schedule) {
        EiffelBaselineDefinedEvent event = unit.build(getTriggeringTask(schedule));
        Collection<BaselinePart> baselineParts = event.getConsistsOf();
        List<BaselinePart> schedules = getSchedules(baselineParts);
        assertThat(schedules, Matchers.hasSize(1));
        return getScheduleFromTag(schedules.get(0).getTag());
    }

    private TriggeringTask getTriggeringTask() {
        TriggeringTaskBuilder unit = new TriggeringTaskBuilder();

        Properties schedule1TestProperties = new Properties();
        schedule1TestProperties.put("myProperty", "myValue");

        Properties schedule2TestProperties = new Properties();
        schedule2TestProperties.put("myProperty2", "myValue2");
        schedule2TestProperties.put("myProperty3", "myValue3");

        Properties globalTestProperties = new Properties();
        globalTestProperties.put("myGlobalProperty", "myGlobalValue");

        return unit
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
                .build();
    }

    private TriggeringTask getTriggeringTask(ScheduleRequest schedule) {
        TriggeringTaskBuilder unit = new TriggeringTaskBuilder();
        return unit.withSchedule(schedule).build();
    }

    private ScheduleRequest getScheduleFromTag(String tag) {
        Gson gson = new Gson();
        return gson.fromJson(tag.replace("Schedule:", ""), ScheduleRequest.class);
    }

    private List<BaselinePart> getCiFwkPackages(Collection<BaselinePart> baselineParts) {
        return extractBaselineParts(baselineParts, "TestPackage:");
    }

    private List<BaselinePart> getTestWares(Collection<BaselinePart> baselineParts) {
        return extractBaselineParts(baselineParts, "TestWare:");
    }

    private List<BaselinePart> getSchedules(Collection<BaselinePart> baselineParts) {
        return extractBaselineParts(baselineParts, "Schedule:");
    }

    private List<BaselinePart> extractBaselineParts(Collection<BaselinePart> baselineParts, final String tagName) {
        Iterable<BaselinePart> filtered = Iterables.filter(baselineParts, new Predicate<BaselinePart>() {
            @Override
            public boolean apply(BaselinePart baselinePart) {
                String baselinePartTag = baselinePart.getTag();
                return baselinePartTag != null && baselinePartTag.contains(tagName);
            }
        });
        assertNotNull(filtered);
        return Lists.newArrayList(filtered);
    }

    private String getSUTDefinition(Collection<Environment> environments) {
        List<Environment> envs = extractEnvironments(environments, "SUTResource");
        return envs.get(0).getValue();
    }

    private String getCommonTestProperties(EiffelBaselineDefinedEvent event) {
        return event.getOptionalParameter(TriggeringEventBuilder.TEST_PROPERTIES_KEY);
    }

    private String getArm(Collection<Environment> environments) {
        List<Environment> envs = extractEnvironments(environments, "ARM");
        return envs.get(0).getValue();
    }

    private String getSlaves(Collection<Environment> environments) {
        List<Environment> testGridResources = extractEnvironments(environments, "TestGridResources");
        return testGridResources.get(0).getValue();
    }

    private List<Environment> extractEnvironments(Collection<Environment> environments, final String name) {
        Iterable<Environment> filtered = Iterables.filter(environments, new Predicate<Environment>() {
            @Override
            public boolean apply(Environment env) {
                return name.equals(env.getName());
            }
        });
        assertNotNull(filtered);
        return Lists.newArrayList(filtered);
    }

    private Host getSlaveHost(String ip, int port, String userName, String password) {
        return new Host(ip, ip, port, userName, password);
    }

    private Environment extractEnvironment(Collection<Environment> environments, final String name) {
        List<Environment> filtered = extractEnvironments(environments, name);
        return filtered.isEmpty() ? null : filtered.get(0);
    }

    private ScheduleRequest getScheduleRequest(ScheduleSource scheduleSource) {
        ScheduleRequest result = new ScheduleRequest();
        result.setTestPropertiesAsString("x=y");
        switch (scheduleSource) {
            case TAF_SCHEDULER:
                result.setTafSchedulerSourceUri("http://tafScheduler/1/2");
                result.setXml("<taf_scheduler_xml/>");
                break;
            case MAVEN_GAV:
                result.setArtifact(new ArtifactInfo("g", "a", "v"));
                result.setName("schedule.xml");
                break;
            case PLAIN_XML:
                result.setXml("<plain_xml/>");
                break;
        }
        return result;
    }

    @Test
    public void toJson() {
        String object = unit.toJson(null);
        assertEquals("", object);
    }

}