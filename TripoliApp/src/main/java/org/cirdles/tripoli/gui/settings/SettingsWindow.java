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

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;
import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;
import org.cirdles.tripoli.expressions.species.SpeciesRecordInterface;
import org.cirdles.tripoli.gui.TripoliGUI;
import org.cirdles.tripoli.gui.TripoliGUIController;
import org.cirdles.tripoli.gui.dataViews.plots.PlotWallPaneIntensities;
import org.cirdles.tripoli.gui.settings.color.fxcomponents.*;
import org.cirdles.tripoli.gui.utilities.events.PlotTabSelectedEvent;
import org.cirdles.tripoli.gui.utilities.events.SaveCurrentSessionEvent;
import org.cirdles.tripoli.gui.utilities.events.SaveSessionAsEvent;
import org.cirdles.tripoli.parameters.Parameters;
import org.cirdles.tripoli.sessions.Session;
import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.settings.plots.RatiosColors;
import org.cirdles.tripoli.settings.plots.species.SpeciesColors;
import org.cirdles.tripoli.utilities.DelegateActionSet;
import org.cirdles.tripoli.utilities.collections.TripoliSpeciesColorMap;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliPersistentState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static javafx.event.Event.fireEvent;
import static org.cirdles.tripoli.gui.TripoliGUI.primaryStage;

public class SettingsWindow {

    private static SettingsWindow instance;
    private RatioColorSelectionPane ratioColorSelectionPane;
    private SettingsWindowController settingsWindowController;
    private Stage stage;
    private AnalysisInterface analysis;
    private Map<SpeciesRecordInterface, SpeciesColors> originalSpeciesColors;
    private DelegateActionSet repaintRatiosDelegateActionSet;
    private RatiosColors originalRatiosColors;
    private ArrayList<IsotopePaneRow> isotopePaneRows;
    private SpeciesColorSelectionScrollPane speciesColorSelectionScrollPane;
    private Parameters originalParameters;
    private SpeciesIntensityColorSelectionScrollPane speciesIntensityColorSelectionScrollPane;


    private SettingsWindow(Window owner, DelegateActionSet delegateActionSet, AnalysisInterface analysis) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SettingsWindow.fxml"));
        try {
            this.stage = new Stage();
            this.stage.setResizable(false);
            this.stage.setScene(new Scene(fxmlLoader.load()));
            this.analysis = analysis;
            this.repaintRatiosDelegateActionSet = delegateActionSet;
            this.settingsWindowController = fxmlLoader.getController();
            this.settingsWindowController.getRatioColorSelectionAnchorPane().prefWidthProperty().bind(stage.widthProperty());
            this.stage.initOwner(owner);
            this.stage.setTitle("Settings");

            owner.xProperty().addListener((observable, oldValue, newValue) -> {
                stage.setX(stage.getX() + newValue.doubleValue() - oldValue.doubleValue());
            });
            owner.yProperty().addListener((observable, oldValue, newValue) -> {
                stage.setY(stage.getY() + newValue.doubleValue() - oldValue.doubleValue());
            });
            this.originalParameters = analysis.getParameters().copy();
            this.originalSpeciesColors = new TripoliSpeciesColorMap(
                    ((Analysis) analysis).getAnalysisMapOfSpeciesToColors());
            this.originalRatiosColors = analysis.getRatioColors();
            this.isotopePaneRows = new ArrayList<>();
            this.ratioColorSelectionPane = new RatioColorSelectionPane(delegateActionSet, analysis);
            this.settingsWindowController.getRatioColorSelectionAnchorPane().getChildren().clear();
            this.settingsWindowController.getRatioColorSelectionAnchorPane().getChildren().add(
                    ratioColorSelectionPane
            );
            speciesColorSelectionScrollPane = SpeciesColorSelectionScrollPane.buildSpeciesColorSelectionScrollPane(
                    AnalysisInterface.convertToAnalysis(analysis),
                    PlotWallPaneIntensities.getDelegateActionSet());
            ratioColorSelectionPane.prefWidthProperty().bind(stage.widthProperty());
            initializeToolbarButtons();
            speciesIntensityColorSelectionScrollPane = new SpeciesIntensityColorSelectionScrollPane();
            speciesIntensityColorSelectionScrollPane.prefWidthProperty().bind(stage.getScene().widthProperty());
            settingsWindowController.getPlotIntensitiesAnchorPaneExp().getChildren().add(
                    speciesIntensityColorSelectionScrollPane
            );
            if (analysis.getAnalysisMethod() != null && analysis.getAnalysisMethod().getSpeciesList() != null) {
                for (SpeciesRecordInterface speciesRecordInterface : analysis.getAnalysisMethod().getSpeciesList()) {
                    Region region = new Region();
                    region.setPrefHeight(20);
                    settingsWindowController.getPlotIntensitiesVBox().getChildren().add(
                            region
                    );
                    IsotopePaneRow row =
                            new IsotopePaneRow(
                                    speciesRecordInterface,
                                    ((Analysis) analysis).getAnalysisMapOfSpeciesToColors(),
                                    PlotWallPaneIntensities.getDelegateActionSet(),
                                    35);
                    isotopePaneRows.add(row);
                    settingsWindowController.getPlotIntensitiesVBox().getChildren().add(row);
                }
            }
            this.stage.getScene().addEventFilter(
                    PlotTabSelectedEvent.PLOT_TAB_SELECTED,
                    plotTabSelectedEvent -> {
                        SettingsRequestType settingsRequestType = plotTabSelectedEvent.getRequestType();
                        switch (settingsRequestType) {
                            case RATIOS -> {
                                settingsWindowController.getSettingsTabPane().
                                        getSelectionModel().
                                        select(settingsWindowController.getRatiosColorTab());
                            }
                            case INTENSITIES -> {
                                // Only select if tab exists (not case 1)
                                if (settingsWindowController.getSettingsTabPane().getTabs().contains(
                                        settingsWindowController.getIntensitiesColorTab())) {
                                    settingsWindowController.getSettingsTabPane().
                                            getSelectionModel().
                                            select(settingsWindowController.getIntensitiesColorTab());
                                } else {
                                    // Fallback to ratios tab if intensities tab is hidden
                                    settingsWindowController.getSettingsTabPane().
                                            getSelectionModel().
                                            select(settingsWindowController.getRatiosColorTab());
                                }
                            }
                            case MENU_ITEM -> {
                                settingsWindowController.getSettingsTabPane().
                                        getSelectionModel().
                                        select(settingsWindowController.getParameterControlTab());
                            }
                        }
                        plotTabSelectedEvent.consume();
                    }
            );
            initParameterTextFields();

            // Hide Intensity/Residuals Color Control tab for case 1
            if (analysis.getAnalysisCaseNumber() == 1) {
                settingsWindowController.getSettingsTabPane().getTabs().remove(
                        settingsWindowController.getIntensitiesColorTab()
                );
            }
        } catch (IOException | TripoliException e) {
            e.printStackTrace();
        }
    }

    public static SettingsWindow requestSettingsWindow(
            Window owner,
            DelegateActionSet delegateActionSet,
            AnalysisInterface analysis,
            SettingsRequestType requestType) {
        if (instance == null) {
            instance = new SettingsWindow(owner, delegateActionSet, analysis);
        } else if (!instance.stage.getOwner().equals(owner) ||
                !analysis.equals(instance.analysis) ||
                !delegateActionSet.equals(instance.repaintRatiosDelegateActionSet)) {
            instance.close();
            instance = new SettingsWindow(owner, delegateActionSet, analysis);
        }
        instance.settingsWindowController.getSettingsTabPane().requestFocus();

        // Hide Intensity/Residuals Color Control tab for case 1
        if (analysis.getAnalysisCaseNumber() == 1) {
            instance.settingsWindowController.getSettingsTabPane().getTabs().remove(
                    instance.settingsWindowController.getIntensitiesColorTab()
            );
        }

        switch (SettingsRequestType.valueOf(requestType.name())) {
            case RATIOS -> {
                instance.settingsWindowController.getSettingsTabPane().getSelectionModel().select(
                        instance.settingsWindowController.getRatiosColorTab()
                );
            }
            case INTENSITIES -> {
                // Only select if tab exists (not case 1)
                if (instance.settingsWindowController.getSettingsTabPane().getTabs().contains(
                        instance.settingsWindowController.getIntensitiesColorTab())) {
                    instance.settingsWindowController.getSettingsTabPane().getSelectionModel().select(
                            instance.settingsWindowController.getIntensitiesColorTab()
                    );
                } else {
                    // Fallback to ratios tab if intensities tab is hidden
                    instance.settingsWindowController.getSettingsTabPane().getSelectionModel().select(
                            instance.settingsWindowController.getRatiosColorTab()
                    );
                }
            }
            case MENU_ITEM -> {
                instance.settingsWindowController.getSettingsTabPane().getSelectionModel().select(
                        instance.settingsWindowController.getParameterControlTab()
                );
                if (instance.analysis.getAnalysisMethod() == null || instance.repaintRatiosDelegateActionSet.isEmpty()) {
                    instance.settingsWindowController.getSettingsTabPane().getTabs().remove(
                            instance.settingsWindowController.getRatiosColorTab()
                    );
                    // Only remove intensities tab if it exists
                    instance.settingsWindowController.getSettingsTabPane().getTabs().remove(
                            instance.settingsWindowController.getIntensitiesColorTab()
                    );
                }
            }
        }
        return instance;
    }

    /**
     * Allows callers to evaluate whether a scene exists in memory.
     *
     * @return An `Optional` wrapper for the current scene, or `Optional.empty()`
     */
    public static Optional<Scene> getCurrentScene() {
        Optional<Scene> optionalScene = Optional.empty();
        if (instance != null &&
                instance.getStage() != null &&
                instance.getStage().getScene() != null) {
            optionalScene = Optional.of(instance.getStage().getScene());
        }
        return optionalScene;
    }

    private void handleLiveDataMenuHidden() {
        MenuItem liveDataMenuItem = ((MenuBar) TripoliGUI.primaryStage.getScene()
                .getRoot().getChildrenUnmodifiable().get(0)).getMenus().get(1).getItems().get(4);
        liveDataMenuItem.setVisible(analysis.getParameters().getMassSpectrometerContext().getMassSpectrometerName().equals("Phoenix"));
    }

    private void initParameterTextFields() {
        initProbabilitySpinner();
        initDatumCountSpinner();
        initMassSpecCombo();
        initScalingDotSizeSpinners();
    }

    private void initMassSpecCombo() {
        ComboBox<MassSpectrometerContextEnum> msCombo = settingsWindowController.getMassSpecComboBox();

        // Populate only displayed values
        msCombo.getItems().setAll(
                Arrays.stream(MassSpectrometerContextEnum.values())
                        .filter(MassSpectrometerContextEnum::isDisplayed)
                        .toList()
        );

        // Show friendly names in the dropdown list
        msCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(MassSpectrometerContextEnum item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        msCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(MassSpectrometerContextEnum item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        // Preselect current value from analysis parameters
        msCombo.getSelectionModel().select(analysis.getParameters().getMassSpectrometerContext());

        // Update analysis parameters when user changes selection
        msCombo.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                analysis.getParameters().setMassSpectrometerContext(newValue);
                TripoliGUI.updateStageTitle(newValue);
                handleLiveDataMenuHidden();
            }
        });
    }

    private void initProbabilitySpinner() {
        Spinner<Double> probabilitySpinner = settingsWindowController.getChauvenetRejectionProbabilitySpinner();
        probabilitySpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(
                0.0,
                1.0,
                analysis.getParameters().getChauvenetRejectionProbability(), 0.05));
        probabilitySpinner.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            String input = event.getCharacter();
            if (!input.matches("[0-9.]") || (input.equals(".") && probabilitySpinner.getEditor().getText().contains("."))) {
                event.consume();
            }
        });
        probabilitySpinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            probabilitySpinner.commitValue();
        });
        probabilitySpinner.getValueFactory().setConverter(new StringConverter<>() {

            @Override
            public String toString(Double value) {
                if (value == null) {
                    return "";
                }
                return String.format("%.2f", value);
            }

            @Override
            public Double fromString(String string) {
                try {
                    return Double.parseDouble(string);
                } catch (NumberFormatException e) {
                    return analysis.getParameters().getChauvenetRejectionProbability();
                }
            }
        });
        probabilitySpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            analysis.getParameters().setChauvenetRejectionProbability(newValue);
        });
    }

    private void initDatumCountSpinner() {
        Spinner<Integer> datumCountSpinner = settingsWindowController.getChauvenetMinimumDatumCountSpinner();
        datumCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                20,
                300,
                analysis.getParameters().getRequiredMinDatumCount(),
                5));
        datumCountSpinner.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            String input = event.getCharacter();
            if (!input.matches("[0-9]")) {
                event.consume();
            }
        });
        datumCountSpinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            datumCountSpinner.commitValue();
        });
        datumCountSpinner.getValueFactory().setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer value) {
                if (value == null) {
                    return "";
                }
                return String.format("%d", value);
            }

            @Override
            public Integer fromString(String string) {
                try {
                    return Integer.parseInt(string);
                } catch (NumberFormatException e) {
                    return analysis.getParameters().getRequiredMinDatumCount();
                }
            }
        });
        datumCountSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            analysis.getParameters().setRequiredMinDatumCount(newValue);
        });
    }

    private void initScalingDotSizeSpinners() {
        Spinner<Double> minSizeSpinner = settingsWindowController.getScalingDotMinSizeSpinner();
        Spinner<Double> maxSizeSpinner = settingsWindowController.getScalingDotMaxSizeSpinner();

        // Get current values, using system defaults if uninitialized (0.0 indicates old serialized Parameters)
        double currentMin = analysis.getParameters().getScalingDotMinSize();
        double currentMax = analysis.getParameters().getScalingDotMaxSize();

        // If values are 0.0, they're likely from old serialization - use system defaults
        if (currentMin == 0.0 && currentMax == 0.0) {
            currentMin = org.cirdles.tripoli.constants.TripoliConstants.SCALING_DOT_DEFAULT_MIN_SIZE;
            currentMax = org.cirdles.tripoli.constants.TripoliConstants.SCALING_DOT_DEFAULT_MAX_SIZE;
            analysis.getParameters().setScalingDotMinSize(currentMin);
            analysis.getParameters().setScalingDotMaxSize(currentMax);
        }

        // Initialize min size spinner (range 2-50, default 5)
        // Max value of min spinner will be dynamically adjusted based on current max value
        SpinnerValueFactory.DoubleSpinnerValueFactory minValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
                2.0,
                50.0,
                currentMin,
                1.0);
        minSizeSpinner.setValueFactory(minValueFactory);

        // Set initial max for min spinner to current max value
        minValueFactory.setMax(currentMax);

        minSizeSpinner.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            String input = event.getCharacter();
            if (!input.matches("[0-9.]") || (input.equals(".") && minSizeSpinner.getEditor().getText().contains("."))) {
                event.consume();
            }
        });
        minSizeSpinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            minSizeSpinner.commitValue();
        });
        minSizeSpinner.getValueFactory().setConverter(new StringConverter<>() {
            @Override
            public String toString(Double value) {
                if (value == null) {
                    return "";
                }
                return String.format("%.1f", value);
            }

            @Override
            public Double fromString(String string) {
                try {
                    double parsed = Double.parseDouble(string);
                    // Clamp to valid range
                    double maxValue = analysis.getParameters().getScalingDotMaxSize();
                    return Math.max(2.0, Math.min(parsed, maxValue));
                } catch (NumberFormatException e) {
                    return analysis.getParameters().getScalingDotMinSize();
                }
            }
        });
        minSizeSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                double maxValue = analysis.getParameters().getScalingDotMaxSize();
                // Ensure min is not greater than max
                if (newValue > maxValue) {
                    // Adjust min to be equal to max
                    minValueFactory.setValue(maxValue);
                    analysis.getParameters().setScalingDotMinSize(maxValue);
                } else {
                    analysis.getParameters().setScalingDotMinSize(newValue);
                }
            }
        });

        // Initialize max size spinner (range 2-50, default 20)
        // Min value of max spinner will be dynamically adjusted based on current min value
        SpinnerValueFactory.DoubleSpinnerValueFactory maxValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
                2.0,
                50.0,
                currentMax,
                1.0);
        maxSizeSpinner.setValueFactory(maxValueFactory);

        // Set initial min for max spinner to current min value
        maxValueFactory.setMin(currentMin);

        maxSizeSpinner.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            String input = event.getCharacter();
            if (!input.matches("[0-9.]") || (input.equals(".") && maxSizeSpinner.getEditor().getText().contains("."))) {
                event.consume();
            }
        });
        maxSizeSpinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            maxSizeSpinner.commitValue();
        });
        maxSizeSpinner.getValueFactory().setConverter(new StringConverter<>() {
            @Override
            public String toString(Double value) {
                if (value == null) {
                    return "";
                }
                return String.format("%.1f", value);
            }

            @Override
            public Double fromString(String string) {
                try {
                    double parsed = Double.parseDouble(string);
                    // Clamp to valid range
                    double minValue = analysis.getParameters().getScalingDotMinSize();
                    return Math.max(minValue, Math.min(parsed, 50.0));
                } catch (NumberFormatException e) {
                    return analysis.getParameters().getScalingDotMaxSize();
                }
            }
        });
        maxSizeSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                double minValue = analysis.getParameters().getScalingDotMinSize();
                // Ensure max is not less than min
                if (newValue < minValue) {
                    // Adjust max to be equal to min
                    maxValueFactory.setValue(minValue);
                    analysis.getParameters().setScalingDotMaxSize(minValue);
                } else {
                    analysis.getParameters().setScalingDotMaxSize(newValue);
                    // Update min spinner's max to allow it to go up to this new max
                    minValueFactory.setMax(newValue);
                }
            }
        });

        // Also update max spinner's min when min changes
        minSizeSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                maxValueFactory.setMin(newValue);
            }
        });
    }

    public Stage getStage() {
        return stage;
    }

    private void centerOverOwner() {
        Window owner = stage.getOwner();
        double ownerX = owner.getX();
        double ownerY = owner.getY();
        double ownerWidth = owner.getWidth();
        double ownerHeight = owner.getHeight();

        double stageWidth = stage.getWidth();
        double stageHeight = stage.getHeight();


        double centerX = ownerX + (ownerWidth - stageWidth) / 2;
        double centerY = ownerY + (ownerHeight - stageHeight) / 2;

        stage.setX(centerX);
        stage.setY(centerY);
    }

    public void initializeToolbarButtons() {
        settingsWindowController.getSaveAnalysisSettingsButton().setOnAction(e -> {
            if (TripoliGUIController.sessionFileName != null) {
                SaveCurrentSessionEvent saveCurrentSessionEvent = new SaveCurrentSessionEvent();
                fireEvent(primaryStage.getScene(), saveCurrentSessionEvent);
            } else {
                SaveSessionAsEvent saveSessionAsEvent = new SaveSessionAsEvent();
                fireEvent(primaryStage.getScene(), saveSessionAsEvent);
            }
        });
        settingsWindowController.getSaveAsSessionDefaultsButton().setOnAction(e -> {
            Session currentSession = ((Analysis) analysis).getParentSession();
            currentSession.getSessionDefaultParameters().setMassSpectrometerContext(
                    analysis.getParameters().getMassSpectrometerContext());
            currentSession.getSessionDefaultParameters().setRequiredMinDatumCount(
                    analysis.getParameters().getRequiredMinDatumCount());
            currentSession.getSessionDefaultParameters().setChauvenetRejectionProbability(
                    analysis.getParameters().getChauvenetRejectionProbability()
            );
            currentSession.getSessionDefaultParameters().setScalingDotMinSize(
                    analysis.getParameters().getScalingDotMinSize());
            currentSession.getSessionDefaultParameters().setScalingDotMaxSize(
                    analysis.getParameters().getScalingDotMaxSize());
            currentSession.setBlockCyclesPlotColors(analysis.getRatioColors());
            currentSession.getSessionDefaultMapOfSpeciesToColors().
                    putAll(((Analysis) analysis).getAnalysisMapOfSpeciesToColors());
            if (TripoliGUIController.sessionFileName != null) {
                SaveCurrentSessionEvent saveCurrentSessionEvent = new SaveCurrentSessionEvent();
                fireEvent(primaryStage.getScene(), saveCurrentSessionEvent);
            } else {
                SaveSessionAsEvent saveSessionAsEvent = new SaveSessionAsEvent();
                fireEvent(primaryStage.getScene(), saveSessionAsEvent);
            }
        });
        settingsWindowController.getSaveAsUserDefaultsButton().setOnAction(e -> {
            try {
                TripoliPersistentState tripoliPersistentState = TripoliPersistentState.getExistingPersistentState();
                tripoliPersistentState.getTripoliPersistentParameters().setMassSpectrometerContext(
                        analysis.getParameters().getMassSpectrometerContext());
                tripoliPersistentState.getTripoliPersistentParameters().setChauvenetRejectionProbability(
                        analysis.getParameters().getChauvenetRejectionProbability());
                tripoliPersistentState.getTripoliPersistentParameters().setRequiredMinDatumCount(
                        analysis.getParameters().getRequiredMinDatumCount());
                tripoliPersistentState.getTripoliPersistentParameters().setScalingDotMinSize(
                        analysis.getParameters().getScalingDotMinSize());
                tripoliPersistentState.getTripoliPersistentParameters().setScalingDotMaxSize(
                        analysis.getParameters().getScalingDotMaxSize());
                tripoliPersistentState.setBlockCyclesPlotColors(analysis.getRatioColors());
                tripoliPersistentState.getMapOfSpeciesToColors().
                        putAll(((Analysis) analysis).getAnalysisMapOfSpeciesToColors());
                tripoliPersistentState.updateTripoliPersistentState();
            } catch (TripoliException ex) {
                ex.printStackTrace();
            }
        });
        settingsWindowController.getRestoreSessionDefaultsButton().setOnAction(e -> {
            Session currentSession = ((Analysis) analysis).getParentSession();
            analysis.getParameters().setChauvenetRejectionProbability(
                    currentSession.getSessionDefaultParameters().getChauvenetRejectionProbability()
            );
            settingsWindowController.getChauvenetRejectionProbabilitySpinner().getValueFactory().setValue(
                    analysis.getParameters().getChauvenetRejectionProbability()
            );
            analysis.getParameters().setRequiredMinDatumCount(
                    currentSession.getSessionDefaultParameters().getRequiredMinDatumCount()
            );
            settingsWindowController.getChauvenetMinimumDatumCountSpinner().getValueFactory().setValue(
                    analysis.getParameters().getRequiredMinDatumCount()
            );
            // Get values from session defaults
            double minSize = currentSession.getSessionDefaultParameters().getScalingDotMinSize();
            double maxSize = currentSession.getSessionDefaultParameters().getScalingDotMaxSize();

            // If both are 0.0, they're likely from old serialization - use system defaults
            if (minSize == 0.0 && maxSize == 0.0) {
                minSize = org.cirdles.tripoli.constants.TripoliConstants.SCALING_DOT_DEFAULT_MIN_SIZE;
                maxSize = org.cirdles.tripoli.constants.TripoliConstants.SCALING_DOT_DEFAULT_MAX_SIZE;
            }

            // Update analysis parameters
            analysis.getParameters().setScalingDotMinSize(minSize);
            analysis.getParameters().setScalingDotMaxSize(maxSize);

            // Update spinner value factories - need to update ranges first
            SpinnerValueFactory.DoubleSpinnerValueFactory minValueFactory =
                    (SpinnerValueFactory.DoubleSpinnerValueFactory) settingsWindowController.getScalingDotMinSizeSpinner().getValueFactory();
            SpinnerValueFactory.DoubleSpinnerValueFactory maxValueFactory =
                    (SpinnerValueFactory.DoubleSpinnerValueFactory) settingsWindowController.getScalingDotMaxSizeSpinner().getValueFactory();

            // Update ranges to allow the new values
            minValueFactory.setMax(maxSize);
            maxValueFactory.setMin(minSize);

            // Now set the values using the spinner's value factory (same pattern as other parameters)
            settingsWindowController.getScalingDotMinSizeSpinner().getValueFactory().setValue(minSize);
            settingsWindowController.getScalingDotMaxSizeSpinner().getValueFactory().setValue(maxSize);
            analysis.setRatioColors(currentSession.getBlockCyclesPlotColors());
            ((Analysis) analysis).getAnalysisMapOfSpeciesToColors().
                    putAll(currentSession.getSessionDefaultMapOfSpeciesToColors());
            isotopePaneRows.forEach(isotopePaneRow -> {
                isotopePaneRow.speciesColorsProperty().set(((Analysis) analysis).
                        getAnalysisMapOfSpeciesToColors().get(isotopePaneRow.getSpeciesRecord()));
            });
            repaintRatiosDelegateActionSet.executeDelegateActions();
            updateRatioColorSelectionPane();
        });
        settingsWindowController.getRestoreUserDefaultsButton().setOnAction(e -> {
            try {
                TripoliPersistentState tripoliPersistentState = TripoliPersistentState.getExistingPersistentState();
                analysis.getParameters().setRequiredMinDatumCount(
                        tripoliPersistentState.getTripoliPersistentParameters().getRequiredMinDatumCount()
                );
                settingsWindowController.getChauvenetMinimumDatumCountSpinner().getValueFactory().setValue(
                        analysis.getParameters().getRequiredMinDatumCount()
                );
                analysis.getParameters().setChauvenetRejectionProbability(
                        tripoliPersistentState.getTripoliPersistentParameters().getChauvenetRejectionProbability()
                );
                settingsWindowController.getChauvenetRejectionProbabilitySpinner().getValueFactory().setValue(
                        analysis.getParameters().getChauvenetRejectionProbability()
                );
                // Get values from persistent state, use system defaults if uninitialized (backward compatibility)
                double minSize = org.cirdles.tripoli.constants.TripoliConstants.SCALING_DOT_DEFAULT_MIN_SIZE;
                double maxSize = org.cirdles.tripoli.constants.TripoliConstants.SCALING_DOT_DEFAULT_MAX_SIZE;

                // Update analysis parameters
                analysis.getParameters().setScalingDotMinSize(minSize);
                analysis.getParameters().setScalingDotMaxSize(maxSize);

                // Update spinner value factories - need to update ranges first
                SpinnerValueFactory.DoubleSpinnerValueFactory minValueFactory =
                        (SpinnerValueFactory.DoubleSpinnerValueFactory) settingsWindowController.getScalingDotMinSizeSpinner().getValueFactory();
                SpinnerValueFactory.DoubleSpinnerValueFactory maxValueFactory =
                        (SpinnerValueFactory.DoubleSpinnerValueFactory) settingsWindowController.getScalingDotMaxSizeSpinner().getValueFactory();

                // Update ranges to allow the new values
                minValueFactory.setMax(maxSize);
                maxValueFactory.setMin(minSize);

                // Now set the values using the spinner's value factory (same pattern as other parameters)
                settingsWindowController.getScalingDotMinSizeSpinner().getValueFactory().setValue(minSize);
                settingsWindowController.getScalingDotMaxSizeSpinner().getValueFactory().setValue(maxSize);
                analysis.setTwoSigmaHexColorString(tripoliPersistentState.getTwoSigmaHexColorString());
                analysis.setOneSigmaHexColorString(tripoliPersistentState.getOneSigmaHexColorString());
                analysis.setTwoStandardErrorHexColorString(tripoliPersistentState.getTwoStdErrHexColorString());
                analysis.setMeanHexColorString(tripoliPersistentState.getMeanHexColorString());
                ((Analysis) analysis).getAnalysisMapOfSpeciesToColors().putAll(
                        tripoliPersistentState.getMapOfSpeciesToColors());
                isotopePaneRows.forEach(isotopePaneRow -> {
                    isotopePaneRow.speciesColorsProperty().set(
                            ((Analysis) analysis).getAnalysisMapOfSpeciesToColors().
                                    get(isotopePaneRow.getSpeciesRecord())
                    );
                });
                repaintRatiosDelegateActionSet.executeDelegateActions();
                updateRatioColorSelectionPane();
//                close();
            } catch (TripoliException ex) {
                ex.printStackTrace();
            }
        });
        settingsWindowController.getUndoAllButton().setOnAction(e -> {
            analysis.getParameters().setChauvenetRejectionProbability(originalParameters.getChauvenetRejectionProbability());
            analysis.getParameters().setRequiredMinDatumCount(originalParameters.getRequiredMinDatumCount());
            double minSize = originalParameters.getScalingDotMinSize();
            double maxSize = originalParameters.getScalingDotMaxSize();

            // If both are 0.0, they're likely from old serialization - use system defaults
            if (minSize == 0.0 && maxSize == 0.0) {
                minSize = org.cirdles.tripoli.constants.TripoliConstants.SCALING_DOT_DEFAULT_MIN_SIZE;
                maxSize = org.cirdles.tripoli.constants.TripoliConstants.SCALING_DOT_DEFAULT_MAX_SIZE;
            }

            analysis.getParameters().setScalingDotMinSize(minSize);
            analysis.getParameters().setScalingDotMaxSize(maxSize);
            settingsWindowController.getChauvenetRejectionProbabilitySpinner().getValueFactory().setValue(
                    originalParameters.getChauvenetRejectionProbability());
            settingsWindowController.getChauvenetMinimumDatumCountSpinner().getValueFactory().setValue(
                    originalParameters.getRequiredMinDatumCount());

            // Update spinner value factories - need to update ranges first
            SpinnerValueFactory.DoubleSpinnerValueFactory minValueFactory =
                    (SpinnerValueFactory.DoubleSpinnerValueFactory) settingsWindowController.getScalingDotMinSizeSpinner().getValueFactory();
            SpinnerValueFactory.DoubleSpinnerValueFactory maxValueFactory =
                    (SpinnerValueFactory.DoubleSpinnerValueFactory) settingsWindowController.getScalingDotMaxSizeSpinner().getValueFactory();

            // Update ranges to allow the new values
            minValueFactory.setMax(maxSize);
            maxValueFactory.setMin(minSize);

            // Now set the values using the spinner's value factory (same pattern as other parameters)
            settingsWindowController.getScalingDotMinSizeSpinner().getValueFactory().setValue(minSize);
            settingsWindowController.getScalingDotMaxSizeSpinner().getValueFactory().setValue(maxSize);
            analysis.setRatioColors(originalRatiosColors);
            ratioColorSelectionPane.updateRatioColorsProperty(analysis.getRatioColors());
            ((Analysis) analysis).getAnalysisMapOfSpeciesToColors().clear();
            ((Analysis) analysis).getAnalysisMapOfSpeciesToColors().putAll(
                    originalSpeciesColors
            );
            for (IsotopePaneRow row : isotopePaneRows) {
                row.speciesColorsProperty().set(originalSpeciesColors.get(row.getSpeciesRecord()));
            }
            repaintRatiosDelegateActionSet.executeDelegateActions();
            speciesColorSelectionScrollPane.getDelegateActionSet().executeDelegateActions(); // TODO: make these standard
        });
    }

    public void updateRatioColorSelectionPane() {
        ratioColorSelectionPane.updateRatioColorsProperty(analysis.getRatioColors());
        speciesColorSelectionScrollPane.getSpeciesIntensityColorSelectionPanes().forEach(
                SpeciesIntensityColorSelectionPane::updateColorProperties);
    }

    public void close() {
        instance = null;
        stage.close();
    }

    public void show() {
        stage.show();
        centerOverOwner();
    }
}
