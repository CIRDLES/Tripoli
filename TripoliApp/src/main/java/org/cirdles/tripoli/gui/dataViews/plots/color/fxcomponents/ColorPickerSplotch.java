package org.cirdles.tripoli.gui.dataViews.plots.color.fxcomponents;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import org.cirdles.tripoli.utilities.DelegateActionSet;

import java.io.IOException;

public class ColorPickerSplotch extends StackPane {

    @FXML
    private ColorPicker colorPicker;

    @FXML
    private Label label;

    /**
     * Contains all delegate actions for anything that needs repainting
     */
    private final DelegateActionSet repaintDelegateActionSet;


    public ColorPickerSplotch() {
        super();
        repaintDelegateActionSet = new DelegateActionSet();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ColorPickerSplotch.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try{
            fxmlLoader.load();
        } catch (IOException e){
            throw new RuntimeException(e);
        }

        // Bind the label's background color to the color picker's value
        colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            colorPicker.backgroundProperty().
                    setValue(
                            new Background(
                                    new BackgroundFill(newValue,CornerRadii.EMPTY, Insets.EMPTY)));
            repaintDelegateActionSet.executeDelegateActions();
        });
        label.backgroundProperty().bind(colorPicker.backgroundProperty());
        label.prefWidthProperty().bind(widthProperty());
        label.prefHeightProperty().bind(heightProperty());
        colorPicker.prefWidthProperty().bind(widthProperty());
        colorPicker.prefHeightProperty().bind(heightProperty());
        label.addEventHandler(MouseEvent.MOUSE_CLICKED, click -> {
            if (! colorPicker.isShowing()) {
                colorPicker.show();
            }
        });
    }

    public DelegateActionSet getDelegateActionSet() {
        return repaintDelegateActionSet;
    }

    public ColorPicker getColorPicker() {
        return colorPicker;
    }

    public Label getLabel() {
        return label;
    }
}
