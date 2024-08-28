package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.scenarios.operators.SimpleUiOperator;
import com.ericsson.cifwk.taf.scenarios.operators.SimpleUiOperatorImpl;
import com.ericsson.cifwk.taf.scenarios.views.TafDocsPageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Provider;

public class TafDocsTest extends TafTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(TafDocsTest.class);

    @Inject
    private Provider<SimpleUiOperatorImpl> registry;

    @Test
    public void testTafDocSections() {
        long startTime = System.currentTimeMillis();

        TafConfiguration config = TafConfigurationProvider.provide();
        int repeatInSeconds = config.getProperty("repeat_for_in_seconds", int.class);
        String siteUrl = config.getString("site_url");

        SimpleUiOperator operator = registry.get();

        do {
            LOGGER.info("Opening browser");
            operator.initBrowser();
            operator.openPage(siteUrl);

            LOGGER.info("Looking for TAF docs components on page");
            TafDocsPageModel view = operator.getView(TafDocsPageModel.class);
            Assert.assertEquals(3, view.tafLandingBlocks.size());

            operator.pauseAfter();

            LOGGER.info("Closing browser");
            operator.closeBrowser();
        } while (System.currentTimeMillis() - startTime < repeatInSeconds * 1000);
    }

}
