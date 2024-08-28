package com.ericsson.cifwk.taf.sampletests;

import com.ericsson.cifwk.taf.TafTestBase;
import com.google.common.collect.Iterables;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 18/02/2016
 */
public class Java8Test extends TafTestBase {

    @Test
    public void lambdaTest() {
        List<String> list = Arrays.asList("a", "b");
        String result = Iterables.find(list, value -> value.equals("a"));
        assertEquals("a", result);
    }

}
