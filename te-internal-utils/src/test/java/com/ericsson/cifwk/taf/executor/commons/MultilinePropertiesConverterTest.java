package com.ericsson.cifwk.taf.executor.commons;

import org.hamcrest.collection.IsMapContaining;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Properties;

import static com.ericsson.cifwk.taf.executor.commons.MultilinePropertiesConverter.LINE_SEPARATOR;
import static com.ericsson.cifwk.taf.executor.commons.MultilinePropertiesConverter.propertyMapToString;
import static com.ericsson.cifwk.taf.executor.commons.MultilinePropertiesConverter.stringToPropertyMap;
import static org.hamcrest.Matchers.containsString;

public class MultilinePropertiesConverterTest {

    @Test
    public void testStringToPropertyMap() {
        Map<?, ?> map = stringToPropertyMap("");
        Assert.assertEquals(0, map.size());

        map = stringToPropertyMap("a=b" + LINE_SEPARATOR + "b=c");
        Assert.assertThat(map, IsMapContaining.<Object, Object>hasEntry("a", "b"));
        Assert.assertThat(map, IsMapContaining.<Object, Object>hasEntry("b", "c"));
    }

    @Test
    public void testPropertyMapToString() {
        String string = propertyMapToString(new Properties());
        Assert.assertEquals("", string);

        Properties properties = new Properties();
        properties.put("a", "b");
        properties.put("b", "c");

        string = propertyMapToString(properties);
        Assert.assertThat(string, containsString("a=b"));
        Assert.assertThat(string, containsString("b=c"));
    }

}