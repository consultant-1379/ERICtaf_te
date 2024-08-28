package com.ericsson.cifwk.taf.executor.cluster.cloud;

/**
 * TODO: move to a separate module
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 15/06/2017
 */
public class KubernetesCloudSlaveProviderValidator extends AbstractCloudSlaveProviderValidator {

    @Override
    public String getProviderName() {
        return "Kubernetes cloud slave provider";
    }

    @Override
    public CloudProviderAdapter getAdapter() throws ClassNotFoundException {
        return new KubernetesCloudSlaveProviderAdapter();
    }
}
