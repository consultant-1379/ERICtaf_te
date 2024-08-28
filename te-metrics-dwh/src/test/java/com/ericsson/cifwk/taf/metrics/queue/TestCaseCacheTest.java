package com.ericsson.cifwk.taf.metrics.queue;

import com.ericsson.cifwk.taf.performance.sample.Sample;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestCaseCacheTest {

    private TestCaseCache cache;

    @Before
    public void setUp() {
        cache = new TestCaseCache();
    }

    @Test
    public void testIfCacheWorks() {
        Sample sample1 = createSample("1", "ts1", "tc1");
        Sample sample2 = createSample("1", "ts1", "tc2");
        Sample sample3 = createSample("1", "ts2", "tc2");
        Sample sample4 = createSample("2", "ts1", "tc2");

        cache.updateCache(sample1, 123L);

        assertThat(cache.contains(sample1).isPresent(), equalTo(true));
        assertThat(cache.contains(sample1).get(), equalTo(123L));
        assertThat(cache.contains(sample2).isPresent(), equalTo(false));
        assertThat(cache.contains(sample3).isPresent(), equalTo(false));
        assertThat(cache.contains(sample4).isPresent(), equalTo(false));
    }

    private Sample createSample(String executionId, String testSuite, String testCase) {
        Sample sample = new Sample();
        sample.setExecutionId(executionId);
        sample.setTestSuite(testSuite);
        sample.setTestCase(testCase);
        return sample;
    }


}