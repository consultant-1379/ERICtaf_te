package com.ericsson.cifwk.taf.executor.security;

import com.ericsson.cifwk.taf.annotations.Attachment;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;

/**
 * Extends Java Security Manager to limit maximum number of threads in JVM.
 * System threads required by JVM (garbage collector, jmx, finalizers, etc.)
 * are allowed to be created any time. Threads created by user
 * (testware or TAF tools) are limited.
 * <p>
 * Currently active threads are calculated without synchronization
 * (fast, but not precise), they include both user and system (JVM) threads.
 * <p>
 * If limit is reached:
 * - {@link SecurityException} is thrown
 * - thread dump is put into logs
 * - thread dump is attached to currently running test in Allure report (if Allure is used)
 * <p>
 * Limit is configured by according system property.
 *
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 22.12.2016
 */
public class TestRuntimeSecurityManager extends NoCheckSecurityManager {

    public static final String THREADS_LIMIT_PROPERTY = "taf.jvm.threads.limit";

    public static final String THREAD_DUMP_ATTACHMENT_NAME = "Thread Dump";

    private static final Logger LOG = LoggerFactory.getLogger(TestRuntimeSecurityManager.class);

    private static final String NL = System.getProperty("line.separator");

    private static final String WARNING = "TAF Security Manager will not limit user threads creation.";

    private static final ThreadGroup ROOT_GROUP = getRootGroup();

    private static AtomicInteger maximumThreadsCount = new AtomicInteger();

    /**
     * Hard limit for threads count (including JVM system threads).
     *
     * On being reached stack dump is collected, logged (attached to Allure report)
     * and {@link SecurityException} is thrown.
     */
    private final int THREADS_LIMIT;

    /**
     * Soft limit for threads count (including JVM system threads).
     * By default is equal to 2/3 of hard limit.
     *
     * On being reached warning message is logged
     * (with current active threads count and current hard limit).
     */
    private final int THREADS_SOFT_LIMIT;

    public TestRuntimeSecurityManager() {
        LOG.info("TestRuntimeSecurityManager activated now");
        THREADS_LIMIT = getThreadsLimit();
        THREADS_SOFT_LIMIT = getSoftLimit(THREADS_LIMIT);
        LOG.info("Threads limit is {}", THREADS_LIMIT);
        LOG.info("Threads soft limit is {}", THREADS_SOFT_LIMIT);
    }

    @VisibleForTesting
    protected final int getSoftLimit(int hardLimit) {
        return Math.max(1, hardLimit / 3 * 2);
    }

    @Override
    public void checkAccess(ThreadGroup threadGroup) {
        super.checkAccess(threadGroup);

        // do not limit creation of system threads (required by JVM)
        if (ROOT_GROUP == threadGroup) {
            return;
        }

        int estimatedActiveCounts = ROOT_GROUP.activeCount();

        if (estimatedActiveCounts > maximumThreadsCount.get()) {
            maximumThreadsCount.set(estimatedActiveCounts);
            final String logMessage = "Maximum active threads count observed increased to " + estimatedActiveCounts;
            System.out.println(logMessage);
            LOG.info(logMessage);
        }

        if (estimatedActiveCounts > THREADS_LIMIT) {

            // logging error
            String message = format("Too many active threads %s, maximum allowed is %s (defined by Test Executor)", estimatedActiveCounts, THREADS_LIMIT);
            LOG.error(message);
            LOG.warn("Getting Thread Dump...");

            // getting and logging thread dump
            long now = System.currentTimeMillis();
            String threadDump = getThreadDump();
            long millisPassed = System.currentTimeMillis() - now;
            LOG.warn(format("Getting Thread Dump took %s millis", millisPassed));
            LOG.error(format("Thread Dump: %n%n%s%n%n", threadDump));

            // breaking test execution
            throw new SecurityException(message);
        }

        // predicting limit overflow
        if (estimatedActiveCounts > THREADS_SOFT_LIMIT) {
            LOG.warn(format("Active threads number (%s) will soon reach the limit (%s) defined by TAF Test Executor", estimatedActiveCounts, THREADS_LIMIT));
        }
    }

    protected final int getThreadsLimit() {

        // no system property found
        String threadsLimitProperty = System.getProperty(THREADS_LIMIT_PROPERTY);
        if (threadsLimitProperty == null || threadsLimitProperty.isEmpty()) {
            LOG.warn(format("Threads limit property (%s) is not defined. %s", THREADS_LIMIT_PROPERTY, WARNING));
            return Integer.MAX_VALUE;
        }

        // invalid value
        try {
            return Integer.parseInt(threadsLimitProperty);
        } catch (NumberFormatException e) {
            LOG.warn(format("Could not parse threads limit property (%s) from value '%s'. %s", THREADS_LIMIT_PROPERTY, threadsLimitProperty, WARNING));
            return Integer.MAX_VALUE;
        }
    }

    @Attachment(value = THREAD_DUMP_ATTACHMENT_NAME, type = "text/plain")
    protected String getThreadDump() {

        StringBuilder sb = new StringBuilder();
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        final int maxNumberOfStackTraceEntries = 100;
        ThreadInfo[] threadInfos = mxBean.getThreadInfo(mxBean.getAllThreadIds(), maxNumberOfStackTraceEntries);
        for (ThreadInfo threadInfo : threadInfos) {
            String threadName = threadInfo.getThreadName();
            Thread.State threadState = threadInfo.getThreadState();

            sb.append('"').append(threadName).append('"').append(NL);
            sb.append("    java.lang.Thread.State: ").append(threadState);
            for (StackTraceElement el : threadInfo.getStackTrace()) {
                sb.append(NL).append("        at ").append(el);
            }
            sb.append(NL).append(NL);
        }
        return sb.toString();
    }

    private static ThreadGroup getRootGroup() {
        ThreadGroup root = Thread.currentThread().getThreadGroup();
        while (root.getParent() != null) {
            root = root.getParent();
        }
        return root;
    }

    public static int getMaximumThreadsCount() {
        return maximumThreadsCount.get();
    }
}
