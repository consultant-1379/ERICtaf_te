package com.ericsson.cifwk.taf.executor.cluster.cloud;

import com.ericsson.cifwk.taf.executor.healthcheck.HealthCheckContext;
import com.ericsson.cifwk.taf.executor.healthcheck.HealthParam;
import com.ericsson.cifwk.taf.executor.utils.JenkinsUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.csanchez.jenkins.plugins.kubernetes.KubernetesCloud;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

import static java.lang.String.format;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 21/06/2017
 */
public class KubernetesCloudSlaveProviderAdapter implements CloudProviderAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(KubernetesCloudSlaveProviderAdapter.class);

    private static final String UNEXPECTED_ERROR_WHILE_VALIDATING = "Unexpected error while validating Kubernetes cluster availability";

    @Override
    public Class<KubernetesCloud> getCloudClass() {
        return KubernetesCloud.class;
    }

    @Override
    public KubernetesCloud getCloud() {
        Jenkins jenkins = JenkinsUtils.getJenkinsInstance();
        return jenkins.clouds.get(getCloudClass());
    }

    @Override
    public String getCloudName() {
        return getCloud().getDisplayName();
    }

    @SuppressFBWarnings // complains about switch
    @Override
    public void checkCloudAvailability(HealthCheckContext context) {
        KubernetesCloud kubeCloud = getCloud();
        KubernetesCloud.DescriptorImpl descriptor = (KubernetesCloud.DescriptorImpl) kubeCloud.getDescriptor();
        HealthParam check = new HealthParam("Kubernetes cluster is accessible",
                format("Slave cloud '%s' powered by Kubernetes plugin", getCloudName()));

        try {
            FormValidation formValidation = descriptor.doTestConnection(
                    kubeCloud.getDisplayName(),
                    kubeCloud.getServerUrl(),
                    kubeCloud.getCredentialsId(),
                    kubeCloud.getServerCertificate(),
                    kubeCloud.isSkipTlsVerify(),
                    kubeCloud.getNamespace(),
                    kubeCloud.getConnectTimeout(),
                    kubeCloud.getReadTimeout());
            switch (formValidation.kind) {
                case WARNING:
                    LOGGER.warn("Kubernetes cloud validation resulted in warning: {}", formValidation.getMessage());
                    context.ok(check);
                    break;
                case OK:
                    context.ok(check);
                    break;
                case ERROR:
                    context.fail(check, format("Kubernetes cluster is not accessible: %s", formValidation.getMessage()));
                    break;
                default:
                    LOGGER.warn("Got unknown form validation result '{}', cannot evaluate", formValidation.kind);
                    break;
            }
        } catch (Exception e) {
            LOGGER.error(UNEXPECTED_ERROR_WHILE_VALIDATING, e);
            context.fail(check, format("%s: %s", UNEXPECTED_ERROR_WHILE_VALIDATING, e.getMessage()));
        }
    }

}
