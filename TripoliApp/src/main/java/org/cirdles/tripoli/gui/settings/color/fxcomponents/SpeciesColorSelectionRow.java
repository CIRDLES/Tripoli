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
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SpeciesColorSelectionRow extends HBox {
    private final ObjectProperty<Color> colorProperty;
    private final ObjectProperty<String> textProperty;
    private final ObjectProperty<Font> fontProperty;
    private ColorPickerSplotch colorPickerSplotch;
    private Label rowLabel;
    private Region center;
    private Region right;


    public SpeciesColorSelectionRow() {
        super();
        this.colorPickerSplotch = new ColorPickerSplotch();
        this.rowLabel = new Label();
        this.center = new Region();
        this.right = new Region();
        this.rowLabel.setAlignment(Pos.CENTER_LEFT);
        this.rowLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        this.colorProperty = new SimpleObjectProperty<>();
        this.textProperty = new SimpleObjectProperty<>();
        this.fontProperty = new SimpleObjectProperty<>();
        this.fontProperty.bindBidirectional(rowLabel.fontProperty());
        this.fontProperty.bindBidirectional(colorPickerSplotch.fontProperty());
        this.setAlignment(Pos.CENTER);
        this.rowLabel.prefWidthProperty().bind(this.widthProperty().divide(3));
        this.center.prefWidthProperty().bind(this.widthProperty().divide(6));
        this.colorPickerSplotch.prefWidthProperty().bind(this.widthProperty().divide(3));
        this.right.prefWidthProperty().bind(this.widthProperty().divide(6));
        textProperty.bindBidirectional(rowLabel.textProperty());
        colorProperty.bindBidirectional(colorPickerSplotch.colorProperty());
        rowLabel.prefHeightProperty().bind(prefHeightProperty());
        colorPickerSplotch.prefHeightProperty().bind(prefHeightProperty());
        this.getChildren().addAll(rowLabel, center, colorPickerSplotch);
        HBox.setMargin(colorPickerSplotch, new Insets(5, 5, 5, 5));
        setPrefHeight(40);//  TODO: remove when setting default
        borderProperty().setValue(
                new Border(
                        new BorderStroke(
                                Color.BLACK,
                                BorderStrokeStyle.SOLID,
                                CornerRadii.EMPTY,
                                new BorderWidths(1, 0, 0, 0))));
    }

    public SpeciesColorSelectionRow(double prefHeight) {
        this();
        this.setPrefHeight(prefHeight);
    }


    public ColorPickerSplotch getColorPickerSplotch() {
        return colorPickerSplotch;
    }

    public Label getRowLabel() {
        return rowLabel;
    }

    public Color getColorProperty() {
        return colorProperty.get();
    }

    public void setColorProperty(Color colorProperty) {
        this.colorProperty.set(colorProperty);
    }

    public ObjectProperty<Color> colorObjectProperty() {
        return colorProperty;
    }

    public String getTextProperty() {
        return textProperty.get();
    }

    public void setTextProperty(String textProperty) {
        this.textProperty.set(textProperty);
    }

    public ObjectProperty<String> textObjectProperty() {
        return textProperty;
    }

    public ObjectProperty<Font> fontProperty() {
        return fontProperty;
    }
}
