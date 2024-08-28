package com.ericsson.cifwk.taf.executor.listeners;

import com.ericsson.cifwk.taf.spi.TafPlugin;
import com.ericsson.cifwk.taf.testng.CompositeTestNGListener;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 14/12/2015
 */
public class TeTafPlugin implements TafPlugin {

    public TeTafPlugin() { }

    @Override
    public void init() {
        CompositeTestNGListener.addListener(new TeExecutionListener(), 100);
        CompositeTestNGListener.addListener(new TeSuiteListener(), 101);
    }

    @Override
    public void shutdown() { }
}
