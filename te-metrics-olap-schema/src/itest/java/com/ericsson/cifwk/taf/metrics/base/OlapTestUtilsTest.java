package com.ericsson.cifwk.taf.metrics.base;
/*
 * COPYRIGHT Ericsson (c) 2014.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class OlapTestUtilsTest {
    @Test
    public void testName() throws Exception {
        assertThat(OlapTestUtils.splitQuoted("a,b,c", ','), equalTo(new String[]{"a", "b", "c"}));
        assertThat(OlapTestUtils.splitQuoted("a,\"b,c", ','), equalTo(new String[]{"a", "\"b", "c"}));
        assertThat(OlapTestUtils.splitQuoted("a,\"b,c\"", ','), equalTo(new String[]{"a", "b,c"}));
        assertThat(OlapTestUtils.splitQuoted("\"a,b\",c", ','), equalTo(new String[]{"a,b", "c"}));
        assertThat(OlapTestUtils.splitQuoted("a,\"b,c\",d", ','), equalTo(new String[]{"a", "b,c","d"}));
        assertThat(OlapTestUtils.splitQuoted("a,\"b\",c", ','), equalTo(new String[]{"a", "b", "c"}));
    }
}
