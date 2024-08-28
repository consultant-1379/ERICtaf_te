package com.ericsson.cifwk.taf.metrics;
/*
 * COPYRIGHT Ericsson (c) 2014.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import com.ericsson.cifwk.taf.metrics.queue.DbWriter;
import com.ericsson.cifwk.taf.metrics.queue.SampleMessageConsumer;
import com.ericsson.cifwk.taf.performance.PerformancePluginServices;
import com.ericsson.cifwk.taf.performance.metric.MetricsName;
import com.ericsson.cifwk.taf.performance.metric.MetricsWriter;
import com.ericsson.cifwk.taf.performance.metric.OperationResult;
import com.ericsson.cifwk.taf.performance.sample.Sample;
import com.ericsson.cifwk.taf.performance.sample.impl.AmqpClient;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;

public class MetricsSampleMessageConsumer extends SampleMessageConsumer {
    public static final String METRICS_DWH = "metricsDwh2";
    MetricsWriter metrics;
    Runtime runtime = Runtime.getRuntime();

    public MetricsSampleMessageConsumer(Kryo kryo, AmqpClient amqp, DbWriter dbWriter) {
        super(kryo, amqp, dbWriter);
        metrics = PerformancePluginServices.getDefaultMetricsWriter();
    }

    @Override
    protected void consumeSample(final byte[] body) {
        final Sample sample = measure(new Measurable<Sample>() {
            @Override
            public Sample run() throws Exception {
                return kryo.readObject(new Input(body), Sample.class);
            }
        }, "readFromAmqp");

        measure(new Measurable() {
            @Override
            public Object run() throws Exception {
                dbWriter.write(sample);
                return null;
            }
        }, "writeToMysql");

        metrics.update(MetricsName.builder()
                .group(METRICS_DWH)
                .protocol("usedmemory")
                .build(),
                (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024,
                OperationResult.UNKNOWN);

    }

    private <T> T measure(Measurable<T> measurable, String name) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        T result = null;
        OperationResult resultStatus = OperationResult.SUCCESS;

        try {
            result = measurable.run();
        } catch (Exception e) {
            resultStatus = OperationResult.FAILURE;
        } finally {
            metrics.update(MetricsName.builder()
                    .group(METRICS_DWH)
                    .protocol(name)
                    .build(),
                    stopwatch.elapsed(TimeUnit.NANOSECONDS),
                    resultStatus);
        }

        return result;
    }

    private interface Measurable<T> {
        T run() throws Exception;
    }
}
