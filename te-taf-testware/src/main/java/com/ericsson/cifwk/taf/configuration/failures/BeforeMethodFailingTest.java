package com.ericsson.cifwk.taf.configuration.failures;

import org.testng.annotations.BeforeMethod;

public class BeforeMethodFailingTest extends AbstractConfigFailingTest {

    @BeforeMethod
    public void beforeMethod() {
        throw new RuntimeException("Planned exception in the configuration method");
    }

}
