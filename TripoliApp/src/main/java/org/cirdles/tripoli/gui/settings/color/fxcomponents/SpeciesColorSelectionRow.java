package org.cirdles.tripoli.gui.settings.color.fxcomponents;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SpeciesColorSelectionRow extends HBox implements Initializable {

    @FXML
    private ColorPickerSplotch colorPickerSplotch;
    @FXML
    private Label rowLabel;
    private ObjectProperty<Color> colorProperty;
    private ObjectProperty<String> textProperty;

    public SpeciesColorSelectionRow() {
        super();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SpeciesColorSelectionRow.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try{
            fxmlLoader.load();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        this.colorProperty = new SimpleObjectProperty<>();
        this.textProperty = new SimpleObjectProperty<>();
        textProperty.bindBidirectional(rowLabel.textProperty());
        colorProperty.bindBidirectional(colorPickerSplotch.colorProperty());
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

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
