package com.ericsson.cifwk.taf;

import org.testng.annotations.Test;

public class ExitTest extends TafTestBase {

    @Test
    public void shouldExit() {
        System.exit(100);
    }

}
