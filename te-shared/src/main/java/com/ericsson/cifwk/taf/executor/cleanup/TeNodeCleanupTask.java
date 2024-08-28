package com.ericsson.cifwk.taf.executor.cleanup;

/**
 * SPI interface to be implemented by cleanup routines in TE node and to be invoked on master.
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 21/08/2017
 */
public interface TeNodeCleanupTask {

    String getDescription();

    void doCleanup();

}
