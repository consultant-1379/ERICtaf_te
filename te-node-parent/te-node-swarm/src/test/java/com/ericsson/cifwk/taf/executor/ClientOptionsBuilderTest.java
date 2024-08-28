package com.ericsson.cifwk.taf.executor;

import hudson.plugins.swarm.Options;
import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 04/08/2017
 */
@RunWith(MockitoJUnitRunner.class)
public class ClientOptionsBuilderTest {

    @Mock
    private Configuration configuration;

    @Mock
    private NodeConfigurationProvider configurationProvider;

    @Spy
    private Options options;

    @Spy
    @InjectMocks
    private ClientOptionsBuilder unit;

    @Before
    public void setUp() throws InterruptedException {
        // mandatory settings
        doReturn((short)6).when(configurationProvider).getExecutorCount();
        doReturn(".").when(configurationProvider).getNodeFsRoot();
    }

    @Test
    public void shouldNotAddAuthenticationDetailsByDefault() throws Exception {
        unit.withHostAddress("hostAddress");
        Options options = unit.build();
        assertNull(options.username);
        assertNull(options.password);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotAddAuthenticationDetails_missingPassword() {
        setUsername("user");
        setPassword(null);
        // No password is defined - should throw exception
        unit.optionallyAddAuthenticationDetails();
    }

    @Test
    public void shouldAddAuthenticationDetails_happyPath() {
        String username = "user";
        String password = "pwd";

        setUsername(username);
        setPassword(password);

        unit.optionallyAddAuthenticationDetails();

        assertThat(options.username).isEqualTo(username);
        assertThat(options.password).isEqualTo(password);
    }

    @Test
    public void shouldDefineAgentName() throws Exception {
        String hostAddress = "172.23.45.22";
        String nodeName = "nodeName";

        unit.withHostAddress(hostAddress);
        doReturn(null).doReturn(nodeName).when(configurationProvider).getJenkinsNodeName();

        unit.defineAgentName();
        assertThat(options.name).isEqualTo(hostAddress);

        unit.defineAgentName();
        assertThat(options.name).isEqualTo(nodeName);
    }

    @Test
    public void shouldOptionallyDisableClientsUniqueId() throws Exception {
        unit.optionallyDisableUniqueClientId();
        assertThat(options.disableClientsUniqueId).isEqualTo(false);

        doReturn(null).doReturn(false).doReturn(true).when(configurationProvider).shouldDisableUniqueClientId();

        unit.optionallyDisableUniqueClientId();
        assertThat(options.disableClientsUniqueId).isEqualTo(false);

        unit.optionallyDisableUniqueClientId();
        assertThat(options.disableClientsUniqueId).isEqualTo(false);

        unit.optionallyDisableUniqueClientId();
        assertThat(options.disableClientsUniqueId).isEqualTo(true);
    }

    @Test
    public void shouldOptionallyDisableReconnection() throws Exception {
        unit.optionallyDisableReconnection();
        assertThat(options.noRetryAfterConnected).isEqualTo(false);

        doReturn(null).doReturn(false).doReturn(true).when(configurationProvider).noRetryAfterConnected();

        unit.optionallyDisableReconnection();
        assertThat(options.noRetryAfterConnected).isEqualTo(false);

        unit.optionallyDisableReconnection();
        assertThat(options.noRetryAfterConnected).isEqualTo(false);

        unit.optionallyDisableReconnection();
        assertThat(options.noRetryAfterConnected).isEqualTo(true);
    }

    private void setUsername(String username) {
        doReturn(username).when(configurationProvider).getJenkinsUsername();
    }

    private void setPassword(String password) {
        doReturn(password).when(configurationProvider).getJenkinsPassword();
    }

}