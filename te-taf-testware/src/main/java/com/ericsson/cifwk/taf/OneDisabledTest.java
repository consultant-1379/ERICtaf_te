package com.ericsson.cifwk.taf;

import org.testng.annotations.Test;

public class OneDisabledTest {

    @Test(enabled = false)
    public void skippedTest() {
    }

}
