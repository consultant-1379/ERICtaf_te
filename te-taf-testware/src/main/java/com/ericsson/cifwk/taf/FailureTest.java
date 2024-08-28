package com.ericsson.cifwk.taf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FailureTest extends TafTestBase {

    private final static Logger LOGGER = LoggerFactory.getLogger(FailureTest.class);

    @Test
    public void shouldFail() {
        LOGGER.info("Running a failing TAF test.");
        Assert.fail();
    }

}
