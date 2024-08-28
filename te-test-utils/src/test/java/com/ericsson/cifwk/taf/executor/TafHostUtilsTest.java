package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class TafHostUtilsTest {

    @Test
    public void testBuildFromProperties() throws Exception {
        String hostDefs = "host.ms1.ip=192.168.0.42\n" +
                "host.ms1.user.root.pass=12shroot\n" +
                "host.ms1.user.root.type=admin\n" +
                "host.ms1.port.ssh=22\n" +
                "host.ms1.type=ms\n" +
                " \n" +
                "host.sc-1.ip=192.168.0.43\n" +
                "host.sc-1.user.root.pass=litpc0b6lEr\n" +
                "host.sc-1.user.root.type=admin\n" +
                "host.sc-1.type=sc-1\n" +
                "host.sc-1.port.ssh=22\n" +
                "host.sc-1.node.FMPMMS.ip=192.168.0.69\n" +
                "host.sc-1.node.FMPMMS.type=jboss\n" +
                "host.sc-1.node.FMPMMS.user.root.pass=shroot\n" +
                "host.sc-1.node.FMPMMS.user.root.type=admin\n" +
                "host.sc-1.node.FMPMMS.user.guest.type=oper\n" +
                "host.sc-1.node.FMPMMS.user.guest.pass=guestp\n" +
                "host.sc-1.node.FMPMMS.port.http=8080\n" +
                "host.sc-1.node.FMPMMS.port.rmi=4447\n" +
                "host.sc-1.node.FMPMMS.port.jmx=9998\n" +
                "host.sc-1.node.FMPMMS.port.jboss_management=9999";

        List<Host> hosts = TafHostUtils.buildFromProperties(hostDefs);
        Assert.assertEquals(2, hosts.size());

        Host ms1 = HostTestUtils.getHostByName(hosts, "ms1");
        Assert.assertNotNull(ms1);
        Assert.assertEquals("192.168.0.42", ms1.getIp());
        Assert.assertEquals("MS", ms1.getType().toString());
        User user = ms1.getUsers(UserType.ADMIN).get(0);
        Assert.assertEquals("root", user.getUsername());
        Assert.assertEquals("12shroot", user.getPassword());

        Host sc1 = HostTestUtils.getHostByName(hosts, "sc-1");
        Assert.assertNotNull(sc1);
        Assert.assertEquals("192.168.0.43", sc1.getIp());
        Assert.assertEquals("SC1", sc1.getType().toString());
        user = sc1.getUsers(UserType.ADMIN).get(0);
        Assert.assertEquals("root", user.getUsername());
        Assert.assertEquals("litpc0b6lEr", user.getPassword());

        List<Host> sc1Nodes = sc1.getNodes();

        Assert.assertEquals(1, sc1Nodes.size());

        Host sc1Node = sc1Nodes.get(0);

        Assert.assertEquals("FMPMMS", sc1Node.getHostname());
        Assert.assertEquals("192.168.0.69", sc1Node.getIp());
        Assert.assertEquals("JBOSS", sc1Node.getType().toString());

        User rootUser = sc1Node.getUsers(UserType.ADMIN).get(0);
        Assert.assertEquals("root", rootUser.getUsername());
        Assert.assertEquals("shroot", rootUser.getPassword());

        User operUser = sc1Node.getUsers(UserType.OPER).get(0);
        Assert.assertEquals("guest", operUser.getUsername());
        Assert.assertEquals("guestp", operUser.getPassword());

        Assert.assertEquals(8080, sc1Node.getPort(Ports.HTTP).intValue());
        Assert.assertEquals(4447, sc1Node.getPort(Ports.RMI).intValue());
        Assert.assertEquals(9998, sc1Node.getPort(Ports.JMX).intValue());
        Assert.assertEquals(9999, sc1Node.getPort(Ports.JBOSS_MANAGEMENT).intValue());
    }

    @Test
    public void testBuildFromMixedProperties() throws Exception {
        String properties = "host.ms1.ip=overridden\n" +
                "host.ms1.user.root.pass=newPass\n" +
                "host.ms1.user.root.type=admin\n" +
                "host.ms1.port.ssh=2201\n" +
                "host.ms1.type=ms\n" +
                "my.property=overridden";
        List<Host> hosts = TafHostUtils.buildFromProperties(properties);
        Assert.assertEquals(1, hosts.size());

        Host ms1 = HostTestUtils.getHostByName(hosts, "ms1");
        Assert.assertNotNull(ms1);
        HostTestUtils.validateHost(ms1, HostType.MS, "ms1", "overridden", null,
                null, null, null, null, 2201, 0, "root", "newPass", UserType.ADMIN);
    }

    @Test
    public void shouldFilterHostProperties() throws IOException {
        String propertiesStr = "host.ms1.ip=overridden\n" +
                "host.ms1.user.root.pass=newPass\n" +
                "host.ms1.user.root.type=admin\n" +
                "host.ms1.port.ssh=2201\n" +
                "host.ms1.type=ms\n" +
                "my.property=overridden";
        Properties properties = new Properties();
        properties.load(new StringReader(propertiesStr));
        Properties result = TafHostUtils.filterProperties(properties, true);
        Assert.assertEquals(5, result.size());
        Assert.assertNull(result.getProperty("my.property"));
    }

    @Test
    public void shouldFilterSimpleProperties() throws IOException {
        String propertiesStr = "host.ms1.ip=overridden\n" +
                "host.ms1.user.root.pass=newPass\n" +
                "host.ms1.user.root.type=admin\n" +
                "host.ms1.port.ssh=2201\n" +
                "host.ms1.type=ms\n" +
                "my.property=overridden";
        Properties properties = new Properties();
        properties.load(new StringReader(propertiesStr));
        Properties result = TafHostUtils.filterProperties(properties, false);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("overridden", result.getProperty("my.property"));
    }

    @Test
    public void generateHostListJson() {
        List<Host> originalHosts = generateHostList();
        verifyHosts(originalHosts);

        String json = TafHostUtils.generateHostListJson(originalHosts);
        System.out.println(json);

        verifyHosts(TafHostUtils.generateHostListFromJson(json));
    }

    @Test
    public void shouldDeserializeJson() {
        String json = "[\n" +
                "    {\n" +
                "        \"hostname\": \"ms1\", \n" +
                "        \"ip\": \"overridden\", \n" +
                "        \"ports\": {\n" +
                "            \"ssh\": 2201\n" +
                "        }, \n" +
                "        \"type\": \"ms\", \n" +
                "        \"users\": [\n" +
                "            {\n" +
                "                \"password\": \"12shroot\", \n" +
                "                \"type\": \"admin\", \n" +
                "                \"username\": \"root\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }," +
                "{\n" +
                "        \"hostname\": \"sc1\", \n" +
                "        \"ip\": \"10.59.142.48\", \n" +
                "        \"nodes\": [\n" +
                "            {\n" +
                "                \"group\": \"internal_opendj\", \n" +
                "                \"hostname\": \"internal_opendj_su0\", \n" +
                "                \"ip\": \"192.110.50.4\", \n" +
                "                \"ports\": {\n" +
                "                    \"http\": 8080\n" +
                "                }, \n" +
                "                \"tunnel\": 1, \n" +
                "                \"type\": \"http\", \n" +
                "                \"users\": [\n" +
                "                    {\n" +
                "                        \"password\": \"shroot\", \n" +
                "                        \"type\": \"admin\", \n" +
                "                        \"username\": \"root\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }, \n" +
                "            {\n" +
                "                \"group\": \"internal_opendj\", \n" +
                "                \"hostname\": \"internal_opendj_su1\", \n" +
                "                \"ip\": \"192.110.50.5\", \n" +
                "                \"ports\": {\n" +
                "                    \"http\": 8080\n" +
                "                }, \n" +
                "                \"tunnel\": 2, \n" +
                "                \"type\": \"http\", \n" +
                "                \"users\": [\n" +
                "                    {\n" +
                "                        \"password\": \"shroot\", \n" +
                "                        \"type\": \"admin\", \n" +
                "                        \"username\": \"root\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ], \n" +
                "        \"ports\": {\n" +
                "            \"ssh\": 22\n" +
                "        }, \n" +
                "        \"type\": \"sc1\", \n" +
                "        \"users\": [\n" +
                "            {\n" +
                "                \"password\": \"litpc0b6lEr\", \n" +
                "                \"type\": \"admin\", \n" +
                "                \"username\": \"root\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }]";
        List<Host> hosts = TafHostUtils.generateHostListFromJson(json);

        Host ms1 = HostTestUtils.getHostByName(hosts, "ms1");
        HostTestUtils.validateHost(ms1, HostType.MS, "ms1", "overridden", null,
                null, null, null, null, 2201, 0, "root", "12shroot", UserType.ADMIN);

        Host sc1 = HostTestUtils.getHostByName(hosts, "sc1");
        HostTestUtils.validateHost(sc1, HostType.SC1, "sc1", "10.59.142.48", null,
                null, null, null, null, 22, 2, "root", "litpc0b6lEr", UserType.ADMIN);

        List<Host> nodes = sc1.getNodes();
        Host node1 = HostTestUtils.getHostByName(nodes, "internal_opendj_su0");
        HostTestUtils.validateHost(node1, HostType.HTTP, "internal_opendj_su0", "192.110.50.4", "internal_opendj",
                null, 8080, null, null, null, 0, "root", "shroot", UserType.ADMIN);
        Assert.assertEquals("1", node1.getTunnelPortOffset());

        Host node2 = HostTestUtils.getHostByName(nodes, "internal_opendj_su1");
        HostTestUtils.validateHost(node2, HostType.HTTP, "internal_opendj_su1", "192.110.50.5", "internal_opendj",
                null, 8080, null, null, null, 0, "root", "shroot", UserType.ADMIN);
        Assert.assertEquals("2", node2.getTunnelPortOffset());
    }

    private List<Host> generateHostList() {
        Host host1Node1 = new Host.HostBuilder()
                .withType(HostType.CIFWK)
                .withName("myHost1Node1")
                .withIp("10.0.1.1")
                .withRmiPort(1234)
                .withUser(new User("node1User1", "node1User1Pwd", UserType.ADMIN))
                .build();
        Host host1Node2 = new Host.HostBuilder()
                .withType(HostType.EBAS)
                .withName("myHost1Node2")
                .withIp("10.0.1.2")
                .withJmsPort(2345)
                .withUser(new User("node2User1", "node2User1Pwd", UserType.OPER))
                .build();

        Host host1 = new Host.HostBuilder()
                .withType(HostType.GATEWAY)
                .withName("myHost1")
                .withIp("10.0.1.0")
                .withAmqpPort(5672)
                .withSshPort(22)
                .withUser(new User("user1", "pwd1", UserType.ADMIN))
                .withNode(host1Node1)
                .withNode(host1Node2)
                .build();

        Host host2 = new Host.HostBuilder()
                .withType(HostType.HTTP)
                .withName("myHost2")
                .withIp("10.0.2.0")
                .withSshPort(2212)
                .withUser(new User("user2", "pwd2", UserType.SYS_ADM))
                .build();

        return Arrays.asList(host1, host2);
    }

    private void verifyHosts(List<Host> originalHosts) {
        Assert.assertEquals(2, originalHosts.size());
        Host myHost1 = HostTestUtils.getHostByName(originalHosts, "myHost1");
        HostTestUtils.validateHost(myHost1, HostType.GATEWAY, "myHost1", "10.0.1.0", null,
                5672, null, null, null, 22, 2, "user1", "pwd1", UserType.ADMIN);
        List<Host> host1Nodes = myHost1.getNodes();
        HostTestUtils.validateHost(HostTestUtils.getHostByName(host1Nodes, "myHost1Node1"), HostType.CIFWK, "myHost1Node1", "10.0.1.1", null,
                null, null, null, 1234, null, 0, "node1User1", "node1User1Pwd", UserType.ADMIN);
        HostTestUtils.validateHost(HostTestUtils.getHostByName(host1Nodes, "myHost1Node2"), HostType.EBAS,  "myHost1Node2", "10.0.1.2", null,
                null, null, 2345, null, null, 0, "node2User1", "node2User1Pwd", UserType.OPER);
        HostTestUtils.validateHost(HostTestUtils.getHostByName(originalHosts, "myHost2"), HostType.HTTP, "myHost2", "10.0.2.0", null,
                null, null, null, null, 2212, 0, "user2", "pwd2", UserType.SYS_ADM);
    }

}
