package com.ericsson.cifwk.taf.configuration.failures;

import org.testng.annotations.AfterMethod;

public class AfterMethodFailingTest extends AbstractConfigFailingTest {

    @AfterMethod
    public void afterMethod() {
        throw new RuntimeException("Planned exception in the configuration method");
    }

}
