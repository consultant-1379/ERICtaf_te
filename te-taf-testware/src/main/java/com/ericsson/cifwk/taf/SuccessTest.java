package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.annotations.TestStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class SuccessTest extends TafTestBase {

    private final static Logger LOGGER = LoggerFactory.getLogger(SuccessTest.class);

    @Test
    @TestId(id = "TORF-507", title = "Testing TE integration with TMS")
    public void shouldPass() {
        LOGGER.info("Running a successful TAF test.");
        testStep1();
        testStep2();
    }

    @TestStep(id = "TS1")
    public void testStep1() {
        LOGGER.info("Inside test step 1.");
        nestedTestStep(1);
    }

    @TestStep(id = "TS2")
    public void testStep2() {
        LOGGER.info("Inside test step 2.");
        nestedTestStep(2);
    }

    @TestStep(id = "NTS '{0}'")
    public void nestedTestStep(int arg) {
        LOGGER.info("Inside nested test step " + arg);
    }

}
