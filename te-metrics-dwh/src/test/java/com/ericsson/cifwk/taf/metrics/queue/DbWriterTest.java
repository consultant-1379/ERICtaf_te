package com.ericsson.cifwk.taf.metrics.queue;

import com.ericsson.cifwk.taf.performance.sample.Sample;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Date;

@Ignore
public class DbWriterTest {

    private static final String JDBC = "jdbc:mysql://atvts994.athtem.eei.ericsson.se:3306/taf_performance?user=saiku_user&password=password";

    DbWriter dbWriter;

    @Before
    public void setUp() throws Exception {
        HikariDataSource dataSource = new HikariDataSource();

        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setConnectionTestQuery("SELECT 1");
        dataSource.setJdbcUrl(JDBC);

        dbWriter = new DbWriter(dataSource);
        dbWriter.initialize();
    }

    @After
    public void tearDown() throws Exception {
        dbWriter.close();
    }

    @Test
    public void checkOneRecordPersisted() {
        Sample sample = createSample("1", "suite", "test");

        dbWriter.write(sample);
    }

    @Test
    public void checkDuplicateRecords() {
        dbWriter.write(createSample("1", "suite", "test"));
        dbWriter.write(createSample("1", "suite", "test"));
    }

    private Sample createSample(String executionId, String suite, String testCase) {
        Sample sample = new Sample();
        sample.setEventTime(new Date());
        sample.setTarget(URI.create("http://ericsson.se"));
        sample.setVuserId("1");
        sample.setLatency(100);
        sample.setProtocol("http");
        sample.setRequestSize(100);
        sample.setResponseSize(200);
        sample.setResponseTime(1000);
        sample.setRequestType("PUT");

        sample.setExecutionId(executionId);
        sample.setTestSuite(suite);
        sample.setTestCase(testCase);
        return sample;
    }

}