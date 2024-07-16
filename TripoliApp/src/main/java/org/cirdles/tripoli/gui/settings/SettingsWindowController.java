package org.cirdles.tripoli.gui.settings;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.cirdles.tripoli.gui.dataViews.plots.color.fxcomponents.ColorPickerSplotch;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class SettingsWindowController implements Initializable {


    @FXML
    private VBox settingsWindowVBoxRoot;
    @FXML
    private StackPane twoSigmaStackPane;
    @FXML
    private StackPane oneSigmaStackPane;
    @FXML
    private StackPane stdErrorStackPane;
    @FXML
    private StackPane meanStackPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        twoSigmaShading.setPrefWidth(144);
//        twoSigmaShading.setPrefHeight(37);
//        twoSigmaShading.getColorPicker().setValue(Color.web("#ffbfcb"));
    }





}
