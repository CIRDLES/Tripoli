package org.cirdles.tripoli.gui.dataViews.plots;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.cirdles.tripoli.constants.TripoliConstants.DetectorPlotFlavor;
import org.cirdles.tripoli.gui.utilities.HexColorConverter;


public class ColorRow extends HBox {

    public static final double ROW_HEIGHT = 25;
    private DetectorPlotFlavor plotFlavor;
    private String hexColor;
    private Label colorPatch;
    private ColorPicker colorPickerReference;

    private static class ColorChangeListener implements ChangeListener<Color> {

        private ColorRow currentRowReference;
        @Override
        public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
            currentRowReference.setHexColor(HexColorConverter.getHexColor(newValue));
            currentRowReference.getColorPatch().setStyle("-fx-background-color: " + currentRowReference.getHexColor());
        }
        public void setColorRow(ColorRow reference) {this.currentRowReference = reference;}
    }
    private static final ColorChangeListener colorChangeListener = new ColorChangeListener();

    public ColorRow(DetectorPlotFlavor plotFlavor, String hexColor, ColorPicker colorPickerReference) {
        super();
        this.plotFlavor = plotFlavor;
        this.hexColor = hexColor;
        this.colorPickerReference = colorPickerReference;
        layoutElements();
        this.setPrefHeight(ROW_HEIGHT);
        this.setOnMouseClicked(event -> {
            colorPickerReference.valueProperty().removeListener(colorChangeListener);
            colorChangeListener.setColorRow(this);
            colorPickerReference.valueProperty().addListener(colorChangeListener);
            colorPickerReference.valueProperty().setValue(Color.web(hexColor));
        });
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
        colorPatch = new Label();
        colorPatch.setStyle("-fx-background-color: " + hexColor);
        colorPatch.setPrefWidth(100);
        colorPatch.setPrefHeight(ROW_HEIGHT);
        return colorPatch;
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

    public ColorPicker getColorPickerReference() {
        return colorPickerReference;
    }

    public void setColorPickerReference(ColorPicker colorPickerReference) {
        this.colorPickerReference = colorPickerReference;
    }

    public Label getColorPatch() {
        return colorPatch;
    }

    public void setColorPatch(Label colorPatch) {
        this.colorPatch = colorPatch;
    }
}
