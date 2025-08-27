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

package org.cirdles.tripoli.gui.settings;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsWindowController implements Initializable {

    @FXML
    public VBox liveDataSettingsVBox;
    @FXML
    public Spinner<Integer> liveDataTimeoutSpinner;
    @FXML
    public ComboBox<MassSpectrometerContextEnum> massSpecComboBox;
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

    public VBox getLiveDataSettingsVBox() {
        return liveDataSettingsVBox;
    }

    public Spinner<Integer> getLiveDataTimeoutSpinner() {
        return liveDataTimeoutSpinner;
    }

    public ComboBox<MassSpectrometerContextEnum> getMassSpecComboBox() {
        return massSpecComboBox;
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
