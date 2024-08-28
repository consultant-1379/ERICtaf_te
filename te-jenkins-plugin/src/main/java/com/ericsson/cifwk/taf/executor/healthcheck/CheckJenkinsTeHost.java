package com.ericsson.cifwk.taf.executor.healthcheck;

import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;
import hudson.remoting.VirtualChannel;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * Created by ekellmi on 4/13/16.
 */
public abstract class CheckJenkinsTeHost implements Check {

    protected final Jenkins jenkins;

    protected final GlobalTeSettings globalTeSettings;

    protected CheckJenkinsTeHost(Jenkins jenkins, GlobalTeSettings globalTeSettings) {
        this.jenkins = jenkins;
        this.globalTeSettings = globalTeSettings;
    }

    protected void runRemoteCheck(RemoteHealthCheckCallable healthCheck, HealthCheckContext context, VirtualChannel nodeChannel,
                                  HealthParam check) {
        if (nodeChannel == null) {
            context.fail(check, "does not have available remote channels.");
            return;
        }
        try {
            String call = nodeChannel.call(healthCheck);
            check = getHealthParamFromRemoteCall(call);
        } catch (Exception e) { // NOSONAR
            context.fail(check, e.getMessage());
            return;
        }
        context.ok(check);
    }

    protected HealthParam getHealthParamFromRemoteCall(String call) {
        JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(call);
        return new HealthParam(jsonObject.getString("name"), jsonObject.getString("scope"))
                .setPassed(jsonObject.getBoolean("passed"))
                .setDescription(jsonObject.getString("description"));
    }

    protected void checkAvailableDiskSpace(HealthCheckContext context, VirtualChannel nodeChannel, String nodeName, HealthParam check) {
        RemoteHealthCheckCallable healthCheck = new TeNodeDiskAvailabilityCheckCallable(nodeName, globalTeSettings.getMinExecutorDiskSpaceGB());
        runRemoteCheck(healthCheck, context, nodeChannel, check);
    }

    protected void checkNodesAvailableMemorySpace(HealthCheckContext context, VirtualChannel nodeChannel, String nodeName, HealthParam check) {
        RemoteHealthCheckCallable healthCheck = new TeNodeMemoryAvailabilityCheckCallable(nodeName, globalTeSettings.getMinExecutorMemorySpaceGB());
        runRemoteCheck(healthCheck, context, nodeChannel, check);
    }

    protected void checkAllureServiceAvailability(HealthCheckContext context, VirtualChannel nodeChannel, String nodeName, HealthParam check) {
        String allureServiceNginxUrl = globalTeSettings.getAllureServiceUrl();
        String allureServiceBackendUrl = globalTeSettings.getAllureServiceBackedUrl();
        RemoteHealthCheckCallable nginxHealthCheck = new TeNodeAllureServiceAccessibilityCheckCallable(nodeName, allureServiceNginxUrl, "Nginx");
        runRemoteCheck(nginxHealthCheck, context, nodeChannel, check);
        RemoteHealthCheckCallable backendHealthCheck = new TeNodeAllureServiceAccessibilityCheckCallable(nodeName, allureServiceBackendUrl, "Backend");
        runRemoteCheck(backendHealthCheck, context, nodeChannel, check);
    }

}
