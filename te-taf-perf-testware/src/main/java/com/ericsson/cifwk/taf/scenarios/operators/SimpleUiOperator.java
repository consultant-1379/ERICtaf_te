package com.ericsson.cifwk.taf.scenarios.operators;

import com.ericsson.cifwk.taf.ui.sdk.GenericViewModel;

public interface SimpleUiOperator {

    void initBrowser();

    void closeBrowser();

    void openPage(String url);

    <T extends GenericViewModel> T getView(Class<T> clazz);

    <T> T getConfigParam(String paramName, Class<T> paramClass);

    void pauseAfter();
}
