package com.ericsson.cifwk.taf.executor.cluster.cloud;

import com.ericsson.cifwk.taf.executor.TAFExecutor;
import com.ericsson.cifwk.taf.executor.healthcheck.HealthCheckContext;
import com.ericsson.cifwk.taf.executor.healthcheck.HealthParam;
import hudson.model.Label;
import hudson.model.labels.LabelAtom;
import hudson.slaves.Cloud;

import static java.lang.String.format;

/**
 * TODO: unit tests
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 15/06/2017
 */
abstract class AbstractCloudSlaveProviderValidator implements CloudSlaveProviderValidator {

    private static final Label TAF_SLAVE_LABEL = new LabelAtom(TAFExecutor.TAF_NODE_LABEL);

    @Override
    public final boolean isProviderSetUp() {
        try {
            return getAdapter().getCloud() != null;
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            return false;
        }
    }

    @Override
    public final void healthCheck(HealthCheckContext context) {
        try {
            CloudProviderAdapter adapter = getAdapter();
            Cloud cloud = adapter.getCloud();
            checkIfCanProvisionSlaves(context, cloud);
            adapter.checkCloudAvailability(context);
        } catch (ClassNotFoundException e) {
            context.fail(new HealthParam("Cloud plugin is set up", DEFAULT_HEALTH_CHECKS_SCOPE),
                    format("Failed to find cloud provider class - %s. Perhaps the cloud plugin is missing?", e.getMessage()));
        }
    }

    private void checkIfCanProvisionSlaves(HealthCheckContext context, Cloud cloud) throws ClassNotFoundException {
        String cloudDisplayName = getAdapter().getCloudName();
        HealthParam check = new HealthParam(format("Cloud '%s' is configured to provide TE slaves", cloudDisplayName), DEFAULT_HEALTH_CHECKS_SCOPE);
        if (!cloud.canProvision(TAF_SLAVE_LABEL)) {
            context.fail(check, format("Cloud '%s' is supposed to provide TE slaves, but is not configured properly", cloudDisplayName));
        } else {
            context.ok(check);
        }
    }

}
