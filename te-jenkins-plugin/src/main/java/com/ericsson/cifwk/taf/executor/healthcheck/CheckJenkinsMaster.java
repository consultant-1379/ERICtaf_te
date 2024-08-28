package com.ericsson.cifwk.taf.executor.healthcheck;

import com.ericsson.cifwk.taf.executor.cluster.cloud.CloudSlaveProviderValidator;
import com.ericsson.cifwk.taf.executor.cluster.cloud.GridCloudSlaveProviders;
import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;
import com.ericsson.cifwk.taf.executor.utils.GlobalTeSettingsProvider;
import com.ericsson.cifwk.taf.executor.utils.JenkinsUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import hudson.model.Node;
import hudson.remoting.VirtualChannel;
import jenkins.model.Jenkins;

import java.util.List;

import static java.lang.String.format;

/**
*
*/
public class CheckJenkinsMaster extends CheckJenkinsTeHost {

    static final String SCOPE = "Jenkins";

    CheckJenkinsMaster(Jenkins jenkins) {
        this(jenkins, GlobalTeSettingsProvider.getInstance().provide());
    }

    @VisibleForTesting
    CheckJenkinsMaster(Jenkins jenkins, GlobalTeSettings globalTeSettings) {
        super(jenkins, globalTeSettings);
    }

    @Override
    public void check(HealthCheckContext context) {
        checkRootUrl(context);
        checkWorkers(context);
        checkAvailableDiskSpace(context);
        checkForAvailableNodes(context);
        if (globalTeSettings.isAllureServiceDefined()) {
            checkAllureServiceAvailability(context);
        }
    }

    private void checkRootUrl(HealthCheckContext context) {
        HealthParam check = new HealthParam("Jenkins URL is set", SCOPE);

        String url = jenkins.getRootUrl();
        if (Strings.isNullOrEmpty(url) || url.contains("localhost")) {
            context.fail(check, "Jenkins URL not set or points to 'localhost'.");
        } else {
            context.ok(check);
        }
    }

    private void checkWorkers(HealthCheckContext context) {
        HealthParam check = new HealthParam("Jenkins master has workers", SCOPE);
        if (jenkins.getNumExecutors() < 1) {
            context.fail(check, "Number of executors is less than 1 on master.");
        } else {
            context.ok(check);
        }
    }

    private void checkAvailableDiskSpace(HealthCheckContext context) {
        HealthParam check = new HealthParam("Jenkins master has adequate disk space", SCOPE);
        VirtualChannel nodeChannel = jenkins.getChannel();
        checkAvailableDiskSpace(context, nodeChannel, "master", check);
    }

    private void checkAllureServiceAvailability(HealthCheckContext context) {
        HealthParam check = new HealthParam("Allure service is accessible from master", SCOPE);
        VirtualChannel nodeChannel = jenkins.getChannel();
        checkAllureServiceAvailability(context, nodeChannel, "master", check);
    }

    private void checkForAvailableNodes(HealthCheckContext context) {
        if (GridCloudSlaveProviders.providersExist()) {
            cloudAvailabilityCheck(context, GridCloudSlaveProviders.getAllValidators());
        } else {
            simpleNodesAvailabilityCheck(context);
        }
    }

    @VisibleForTesting
    void cloudAvailabilityCheck(HealthCheckContext parentContext, List<CloudSlaveProviderValidator> providerValidators) {
        List<HealthCheckContext> failedContexts = Lists.newArrayList();
        for (CloudSlaveProviderValidator validator : providerValidators) {
            DefaultHealthCheckContext context = new DefaultHealthCheckContext();
            validator.healthCheck(context);
            if (context.isHealthy()) {
                parentContext.ok(new HealthParam(format("At least one cloud slave provider (%s) works", validator.getProviderName()), SCOPE));
                return;
            } else {
                failedContexts.add(context);
            }
        }
        failedContexts.forEach(parentContext::merge);
    }

    private void simpleNodesAvailabilityCheck(HealthCheckContext context) {
        HealthParam check = new HealthParam("Jenkins has TE Slaves", SCOPE);
        List<Node> nodes = JenkinsUtils.getTeNodes(jenkins);
        if (nodes.isEmpty()) {
            context.fail(check, "Connect at least one TE slave node or a set up a cloud (e.g., Kubernetes) providing slaves.");
        } else {
            context.ok(check);
        }
    }

}
