package com.ericsson.cifwk.taf.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;

public class MemoryTest extends AbstractConfigurationAwareTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(MemoryTest.class);

    private Runtime runtime = Runtime.getRuntime();


    @Test
    public void shouldApplyCustomMemorySettings() throws Exception {
        verifyMemorySetting("totalMemory");
        verifyMemorySetting("maxMemory");
    }

    private Long getLongProperty(String propertyName) {
        return tafConfiguration.getProperty(propertyName, null, Long.class);
    }

    private void verifyMemorySetting(String settingName) throws Exception {
        Method method = Runtime.class.getMethod(settingName);
        Long value = (Long) method.invoke(runtime);
        LOGGER.info(String.format("runtime.%s = %d", settingName, value));
        Long expectedMinTotalSizeInBytes = getLongProperty("runtime." + settingName + ".expectedMinInBytes");
        if (expectedMinTotalSizeInBytes != null) {
            assertThat(value, greaterThanOrEqualTo(expectedMinTotalSizeInBytes));
        }
    }
}
