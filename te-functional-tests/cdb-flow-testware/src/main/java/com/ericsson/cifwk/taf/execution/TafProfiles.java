package com.ericsson.cifwk.taf.execution;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 08/06/2017
 */
public enum TafProfiles {

    LOCAL("local_tests"), DEFAULT("");

    private final String name;

    TafProfiles(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
