package com.ericsson.cifwk.taf;

import org.testng.Assert;
import org.testng.annotations.Test;

public class EisMbStressTest extends TafTestBase {

    @Test
    public void shouldPassSometimes() {
        if (System.currentTimeMillis() % 2 == 0) {
            Assert.fail();
        }
    }

}
