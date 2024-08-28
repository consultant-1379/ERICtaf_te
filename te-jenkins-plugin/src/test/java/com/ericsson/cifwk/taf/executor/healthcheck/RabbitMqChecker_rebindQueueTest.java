package com.ericsson.cifwk.taf.executor.healthcheck;

import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
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
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RabbitMqChecker_rebindQueueTest {

    public static final String TEST_MB_DOMAIN_ID = "test.execution";
    public static final String ER_QUEUE_NAME = TEST_MB_DOMAIN_ID + ".EventRepository.DefaultConsumer.durable";

    @Spy
    private RabbitMqChecker unit;

    @Mock
    private Connection connection;

    @Mock
    private Channel channel;

    @Mock
    GlobalTeSettings globalTeSettings;

    @Before
    public void setUp() throws Exception {
        doThrow(IOException.class).when(unit).verifyExchangeExistence(anyString(), any(Connection.class));
        doReturn(globalTeSettings).when(unit).getGlobalSettings();
        when(globalTeSettings.getReportMbDomainId()).thenReturn(TEST_MB_DOMAIN_ID);
        doReturn(connection).when(unit).createConnection(anyString(), anyInt(), anyString(), anyString());
        doReturn(channel).when(connection).createChannel();
    }

    @Test
    public void testRebindQueue_happyPath() throws Exception {
        doReturn(true).when(unit).erQueueExists(eq(connection), anyString());
        doNothing().when(unit).bindErQueueToExchange(any(Connection.class), anyString(), anyString(), anyString());
        Check.Result result = unit.rebindErQueue("exchangeName", connection);
        assertTrue(result.isSuccess());
        verify(unit, never()).createErQueue(any(Connection.class), anyString());
        verify(unit).bindErQueueToExchange(connection, ER_QUEUE_NAME, "exchangeName", TEST_MB_DOMAIN_ID);
    }

    @Test
    public void testRebindQueue_andRecreate() throws Exception {
        doReturn(false).when(unit).erQueueExists(eq(connection), anyString());
        doNothing().when(unit).bindErQueueToExchange(any(Connection.class), anyString(), anyString(), anyString());
        Check.Result result = unit.rebindErQueue("exchangeName", connection);
        assertTrue(result.isSuccess());
        verify(unit).createErQueue(connection, ER_QUEUE_NAME);
        verify(unit).bindErQueueToExchange(connection, ER_QUEUE_NAME, "exchangeName", TEST_MB_DOMAIN_ID);
    }

    @Test
    public void testRebindQueue_error() throws Exception {
        doReturn(true).when(unit).erQueueExists(eq(connection), anyString());
        doThrow(IOException.class).when(unit).bindErQueueToExchange(any(Connection.class), anyString(), anyString(), anyString());
        Check.Result result = unit.rebindErQueue("exchangeName", connection);
        assertFalse(result.isSuccess());
        verify(unit).bindErQueueToExchange(connection, ER_QUEUE_NAME, "exchangeName", TEST_MB_DOMAIN_ID);
    }

}