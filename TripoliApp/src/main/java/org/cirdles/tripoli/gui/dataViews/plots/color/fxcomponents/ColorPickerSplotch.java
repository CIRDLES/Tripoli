package org.cirdles.tripoli.gui.dataViews.plots.color.fxcomponents;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.ResourceBundle;

public class ColorPickerSplotch extends ColorPicker implements Initializable {


    private Label colorSplotch;

    public ColorPickerSplotch() {
        super();
    }

    private void updateBackgroundColor() {
            colorSplotch.setBackground(
                    new Background(
                            new BackgroundFill(
                                    getValue(),
                                    CornerRadii.EMPTY,
                                    Insets.EMPTY)));
    }




    public Label getColorSplotch() {return colorSplotch;}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.setStyle("-fx-color-label-visible: false; -fx-color-picker-visible: false;");
        this.colorSplotch = new Label();
        this.getChildren().add(colorSplotch);
        colorSplotch.prefWidthProperty().bind(this.widthProperty());
        colorSplotch.prefHeightProperty().bind(this.heightProperty());;
        valueProperty().addListener((observable, oldValue, newValue) -> {updateBackgroundColor();});
    }
}
