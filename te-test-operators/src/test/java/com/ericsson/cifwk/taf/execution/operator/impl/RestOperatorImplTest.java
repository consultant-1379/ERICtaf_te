package com.ericsson.cifwk.taf.execution.operator.impl;

import com.ericsson.cifwk.taf.executor.api.TriggeringTask;
import com.ericsson.cifwk.taf.executor.api.TriggeringTaskBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class RestOperatorImplTest {

    private RestOperatorImpl unit = new RestOperatorImpl();

    @BeforeMethod
    public void setUp() {
        unit = spy(unit);
        doReturn("1.0.0").when(unit).getPluginVersion();
        doReturn("http://nexus").when(unit).getNexusUri();
    }

    @Test
    public void createTriggeringTask() throws Exception {
        TriggeringTaskBuilder triggeringTaskBuilder = unit.triggeringTaskBuilderFor("testware-group", "testware-artifact", "/schedules/complex.xml");
        TriggeringTask triggeringTask = triggeringTaskBuilder.build();
        assertThat(triggeringTask.getSchedules(), hasSize(1));
        assertThat(triggeringTask.getTestWare(), hasSize(1));
        assertThat(triggeringTask.getCiFwkPackages(), hasSize(1));
    }
}
