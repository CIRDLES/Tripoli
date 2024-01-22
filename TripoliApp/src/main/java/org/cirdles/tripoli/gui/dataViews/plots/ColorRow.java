package org.cirdles.tripoli.gui.dataViews.plots;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import org.cirdles.tripoli.constants.TripoliConstants.DetectorPlotFlavor;


public class ColorRow extends HBox {

    public static final double ROW_HEIGHT = 25;
    private DetectorPlotFlavor plotFlavor;
    private String hexColor;

    public ColorRow(DetectorPlotFlavor plotFlavor, String hexColor) {
        super();
        this.plotFlavor = plotFlavor;
        this.hexColor = hexColor;
        layoutElements();
        this.setPrefHeight(ROW_HEIGHT);
        ColorRow reference = this;

    }

    private void layoutElements() {
        StringBuilder styleBuilder = new StringBuilder(getStyle());
        styleBuilder.append("-fx-border-width: 1pt; -fx-border-color: #000000;");
        setStyle("-fx-alignment: center");
        setStyle(styleBuilder.toString());
        Label title = new Label(plotFlavor.getName());
        title.setMinWidth(150);
        title.setPrefHeight(ROW_HEIGHT);
        getChildren().addAll(title, createColorPatch());
    }
    private Label createColorPatch() {
        Label label = new Label();
        label.setStyle("-fx-background-color: " + hexColor);
        label.setPrefWidth(100);
        label.setPrefHeight(ROW_HEIGHT);
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
