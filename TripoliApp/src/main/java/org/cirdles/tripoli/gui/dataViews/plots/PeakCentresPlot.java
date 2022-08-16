package org.cirdles.tripoli.gui.dataViews.plots;

import javafx.scene.shape.Rectangle;
import org.cirdles.tripoli.visualizationUtilities.linePlots.LinePlotBuilder;

public class PeakCentresPlot extends AbstractDataView {

    /**
     * @param bounds
     * @param linePlotBuilder
     */
    protected PeakCentresPlot(Rectangle bounds, LinePlotBuilder linePlotBuilder) {
        super(bounds, 100, 100);
    }

    @Override
    public void preparePanel() {

    }
}
