package com.ericsson.cifwk.taf.configuration.failures;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public abstract class AbstractConfigFailingTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConfigFailingTest.class);

    @Test
    public void testThatShouldNotStart() {
        LOGGER.error("This test shouldn't have been started at all");
    }

}
