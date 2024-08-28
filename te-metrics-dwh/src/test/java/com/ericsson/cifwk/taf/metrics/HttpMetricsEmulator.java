package com.ericsson.cifwk.taf.metrics;

import com.ericsson.cifwk.taf.performance.sample.Sample;
import com.ericsson.cifwk.taf.performance.sample.SampleBuilder;
import com.ericsson.cifwk.taf.performance.sample.SampleWriter;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

public final class HttpMetricsEmulator {

    private final Logger logger = LoggerFactory.getLogger(HttpMetricsEmulator.class);
    private final SampleWriter dumpling;
    private final String executionId;
    private String testSuite;
    private String testCase;

    public HttpMetricsEmulator(SampleWriter dumpling, String executionId) {
        this.dumpling = dumpling;
        this.executionId = executionId;
    }

    public static void emulate(SampleWriter dumpling, final int count) throws IOException {
        String executionId = UUID.randomUUID().toString();
        HttpMetricsEmulator emulator = new HttpMetricsEmulator(dumpling, executionId);
        emulator.run(count);
    }

    private void run(int totalCount) throws IOException {
        logger.info("Will generate dataset with execution id: {}", executionId);
        logger.info("Generating: ~{}", totalCount);

        int count = totalCount;
        Random random = new Random();
        HttpSampleGenerator sampleGenerator = new HttpSampleGenerator(random);

        while (true) {
            if (count < 0) {
                break;
            }
            logger.info("Remaining samples: {}", count);
            startTestSuite("Suite " + UUID.randomUUID());
            int testCaseCount = random.nextInt(20) + 2;
            for (int i = 0; i < testCaseCount; i++) {
                startTestCase("Test " + UUID.randomUUID());
                int sampleCount = random.nextInt(20) + 2;
                for (int j = 0; j < sampleCount; j++) {
                    SampleBuilder sampleBuilder = sampleGenerator.random();
                    Sample sample = randomSample(sampleBuilder);
                    dumpling.write(sample);
                    count--;
                }
                finishTestCase();
            }
            finishTestSuite();
            dumpling.flush();
        }
        logger.info("Sent {} samples", totalCount - count);
    }

    public void startTestSuite(String name) {
        testSuite = name;
    }

    public void finishTestSuite() {
        testSuite = null;
    }

    public void startTestCase(String name) {
        testCase = name;
    }

    public void finishTestCase() {
        testCase = null;
    }

    public Sample randomSample(SampleBuilder builder) {
        Preconditions.checkNotNull(testSuite, "Test suite not started");
        Preconditions.checkNotNull(testCase, "Test case not started");
        return builder
                .executionId(executionId)
                .testSuite(testSuite)
                .testCase(testCase)
                .build();
    }

}
