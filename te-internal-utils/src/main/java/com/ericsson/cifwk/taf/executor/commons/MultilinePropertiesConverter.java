package com.ericsson.cifwk.taf.executor.commons;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 01/02/2016
 */
public class MultilinePropertiesConverter {

    @VisibleForTesting
    static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static Map<?, ?> stringToPropertyMap(String propertiesAsString) {
        if (StringUtils.isBlank(propertiesAsString)) {
            return Maps.newHashMap();
        }
        return Splitter.on(LINE_SEPARATOR).withKeyValueSeparator("=").split(propertiesAsString);
    }

    public static String propertyMapToString(Map<?, ?> propertyMap) {
        if (propertyMap == null) {
            return "";
        }
        return Joiner.on(LINE_SEPARATOR).withKeyValueSeparator("=").join(propertyMap);
    }

}
