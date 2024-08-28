package com.ericsson.cifwk.taf.execution;

import org.testng.annotations.BeforeClass;

import static com.ericsson.cifwk.taf.execution.RunScheduleLocalTest.loadTafProfileData;

/**
 * Run this test to create mandatory TE projects on embedded Jenkins started via hpi:run
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 27/06/2017
 */
public class LocalTeJenkinsJobCreatingTest extends TeJenkinsJobCreatingTest {

    @BeforeClass
    public static void beforeTests() {
        loadTafProfileData(TafProfiles.LOCAL);
    }
}
