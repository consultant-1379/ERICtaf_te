package com.ericsson.cifwk.taf.scenarios.operators;

import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.ui.Browser;
import com.ericsson.cifwk.taf.ui.BrowserTab;
import com.ericsson.cifwk.taf.ui.BrowserType;
import com.ericsson.cifwk.taf.ui.UI;
import com.ericsson.cifwk.taf.ui.sdk.GenericViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Operator
public class SimpleUiOperatorImpl implements SimpleUiOperator {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleUiOperatorImpl.class);

    private Browser browser;

    @Override
    public void initBrowser() {
        BrowserType browserType = BrowserType.HEADLESS;
        this.browser = UI.newBrowser(browserType);
    }

    @Override
    public void closeBrowser() {
        if (!browser.isClosed()) {
            browser.close();
        }
    }

    @Override
    public void openPage(String url) {
        BrowserTab browserTab = browser.open(url);
        browserTab.maximize();
    }

    @Override
    public <T extends GenericViewModel> T getView(Class<T> clazz) {
        BrowserTab browserTab = browser.getCurrentWindow();
        browserTab.maximize();
        return browserTab.getView(clazz);
    }

    @Override
    public <T> T getConfigParam(String paramName, Class<T> paramClass) {
        TafConfiguration config = TafConfigurationProvider.provide();
        return config.getProperty(paramName, paramClass);
    }

    @Override
    public void pauseAfter() {
        try {
            int delay = getConfigParam("delay_in_millis_afer_test", int.class);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            LOG.error("Interruped while sleeping");
            Thread.currentThread().interrupt();
        }

    }

}
