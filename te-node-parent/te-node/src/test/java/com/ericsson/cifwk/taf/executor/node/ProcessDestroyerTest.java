package com.ericsson.cifwk.taf.executor.node;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class ProcessDestroyerTest {

    private int pid = 1234;
    private ProcessDestroyer unit;

    @Before
    public void setUp() throws IOException{
        unit = new ProcessDestroyer();
        unit = spy(unit);

        doReturn(mock(Process.class)).when(unit).executeCommand(anyString());
    }

    @Test
    public void happyPath() throws Exception {
        doReturn(false).when(unit).isProcessRunning(eq(pid));
        Assert.assertTrue(unit.kill("kill", pid, 500));
        verify(unit, atLeast(1)).isCurrentTimeBefore(anyLong());
    }

    @Test
    public void processNotKilled() throws Exception {
        doReturn(true).when(unit).isProcessRunning(eq(pid));
        Assert.assertFalse(unit.kill("kill", pid, 500));
        verify(unit, atLeast(1)).isCurrentTimeBefore(anyLong());
    }

    @Test
    public void shouldKillHarsherIfFailsInPoliteWay() throws Exception {
        doReturn(true).doReturn(false).when(unit).isProcessRunning(eq(pid));
        Assert.assertTrue(unit.kill(pid, 100));
        verify(unit).getSigIntCommand(eq(pid));
        verify(unit).getSigTermCommand(eq(pid));
        verify(unit, never()).getSigKillCommand(eq(pid));

        reset(unit);
        doReturn(mock(Process.class)).when(unit).executeCommand(anyString());
        // Undying process
        doReturn(true).when(unit).isProcessRunning(eq(pid));

        Assert.assertFalse(unit.kill(pid, 100));
        verify(unit).getSigIntCommand(eq(pid));
        verify(unit).getSigTermCommand(eq(pid));
        verify(unit).getSigKillCommand(eq(pid));
    }

}