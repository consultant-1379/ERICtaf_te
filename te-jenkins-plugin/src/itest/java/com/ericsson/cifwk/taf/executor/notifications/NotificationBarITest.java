package com.ericsson.cifwk.taf.executor.notifications;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class NotificationBarITest {
    NotificationBar spy;

    @Before
    public void setUp() throws Exception {
        spy = spy(new NotificationBar());
        doReturn(6000).when(spy).getRefreshIntervalMs();

    }

    @Test
    public void testName() throws Exception {
        spy.notify(NotificationBar.NotificationType.ERROR, "One");
        Thread.sleep(2000);
        spy.notify(NotificationBar.NotificationType.ERROR, "Two");
        Thread.sleep(2000);
        spy.notify(NotificationBar.NotificationType.ERROR, "Three");

        String result = spy.getMessages().toString();

        assertTrue(result.contains("One"));
        assertTrue(result.contains("Two"));
        assertTrue(result.contains("Three"));

        Thread.sleep(2000);

        result = spy.getMessages().toString();

        assertFalse(result.contains("One"));
        assertTrue(result.contains("Two"));
        assertTrue(result.contains("Three"));

        Thread.sleep(2000);

        result = spy.getMessages().toString();

        assertFalse(result.contains("One"));
        assertFalse(result.contains("Two"));
        assertTrue(result.contains("Three"));

        Thread.sleep(2000);

        result = spy.getMessages().toString();

        assertEquals("[]", result);

        result = spy.getMessages().toString();

        assertEquals("[]", result);
    }
}
