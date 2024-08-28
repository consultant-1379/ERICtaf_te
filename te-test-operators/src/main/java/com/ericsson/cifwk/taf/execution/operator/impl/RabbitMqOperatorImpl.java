package com.ericsson.cifwk.taf.execution.operator.impl;

import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.execution.operator.RabbitMqOperator;
import com.google.common.base.Throwables;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Operator
@SuppressWarnings("unused")
public class RabbitMqOperatorImpl implements RabbitMqOperator {

    private static final Logger LOGGER = Logger.getLogger(RabbitMqOperatorImpl.class);

    @Override
    public boolean exchangeExists(Host mbHost, final String exchangeName) throws Exception {
        ChannelOperation<Boolean> channelOperation = new ChannelOperation<Boolean>() {
            @Override
            Boolean operation(Channel channel) {
                try {
                    channel.exchangeDeclarePassive(exchangeName);
                    return true;
                } catch (IOException e) { // NOSONAR
                    return false;
                }
            }
        };
        return channelOperation.actWithChannel(mbHost);
    }

    @Override
    public void deleteExchange(Host mbHost, final String exchangeName) {
        ChannelOperation<Void> channelOperation = new ChannelOperation<Void>() {
            @Override
            Void operation(Channel channel) {
                try {
                    channel.exchangeDelete(exchangeName);
                } catch (IOException e) {
                    LOGGER.error("Failed to delete exchange", e);
                    throw Throwables.propagate(e);
                }
                return null;
            }
        };
        channelOperation.actWithChannel(mbHost);
    }

    abstract class ChannelOperation<T> {

        final T actWithChannel(Host mbHost) {
            Connection connection = null;
            Channel channel = null;
            try {
                connection = createConnection(mbHost);
                channel = openChannel(connection);
                return operation(channel);
            }
            catch (IOException | TimeoutException e) {
                throw Throwables.propagate(e);
            } finally {
                closeChannelSafely(channel);
                closeConnectionSafely(connection);
            }
        }

        abstract T operation(Channel channel);

        private Channel openChannel(Connection connection) {
            try {
                return connection.createChannel();
            } catch (Exception e) {
                LOGGER.error("Failed to open channel", e);
                throw Throwables.propagate(e);
            }
        }

        private Connection createConnection(Host mbHost) throws IOException, TimeoutException {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(mbHost.getIp());
            factory.setPort(mbHost.getPort(Ports.AMQP));
            factory.setUsername(mbHost.getUser(UserType.ADMIN));
            factory.setPassword(mbHost.getPass(UserType.ADMIN));
            return factory.newConnection();
        }

    }

    private void closeConnectionSafely(Connection connection) {
        try {
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        } catch (Exception e) { // NOSONAR
            // Ignore
        }
    }

    private void closeChannelSafely(Channel channel) {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (Exception e) { // NOSONAR
            // Ignore
        }
    }

}
