package com.ericsson.cifwk.taf;

import org.testng.annotations.Test;

public class EndlessLoopTest extends TafTestBase  {

    @Test
    public void shouldHang() {
        for (;;);
    }
}
