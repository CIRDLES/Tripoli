package org.cirdles.tripoli.gui.settings.color.fxcomponents;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;



public class SpeciesColorSelectionRow extends HBox {
    private ColorPickerSplotch colorPickerSplotch;
    private Label rowLabel;
    private Region center;
    private Region right;

    private final ObjectProperty<Color> colorProperty;
    private final ObjectProperty<String> textProperty;
    private final ObjectProperty<Font> fontProperty;


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
        this.setAlignment(Pos.CENTER);
        this.rowLabel.prefWidthProperty().bind(this.widthProperty().divide(3));
        this.center.prefWidthProperty().bind(this.widthProperty().divide(6));
        this.colorPickerSplotch.prefWidthProperty().bind(this.widthProperty().divide(3));
        this.right.prefWidthProperty().bind(this.widthProperty().divide(6));
        textProperty.bindBidirectional(rowLabel.textProperty());
        colorProperty.bindBidirectional(colorPickerSplotch.colorProperty());
        rowLabel.prefHeightProperty().bind(prefHeightProperty());
        colorPickerSplotch.prefHeightProperty().bind(prefHeightProperty());
        this.getChildren().addAll(rowLabel,center, colorPickerSplotch);
        HBox.setMargin(colorPickerSplotch, new Insets(5, 5, 5, 5));
        setPrefHeight(40);
        borderProperty().setValue(
                new Border(
                        new BorderStroke(
                                Color.BLACK,
                                BorderStrokeStyle.SOLID,
                                CornerRadii.EMPTY,
                                new BorderWidths(1, 0,0,0))));
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

    public ObjectProperty<Color> colorObjectProperty() {
        return colorProperty;
    }

    public void setColorProperty(Color colorProperty) {
        this.colorProperty.set(colorProperty);
    }

    public String getTextProperty() {
        return textProperty.get();
    }

    public ObjectProperty<String> textObjectProperty() {
        return textProperty;
    }

    public void setTextProperty(String textProperty) {
        this.textProperty.set(textProperty);
    }
}
