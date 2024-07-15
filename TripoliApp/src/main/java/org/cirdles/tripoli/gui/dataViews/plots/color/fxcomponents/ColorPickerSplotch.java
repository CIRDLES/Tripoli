package org.cirdles.tripoli.gui.dataViews.plots.color.fxcomponents;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;

public class ColorPickerSplotch extends ColorPicker {
    private Label colorSplotch;

    public ColorPickerSplotch() {
        super();
        valueProperty().addListener((observable, oldValue, newValue) -> {updateBackgroundColor();});
    }

    private void updateBackgroundColor() {
        if (colorSplotch != null) {
            colorSplotch.setBackground(
                    new Background(
                            new BackgroundFill(
                                    getValue(),
                                    CornerRadii.EMPTY,
                                    Insets.EMPTY)));
        }
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        if(colorSplotch == null) {
            for (Node child : getChildrenUnmodifiable()) {
                if (child instanceof Label) {
                    colorSplotch = (Label) child;
                    colorSplotch.prefWidthProperty().bind(widthProperty());
                    colorSplotch.prefHeightProperty().bind(heightProperty());
                    updateBackgroundColor();
                }
            }
        }
    }

    public Label getColorSplotch() {return colorSplotch;}

}
