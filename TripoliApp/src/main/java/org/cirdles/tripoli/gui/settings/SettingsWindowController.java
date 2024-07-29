package org.cirdles.tripoli.gui.settings;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsWindowController implements Initializable {


    @FXML
    private VBox vBoxRoot;
    @FXML
    private TabPane plotRatiosTabPane;
    @FXML
    private AnchorPane ratioColorSelectionAnchorPane;
    @FXML
    private ToolBar settingsWindowToolbar;
    @FXML
    private Button okButton;
    @FXML
    private Button revertToSavedButton;
    @FXML
    private Button restoreDefaultsButton;
    @FXML
    private Button cancelButton;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public VBox getvBoxRoot() {
        return vBoxRoot;
    }

    public TabPane getPlotRatiosTabPane() {
        return plotRatiosTabPane;
    }

    public AnchorPane getRatioColorSelectionAnchorPane() {
        return ratioColorSelectionAnchorPane;
    }

    public ToolBar getSettingsWindowToolbar() {
        return settingsWindowToolbar;
    }

    public Button getOkButton() {
        return okButton;
    }

    public Button getRevertToSavedButton() {
        return revertToSavedButton;
    }

    public Button getRestoreDefaultsButton() {
        return restoreDefaultsButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

}
