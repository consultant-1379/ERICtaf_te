package com.ericsson.cifwk.taf.configuration.failures;

import org.testng.annotations.AfterSuite;

public class AfterSuiteFailingTest extends AbstractConfigFailingTest {

    @AfterSuite
    public void afterSuite() {
        throw new RuntimeException("Planned exception in the configuration method");
    }

}
