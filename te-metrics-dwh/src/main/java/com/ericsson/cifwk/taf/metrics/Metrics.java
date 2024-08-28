package com.ericsson.cifwk.taf.metrics;

import com.ericsson.cifwk.taf.metrics.queue.DbWriter;
import com.ericsson.cifwk.taf.metrics.queue.SampleMessageConsumer;
import com.ericsson.cifwk.taf.performance.sample.impl.AmqpClient;
import com.esotericsoftware.kryo.Kryo;
import com.google.common.base.Throwables;
import com.google.common.io.Resources;
import com.rabbitmq.client.ConnectionFactory;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 *
 */
public final class Metrics {

    private static final Logger logger = LoggerFactory.getLogger(Metrics.class);

    private static final String LOCAL_PROPERTIES = "local.properties";
    public static final int HEARTBEAT = 30;

    public static void main(String[] args) throws Exception {
        logger.info("Loading configuration");
        Configuration configuration = loadConfiguration();

        final DbWriter dbWriter = createDbWriter(configuration);
        final SampleMessageConsumer amqpConsumer = createAmqpConsumer(configuration, dbWriter);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.info("Shutting down metrics server");
                amqpConsumer.shutdown();
                try {
                    dbWriter.close();
                } catch (IOException e) {   // NOSONAR
                    // ignore
                }
            }
        });
    }

    private static DbWriter createDbWriter(Configuration configuration) {
        String dbUrl = configuration.getString("conn.db.url");
        logger.info("Connecting to MySQL using '{}'", dbUrl);

        try (HikariDataSource dataSource = new HikariDataSource()) {
            dataSource.setDriverClassName("com.mysql.jdbc.Driver");
            dataSource.setConnectionTestQuery("SELECT 1");
            dataSource.setJdbcUrl(dbUrl);

            DbWriter dbWriter = new DbWriter(dataSource);
            dbWriter.initialize();
            return dbWriter;
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private static SampleMessageConsumer createAmqpConsumer(Configuration configuration, DbWriter dbWriter) throws NoSuchAlgorithmException, KeyManagementException, URISyntaxException, IOException {
        String amqpUrl = configuration.getString("conn.amqp.url");
        String amqpExchange = configuration.getString("conn.amqp.exchange");
        logger.info("Starting listening to '{}:{}' with heartbeat {}", amqpUrl, amqpExchange, HEARTBEAT);
        AmqpClient amqp = createAmqpClient(amqpUrl, amqpExchange);
        logger.info("Starting AMQP -> DB transfer");
        Kryo kryo = new Kryo();
        final SampleMessageConsumer amqpConsumer = new SampleMessageConsumer(kryo, amqp, dbWriter);
        amqpConsumer.start();
        return amqpConsumer;
    }

    private static AmqpClient createAmqpClient(String amqpUrl, String amqpExchange) throws NoSuchAlgorithmException, KeyManagementException, URISyntaxException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(amqpUrl);
        factory.setRequestedHeartbeat(HEARTBEAT);
        return new AmqpClient(factory, amqpExchange);
    }

    public static Configuration loadConfiguration() {
        URL resource = Resources.getResource("com/ericsson/cifwk/taf/metrics/metrics.properties");
        CompositeConfiguration configuration = new CompositeConfiguration();
        configuration.addConfiguration(new SystemConfiguration());

        if (Files.exists(Paths.get(LOCAL_PROPERTIES))) {
            try {
                PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration(LOCAL_PROPERTIES);
                configuration.addConfiguration(propertiesConfiguration);
            } catch (ConfigurationException e) {
                throw Throwables.propagate(e);
            }
        }

        try {
            PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration(resource);
            configuration.addConfiguration(propertiesConfiguration);
        } catch (ConfigurationException e) {
            throw Throwables.propagate(e);
        }
        return configuration;
    }

}
