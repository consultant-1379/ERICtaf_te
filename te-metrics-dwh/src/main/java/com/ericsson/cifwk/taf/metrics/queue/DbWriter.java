package com.ericsson.cifwk.taf.metrics.queue;

import com.ericsson.cifwk.taf.performance.sample.Sample;
import com.ericsson.cifwk.taf.performance.sample.SampleWriter;
import com.google.common.base.Optional;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang.time.DateUtils;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import static com.ericsson.cifwk.taf.metrics.jooq.Tables.*;

/**
 * Writes metrics directly into database.
 */
public final class DbWriter implements SampleWriter {

    private final TestCaseCache testCaseCache = new TestCaseCache();
    private Connection connection;
    private HikariDataSource dataSource;
    private DSLContext context;

    public DbWriter(Connection connection) {
        this.connection = connection;
    }

    public DbWriter(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void initialize() throws IOException {
        if (dataSource != null) {
            this.context = DSL.using(dataSource, SQLDialect.MYSQL);
        } else {
            this.context = DSL.using(connection, SQLDialect.MYSQL);
        }
    }

    @Override
    public void write(Sample sample) {
        Long testCaseId;


        Optional<Long> optionalId = testCaseCache.contains(sample);
        if (optionalId.isPresent()) {
            testCaseId = optionalId.get();
        } else {
            Long executionId = insertExecution(sample, context);
            Long testSuiteId = insertTestSuite(context, sample, executionId);
            testCaseId = insertTestCase(context, sample, testSuiteId);

            testCaseCache.updateCache(sample, testCaseId);
        }

        Timestamp timeId = insertTime(sample, context);
        insertSample(context, sample, timeId, testCaseId);
    }

    private Long insertExecution(Sample sample, DSLContext context) {
        context.insertInto(EXECUTIONS)
                .set(EXECUTIONS.NAME, sample.getExecutionId())
                .onDuplicateKeyIgnore()
                .execute();

        return context
                .selectFrom(EXECUTIONS)
                .where(EXECUTIONS.NAME.eq(sample.getExecutionId()))
                .fetchOne(EXECUTIONS.ID);
    }

    private Long insertTestSuite(DSLContext context, Sample sample, Long executionId) {
        context.insertInto(TEST_SUITES)
                .set(TEST_SUITES.NAME, sample.getTestSuite())
                .set(TEST_SUITES.EXECUTION_ID, executionId)
                .onDuplicateKeyIgnore()
                .execute();

        return context
                .selectFrom(TEST_SUITES)
                .where(TEST_SUITES.NAME.eq(sample.getTestSuite()))
                .and(TEST_SUITES.EXECUTION_ID.eq(executionId))
                .fetchOne(TEST_SUITES.ID);
    }

    private Long insertTestCase(DSLContext context, Sample sample, Long testSuiteId) {
        context.insertInto(TEST_CASES)
                .set(TEST_CASES.NAME, sample.getTestCase())
                .set(TEST_CASES.TEST_SUITE_ID, testSuiteId)
                .onDuplicateKeyIgnore()
                .execute();

        return context
                .selectFrom(TEST_CASES)
                .where(TEST_CASES.NAME.eq(sample.getTestCase()))
                .and(TEST_CASES.TEST_SUITE_ID.eq(testSuiteId))
                .fetchOne(TEST_CASES.ID);
    }

    private Timestamp insertTime(Sample sample, DSLContext context) {
        Date eventTime = sample.getEventTime();
        Date truncatedToMinute = DateUtils.truncate(eventTime, Calendar.MINUTE);
        Date truncatedToDate = DateUtils.truncate(eventTime, Calendar.DATE);

        Timestamp timestamp = new Timestamp(truncatedToMinute.getTime());

        context.insertInto(TIME)
                .set(TIME.ID, timestamp)
                .set(TIME.DAY, new java.sql.Date(truncatedToDate.getTime()))
                .set(TIME.HOUR, truncatedToMinute.getHours())
                .set(TIME.MINUTE, truncatedToMinute.getMinutes())
                .onDuplicateKeyIgnore()
                .execute();

        return timestamp;
    }

    private void insertSample(DSLContext context, Sample sample, Timestamp timeId, Long testCaseId) {
        context.insertInto(SAMPLES)
                .set(SAMPLES.THREAD_ID, sample.getThreadId())
                .set(SAMPLES.VUSER_ID, sample.getVuserId())
                .set(SAMPLES.TEST_CASE_ID, testCaseId)
                .set(SAMPLES.TIME_ID, timeId)
                .set(SAMPLES.PROTOCOL, sample.getProtocol())
                .set(SAMPLES.TARGET, sample.getTarget().toString())
                .set(SAMPLES.REQUEST_TYPE, sample.getRequestType())
                .set(SAMPLES.REQUEST_SIZE, sample.getRequestSize())
                .set(SAMPLES.RESPONSE_CODE, sample.getResponseCode())
                .set(SAMPLES.SUCCESS, (byte) (sample.isSuccess() ? 1 : 0))
                .set(SAMPLES.RESPONSE_TIME, sample.getResponseTime())
                .set(SAMPLES.LATENCY, sample.getLatency())
                .set(SAMPLES.RESPONSE_SIZE, sample.getResponseSize())
                .execute();
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
        dataSource.close();
    }

}
