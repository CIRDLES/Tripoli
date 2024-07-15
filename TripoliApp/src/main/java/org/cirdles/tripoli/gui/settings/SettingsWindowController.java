package org.cirdles.tripoli.gui.settings;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.cirdles.tripoli.gui.dataViews.plots.color.fxcomponents.ColorPickerSplotch;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsWindowController implements Initializable {

    @FXML
    public ColorPickerSplotch twoSigmaShading;
    @FXML
    private VBox root;
    @FXML
    private StackPane stackPaneOneSigma;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        twoSigmaShading.setPrefWidth(144);
        twoSigmaShading.setPrefHeight(37);
    }

    public VBox getRoot() {
        return root;
    }
}
