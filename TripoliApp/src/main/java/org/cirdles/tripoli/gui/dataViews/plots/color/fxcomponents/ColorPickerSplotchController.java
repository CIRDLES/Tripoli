package org.cirdles.tripoli.gui.dataViews.plots.color.fxcomponents;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;

public class ColorPickerSplotchController {

    @FXML
    private ColorPicker colorPicker;

    @FXML
    private Label label;

    @FXML
    private StackPane stackPane;

    @FXML
    private void initialize() {
        // Bind the label's background color to the color picker's value
        colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            colorPicker.backgroundProperty().
                    setValue(
                            new Background(
                                    new BackgroundFill(newValue,CornerRadii.EMPTY, Insets.EMPTY)));
        });
        label.backgroundProperty().bind(colorPicker.backgroundProperty());
        label.prefWidthProperty().bind(stackPane.widthProperty());
        label.prefHeightProperty().bind(stackPane.heightProperty());
        colorPicker.prefWidthProperty().bind(stackPane.widthProperty());
        colorPicker.prefHeightProperty().bind(stackPane.heightProperty());

    }
}
