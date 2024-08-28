package com.ericsson.cifwk.taf.executor.schedule;

import com.ericsson.cifwk.taf.executor.model.ScheduleBuildParameters;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class FlowExecutorTest {
        FlowExecutor flowExecutor;

    @Before
    public void setUp() throws Exception {
        flowExecutor = spy(new FlowExecutor(null));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        Date date = sdf.parse("21/12/2012 22:11:30.123");

        doReturn(date).when(flowExecutor).getDate();
    }

    @Test
    public void testGenerateFlowName() throws Exception {
        ScheduleBuildParameters params = new ScheduleBuildParameters();
        params.setTestware("com.ericsson.cifwk.taf.executor:te-taf-testware:1.0.2-SNAPSHOT");
        params.setScheduleName("schedule/success.xml");

        String result = flowExecutor.generateFlowName(params);

        assertThat(result, equalTo("TAF_Execution_te-taf-testware_1.0.2-SNAPSHOT_success_2012-12-21_22-11-30.123"));
    }

    @Test
    public void testGenerateFlowNameNoPath() throws Exception {
        ScheduleBuildParameters params = new ScheduleBuildParameters();
        params.setTestware("com.ericsson.cifwk.taf.executor:te-taf-testware:1.0.2-SNAPSHOT");
        params.setScheduleName("success.xml");

        String result = flowExecutor.generateFlowName(params);

        assertThat(result, equalTo("TAF_Execution_te-taf-testware_1.0.2-SNAPSHOT_success_2012-12-21_22-11-30.123"));
    }

    @Test
    public void testGenerateFlowNameNull() throws Exception {
        String result = flowExecutor.generateFlowName(new ScheduleBuildParameters());

        assertThat(result, equalTo("TAF_Execution___2012-12-21_22-11-30.123"));
    }

    @Test
    public void testGenerateFlowNonStandardTestware() throws Exception {
        ScheduleBuildParameters params = new ScheduleBuildParameters();
        params.setTestware("@@@!!!>>>test");
        params.setScheduleName("success.xml");

        String result = flowExecutor.generateFlowName(params);

        assertThat(result, equalTo("TAF_Execution_test_success_2012-12-21_22-11-30.123"));
    }


    @Test
    public void testGenerateFlowNameNotXml() throws Exception {
        ScheduleBuildParameters params = new ScheduleBuildParameters();
        params.setTestware("te-taf-testware");
        params.setScheduleName("success.txt");

        String result = flowExecutor.generateFlowName(params);

        assertThat(result, equalTo("TAF_Execution_te-taf-testware_success.txt_2012-12-21_22-11-30.123"));
    }

    @Test
    public void testGenerateFlowNameLong() throws Exception {
        ScheduleBuildParameters params = new ScheduleBuildParameters();
        params.setTestware("abcdefghij:klmopqrtabcdef.ghijklmopqrtabcdefghijklmopqrtabcdefghijklmopqrtab" +
                "cdefghijklmopqrtabcdefghijklmopqrtabcdefghijklmopqrtabcd.efghijklmopqrtabcdefghijklmopqrtabcdefghijklmop" +
                "qrtabcdefghijklmopqrtabcdefghijklmopqrtabcdefghijklmopqrtabcdefghijklmopqrtabcdefghijklmopqrtate-taf-testware:1.0.2-SNAPSHOT");
        params.setScheduleName("success.txt");

        String result = flowExecutor.generateFlowName(params);

        assertThat(result.length(), equalTo(255));
    }
}
