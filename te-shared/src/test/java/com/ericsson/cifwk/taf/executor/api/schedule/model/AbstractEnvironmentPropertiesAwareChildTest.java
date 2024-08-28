package com.ericsson.cifwk.taf.executor.api.schedule.model;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.junit.Test;

import java.util.List;

import static com.ericsson.cifwk.taf.executor.schedule.model.ScheduleHelper.envProperties;
import static com.ericsson.cifwk.taf.executor.schedule.model.ScheduleHelper.envProperty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class AbstractEnvironmentPropertiesAwareChildTest {

    @Test
    public void shouldGetEffectiveProperties() throws Exception {
        ScheduleItem scheduleItem = getScheduleItem();

        List<ScheduleEnvironmentProperty> effectiveEnvironmentProperties = scheduleItem.getEffectiveEnvironmentProperties();
        assertThat(effectiveEnvironmentProperties, hasItem(new ScheduleEnvironmentProperty("system", "systemOption1", "systemOption1Group1Value")));
        assertThat(effectiveEnvironmentProperties, hasItem(new ScheduleEnvironmentProperty("jvm", "version", "8")));
    }

    @Test
    public void shouldSerializeItem() {
        ScheduleItem scheduleItem = getScheduleItem();
        String json = new Gson().toJson(scheduleItem);
        assertThat(json, not(isEmptyString()));
    }

    private ScheduleItem getScheduleItem() {
        ScheduleRoot scheduleRoot = new ScheduleRoot(null, envProperties(
                envProperty("system", "systemOption1", "systemOption1GlobalValue"),
                envProperty("maven", "maven_opts", "-Xms1024m -Xmx4096m -XX:PermSize=1024m")
        ));
        List<ScheduleChild> group1Children = Lists.newArrayList();
        ScheduleItemGroup scheduleItemGroup1 = new ScheduleItemGroup(scheduleRoot, group1Children, false, envProperties(
                envProperty("system", "systemOption1", "systemOption1Group1Value"),
                envProperty("jvm", "version", "7")
        ));
        ScheduleItem scheduleItem1 = new ScheduleItem(scheduleItemGroup1, "", null, null, null, null, false, null, envProperties(
                envProperty("jvm", "version", "8")
        ));
        group1Children.add(scheduleItem1);
        return scheduleItem1;
    }

}
