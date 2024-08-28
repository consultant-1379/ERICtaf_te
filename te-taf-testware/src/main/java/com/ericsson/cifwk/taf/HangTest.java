package com.ericsson.cifwk.taf;

import org.testng.annotations.Test;

public class HangTest extends TafTestBase  {

    @Test
    public void shouldHang() {
        try {
            Thread.sleep(1000 * 60 * 60);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
