package org.cirdles.tripoli.gui.settings.color.fxcomponents;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.cirdles.tripoli.gui.constants.ConstantsTripoliApp;
import org.cirdles.tripoli.utilities.DelegateActionSet;
import org.cirdles.tripoli.utilities.Setter;

public class ColorPickerSplotch extends StackPane {

    private final ColorPicker colorPicker;

    private final Label label;

    private final ObjectProperty<Color> colorValue;
    private final ObjectProperty<String> textValue;
    private String hexColor;// TODO: make this an ObjectProperty<String>
    private Setter<String> hexColorSetter;

    private final ObjectProperty<Font> fontObjectProperty;


    /**
     * Contains all delegate actions for anything that needs repainting
     */
    private final DelegateActionSet repaintDelegateActionSet;


    public ColorPickerSplotch() {
        super();
        repaintDelegateActionSet = new DelegateActionSet();
        hexColorSetter = hexString -> {};
        this.fontObjectProperty = new SimpleObjectProperty<>();
        this.colorPicker = new ColorPicker();
        this.label = new Label("Click to Change Color");
        this.label.fontProperty().bindBidirectional(fontObjectProperty);
        this.label.setAlignment(Pos.CENTER);
        this.label.setStyle(this.label.getStyle() +";-fx-font-weight: bold;");
        this.getChildren().addAll(this.colorPicker, this.label);
        this.colorPicker.prefWidthProperty().bind(label.prefWidthProperty());
        this.colorPicker.prefHeightProperty().bind(label.prefHeightProperty());
        this.colorPicker.setVisible(false);
        this.hexColor = ConstantsTripoliApp.convertColorToHex(colorPicker.getValue());
        label.backgroundProperty().bind(colorPicker.backgroundProperty());
        label.prefWidthProperty().bind(prefWidthProperty());
        label.prefHeightProperty().bind(prefHeightProperty());
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

    public ObjectProperty<Font> fontProperty() {
        return fontObjectProperty;
    }
}
