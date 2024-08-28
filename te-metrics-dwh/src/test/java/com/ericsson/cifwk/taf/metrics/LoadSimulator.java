package com.ericsson.cifwk.taf.metrics;/*
 * COPYRIGHT Ericsson (c) 2014.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import com.ericsson.cifwk.taf.performance.sample.impl.AmqpClient;
import com.ericsson.cifwk.taf.performance.sample.impl.AmqpSampleWriter;
import org.junit.Test;

public class LoadSimulator {
    @Test
    public void testLoad() throws Exception {
        AmqpClient amqpClient = AmqpClient.create("amqp://atvts994.athtem.eei.ericsson.se:5672", "taf.samples");
        amqpClient.connect();
        AmqpSampleWriter writer = AmqpSampleWriter.create(amqpClient);

        HttpMetricsEmulator.emulate(writer, 100000);

        amqpClient.shutdown();

    }
}
