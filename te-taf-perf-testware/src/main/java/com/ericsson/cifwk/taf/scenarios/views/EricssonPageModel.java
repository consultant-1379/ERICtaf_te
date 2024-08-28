/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.cifwk.taf.scenarios.views;

import com.ericsson.cifwk.taf.ui.core.SelectorType;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.Button;
import com.ericsson.cifwk.taf.ui.sdk.GenericViewModel;
import com.ericsson.cifwk.taf.ui.sdk.Label;
import com.ericsson.cifwk.taf.ui.sdk.TextBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class EricssonPageModel extends GenericViewModel {

    private static final Logger LOG = LoggerFactory.getLogger(EricssonPageModel.class);

    @UiComponentMapping(selectorType = SelectorType.CSS, selector = ".eSearchFormInput")
    TextBox ericssonSearchBox;
    @UiComponentMapping(selectorType = SelectorType.CSS, selector = ".eSearchFormSubmit")
    Button ericssonSearchButton;

    @UiComponentMapping(selectorType = SelectorType.CSS, selector = ".eNoResults h2")
    Label noResults;

    @UiComponentMapping(selectorType = SelectorType.CSS, selector = "#eSearchList li h2")
    Label firstResult;

    public void searchFor(String value) {
        ericssonSearchBox.setText(value);
        ericssonSearchButton.click();
    }

    public void verifyNoResultsFound() {
        assertThat(noResults.getText(), containsString("No results found"));
    }

    public void verifyResults(String result) {
        assertThat(firstResult.getText(), containsString(result));
    }
}
