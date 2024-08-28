package com.ericsson.cifwk.taf.executor.utils;

import com.ericsson.cifwk.taf.executor.TimeoutException;
import com.google.common.base.Optional;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TimeLimitedTaskTest {

    @Test(expected = TimeoutException.class, timeout = 2000)
    public void testPerformUntilTimeout_timesOut() throws Exception {
        final MyService service = mock(MyService.class);
        when(service.getString()).thenReturn(null);

        TimeLimitedTask.performUntilTimeout(new TimeLimitedWorker<String>() {
            @Override
            public Optional<String> doWork() {
                String result = service.getString();
                return result == null ? Optional.<String>absent() : Optional.of(result);
            }
        }, 1, 300);
    }

    @Test(timeout = 2000)
    public void testPerformUntilTimeout_toleratesFault() throws Exception {
        final MyService service = mock(MyService.class);
        final String string = "abcde";
        when(service.getString()).thenReturn(null).thenReturn(string);

        String value = TimeLimitedTask.performUntilTimeout(new TimeLimitedWorker<String>() {
            @Override
            public Optional<String> doWork() {
                String result = service.getString();
                return result == null ? Optional.<String>absent() : Optional.of(result);
            }
        }, 1, 300);
        Assert.assertEquals(string, value);
        verify(service, atLeast(2)).getString();
    }

    private interface MyService {

        String getString();

    }
}