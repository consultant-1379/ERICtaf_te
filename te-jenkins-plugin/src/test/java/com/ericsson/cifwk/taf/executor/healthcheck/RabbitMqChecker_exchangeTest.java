package com.ericsson.cifwk.taf.executor.healthcheck;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RabbitMqChecker_exchangeTest {

    @Spy
    private RabbitMqChecker unit;

    @Mock
    private Connection connection;

    @Mock
    private Channel channel;

    @Before
    public void setUp() throws Exception {
        doReturn(new Check.Result(true, "")).when(unit).rebindErQueue(anyString(), any(Connection.class));
        doReturn(connection).when(unit).createConnection(anyString(), anyInt(), anyString(), anyString());
        doReturn(channel).when(connection).createChannel();
    }

    @Test
    public void testCheckExchange_happyPath() throws Exception {
        doNothing().when(unit).verifyExchangeExistence(eq("exchangeName"), eq(connection));
        Check.Result result = unit.checkExchange("host", 5672, "exchangeName", "username", "password", true);
        verify(unit, never()).tryToCreateExchange(any(Connection.class), eq("host"), eq("exchangeName"));
        assertTrue(result.isSuccess());
    }

    @Test
    public void testCheckExchange_cantConnect() throws Exception {
        doThrow(new IOException()).when(unit).createConnection(anyString(), anyInt(), anyString(), anyString());
        Check.Result result = unit.checkExchange("host", 5672, "exchangeName", "username", "password", false);
        verify(unit, never()).tryToCreateExchange(any(Connection.class), eq("host"), eq("exchangeName"));
        assertFalse(result.isSuccess());
    }

    @Test
    public void testCheckExchange_missingExchange() throws Exception {
        doThrow(new IOException()).when(unit).verifyExchangeExistence(eq("exchangeName"), eq(connection));
        unit.checkExchange("host", 5672, "exchangeName", "username", "password", false);
        verify(unit, never()).tryToCreateExchange(any(Connection.class), eq("host"), eq("exchangeName"));

        unit.checkExchange("host", 5672, "exchangeName", "username", "password", true);
        verify(unit).tryToCreateExchange(eq(connection), eq("host"), eq("exchangeName"));
    }

    @Test
    public void testTryToCreateExchange_happyPath() throws Exception {
        Check.Result result = unit.tryToCreateExchange(connection, "host", "exchangeName");
        assertTrue(result.isSuccess());
    }

    @Test
    public void testTryToCreateExchange_failure() throws Exception {
        doThrow(new IOException()).when(unit).createExchange(eq(channel), eq("exchangeName"));
        Check.Result result = unit.tryToCreateExchange(connection, "host", "exchangeName");
        assertFalse(result.isSuccess());
        assertThat(result.getMessage(), startsWith("Failed to recreate missing exchange 'exchangeName' on host 'host'"));
    }

}