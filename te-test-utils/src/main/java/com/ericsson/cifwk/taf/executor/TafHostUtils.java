package com.ericsson.cifwk.taf.executor;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.data.postprocessor.HostsDataPostProcessor;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class TafHostUtils {

    public static List<Host> buildFromProperties(String sutResource) {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(sutResource));
            properties = filterProperties(properties, true);
            return HostsDataPostProcessor.processIsolated(properties);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public static Properties filterProperties(Properties properties, boolean filterHostProperties) {
        Properties result = new Properties();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String)entry.getKey();
            if (filterHostProperties && key.startsWith("host.")) {
                if (key.endsWith(".type")) {
                    // Workaround for CI portal's buggy host types
                    result.put(key, adaptCiPortalHostTypes((String) entry.getValue()));
                } else {
                    result.put(key, entry.getValue());
                }
            } else if (!filterHostProperties && !key.startsWith("host.")) {
                result.put(key, entry.getValue());
            }
        }
        return result;
    }

    @VisibleForTesting
    static String adaptCiPortalHostTypes(String hostType) {
        if ("sc-1".equalsIgnoreCase(hostType)) {
            return HostType.SC1.toString();
        }
        if ("sc-2".equalsIgnoreCase(hostType)) {
            return HostType.SC2.toString();
        }
        if ("opendj".equalsIgnoreCase(hostType)) {
            return HostType.UNKNOWN.toString();
        }
        if ("httpd".equalsIgnoreCase(hostType)) {
            return HostType.HTTP.toString();
        }
        if ("opendj".equalsIgnoreCase(hostType)) {
            return HostType.UNKNOWN.toString();
        }
        if ("openidm".equalsIgnoreCase(hostType)) {
            return HostType.UNKNOWN.toString();
        }
        if ("logstash".equalsIgnoreCase(hostType)) {
            return HostType.UNKNOWN.toString();
        }
        if ("visinotify".equalsIgnoreCase(hostType)) {
            return HostType.UNKNOWN.toString();
        }
        if ("visinaming".equalsIgnoreCase(hostType)) {
            return HostType.UNKNOWN.toString();
        }
        if ("visnamingsb".equalsIgnoreCase(hostType)) {
            return HostType.UNKNOWN.toString();
        }
        return hostType;
    }

    public static String generateHostListJson(List<Host> hosts) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Ports.class, new PortsSerializer())
                .create();
        return gson.toJson(hosts.toArray());
    }

    public static List<Host> generateHostListFromJson(String json) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Ports.class, new PortsDeserializer())
                .create();
        Host[] hosts = gson.fromJson(json, Host[].class);
        List<Host> result = Arrays.asList(hosts);
        // Workaround when nodes are not set as empty JSON array
        ensureNotNullNodes(result);
        return result;
    }

    private static void ensureNotNullNodes(List<Host> hosts) {
        for (Host host : hosts) {
            if (host.getNodes() == null) {
                host.setNodes(Lists.<Host>newArrayList());
            } else {
                ensureNotNullNodes(host.getNodes());
            }
        }
    }

    private static class PortsSerializer implements JsonSerializer<Ports> {
        @Override
        public JsonElement serialize(Ports port, Type typeOfSrc, JsonSerializationContext context) {
            String portName = port.toString().toLowerCase();
            return new JsonPrimitive(portName);
        }
    }

    private static class MapSerializer implements JsonSerializer<Map<Ports,String>> {
        @Override
        public JsonElement serialize(Map<Ports,String> port, Type typeOfSrc, JsonSerializationContext context) {
            String portName = port.toString().toLowerCase();
            return new JsonPrimitive(portName);
        }
    }

    public static class PortsDeserializer implements JsonDeserializer<Ports> {
        @Override
        public Ports deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) {
            String portType = jsonElement.getAsString();
            return Ports.valueOf(portType.toUpperCase());
        }
    }
}
