package org.cirdles.tripoli.gui.settings;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsWindowController implements Initializable {

    @FXML
    private TabPane settingsTabPane;
    @FXML
    private Tab ratiosColorTab;
    @FXML
    private Tab intensitiesColorTab;
    @FXML
    private Tab parameterControlTab;
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
    private AnchorPane ratioColorSelectionAnchorPane;
    @FXML
    private AnchorPane plotIntensitiesAnchorPaneExp;
    @FXML
    private VBox plotIntensitiesVBox;
    @FXML
    private Spinner<Double> chauvenetRejectionProbabilitySpinner;
    @FXML
    private Spinner<Integer> chauvenetMinimumDatumCountSpinner;
    @FXML
    private Label speciesHeader;
    @FXML
    private Button undoAllButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        HBox.setMargin(speciesHeader, new Insets(0, 0, 0, 10));
    }

    public AnchorPane getRatioColorSelectionAnchorPane() {
        return ratioColorSelectionAnchorPane;
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

    public Button getUndoAllButton() {
        return undoAllButton;
    }

    public AnchorPane getPlotIntensitiesAnchorPaneExp() {
        return plotIntensitiesAnchorPaneExp;
    }

    public VBox getPlotIntensitiesVBox() {
        return plotIntensitiesVBox;
    }

    public Spinner<Double> getChauvenetRejectionProbabilitySpinner() {
        return chauvenetRejectionProbabilitySpinner;
    }

    public Spinner<Integer> getChauvenetMinimumDatumCountSpinner() {
        return chauvenetMinimumDatumCountSpinner;
    }

    public TabPane getSettingsTabPane() {
        return settingsTabPane;
    }

    public Tab getRatiosColorTab() {
        return ratiosColorTab;
    }

    public Tab getIntensitiesColorTab() {
        return intensitiesColorTab;
    }

    public Tab getParameterControlTab() {
        return parameterControlTab;
    }
}
