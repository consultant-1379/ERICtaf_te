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
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.Button;
import com.ericsson.cifwk.taf.ui.sdk.GenericViewModel;
import com.ericsson.cifwk.taf.ui.sdk.TextBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.testng.Assert.assertTrue;

public class TafOverflowPageModel extends GenericViewModel {

    private static final Logger LOG = LoggerFactory.getLogger(TafOverflowPageModel.class);

    @UiComponentMapping(selectorType = SelectorType.CSS, selector = ".qa-search-field")
    TextBox searchBox;

    @UiComponentMapping(selectorType = SelectorType.CSS, selector = ".qa-search-button")
    Button searchButton;

    public void searchFor(String value) {
        searchBox.setText(value);
        searchButton.click();
    }

//    public void verifyNoResultsFound() {
//        assertThat(noResults.getText(), containsString("No results found"));
//    }

    public void verifyResults() {
        assertTrue(!getViewComponents(".qa-q-list-item", UiComponent.class).isEmpty());
    }
}
