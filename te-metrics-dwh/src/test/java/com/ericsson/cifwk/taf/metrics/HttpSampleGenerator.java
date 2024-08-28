package com.ericsson.cifwk.taf.metrics;

import com.ericsson.cifwk.taf.performance.sample.Sample;
import com.ericsson.cifwk.taf.performance.sample.SampleBuilder;
import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;

import java.net.URI;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

public final class HttpSampleGenerator {

    private final Random random;
    private long clock;
    private final List<URI> targets;
    private final List<String> requestTypes;
    private final List<Integer> responseCodes;

    public HttpSampleGenerator(Random random) {
        this.random = random;
        clock = System.currentTimeMillis();
        targets = Arrays.asList(
                URI.create("http://example.com/foo"),
                URI.create("http://example.com/bar"),
                URI.create("http://example.com/baz")
        );
        requestTypes = Arrays.asList("GET", "POST", "PUT");
        responseCodes = Arrays.asList(200, 400, 404, 500);
    }

    public SampleBuilder random() {
        int responseTime = random.nextInt(2000) + 50;
        int latency = responseTime + random.nextInt(500);
        int responseCode = randomItem(responseCodes);
        SampleBuilder builder = Sample.builder()
                .vuserId("vuser" + Thread.currentThread().getId())
                .eventTime(new Timestamp(new Date(clock).getTime()))
                .protocol("http")
                .target(randomItem(targets))
                .requestType(randomItem(requestTypes))
                .requestBody(randomPayload(), Charsets.UTF_8)
                .responseCode(responseCode)
                .success(responseCode < 400)
                .responseTime(responseTime)
                .latency(latency);
        if (random.nextBoolean()) {
            builder.responseBody(randomPayload(), Charsets.UTF_8);
        }

        clock += latency;
        return builder;
    }

    private <T> T randomItem(List<T> items) {
        int index = random.nextInt(items.size());
        return items.get(index);
    }

    private String randomPayload() {
        byte[] buffer = new byte[random.nextInt(50)];
        random.nextBytes(buffer);
        return BaseEncoding.base16()
                .omitPadding()
                .encode(buffer);
    }

}
