package com.ericsson.cifwk.taf.executor.eiffel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EiffelMessageHelperTest {

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerException_when_testWares_is_null() {
        EiffelMessageHelper.parseTestWares(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerException_when_testResources_is_null() {
        EiffelMessageHelper.parseTestResource(null);
    }

}
