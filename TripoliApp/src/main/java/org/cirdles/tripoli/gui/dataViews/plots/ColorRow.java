package org.cirdles.tripoli.gui.dataViews.plots;

import javafx.scene.layout.HBox;
import org.cirdles.tripoli.constants.TripoliConstants.DetectorPlotFlavor;

public class ColorRow extends HBox {
    private DetectorPlotFlavor plotFlavor;
    private String hexColor;

    public ColorRow(DetectorPlotFlavor plotFlavor, String hexColor) {
        super();
        this.plotFlavor = plotFlavor;
        this.hexColor = hexColor;
    }

    public DetectorPlotFlavor getPlotFlavor() {
        return plotFlavor;
    }

    public void setPlotFlavor(DetectorPlotFlavor plotFlavor) {
        this.plotFlavor = plotFlavor;
    }

    public String getHexColor() {
        return hexColor;
    }

    public void setHexColor(String hexColor) {
        this.hexColor = hexColor;
    }
}
