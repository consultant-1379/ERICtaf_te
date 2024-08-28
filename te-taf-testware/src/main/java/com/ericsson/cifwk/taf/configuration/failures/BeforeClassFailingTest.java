package com.ericsson.cifwk.taf.configuration.failures;

import org.testng.annotations.BeforeClass;

public class BeforeClassFailingTest extends AbstractConfigFailingTest {

    @BeforeClass
    public void beforeClass() {
        throw new RuntimeException("Planned exception in the configuration method");
    }

}
