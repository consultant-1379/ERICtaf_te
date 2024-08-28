package com.ericsson.cifwk.taf.executor.cluster.cloud;

import com.ericsson.cifwk.taf.executor.utils.JenkinsUtils;
import com.google.common.annotations.VisibleForTesting;

import java.util.List;
import java.util.ServiceLoader;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 15/06/2017
 */
public class GridCloudSlaveProviders {

    @VisibleForTesting
    static GridCloudSlaveProviders INSTANCE = new GridCloudSlaveProviders();

    private GridCloudSlaveProviders() {}

    @VisibleForTesting
    List<CloudSlaveProviderValidator> loadAllValidatorsViaSpi() {
        // SPI classes will not be visible to default class loader
        ClassLoader uberClassLoader = JenkinsUtils.getJenkinsInstance().getPluginManager().uberClassLoader;
        return newArrayList(ServiceLoader.load(CloudSlaveProviderValidator.class, uberClassLoader));
    }

    public static List<CloudSlaveProviderValidator> getAllValidators() {
        return INSTANCE.loadAllValidatorsViaSpi();
    }

    /**
     * @return <code>true</code> if at least one cloud slave provider is set up
     */
    public static boolean providersExist() {
        return getAllValidators().stream().anyMatch(CloudSlaveProviderValidator::isProviderSetUp);
    }

}
