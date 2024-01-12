package org.cirdles.tripoli.gui.dataViews.plots;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import org.cirdles.tripoli.constants.TripoliConstants.DetectorPlotFlavor;

public class ColorRow extends HBox {
    private DetectorPlotFlavor plotFlavor;
    private String hexColor;

    public ColorRow(DetectorPlotFlavor plotFlavor, String hexColor) {
        super();
        this.plotFlavor = plotFlavor;
        this.hexColor = hexColor;
        layoutElements();
    }

    private void layoutElements() {
        StringBuilder styleBuilder = new StringBuilder(getStyle());
        styleBuilder.append("-fx-border-width: 1pt; -fx-border-color: #000000;");
        setStyle("-fx-alignment: center");
        setStyle(styleBuilder.toString());
        Label title = new Label(plotFlavor.getName());
        getChildren().addAll(title, createColorPatch());
    }
    private Label createColorPatch() {
        Label label = new Label(" ".repeat(plotFlavor.getName().length()));
        label.setStyle("-fx-background-color: " + hexColor);
        return label;
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
