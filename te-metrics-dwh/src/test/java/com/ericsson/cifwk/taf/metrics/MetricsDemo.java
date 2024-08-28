package com.ericsson.cifwk.taf.metrics;

import com.ericsson.cifwk.taf.metrics.queue.DbWriter;
import com.ericsson.cifwk.taf.metrics.queue.SampleMessageConsumer;
import com.ericsson.cifwk.taf.performance.sample.SampleWriter;
import com.ericsson.cifwk.taf.performance.sample.impl.AmqpClient;
import com.ericsson.cifwk.taf.performance.sample.impl.AmqpSampleWriter;
import com.esotericsoftware.kryo.Kryo;
import com.google.common.base.Throwables;
import com.google.common.io.Resources;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;

public final class MetricsDemo {

    private final Logger logger = LoggerFactory.getLogger(MetricsDemo.class);
    private final Configuration configuration;

    public MetricsDemo(Configuration configuration) {
        this.configuration = configuration;
    }

    public static void main(String[] args) throws Exception {
        URL resource = Resources.getResource("com/ericsson/cifwk/taf/metrics/metrics.properties");
        CompositeConfiguration configuration = new CompositeConfiguration();
        configuration.addConfiguration(new SystemConfiguration());
        try {
            configuration.addConfiguration(new PropertiesConfiguration(resource));
        } catch (ConfigurationException e) {
            throw Throwables.propagate(e);
        }
        MetricsDemo demo = new MetricsDemo(configuration);
        demo.start();
    }

    private void start() throws Exception {
        startDirectWrite();
//        startConsumer();
//        startProducer();
    }

    private void startDirectWrite() throws Exception {
        String dbUrl = configuration.getString("conn.db.url");
        Connection connection = DriverManager.getConnection(dbUrl);
        DbWriter dbWriter = new DbWriter(connection);
        int count = configuration.getInt("emulator.samples");
        HttpMetricsEmulator.emulate(dbWriter, count);
    }

    private void startConsumer() throws Exception {
        logger.info("Starting consumer");
        String amqpUrl = configuration.getString("conn.amqp.url");
        String amqpExchange = configuration.getString("conn.amqp.exchange");
        AmqpClient amqp = AmqpClient.create(amqpUrl, amqpExchange);

        String dbUrl = configuration.getString("conn.db.url");
        Connection connection = DriverManager.getConnection(dbUrl);
        DbWriter dbWriter = new DbWriter(connection);

        logger.info("Starting AMQP -> DB transfer");
        SampleMessageConsumer amqpConsumer = new SampleMessageConsumer(new Kryo(), amqp, dbWriter);
        amqpConsumer.start();
    }

    private void startProducer() throws Exception {
        logger.info("Starting producer");
        String amqpUrl = configuration.getString("conn.amqp.url");
        String amqpExchange = configuration.getString("conn.amqp.exchange");
        int count = configuration.getInt("emulator.samples");
        AmqpClient amqp = AmqpClient.create(amqpUrl, amqpExchange);
        try (SampleWriter writer = AmqpSampleWriter.create(amqp)) {
            writer.initialize();
            HttpMetricsEmulator.emulate(writer, count);
        }
    }
}
