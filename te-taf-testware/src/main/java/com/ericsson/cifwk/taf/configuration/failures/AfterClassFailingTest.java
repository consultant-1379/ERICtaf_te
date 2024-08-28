package com.ericsson.cifwk.taf.configuration.failures;

import org.testng.annotations.AfterClass;

public class AfterClassFailingTest extends AbstractConfigFailingTest {

    @AfterClass
    public void afterClass() {
        throw new RuntimeException("Planned exception in the configuration method");
    }

}
