package com.ericsson.cifwk.taf.threads;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 04/01/2017
 */
public class ThreadCreatingTest extends TafTestBase {

    private static final int DEFAULT_THREADS_TO_CREATE = 1001;
    private static final long DEFAULT_THREAD_MILLIS_TO_WAIT = 20000;
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadCreatingTest.class);
    public static final int DEFAULT_DELAY_BETWEEN_THREAD_STARTS_MILLIS = 10;

    @Inject
    private TafConfiguration config;

    @Test
    public void shouldCreateManyThreads() throws Exception {
        int threadsToCreate = config.getInt("threadsToCreate", DEFAULT_THREADS_TO_CREATE);
        int delayBetweenThreads = config.getInt("threadCreationDelay", DEFAULT_DELAY_BETWEEN_THREAD_STARTS_MILLIS);
        long timeToWait = config.getProperty("threadTimeToWait", DEFAULT_THREAD_MILLIS_TO_WAIT, Long.class);
        LOGGER.info("This test is going to create {} threads now", threadsToCreate);
        LOGGER.info("Each thread will wait for {} millis", timeToWait);
        LOGGER.info("Delay between thread creation will be {} millis", delayBetweenThreads);
        createConcurrentThreads(threadsToCreate, delayBetweenThreads, timeToWait);
    }

    private void createConcurrentThreads(int threadsToCreate, int delayBetweenThreads, long timeToWait) throws InterruptedException {
        List<Thread> createdThreads = new ArrayList<>();
        for (int i = 1; i <= threadsToCreate; i++) {
            LOGGER.info("Creating thread " + i);
            createdThreads.add(createAndStartThread(i, timeToWait));
            Thread.sleep(delayBetweenThreads);
        }
        for (Thread thread : createdThreads) {
            thread.join();
        }
    }

    private Thread createAndStartThread(final int number, final long timeToWait) {
        Thread thread = new Thread(String.format("Thread-%d", number)) {
            @Override
            public void run() {
                LOGGER.info("New Thread started");
                try {
                    Thread.sleep(timeToWait);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
                LOGGER.info("Thread {} finished", number);
            }
        };
        try {
            thread.start();
        } catch (Throwable e) {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e1) {
                Thread.currentThread().interrupt();
            }
            throw e;
        }
        return thread;
    }

}
