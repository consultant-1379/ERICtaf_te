package com.ericsson.cifwk.taf.executor.schedule.parser.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 14/03/2016
 */
@Root(name = "test-campaign")
public class TestCampaign {

    @Attribute(name = "id")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
