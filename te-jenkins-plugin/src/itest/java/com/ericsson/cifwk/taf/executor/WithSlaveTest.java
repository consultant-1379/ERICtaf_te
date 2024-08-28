package com.ericsson.cifwk.taf.executor;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 29/08/2017
 */
public abstract class WithSlaveTest extends RestServiceAwareITest {

    private static final String DEFAULT_SLAVE_NAME = "default_slave";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        createSlaveNode(DEFAULT_SLAVE_NAME, 3);
    }

    public void tearDown() throws Exception {
        shutdownSlave(DEFAULT_SLAVE_NAME);
    }
}
