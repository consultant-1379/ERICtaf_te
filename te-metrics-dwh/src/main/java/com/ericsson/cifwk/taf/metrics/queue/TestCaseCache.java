package com.ericsson.cifwk.taf.metrics.queue;

import com.ericsson.cifwk.taf.performance.sample.Sample;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.WeakHashMap;

/**
 *
 */
public final class TestCaseCache {

    private final WeakHashMap<String, Map<String, Map<String, Long>>> testCaseCache = new WeakHashMap<>();

    public Optional<Long> contains(Sample sample) {
        String executionId = sample.getExecutionId();
        String testSuite = sample.getTestSuite();
        String testCase = sample.getTestCase();

        if (testCaseCache.containsKey(executionId)
                && testCaseCache.get(executionId).containsKey(testSuite)
                && testCaseCache.get(executionId).get(testSuite).containsKey(testCase)) {
            Long id = testCaseCache.get(executionId).get(testSuite).get(testCase);
            return Optional.of(id);
        }

        return Optional.absent();
    }

    public void updateCache(Sample sample, Long id) {
        String executionId = sample.getExecutionId();
        String testSuite = sample.getTestSuite();
        String testCase = sample.getTestCase();

        Map<String, Map<String, Long>> testCaseMap;
        if (testCaseCache.containsKey(executionId)) {
            testCaseMap = testCaseCache.get(executionId);
        } else {
            testCaseMap = Maps.newHashMap();
            testCaseCache.put(executionId, testCaseMap);
        }

        Map<String, Long> testCases;
        if (testCaseMap.containsKey(testSuite)) {
            testCases = testCaseMap.get(testSuite);
        } else {
            testCases = Maps.newHashMap();
            testCaseMap.put(testSuite, testCases);
        }

        testCases.put(testCase, id);
    }

}
