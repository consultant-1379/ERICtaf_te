package com.ericsson.cifwk.taf;

import org.testng.annotations.Test;

public class ErrorTest extends TafTestBase {

    @Test
    public void shouldThrowException() {
        throw new RuntimeException("exception");
    }

}
