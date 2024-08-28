package com.ericsson.cifwk.taf.executor.security;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Integer.MAX_VALUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 22.12.2016
 */
public class TestRuntimeSecurityManagerTest {

    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    private TestRuntimeSecurityManager securityManager;

    @Before
    public void setUp() {
        securityManager = new TestRuntimeSecurityManager();
    }

    @After
    public void tearDown() {
        System.setSecurityManager(null);
    }

    @Test
    public void getSoftLimit() {
        assertThat(securityManager.getSoftLimit(0)).isEqualTo(1);
        assertThat(securityManager.getSoftLimit(1)).isEqualTo(1);
        assertThat(securityManager.getSoftLimit(2)).isEqualTo(1);
        assertThat(securityManager.getSoftLimit(3)).isEqualTo(2);
        assertThat(securityManager.getSoftLimit(4)).isEqualTo(2);
        assertThat(securityManager.getSoftLimit(5)).isEqualTo(2);
        assertThat(securityManager.getSoftLimit(10)).isEqualTo(6);
        assertThat(securityManager.getSoftLimit(100)).isEqualTo(66);
        assertThat(securityManager.getSoftLimit(1000)).isEqualTo(666);
        assertThat(securityManager.getSoftLimit(Integer.MAX_VALUE)).isEqualTo(1431655764);
    }

    @Test
    public void getThreadsLimit() {

        // no property defined
        assertThat(securityManager.getThreadsLimit()).isEqualTo(MAX_VALUE);

        // empty property defined
        System.setProperty(TestRuntimeSecurityManager.THREADS_LIMIT_PROPERTY, "");
        assertThat(securityManager.getThreadsLimit()).isEqualTo(MAX_VALUE);

        // invalid value defined
        System.setProperty(TestRuntimeSecurityManager.THREADS_LIMIT_PROPERTY, "notInteger");
        assertThat(securityManager.getThreadsLimit()).isEqualTo(MAX_VALUE);

        // valid value defined
        System.setProperty(TestRuntimeSecurityManager.THREADS_LIMIT_PROPERTY, "1000");
        assertThat(securityManager.getThreadsLimit()).isEqualTo(1000);
    }

    @Test
    public void getThreadDump() {
        String threadDump = securityManager.getThreadDump();
        assertThat(threadDump).contains("TestRuntimeSecurityManager.getThreadDump");
    }

    @Test
    public void integrationTest() throws InterruptedException {

        // happy path
        setSecurityManager("50");
        createConcurrentThreads(10);

        // resource leak
        setSecurityManager("20");
        try {
            createConcurrentThreads(30);
            fail();
        } catch (SecurityException e) {
            assertThat(e.getMessage())
                    .contains("Too many active threads")
                    .contains("maximum allowed is 20 (defined by Test Executor)");
        }

        assertEquals(21, TestRuntimeSecurityManager.getMaximumThreadsCount());
    }

    private void createConcurrentThreads(int threads) throws InterruptedException {
        List<Thread> createdThreads = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            System.out.println("Creating thread " + i);
            createdThreads.add(createAndStartThread());
            Thread.sleep(10);
        }
        for (Thread thread : createdThreads) {
            thread.join();
        }
    }

    private Thread createAndStartThread() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                System.out.println("New Thread started");
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
        };
        thread.start();
        return thread;
    }

    private void setSecurityManager(String threadsLimit) {
        System.setProperty(TestRuntimeSecurityManager.THREADS_LIMIT_PROPERTY, threadsLimit);
        System.setSecurityManager(new TestRuntimeSecurityManager());
    }

}