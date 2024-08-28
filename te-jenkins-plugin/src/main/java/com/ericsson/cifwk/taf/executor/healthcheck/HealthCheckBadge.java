package com.ericsson.cifwk.taf.executor.healthcheck;

import hudson.Extension;
import hudson.model.RootAction;

import static java.lang.String.format;

/**
 * Creates a left-side badge in Jenkins to see TE health state.
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 01/06/2017
 */
@Extension
public class HealthCheckBadge implements RootAction {

    @Override
    public String getIconFileName() {
        return "installer.png"; // One of the default Jenkins icons
    }

    @Override
    public String getDisplayName() {
        return "TE health state";
    }

    @Override
    public String getUrlName() {
        return format("descriptorByName/%s/healthCheck", HealthCheck.class.getName());
    }
}
