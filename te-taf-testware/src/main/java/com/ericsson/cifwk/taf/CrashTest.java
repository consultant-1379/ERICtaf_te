package com.ericsson.cifwk.taf;

import org.testng.annotations.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class CrashTest extends TafTestBase {

    @Test
    public void shouldCrashJvm() throws Exception {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        ((Unsafe) theUnsafe.get(null)).getByte(0);
    }

}
