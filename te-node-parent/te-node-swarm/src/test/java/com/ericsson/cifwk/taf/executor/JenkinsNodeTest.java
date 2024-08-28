package com.ericsson.cifwk.taf.executor;

import hudson.plugins.swarm.Options;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JenkinsNodeTest {

    private static final String HOST_NAME = "hostName.ericsson.se";

    @Mock
    private InetAddress localAddress;

    @Mock
    private NodeConfigurationProvider configurationProvider;

    @Spy
    @InjectMocks
    private JenkinsNode unit;

    @Before
    public void setUp() throws InterruptedException {
        doReturn(300).when(configurationProvider).getSelfLookupReattemptDelayInMillis();
        doReturn(1000).when(configurationProvider).getSelfLookupReattemptTimeoutInMillis();

        when(localAddress.getCanonicalHostName()).thenReturn(HOST_NAME);

        doNothing().when(unit).startClient(any(Options.class));
    }

    @Test
    public void shouldGetCurrentHostName_happyPath() throws Exception {
        doReturn(localAddress).when(unit).getLocalHost();
        assertEquals(HOST_NAME, unit.getCurrentHostAddress());
    }

    @Test
    public void shouldGetCurrentHostName_unknownHost() throws Exception {
        doThrow(new UnknownHostException()).
                doThrow(new UnknownHostException()).
                doReturn(localAddress).when(unit).getLocalHost();
        assertEquals(HOST_NAME, unit.getCurrentHostAddress());
    }

    @Test
    public void shouldGetCurrentHostName_unknownHostTimeout() throws Exception {
        doThrow(new UnknownHostException()).when(unit).getLocalHost();
        assertThat(unit.getCurrentHostAddress(), startsWith(JenkinsNode.UNRESOLVED_HOSTNAME_PREFIX));
    }

}