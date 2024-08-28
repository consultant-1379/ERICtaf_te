package com.ericsson.cifwk.taf.executor.healthcheck;

import hudson.remoting.Callable;

import java.io.Serializable;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 01/06/2017
 */
public interface RemoteHealthCheckCallable extends Callable<String, Exception>, Serializable {

    String doCheck(HealthParam check);

    String getCheckName(String nodeName);

}
