package com.ericsson.cifwk.taf.execution.operator.model.ve;

import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.GenericViewModel;

public class VisualizationEngineViewModel extends GenericViewModel {

    @UiComponentMapping(".eaVEApp-rTreeChartArea")
    UiComponent treeChart;

    @UiComponentMapping(".eaVEApp-wTreeChart-svgArea > svg")
    UiComponent svg;

    public UiComponent getTreeChart() {
        return treeChart;
    }

    public UiComponent getSvg() {
        return svg;
    }

}
