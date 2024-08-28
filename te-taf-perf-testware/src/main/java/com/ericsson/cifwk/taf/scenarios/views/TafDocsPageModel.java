package com.ericsson.cifwk.taf.scenarios.views;

import com.ericsson.cifwk.taf.ui.core.SelectorType;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.GenericViewModel;

import java.util.List;

public class TafDocsPageModel extends GenericViewModel {

    @UiComponentMapping(selectorType = SelectorType.CSS, selector = ".eaTafLanding-links")
    public List<UiComponent> tafLandingBlocks;

}
