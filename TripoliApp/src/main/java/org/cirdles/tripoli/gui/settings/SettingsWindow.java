package org.cirdles.tripoli.gui.settings;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;
import org.cirdles.tripoli.expressions.species.SpeciesRecordInterface;
import org.cirdles.tripoli.gui.dataViews.plots.PlotWallPaneIntensities;
import org.cirdles.tripoli.gui.settings.color.fxcomponents.*;
import org.cirdles.tripoli.parameters.Parameters;
import org.cirdles.tripoli.sessions.Session;
import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.settings.plots.BlockCyclesPlotColors;
import org.cirdles.tripoli.settings.plots.species.SpeciesColors;
import org.cirdles.tripoli.utilities.DelegateActionSet;
import org.cirdles.tripoli.utilities.collections.TripoliSpeciesColorMap;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliPersistentState;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class SettingsWindow {

    private RatioColorSelectionPane ratioColorSelectionPane;
    private SettingsWindowController settingsWindowController;
    private Stage stage;
    private AnalysisInterface analysis;
    private Map<SpeciesRecordInterface, SpeciesColors> originalSpeciesColors;
    private DelegateActionSet repaintRatiosDelegateActionSet;
    private BlockCyclesPlotColors originalBlockCyclesPlotColors;
    private ArrayList<IsotopePaneRow> isotopePaneRows;
    private SpeciesColorSelectionScrollPane speciesColorSelectionScrollPane;
    private Parameters originalParameters;


    private SpeciesIntensityColorSelectionScrollPane speciesIntensityColorSelectionScrollPane;

    private static SettingsWindow instance;


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
                stage.setX(stage.getX() + newValue.doubleValue()- oldValue.doubleValue());
            });
            owner.yProperty().addListener((observable, oldValue, newValue) -> {
                stage.setY(stage.getY() + newValue.doubleValue()- oldValue.doubleValue());
            });
            this.originalParameters = analysis.getParameters().copy();
            this.originalSpeciesColors = new TripoliSpeciesColorMap(
                    ((Analysis) analysis).getAnalysisMapOfSpeciesToColors());
            this.originalBlockCyclesPlotColors = analysis.getBlockCyclesPlotColors();
            this.isotopePaneRows = new ArrayList<>();
            this.ratioColorSelectionPane = new RatioColorSelectionPane(delegateActionSet, analysis);
            this.settingsWindowController.getRatioColorSelectionAnchorPane().getChildren().clear();
            this.settingsWindowController.getRatioColorSelectionAnchorPane().getChildren().add(
                    ratioColorSelectionPane
            );
            speciesColorSelectionScrollPane = SpeciesColorSelectionScrollPane.buildSpeciesColorSelectionScrollPane(
                    AnalysisInterface.convertToAnalysis(analysis),
                    PlotWallPaneIntensities.getDelegateActionSet());
            ratioColorSelectionPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
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
            initParameterTextFields();
        } catch (IOException | TripoliException e) {
            e.printStackTrace();
        }
    }

    private void initParameterTextFields() {
        initProbabilitySpinner();
        initDatumCountSpinner();
    }

    private void initProbabilitySpinner() {
        Spinner<Double> probabilitySpinner = settingsWindowController.getChauvenetRejectionProbabilitySpinner();
        probabilitySpinner.setValueFactory( new SpinnerValueFactory.DoubleSpinnerValueFactory(
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
        datumCountSpinner.setValueFactory( new SpinnerValueFactory.IntegerSpinnerValueFactory(
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
                return String.format("%d",value);
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

    public static SettingsWindow requestSettingsWindow(
            Window owner,
            DelegateActionSet delegateActionSet,
            AnalysisInterface analysis,
            SettingsRequestType requestType) {
        if (instance == null) {
            instance = new SettingsWindow(owner, delegateActionSet, analysis);
        }
        else if (!instance.stage.getOwner().equals(owner) ||
                !analysis.equals(instance.analysis) ||
                !delegateActionSet.equals(instance.repaintRatiosDelegateActionSet)) {
            instance.close();
            instance = new SettingsWindow(owner, delegateActionSet, analysis);
        }
        instance.settingsWindowController.getSettingsTabPane().requestFocus();
        switch (SettingsRequestType.valueOf(requestType.name())) {
            case RATIOS -> {
                instance.settingsWindowController.getSettingsTabPane().getSelectionModel().select(
                        instance.settingsWindowController.getRatiosColorTab()
                );
            }
            case INTENSITIES -> {
                instance.settingsWindowController.getSettingsTabPane().getSelectionModel().select(
                        instance.settingsWindowController.getIntensitiesColorTab()
                );
            }
            case MENU_ITEM -> {
                instance.settingsWindowController.getSettingsTabPane().getSelectionModel().select(
                        instance.settingsWindowController.getParameterControlTab()
                );
                if (instance.analysis.getAnalysisMethod() == null || instance.repaintRatiosDelegateActionSet.isEmpty()) {
                    instance.settingsWindowController.getSettingsTabPane().getTabs().remove(
                            instance.settingsWindowController.getRatiosColorTab()
                    );
                    instance.settingsWindowController.getSettingsTabPane().getTabs().remove(
                            instance.settingsWindowController.getIntensitiesColorTab()
                    );
                }
            }
        }
        return instance;
    }

    public Stage getStage() {return stage;}


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
            try {
                Session currentSession = ((Analysis) analysis).getParentSession();
                TripoliSerializer.serializeObjectToFile(currentSession,
                    TripoliPersistentState.getExistingPersistentState().getMRUSessionFile().getAbsolutePath());
                close();
            } catch (TripoliException ex) {
                ex.printStackTrace();
            }
        });
        settingsWindowController.getSaveAsSessionDefaultsButton().setOnAction(e -> {
            Session currentSession = ((Analysis) analysis).getParentSession();
            currentSession.getSessionDefaultParameters().setRequiredMinDatumCount(
                    analysis.getParameters().getRequiredMinDatumCount());
            currentSession.getSessionDefaultParameters().setChauvenetRejectionProbability(
                    analysis.getParameters().getChauvenetRejectionProbability()
            );
            currentSession.setBlockCyclesPlotColors(analysis.getBlockCyclesPlotColors());
            currentSession.getSessionDefaultMapOfSpeciesToColors().
                    putAll(((Analysis) analysis).getAnalysisMapOfSpeciesToColors());
            try {
                TripoliSerializer.serializeObjectToFile(currentSession,
                        TripoliPersistentState.getExistingPersistentState().getMRUSessionFile().getAbsolutePath());
            } catch (TripoliException ex) {
                ex.printStackTrace();
            }
        });
        settingsWindowController.getSaveAsUserDefaultsButton().setOnAction(e -> {
            try{
                TripoliPersistentState tripoliPersistentState = TripoliPersistentState.getExistingPersistentState();
                tripoliPersistentState.getTripoliPersistentParameters().setChauvenetRejectionProbability(
                        analysis.getParameters().getChauvenetRejectionProbability());
                tripoliPersistentState.getTripoliPersistentParameters().setRequiredMinDatumCount(
                        analysis.getParameters().getRequiredMinDatumCount());
                tripoliPersistentState.setBlockCyclesPlotColors(analysis.getBlockCyclesPlotColors());
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
            analysis.setBlockCyclesPlotColors(currentSession.getBlockCyclesPlotColors());
            ((Analysis) analysis).getAnalysisMapOfSpeciesToColors().
                    putAll(currentSession.getSessionDefaultMapOfSpeciesToColors());
            repaintRatiosDelegateActionSet.executeDelegateActions();
            updateRatioColorSelectionPane();
        });
        settingsWindowController.getRestoreUserDefaultsButton().setOnAction(e -> {
            try{
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
                analysis.setTwoSigmaHexColorString(tripoliPersistentState.getTwoSigmaHexColorString());
                analysis.setOneSigmaHexColorString(tripoliPersistentState.getOneSigmaHexColorString());
                analysis.setTwoStandardErrorHexColorString(tripoliPersistentState.getTwoStdErrHexColorString());
                analysis.setMeanHexColorString(tripoliPersistentState.getMeanHexColorString());
                ((Analysis) analysis).getAnalysisMapOfSpeciesToColors().putAll(
                        tripoliPersistentState.getMapOfSpeciesToColors());
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
            settingsWindowController.getChauvenetRejectionProbabilitySpinner().getValueFactory().setValue(
                    originalParameters.getChauvenetRejectionProbability());
            settingsWindowController.getChauvenetMinimumDatumCountSpinner().getValueFactory().setValue(
                    originalParameters.getRequiredMinDatumCount());
            analysis.setBlockCyclesPlotColors(originalBlockCyclesPlotColors);
//            analysis.setTwoSigmaHexColorString(originalTwoSigmaHexColor);
            ratioColorSelectionPane.getTwoSigmaSplotch().
                    colorProperty().setValue(Color.web(analysis.getTwoSigmaHexColorString()));
//            analysis.setOneSigmaHexColorString(originalOneSigmaHexColor);
            ratioColorSelectionPane.getOneSigmaSplotch().
                    colorProperty().setValue(Color.web(analysis.getOneSigmaHexColorString()));
//            analysis.setTwoStandardErrorHexColorString(originalStdErrHexColor);
            ratioColorSelectionPane.getStdErrorSplotch().
                    colorProperty().setValue(Color.web(analysis.getTwoStandardErrorHexColorString()));
//            analysis.setMeanHexColorString(originalMeanHexColor);
            ratioColorSelectionPane.getMeanSplotch().
                    colorProperty().setValue(Color.web(analysis.getMeanHexColorString()));
            ((Analysis) analysis).getAnalysisMapOfSpeciesToColors().clear();
            ((Analysis) analysis).getAnalysisMapOfSpeciesToColors().putAll(
                    originalSpeciesColors
            );
            for(IsotopePaneRow row : isotopePaneRows) {
                row.speciesColorsProperty().set(originalSpeciesColors.get(row.getSpeciesRecord()));
            }
            repaintRatiosDelegateActionSet.executeDelegateActions();
            speciesColorSelectionScrollPane.getDelegateActionSet().executeDelegateActions(); // TODO: make these standard
//            close();
        });
    }

    public void updateRatioColorSelectionPane() {
        ratioColorSelectionPane.getTwoSigmaSplotch().colorProperty().setValue(
                Color.web(analysis.getTwoSigmaHexColorString()));
        ratioColorSelectionPane.getOneSigmaSplotch().colorProperty().setValue(Color.web(
                analysis.getOneSigmaHexColorString()));
        ratioColorSelectionPane.getStdErrorSplotch().colorProperty().setValue(Color.web(
                analysis.getTwoStandardErrorHexColorString()));
        ratioColorSelectionPane.getMeanSplotch().colorProperty().setValue(Color.web(
                analysis.getMeanHexColorString()));
        speciesColorSelectionScrollPane.getSpeciesIntensityColorSelectionPanes().forEach(
                SpeciesIntensityColorSelectionPane::updateColorProperties);
    };

    public void close() {
        instance = null;
        stage.close();
    }

    public void show(){
        stage.show();
        centerOverOwner();
    }
}
