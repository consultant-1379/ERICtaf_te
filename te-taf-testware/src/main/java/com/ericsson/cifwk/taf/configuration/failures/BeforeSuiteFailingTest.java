package com.ericsson.cifwk.taf.configuration.failures;

import org.testng.annotations.BeforeSuite;

public class BeforeSuiteFailingTest extends AbstractConfigFailingTest {

    @BeforeSuite
    public void beforeSuite() {
        throw new RuntimeException("Planned exception in the configuration method");
    }

}
