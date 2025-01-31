/*
 * Copyright 2022 James Bowring, Noah McLean, Scott Burdick, and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.cirdles.tripoli.constants.TripoliConstants;
import org.cirdles.tripoli.gui.constants.ConstantsTripoliApp;
import org.cirdles.tripoli.utilities.DelegateActionSet;
import org.cirdles.tripoli.utilities.Setter;

public class ColorPickerSplotch extends StackPane {

    private static final String DEFAULT_TEXT = "Click to Change Color";

    private ColorPicker colorPicker;

    private Label label;

    private ObjectProperty<Color> colorValue;

    private ObjectProperty<String> textObjectProperty;

    private Setter<String> hexColorSetter;

    private ObjectProperty<Font> fontObjectProperty;

    /**
     * Contains all delegate actions for anything that needs repainting
     */
    private DelegateActionSet repaintDelegateActionSet;


    public ColorPickerSplotch() {
        super();
        initializeComponents();
        bindProperties();
        this.label.setStyle(this.label.getStyle() +";-fx-font-weight: bold;");
        this.label.setAlignment(Pos.CENTER);
        this.getChildren().addAll(this.colorPicker, this.label);

        this.label.addEventHandler(MouseEvent.MOUSE_CLICKED, click -> {
            if (! colorPicker.isShowing()) {
                colorPicker.show();
            }
        });
        colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            colorPicker.backgroundProperty().
                    setValue(
                            new Background(
                                    new BackgroundFill(
                                            newValue,
                                            new CornerRadii(5, false),
                                            new Insets(2))));
            this.label.setTextFill(newValue.invert());
            this.hexColorSetter.set(ConstantsTripoliApp.convertColorToHex(newValue));
            repaintDelegateActionSet.executeDelegateActions();
        });

    }

    private void bindProperties() {
        this.label.textProperty().bind(textObjectProperty);
        this.label.fontProperty().bindBidirectional(fontObjectProperty);
        this.label.backgroundProperty().bind(colorPicker.backgroundProperty());
        this.label.prefWidthProperty().bind(prefWidthProperty());
        this.label.prefHeightProperty().bind(prefHeightProperty());
        this.colorPicker.prefWidthProperty().bind(label.prefWidthProperty());
        this.colorPicker.prefHeightProperty().bind(label.prefHeightProperty());
        this.colorPicker.valueProperty().bindBidirectional(colorValue);
    }

    private void initializeComponents() {
        this.repaintDelegateActionSet = new DelegateActionSet();
        this.hexColorSetter = hexString -> {};
        this.fontObjectProperty = new SimpleObjectProperty<>();
        this.colorPicker = new ColorPicker();
        this.colorValue = new SimpleObjectProperty<>(colorPicker.getValue());
        this.label = new Label(DEFAULT_TEXT);
        this.textObjectProperty = new SimpleObjectProperty<>(
                label.textProperty().get());
        this.colorPicker.setVisible(false);
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

    public ObjectProperty<Font> fontProperty() {
        return fontObjectProperty;
    }

    public ObjectProperty<String> textObjectProperty() {
        return textObjectProperty;
    }
}
