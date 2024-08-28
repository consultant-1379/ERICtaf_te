package com.ericsson.cifwk.taf.properties;

import org.testng.annotations.Test;

import com.google.common.truth.Truth;

public class PropertyOverridingTest extends AbstractConfigurationAwareTest {


    @Test
    // This test relies on default property overridding
    public void realPropertyValuesShouldMatchExpected() {
        verifyPropertyValue("my.property");
    }

    private void verifyPropertyValue(String propertyName) {
        Truth.assertThat(tafConfiguration.getProperty(propertyName + ".expectedValue"))
                .isEqualTo(tafConfiguration.getProperty(propertyName));
    }
}
