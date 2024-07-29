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
    private Button saveAnalysisSettingsButton;
    @FXML
    private Button saveAsSessionDefaultsButton;
    @FXML
    private Button saveAsUserDefaultsButton;
    @FXML
    private Button restoreSessionDefaultsButton;
    @FXML
    private Button restoreUserDefaultsButton;
    @FXML
    private ToolBar revertToolbar;
    @FXML
    private VBox vBoxRoot;
    @FXML
    private TabPane plotRatiosTabPane;
    @FXML
    private AnchorPane ratioColorSelectionAnchorPane;
    @FXML
    private ToolBar saveDefaultsToolbar;
//    @FXML
//    private Button okButton;
//    @FXML
//    private Button revertToSavedButton;
//    @FXML
//    private Button restoreDefaultsButton;
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

    public ToolBar getSaveDefaultsToolbar() {
        return saveDefaultsToolbar;
    }

    public Button getSaveAnalysisSettingsButton() {
        return saveAnalysisSettingsButton;
    }

    public Button getSaveAsSessionDefaultsButton() {
        return saveAsSessionDefaultsButton;
    }

    public Button getSaveAsUserDefaultsButton() {
        return saveAsUserDefaultsButton;
    }

    public Button getRestoreSessionDefaultsButton() {
        return restoreSessionDefaultsButton;
    }

    public Button getRestoreUserDefaultsButton() {
        return restoreUserDefaultsButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

}
