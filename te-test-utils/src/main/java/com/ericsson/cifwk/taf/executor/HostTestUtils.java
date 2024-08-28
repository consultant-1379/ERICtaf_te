package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import org.junit.Assert;

import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;

public class HostTestUtils {

    public static void validateHost(Host host, HostType hostType, String hostName, String ip, String group,
                              Integer amqpPort, Integer httpPort, Integer jmsPort, Integer rmiPort, Integer sshPort,
                              int nodeAmount, String userName, String userPwd, UserType userType) {
        Assert.assertNotNull(host);
        Assert.assertThat(host.getHostname(), is(hostName));
        Assert.assertThat(host.getType(), is(hostType));
        Assert.assertThat(host.getGroup(), is(group));
        Assert.assertThat(host.getIp(), is(ip));
        Map<Ports, String> ports = host.getPort();

        validatePort(ports, Ports.AMQP, amqpPort);
        validatePort(ports, Ports.HTTP, httpPort);
        validatePort(ports, Ports.JMS, jmsPort);
        validatePort(ports, Ports.RMI, rmiPort);
        validatePort(ports, Ports.SSH, sshPort);

        Assert.assertEquals(nodeAmount, host.getNodes().size());
        User user = host.getUsers().get(0);
        Assert.assertThat(user.getUsername(), is(userName));
        Assert.assertThat(user.getPassword(), is(userPwd));
        Assert.assertThat(user.getType(), is(userType));
    }

    public static void validatePort(Map<Ports, String> ports, Ports portType, Integer port) {
        if (port == null) {
            Assert.assertNull(ports.get(portType));
        } else {
            Assert.assertThat(ports.get(portType), is(String.valueOf(port)));
        }
    }

    public static Host getHostByName(List<Host> hosts, String hostName) {
        for (Host host : hosts) {
            if (hostName.equals(host.getHostname())) {
                return host;
            }
        }
        return null;
    }

}
