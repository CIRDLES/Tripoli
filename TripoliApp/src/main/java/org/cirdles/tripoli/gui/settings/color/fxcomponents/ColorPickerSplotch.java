package org.cirdles.tripoli.gui.settings.color.fxcomponents;

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
import javafx.scene.paint.Color;
import org.cirdles.tripoli.gui.constants.ConstantsTripoliApp;
import org.cirdles.tripoli.utilities.DelegateActionSet;
import org.cirdles.tripoli.utilities.Setter;

import java.io.IOException;

public class ColorPickerSplotch extends StackPane {

    @FXML
    private ColorPicker colorPicker;

    @FXML
    private Label label;

    private final ObjectProperty<Color> colorValue;
    private final ObjectProperty<String> textValue;
    private String hexColor;
    private Setter<String> hexColorSetter;

    /**
     * Contains all delegate actions for anything that needs repainting
     */
    private final DelegateActionSet repaintDelegateActionSet;


    public ColorPickerSplotch() {
        super();
        repaintDelegateActionSet = new DelegateActionSet();
        hexColorSetter = hexString -> {};
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ColorPickerSplotch.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try{
            fxmlLoader.load();
        } catch (IOException e){
            e.printStackTrace();
        }

        this.hexColor = ConstantsTripoliApp.convertColorToHex(colorPicker.getValue());
        // Bind the label's background color to the color picker's color
        label.backgroundProperty().bind(colorPicker.backgroundProperty());
//        label.prefWidthProperty().bind(widthProperty());
        prefWidthProperty().bindBidirectional(label.prefWidthProperty());
        label.prefHeightProperty().bind(heightProperty());
        colorPicker.prefWidthProperty().bind(widthProperty());
        colorPicker.prefHeightProperty().bind(heightProperty());
        label.addEventHandler(MouseEvent.MOUSE_CLICKED, click -> {
            if (! colorPicker.isShowing()) {
                colorPicker.show();
            }
        });
        colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            colorPicker.backgroundProperty().
                    setValue(
                            new Background(
                                    new BackgroundFill(newValue,CornerRadii.EMPTY, Insets.EMPTY)));
            // Lets see if we can change the color of the text to be more contrasting here
            label.setTextFill(newValue.invert());
            this.hexColor = ConstantsTripoliApp.convertColorToHex(newValue);
            this.hexColorSetter.set(hexColor);
            repaintDelegateActionSet.executeDelegateActions();
        });
        this.colorValue = new SimpleObjectProperty<>(colorPicker.getValue());
        colorPicker.valueProperty().bindBidirectional(colorValue);
        this.textValue = new SimpleObjectProperty<>(label.textProperty().get());
        this.label.textProperty().bind(textValue);
    }

    public void setHexColorSetter(Setter<String> hexColorSetter) {
        this.hexColorSetter = hexColorSetter;
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

    public ObjectProperty<Color> colorProperty() {
        return colorValue;
    }

    public Color getColor() { return colorValue.get(); }
    public void setColor(Color color) {
        this.colorValue.set(color);
    }

    public String getHexColor() {
        return hexColor;
    }
}
