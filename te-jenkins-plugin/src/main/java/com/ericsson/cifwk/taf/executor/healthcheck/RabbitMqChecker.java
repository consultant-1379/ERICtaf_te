package com.ericsson.cifwk.taf.executor.healthcheck;

import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;
import com.ericsson.cifwk.taf.executor.utils.GlobalTeSettingsProvider;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 19/02/2016
 */
public class RabbitMqChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMqChecker.class);

    public Check.Result checkExchange(String host, Integer port, String exchangeName,
                                      String username, String password,
                                      boolean createExchangeIfMissing) {
        if (port == null) {
            return new Check.Result(false, "AMQP port is undefined");
        }
        Connection connection = null;
        try {
            connection = createConnection(host, port, username, password);
            try {
                verifyExchangeExistence(exchangeName, connection);
            } catch (IOException e) {
                String errorMessage = String.format("Exchange '%s' doesn't exist on host '%s'", exchangeName, host);
                LOGGER.error(healthCheckErrorMsg(errorMessage), e);
                if (createExchangeIfMissing) {
                    Check.Result creationResult = tryToCreateExchange(connection, host, exchangeName);
                    if (!creationResult.isSuccess()) {
                        return creationResult;
                    }
                    // Re-bind queue to exchange as well, otherwise ER fails to proceed without restart
                    Check.Result rebindResult = rebindErQueue(exchangeName, connection);
                    if (!rebindResult.isSuccess()) {
                        return rebindResult;
                    }
                } else {
                    return new Check.Result(false, errorMessage);
                }
            }
        } catch (Exception e) {
            LOGGER.error(healthCheckErrorMsg("Failed to connect to AMQP: " + host), e);
            return new Check.Result(false, e.getMessage());
        } finally {
            closeConnectionSafely(connection);
        }

        return new Check.Result(true, "");
    }

    @VisibleForTesting
    Check.Result rebindErQueue(String exchangeName, Connection connection) {
        GlobalTeSettings globalSettings = getGlobalSettings();
        String reportMbDomainId = globalSettings.getReportMbDomainId();
        String queueName = String.format("%s.EventRepository.DefaultConsumer.durable", reportMbDomainId);
        try {
            if (!erQueueExists(connection, queueName)) {
                createErQueue(connection, queueName);
            }
            bindErQueueToExchange(connection, queueName, exchangeName, reportMbDomainId);
        } catch (IOException e) {
            LOGGER.error(healthCheckErrorMsg("Failed to rebind queue to new exchange: "), e);
            return new Check.Result(false, e.getMessage());
        }
        return new Check.Result(true, "");
    }

    @VisibleForTesting
    void bindErQueueToExchange(Connection connection, String queueName,
                               String exchangeName, String reportMbDomainId) throws IOException {
        Channel channel = connection.createChannel();
        try {
            channel.queueBind(queueName, exchangeName, "#." + reportMbDomainId);
        } finally {
            closeChannelSafely(channel);
        }
    }

    @VisibleForTesting
    void createErQueue(Connection connection, String queueName) throws IOException {
        Channel channel = connection.createChannel();
        try {
            channel.queueDeclare(queueName, true, false, false, Maps.<String, Object>newHashMap());
        } finally {
            closeChannelSafely(channel);
        }
    }

    @VisibleForTesting
    boolean erQueueExists(Connection connection, String queueName) throws IOException {
        Channel channel = connection.createChannel();
        try {
            channel.queueDeclarePassive(queueName);
            return true;
        } catch (IOException e) { // NOSONAR
            return false;
        } finally {
            closeChannelSafely(channel);
        }
    }

    @VisibleForTesting
    GlobalTeSettings getGlobalSettings() {
        return GlobalTeSettingsProvider.getInstance().provide();
    }

    @VisibleForTesting
    void verifyExchangeExistence(String exchangeName, Connection connection) throws IOException {
        Channel channel = connection.createChannel();
        try {
            channel.exchangeDeclarePassive(exchangeName);
        } finally {
            closeChannelSafely(channel);
        }
    }

    @VisibleForTesting
    Connection createConnection(String host, Integer port, String username, String password) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        return factory.newConnection();
    }

    @VisibleForTesting
    Check.Result tryToCreateExchange(Connection connection, String host, String exchangeName) throws IOException {
        String errorMessage = String.format("Failed to recreate missing exchange '%s' on host '%s'. " +
                "This will lead to missing Eiffel events but will not affect the test runs", exchangeName, host);
        Channel channel = connection.createChannel();
        try {
            createExchange(channel, exchangeName);
            return new Check.Result(true, "");
        } catch (Exception e) {
            LOGGER.error(healthCheckErrorMsg(errorMessage), e);
            return new Check.Result(false, errorMessage + ": " + e.getMessage());
        } finally {
            closeChannelSafely(channel);
        }
    }

    @VisibleForTesting
    AMQP.Exchange.DeclareOk createExchange(Channel channel, String exchangeName) throws IOException {
        return channel.exchangeDeclare(exchangeName, "topic");
    }

    private String healthCheckErrorMsg(String errorMsg) {
        return "TE Health check error: " + errorMsg;
    }

    @VisibleForTesting
    void closeConnectionSafely(Connection connection) {
        try {
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        } catch (Exception e) { // NOSONAR
            // Ignore
        }
    }

    @VisibleForTesting
    void closeChannelSafely(Channel channel) {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (Exception e) { // NOSONAR
            // Ignore
        }
    }

}
