package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.model.ScheduleBuildParameters;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class TafScheduleBuildTest {

    private TafScheduleBuild unit;

    @Before
    public void setUp() throws IOException {
        TafScheduleProject project = mock(TafScheduleProject.class);
        when(project.getBuildDir()).thenReturn(new File("./target"));
        unit = new TafScheduleBuild(project);
        unit = spy(unit);
        ScheduleBuildParameters scheduleBuildParameters = new ScheduleBuildParameters();
        scheduleBuildParameters.setSutResource("host.ms1.ip=overridden\n" +
                "host.ms1.user.root.pass=newPass\n" +
                "host.ms1.user.root.type=admin\n" +
                "host.ms1.port.ssh=2201\n" +
                "host.ms1.type=ms\n" +
                "my.property=overridden");
        doReturn(scheduleBuildParameters).when(unit).getBuildParameters();
    }

    @Test
    public void testDoConfig_getHosts() throws Exception {
        unit.doConfig(null, null, "hosts");
    }

    @Test
    public void testDoConfig_getProperties() throws Exception {
        unit.doConfig(null, null, "properties");
    }
}