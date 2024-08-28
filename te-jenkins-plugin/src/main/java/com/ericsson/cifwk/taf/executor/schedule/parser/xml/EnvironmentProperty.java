package com.ericsson.cifwk.taf.executor.schedule.parser.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 12/02/2016
 */
@Root(name = "property")
public class EnvironmentProperty {

    @Attribute(name = "type")
    private String type;

    @Attribute(name = "key")
    private String key;

    @Text
    private String value;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
