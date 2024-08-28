package com.ericsson.cifwk.taf.executor.security;

import java.security.Permission;

/**
 * Equivalent to security manager being not defined.
 *
 * Standard SecurityManager does restrict numerous actions
 * comparing to standard JVM program (without security manager defined).
 *
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 22.12.2016
 */
public class NoCheckSecurityManager extends SecurityManager {

    @Override
    public void checkPermission(Permission perm) {
        // no check
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        // no check
    }

}
