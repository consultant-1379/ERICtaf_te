package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;
import com.ericsson.cifwk.taf.executor.model.ScheduleBuildParameters;
import com.ericsson.cifwk.taf.executor.model.TeBuildMainParameters;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class AbstractTestExecutionBuilderTest {

    private AbstractTestExecutionBuilder unit;

    @Before
    public void setUp() {
        unit = new AbstractTestExecutionBuilder() {};
        unit = spy(unit);
    }

    @Test
    public void testIsTePipelineOk() throws Exception {
        doReturn(new TeBuildMainParameters(null, null)).
                when(unit).getMainParameters(eq("badExecutionId1"));
        doReturn(new TeBuildMainParameters(null, new ScheduleBuildParameters())).
                when(unit).getMainParameters(eq("badExecutionId2"));
        doReturn(new TeBuildMainParameters(new GlobalTeSettings(), new ScheduleBuildParameters())).
                when(unit).getMainParameters(eq("goodExecutionId"));

        assertFalse(unit.isTePipelineOk("badExecutionId1", mock(PrintStream.class)));
        assertFalse(unit.isTePipelineOk("badExecutionId2", mock(PrintStream.class)));
        assertTrue(unit.isTePipelineOk("goodExecutionId", mock(PrintStream.class)));
    }


}