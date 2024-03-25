package org.cirdles.tripoli.gui;

import jakarta.xml.bind.JAXBException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.cirdles.tripoli.expressions.species.IsotopicRatio;
import org.cirdles.tripoli.expressions.species.SpeciesRecordInterface;
import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots.MCMCPlotsController;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots.MCMCPlotsWindow;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.OGTripoliPlotsWindow;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.OGTripoliViewController;
import org.cirdles.tripoli.gui.dialogs.TripoliMessageDialog;
import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne.initializers.AllBlockInitForDataLiteOne;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.initializers.AllBlockInitForMCMC;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputBlockRecordFull;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.sessions.analysis.methods.baseline.BaselineCell;
import org.cirdles.tripoli.sessions.analysis.methods.sequence.SequenceCell;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliPersistentState;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.cirdles.tripoli.constants.TripoliConstants.MISSING_STRING_FIELD;
import static org.cirdles.tripoli.constants.TripoliConstants.TRIPOLI_RATIO_FLIPPER_URL;
import static org.cirdles.tripoli.gui.constants.ConstantsTripoliApp.*;
import static org.cirdles.tripoli.gui.dialogs.TripoliMessageDialog.showChoiceDialog;
import static org.cirdles.tripoli.gui.utilities.UIUtilities.showTab;
import static org.cirdles.tripoli.gui.utilities.fileUtilities.FileHandlerUtil.selectDataFile;
import static org.cirdles.tripoli.gui.utilities.fileUtilities.FileHandlerUtil.selectMethodFile;
import static org.cirdles.tripoli.sessions.analysis.Analysis.*;
import static org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod.compareAnalysisMethodToDataFileSpecs;

public class AnalysisManagerController implements Initializable, AnalysisManagerCallbackI {

    public static AnalysisInterface analysis;
    public static MCMCPlotsWindow MCMCPlotsWindow;
    public static OGTripoliPlotsWindow ogTripoliReviewPlotsWindow;
    public static OGTripoliPlotsWindow ogTripoliPreviewPlotsWindow;
    private final Map<String, boolean[][]> mapOfGridPanesToCellUse = new TreeMap<>();
    public Tab detectorDetailTab;
    public TabPane analysiMethodTabPane;
    @FXML
    public HBox blockStatusHBox;
    @FXML
    public GridPane selectRatiosGridPane;
    @FXML
    public Button mcmcButton;
    @FXML
    public TextFlow numeratorMassesListTextFlow;
    @FXML
    public TextFlow denominatorMassesListTextFlow;
    @FXML
    public TextFlow ratiosListTextFlow;
    @FXML
    public ToggleGroup knotsToggleGroup;
    @FXML
    public Button reviewSculptData;
    @FXML
    public ToolBar processingToolBar;
    public Tab baselineTableTab;
    public Tab sequenceTableTab;
    public Tab selectRatiosToPlotTab;
    public Tab selectColumnsToPlot;
    public VBox ratiosVBox;
    public VBox functionsVBox;
    @FXML
    private GridPane analysisManagerGridPane;
    @FXML
    private TextField analysisNameTextField;
    @FXML
    private TextField sampleNameTextField;
    @FXML
    private TextField sampleDescriptionTextField;
    @FXML
    private TextField dataFilePathNameTextField;
    @FXML
    private TextField analystNameTextField;
    @FXML
    private TextField labNameTextField;
    @FXML
    private TextArea metaDataTextArea;
    @FXML
    private TextArea dataSummaryTextArea;
    @FXML
    private TextArea aboutAnalysisTextArea;
    @FXML
    private GridPane analysisDetectorsGridPane;
    @FXML
    private GridPane sequenceTableGridPane;
    @FXML
    private GridPane baselineTableGridPane;
    private VBox proposedRatioVBox;
    private Set<IsotopicRatio> activeRatiosList;
    private List<IsotopicRatio> allRatios;
    @FXML
    private Button addRatioButton;

    public static void closePlotWindows() {
        if (null != ogTripoliPreviewPlotsWindow) {
            ogTripoliPreviewPlotsWindow.close();
        }
        if (null != ogTripoliReviewPlotsWindow) {
            ogTripoliReviewPlotsWindow.close();
        }
        if (null != MCMCPlotsWindow) {
            MCMCPlotsWindow.close();
        }
    }

    public static StackPane makeMassStackPane(String massName, String color) {
        Text massText = new Text(massName);
        massText.setFont(new Font("Monospaced Bold", 14));

        Shape circle = new Ellipse(15, 15, 30, 20);
        circle.setFill(Paint.valueOf(color));
        circle.setStroke(Paint.valueOf("black"));
        circle.setStrokeWidth(1);

        StackPane mass = new StackPane(circle, massText);
        mass.setUserData(massName);
        mass.setAlignment(Pos.CENTER);

        return mass;
    }

    public static VBox makeRatioVBox(String ratioName, Color textColor) {
        String[] numDen = ratioName.split("/");
        Text num = new Text(numDen[0].trim());
        num.setFont(new Font("Monospaced Bold", 14));
        num.setFill(textColor);
        Text den = new Text(numDen[1].trim());
        den.setFont(new Font("Monospaced Bold", 14));
        den.setFill(textColor);
        Shape line = new Line(0, 0, 40, 0);

        VBox ratioVBox = new VBox(num, line, den);
        ratioVBox.setUserData(ratioName);
        ratioVBox.setAlignment(Pos.CENTER);
        ratioVBox.setPadding(new Insets(1, 5, 1, 5));
        ratioVBox.setStyle(ratioVBox.getStyle() + "-fx-border-color: black;");

        ImageView ratioFlipperImageView = new ImageView();
        Image ratioFlipper = new Image(TRIPOLI_RATIO_FLIPPER_URL);
        ratioFlipperImageView.setImage(ratioFlipper);
        ratioFlipperImageView.setFitHeight(16);
        ratioFlipperImageView.setFitWidth(16);
        HBox ratioFlipperHBox = new HBox(ratioFlipperImageView);
        ratioFlipperHBox.setAlignment(Pos.CENTER);
        ratioFlipperHBox.setPadding(new Insets(1, 0, 1, 0));

        VBox ratioFlipperVBox = new VBox(ratioVBox, ratioFlipperHBox);
        ratioFlipperVBox.setAlignment(Pos.CENTER);

        return ratioFlipperVBox;
    }

    private void populateDetectorDetailRow(GridPane target, String entry, int colIndex, int rowIndex) {
        if (!mapOfGridPanesToCellUse.get(target.getId())[rowIndex][colIndex]) {
            Label detectorResistanceLabel = new Label(entry);
            GridPane.setHalignment(detectorResistanceLabel, HPos.CENTER);
            target.add(detectorResistanceLabel, colIndex, rowIndex);
            mapOfGridPanesToCellUse.get(target.getId())[rowIndex][colIndex] = true;
        }
    }

    /**
     * @param location  The location used to resolve relative paths for the root object, or
     *                  {@code null} if the location is not known.
     * @param resources The resources used to localize the root object, or {@code null} if
     *                  the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // March 2024 implement drag n drop of files ===================================================================
        analysisManagerGridPane.setOnDragOver(event -> {
            event.acceptTransferModes(TransferMode.MOVE);
        });
        analysisManagerGridPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (event.getDragboard().hasFiles()) {
                File dataFile = db.getFiles().get(0);

                // new analysis
                MenuItem menuItemAnalysesNew = ((MenuBar) TripoliGUI.primaryStage.getScene()
                        .getRoot().getChildrenUnmodifiable().get(0)).getMenus().get(1).getItems().get(1);
                menuItemAnalysesNew.fire();

                AnalysisInterface analysisSelected = analysis;

                try {
                    analysisSelected.extractMassSpecDataFromPath(Path.of(dataFile.toURI()));
                } catch (JAXBException | IOException | InvocationTargetException | NoSuchMethodException e) {
//                    throw new RuntimeException(e);
                } catch (IllegalAccessException | TripoliException e) {
//                    throw new RuntimeException(e);
                }

                // manage analysis
                MenuItem menuItemAnalysesManager = ((MenuBar) TripoliGUI.primaryStage.getScene()
                        .getRoot().getChildrenUnmodifiable().get(0)).getMenus().get(1).getItems().get(0);
                menuItemAnalysesManager.fire();
            }
        });
        // end implement drag n drop of files ===================================================================

        MCMCPlotsController.analysis = analysis;
        OGTripoliViewController.analysis = analysis;
        analysisManagerGridPane.setStyle("-fx-background-color: " + convertColorToHex(TRIPOLI_ANALYSIS_YELLOW));

        populateAnalysisManagerGridPane(analysis.getAnalysisCaseNumber());
        setupListeners();

        try {
            previewAndSculptDataAction();
        } catch (TripoliException e) {
            throw new RuntimeException(e);
        }

    }

    private void setupListeners() {
        analysisNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            assert null != analysis;
            analysis.setAnalysisName(newValue.isBlank() ? MISSING_STRING_FIELD : newValue);
        });
        analystNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            assert null != analysis;
            analysis.setAnalystName(newValue.isBlank() ? MISSING_STRING_FIELD : newValue);
        });
        labNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            assert null != analysis;
            analysis.setLabName(newValue.isBlank() ? MISSING_STRING_FIELD : newValue);
        });
        sampleNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            assert null != analysis;
            analysis.setAnalysisSampleName(newValue.isBlank() ? MISSING_STRING_FIELD : newValue);
        });
        sampleDescriptionTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            assert null != analysis;
            analysis.setAnalysisSampleDescription(newValue.isBlank() ? MISSING_STRING_FIELD : newValue);
        });
    }

    private void populateAnalysisManagerGridPane(int caseNumber) {
        analysisNameTextField.setEditable(analysis.isMutable());
        analysisNameTextField.setText(analysis.getAnalysisName());

        analystNameTextField.setEditable(analysis.isMutable());
        analystNameTextField.setText(analysis.getAnalystName());

        labNameTextField.setEditable(analysis.isMutable());
        labNameTextField.setText(analysis.getLabName());

        sampleNameTextField.setEditable(analysis.isMutable());
        sampleNameTextField.setText(analysis.getAnalysisSampleName());

        sampleDescriptionTextField.setEditable(analysis.isMutable());
        sampleDescriptionTextField.setText(analysis.getAnalysisSampleDescription());

        dataFilePathNameTextField.setEditable(false);
        dataFilePathNameTextField.setText(analysis.getDataFilePathString());

        if (0 != analysis.getDataFilePathString().compareToIgnoreCase(MISSING_STRING_FIELD)) {
            populateAnalysisDataFields();
        }

        switch (caseNumber) {
            case 0 -> {
                analysiMethodTabPane.getTabs().remove(detectorDetailTab);
                analysiMethodTabPane.getTabs().remove(baselineTableTab);
                analysiMethodTabPane.getTabs().remove(sequenceTableTab);
                analysiMethodTabPane.getTabs().remove(selectRatiosToPlotTab);
                analysiMethodTabPane.getTabs().remove(selectColumnsToPlot);
            }
            case 1 -> {
                analysiMethodTabPane.getTabs().remove(detectorDetailTab);
                analysiMethodTabPane.getTabs().remove(baselineTableTab);
                analysiMethodTabPane.getTabs().remove(sequenceTableTab);
                analysiMethodTabPane.getTabs().remove(selectRatiosToPlotTab);
                showTab(analysiMethodTabPane, 1, selectColumnsToPlot);
                populateAnalysisMethodColumnsSelectorPane();
                processingToolBar.setVisible(false);
            }
            case 2, 3, 4 -> {
                showTab(analysiMethodTabPane, 1, detectorDetailTab);
                showTab(analysiMethodTabPane, 2, baselineTableTab);
                showTab(analysiMethodTabPane, 3, sequenceTableTab);
                showTab(analysiMethodTabPane, 4, selectRatiosToPlotTab);
                analysiMethodTabPane.getTabs().remove(selectColumnsToPlot);
                populateAnalysisMethodGridPane();
                populateAnalysisMethodRatioBuilderPane();
                populateBlocksStatus();
                processingToolBar.setVisible(true);
            }
        }

        processingToolBar.setDisable(null == analysis.getAnalysisMethod());
    }

    private void populateAnalysisDataFields() {
        metaDataTextArea.setText(analysis.prettyPrintAnalysisMetaData());
        dataSummaryTextArea.setText(analysis.prettyPrintAnalysisDataSummary());
        aboutAnalysisTextArea.setText((null == analysis.getAnalysisMethod()) ? "No analysis method loaded" : analysis.getAnalysisMethod().prettyPrintMethodSummary(true));
    }

    private void populateAnalysisMethodGridPane() {
        // column 0 of GridPanes is reserved for row labels
        AnalysisMethod analysisMethod = analysis.getMethod();
        Map<String, Detector> mapOfDetectors = analysis.getMassSpecExtractedData().getDetectorSetup().getMapOfDetectors();
        List<Detector> detectorsInOrderList = mapOfDetectors.values().stream().sorted(Comparator.comparing(Detector::getOrdinalIndex)).collect(Collectors.toList());

        setUpGridPaneRows(analysisDetectorsGridPane, 7, detectorsInOrderList.size() + 1);
        prepareAnalysisMethodGridPanes(analysisDetectorsGridPane, detectorsInOrderList);

//        aboutAnalysisTextArea.setText((null == analysisMethod) ? "No analysis method loaded" : analysisMethod.prettyPrintMethodSummary(true));

        setUpGridPaneRows(baselineTableGridPane, (null == analysisMethod) ? 1 : analysisMethod.getBaselineTable().getSequenceCount() + 1, detectorsInOrderList.size() + 1);
        prepareAnalysisMethodGridPanes(baselineTableGridPane, detectorsInOrderList);

        setUpGridPaneRows(sequenceTableGridPane, (null == analysisMethod) ? 1 : analysisMethod.getSequenceTable().getSequenceCount() + 1, detectorsInOrderList.size() + 1);
        prepareAnalysisMethodGridPanes(sequenceTableGridPane, detectorsInOrderList);
    }

    private void prepareAnalysisMethodGridPanes(GridPane methodGridPane, List<Detector> detectorsInOrderList) {
        while (1 < methodGridPane.getColumnConstraints().size()) {
            methodGridPane.getColumnConstraints().remove(1);
        }
        int startingColumnCount = methodGridPane.getColumnConstraints().size();
        int detectorCount = detectorsInOrderList.size();
        for (int col = 0; col < detectorCount + (methodGridPane.equals(sequenceTableGridPane) ? 2 : 1); col++) {
            if (col >= startingColumnCount) {
                ColumnConstraints column = new ColumnConstraints();
                column.setPrefWidth(0);
                column.setHgrow(Priority.SOMETIMES);
                methodGridPane.getColumnConstraints().add(column);
            }
            methodGridPane.getColumnConstraints().get(col).setPrefWidth(0);
            methodGridPane.getColumnConstraints().get(col).setHgrow(Priority.SOMETIMES);
        }
        methodGridPane.getColumnConstraints().get(0).setPrefWidth(25);

        Map<Detector, List<SequenceCell>> mapOfDetectorsToSequenceCell = new TreeMap<>();
        Map<Detector, List<BaselineCell>> mapOfDetectorsToBaselineCell = new TreeMap<>();
        if (null != analysis.getMethod()) {
            mapOfDetectorsToSequenceCell = analysis.getMethod().getSequenceTable().getMapOfDetectorsToSequenceCells();
            mapOfDetectorsToBaselineCell = analysis.getMethod().getBaselineTable().getMapOfDetectorsToBaselineCells();
        }
        for (int col = 0; col < detectorCount + 1; col++) {
            populateDetectorDetailRow(methodGridPane, (0 < col) ? detectorsInOrderList.get(col - 1).getDetectorName() : "spec\u2193/detector\u2192", col, 0);

            if (methodGridPane.equals(analysisDetectorsGridPane)) {
                populateDetectorDetailRow(methodGridPane, (0 < col) ? detectorsInOrderList.get(col - 1).getDetectorType().getName() : "type", col, 1);
                populateDetectorDetailRow(methodGridPane, (0 < col) ? detectorsInOrderList.get(col - 1).getAmplifierType().getName() : "amplifier", col, 2);
                populateDetectorDetailRow(methodGridPane, (0 < col) ? String.valueOf(detectorsInOrderList.get(col - 1).getAmplifierResistanceInOhms()) : "resistance", col, 3);
                populateDetectorDetailRow(methodGridPane, (0 < col) ? String.valueOf(detectorsInOrderList.get(col - 1).getAmplifierGain()) : "gainCorr", col, 4);
                populateDetectorDetailRow(methodGridPane, (0 < col) ? String.valueOf(detectorsInOrderList.get(col - 1).getAmplifierEfficiency()) : "efficiency", col, 5);
                populateDetectorDetailRow(methodGridPane, (0 < col) ? String.valueOf(detectorsInOrderList.get(col - 1).getDetectorDeadTime()) : "dead time", col, 6);
            }

            if (methodGridPane.equals(baselineTableGridPane) && (col < detectorCount)) {
                List<BaselineCell> detectorBaselineCells = mapOfDetectorsToBaselineCell.get(detectorsInOrderList.get(col));
                if (null != detectorBaselineCells) {
                    for (BaselineCell baselineCell : detectorBaselineCells) {
                        int sequenceNumber = baselineCell.getBaselineSequence();
                        populateDetectorDetailRow(methodGridPane, String.valueOf(baselineCell.getCellMass()), col + 1, sequenceNumber);
                        populateDetectorDetailRow(methodGridPane, baselineCell.getBaselineID(), 0, sequenceNumber);
                    }
                }
            }

            if (methodGridPane.equals(sequenceTableGridPane)) {
                if (0 == col) {
                    populateDetectorDetailRow(methodGridPane, "cross ref", detectorCount + 1, 0);
                }
                if (col < detectorCount) {
                    List<SequenceCell> detectorSequenceCells = mapOfDetectorsToSequenceCell.get(detectorsInOrderList.get(col));
                    if (null != detectorSequenceCells) {
                        for (SequenceCell sequenceCell : detectorSequenceCells) {
                            int sequenceNumber = sequenceCell.getOnPeakSequence();
                            populateDetectorDetailRow(methodGridPane, sequenceCell.getTargetSpecies().prettyPrintShortForm(), col + 1, sequenceNumber);
                            populateDetectorDetailRow(methodGridPane, sequenceCell.getSequenceId(), 0, sequenceNumber);
                            populateDetectorDetailRow(methodGridPane, sequenceCell.prettyPrintBaseLineRefs(), detectorCount + 1, sequenceNumber);
                        }
                    }
                }
            }
        }

        methodGridPane.requestLayout();
    }

    private void setUpGridPaneRows(GridPane methodGridPane, int rowCount, int colCount) {
        while (1 < methodGridPane.getRowConstraints().size()) {
            methodGridPane.getRowConstraints().remove(1);
        }
        List<Node> removals = new ArrayList<>();
        for (Node child : methodGridPane.getChildren()) {
            if (child instanceof Label) {
                removals.add(child);
            }
        }
        for (Node child : removals) {
            methodGridPane.getChildren().remove(child);
        }

        boolean[][] cellUse = new boolean[rowCount][colCount + 1]; // assignments
        mapOfGridPanesToCellUse.put(methodGridPane.getId(), cellUse);

        int startingCount = methodGridPane.getRowConstraints().size();
        for (int row = 0; row < rowCount; row++) {
            if (startingCount > row) {
                methodGridPane.getRowConstraints().get(row).setPrefHeight(25);
            } else {
                methodGridPane.getRowConstraints().add(new RowConstraints(25));
            }
        }
    }

    private void populateAnalysisMethodColumnsSelectorPane() {
        List<CheckBox> ratioCheckBoxList = new ArrayList<>();
        List<CheckBox> ratioInvertedCheckBoxList = new ArrayList<>();
        List<CheckBox> functionCheckBoxList = new ArrayList<>();

        ChangeListener<Boolean> allRatiosChangeListener = (observable, oldValue, newValue) -> {
            for (CheckBox checkBoxRatio : ratioCheckBoxList) {
                checkBoxRatio.setSelected(newValue);
            }
        };
        ChangeListener<Boolean> allRatiosInvertedChangeListener = (observable, oldValue, newValue) -> {
            for (CheckBox checkBoxInvertedRatio : ratioInvertedCheckBoxList) {
                checkBoxInvertedRatio.setSelected(newValue);
            }
        };
        ChangeListener<Boolean> allFunctionsChangeListener = (observable, oldValue, newValue) -> {
            for (CheckBox checkBoxRatio : functionCheckBoxList) {
                checkBoxRatio.setSelected(newValue);
            }
        };

        ratiosVBox.getChildren().clear();
        HBox hBox = new HBox();
        hBox.setSpacing(50);
        hBox.setPadding(new Insets(5, 5, 5, 5));
        hBox.setAlignment(Pos.CENTER);
        CheckBox checkBoxSelectAllRatios = new CheckBox("Select all");
        int count = 0;
        int selected = 0;
        for (UserFunction userFunction : analysis.getAnalysisMethod().getUserFunctions()) {
            if (userFunction.isTreatAsIsotopicRatio()) {
                count++;
                selected += userFunction.isDisplayed() ? 1 : 0;
            }
            checkBoxSelectAllRatios.setSelected(selected == count);
            checkBoxSelectAllRatios.setIndeterminate((0 < selected) && (selected < count));
        }
        checkBoxSelectAllRatios.selectedProperty().addListener(allRatiosChangeListener);
        hBox.getChildren().add(checkBoxSelectAllRatios);

        hBox.getChildren().add(new Label("Isotopic Ratios"));

        CheckBox checkBoxSelectAllRatiosInverted = new CheckBox("Invert all");
        count = 0;
        selected = 0;
        for (UserFunction userFunction : analysis.getAnalysisMethod().getUserFunctions()) {
            if (userFunction.isTreatAsIsotopicRatio()) {
                count++;
                selected += userFunction.isInverted() ? 1 : 0;
            }
            checkBoxSelectAllRatiosInverted.setSelected(selected == count);
            checkBoxSelectAllRatiosInverted.setIndeterminate((0 < selected) && (selected < count));
        }
        checkBoxSelectAllRatiosInverted.selectedProperty().addListener(allRatiosInvertedChangeListener);
        hBox.getChildren().add(checkBoxSelectAllRatiosInverted);
        hBox.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
        ratiosVBox.getChildren().add(hBox);

        functionsVBox.getChildren().clear();
        hBox = new HBox();
        hBox.setSpacing(50);
        hBox.setAlignment(Pos.CENTER);
        hBox.setPadding(new Insets(5, 5, 5, 5));
        CheckBox checkBoxSelectAllFunctions = new CheckBox("Select all");
        count = 0;
        selected = 0;
        for (UserFunction userFunction : analysis.getAnalysisMethod().getUserFunctions()) {
            if (!userFunction.isTreatAsIsotopicRatio()) {
                count++;
                selected += userFunction.isDisplayed() ? 1 : 0;
            }
            checkBoxSelectAllFunctions.setSelected(selected == count);
            checkBoxSelectAllFunctions.setIndeterminate((0 < selected) && (selected < count));
        }
        checkBoxSelectAllFunctions.selectedProperty().addListener(allFunctionsChangeListener);
        hBox.getChildren().add(checkBoxSelectAllFunctions);
        hBox.getChildren().add(new Label("User Functions"));
        hBox.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
        functionsVBox.getChildren().add(hBox);

        for (UserFunction userFunction : analysis.getAnalysisMethod().getUserFunctions()) {
            if (userFunction.isTreatAsIsotopicRatio()) {
                hBox = new HBox();
                CheckBox checkBoxRatio = new CheckBox(userFunction.getName());
                checkBoxRatio.setUserData(userFunction);
                checkBoxRatio.setSelected(userFunction.isDisplayed());
                checkBoxRatio.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    ((UserFunction) checkBoxRatio.getUserData()).setDisplayed(newValue);
                    int selectedR = 0;
                    for (CheckBox checkBoxRatio2 : ratioCheckBoxList) {
                        selectedR += (checkBoxRatio2.isSelected() ? 1 : 0);
                    }
                    checkBoxSelectAllRatios.selectedProperty().removeListener(allRatiosChangeListener);
                    checkBoxSelectAllRatios.setSelected(selectedR == ratioCheckBoxList.size());
                    checkBoxSelectAllRatios.setIndeterminate((0 < selectedR) && (selectedR < ratioCheckBoxList.size()));
                    checkBoxSelectAllRatios.selectedProperty().addListener(allRatiosChangeListener);
                    populateAnalysisMethodColumnsSelectorPane();
                });
                ratioCheckBoxList.add(checkBoxRatio);
                checkBoxRatio.setPrefWidth(175);

                CheckBox checkBoxInvert = new CheckBox("Invert to: " + userFunction.showInvertedRatioName());
                checkBoxInvert.setUserData(userFunction);
                checkBoxInvert.setSelected(userFunction.isInverted());
                checkBoxInvert.setDisable(!userFunction.isDisplayed());
                checkBoxInvert.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    ((UserFunction) checkBoxInvert.getUserData()).setInverted(newValue);
                    int selectedI = 0;
                    for (CheckBox checkBoxRatioInverted : ratioInvertedCheckBoxList) {
                        selectedI += (checkBoxRatioInverted.isSelected() ? 1 : 0);
                    }
                    checkBoxSelectAllRatiosInverted.selectedProperty().removeListener(allRatiosInvertedChangeListener);
                    checkBoxSelectAllRatiosInverted.setSelected(selectedI == ratioInvertedCheckBoxList.size());
                    checkBoxSelectAllRatiosInverted.setIndeterminate((0 < selectedI) && (selectedI < ratioCheckBoxList.size()));
                    checkBoxSelectAllRatiosInverted.selectedProperty().addListener(allRatiosInvertedChangeListener);

                });
                ratioInvertedCheckBoxList.add(checkBoxInvert);

                hBox.getChildren().add(checkBoxRatio);
                hBox.getChildren().add(checkBoxInvert);
                hBox.setSpacing(10);
                hBox.setPadding(new Insets(1, 1, 1, 125));

                ratiosVBox.getChildren().add(hBox);
            } else {
                hBox = new HBox();
                CheckBox checkBoxFunction = new CheckBox(userFunction.getName());
                checkBoxFunction.setUserData(userFunction);
                checkBoxFunction.setSelected(userFunction.isDisplayed());
                checkBoxFunction.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    ((UserFunction) checkBoxFunction.getUserData()).setDisplayed(newValue);
                    int selectedF = 0;
                    for (CheckBox checkBoxRatio2 : functionCheckBoxList) {
                        selectedF += (checkBoxRatio2.isSelected() ? 1 : 0);
                    }
                    checkBoxSelectAllFunctions.selectedProperty().removeListener(allFunctionsChangeListener);
                    checkBoxSelectAllFunctions.setSelected(selectedF == functionCheckBoxList.size());
                    checkBoxSelectAllFunctions.setIndeterminate((0 < selectedF) && (selectedF < functionCheckBoxList.size()));
                    checkBoxSelectAllFunctions.selectedProperty().addListener(allFunctionsChangeListener);

                });
                functionCheckBoxList.add(checkBoxFunction);
                checkBoxFunction.setPrefWidth(175);
                hBox.getChildren().add(checkBoxFunction);
                hBox.setSpacing(10);
                hBox.setPadding(new Insets(1, 1, 1, 125));
                functionsVBox.getChildren().add(hBox);
            }
        }
    }

    private void populateAnalysisMethodRatioBuilderPane() {
        addRatioButton.setStyle(addRatioButton.getStyle() + ";-fx-font-size:15");
        numeratorMassesListTextFlow.getChildren().clear();
        denominatorMassesListTextFlow.getChildren().clear();
        if (null != analysis.getAnalysisMethod()) {
            activeRatiosList = new TreeSet<>();
            List<SpeciesRecordInterface> species = analysis.getAnalysisMethod().getSpeciesListSortedByMass();
            StackPane numeratorMass;
            StackPane denominatorMass;
            for (SpeciesRecordInterface specie : species) {
                numeratorMass = makeMassStackPane(specie.prettyPrintShortForm(), "white");
                numeratorMass.setUserData(specie);
                numeratorMassesListTextFlow.getChildren().add(numeratorMass);
                numeratorMass.setOnMouseClicked((MouseEvent event) -> {
                    if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
                        ((Text) ((VBox) proposedRatioVBox.getChildren().get(0)).getChildren().get(0)).setText(specie.prettyPrintShortForm());
                        ((VBox) proposedRatioVBox.getChildren().get(0)).getChildren().get(0).setUserData(specie);
                        addRatioButton.setDisable(!checkLegalityOfProposedRatio());
                    }
                });

                denominatorMass = makeMassStackPane(specie.prettyPrintShortForm(), "white");
                denominatorMass.setUserData(specie);
                denominatorMassesListTextFlow.getChildren().add(denominatorMass);
                denominatorMass.setOnMouseClicked((MouseEvent event) -> {
                    if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
                        ((Text) ((VBox) proposedRatioVBox.getChildren().get(0)).getChildren().get(2)).setText(specie.prettyPrintShortForm());
                        ((VBox) proposedRatioVBox.getChildren().get(0)).getChildren().get(2).setUserData(specie);
                        addRatioButton.setDisable(!checkLegalityOfProposedRatio());
                    }
                });
            }

            allRatios = new ArrayList<>();
            allRatios.addAll(analysis.getAnalysisMethod().getIsotopicRatiosList());
            allRatios.addAll(analysis.getAnalysisMethod().getDerivedIsotopicRatiosList());
            Collections.sort(allRatios, IsotopicRatio::compareTo);

            populateRatiosForDisplay(allRatios);

            addRatioButton.setDisable(true);
            addRatioButton.setOnAction((evt) -> {
                IsotopicRatio ratio = new IsotopicRatio(
                        (SpeciesRecordInterface) ((VBox) proposedRatioVBox.getChildren().get(0)).getChildren().get(0).getUserData(),
                        (SpeciesRecordInterface) ((VBox) proposedRatioVBox.getChildren().get(0)).getChildren().get(2).getUserData(),
                        true);
                addRatioAction(ratio, proposedRatioVBox);
            });
        }
    }

    private void addRatioAction(IsotopicRatio ratio, VBox targetVBox) {
        if (checkLegalityOfProposedRatio(targetVBox)) {
            VBox ratioVBox = makeRatioVBox(ratio.prettyPrint(), Color.BLACK);
            ratioVBox.getChildren().get(0).setUserData(ratio);
            ratioVBox.getChildren().get(0).setOnMouseClicked(new RatioClickHandler(ratio, ratioVBox));
            updateAnalysisRatios(ratio, true);
            activeRatiosList.add(ratio);
            populateRatiosForDisplay(allRatios);
        }
    }

    private void populateRatiosForDisplay(List<IsotopicRatio> allRatios) {
        addRatioButton.setDisable(true);
        ratiosListTextFlow.getChildren().clear();
        for (IsotopicRatio ratio : allRatios) {
            if (ratio.isDisplayed()) {
                VBox ratioVBox = makeRatioVBox(ratio.prettyPrint(), Color.BLACK);
                ratioVBox.getChildren().get(0).setUserData(ratio);
                activeRatiosList.add(ratio);
                ratioVBox.getChildren().get(0).setOnMouseClicked(new RatioClickHandler(ratio, ratioVBox));
                ratioVBox.getChildren().get(1).setOnMouseClicked(new FlipRatioClickHandler(ratio, ratioVBox));
                ratiosListTextFlow.getChildren().add(ratioVBox);
            }
        }

        proposedRatioVBox = makeRatioVBox(" ? / ? ", Color.RED);
        proposedRatioVBox.getChildren().get(0).setStyle(proposedRatioVBox.getStyle() + "-fx-border-color: red;");
        ratiosListTextFlow.getChildren().add(proposedRatioVBox);
        proposedRatioVBox.getChildren().get(0).setOnMouseClicked((MouseEvent event) -> {
            if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
                clearProposedRatio();
            }
        });
        proposedRatioVBox.getChildren().get(1).setOnMouseClicked((MouseEvent event) -> {
            if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
                invertProposedRatio();
            }
        });
    }

    private boolean checkLegalityOfProposedRatio() {
        return checkLegalityOfProposedRatio(proposedRatioVBox);
    }

    private boolean checkLegalityOfProposedRatio(VBox targetVBox) {
        boolean isLegal = !((Text) ((VBox) targetVBox.getChildren().get(0)).getChildren().get(0)).getText().contains("?");
        isLegal = isLegal && !((Text) ((VBox) targetVBox.getChildren().get(0)).getChildren().get(2)).getText().contains("?");
        if (isLegal) {
            SpeciesRecordInterface num = (SpeciesRecordInterface) ((VBox) targetVBox.getChildren().get(0)).getChildren().get(0).getUserData();
            SpeciesRecordInterface den = (SpeciesRecordInterface) ((VBox) targetVBox.getChildren().get(0)).getChildren().get(2).getUserData();
            if (!num.equals(den)) {
                IsotopicRatio ratio = new IsotopicRatio(num, den, true);
                isLegal = !activeRatiosList.contains(ratio);
            } else {
                isLegal = false;
            }
        }
        return isLegal;
    }

    private void clearProposedRatio() {
        ((Text) ((VBox) proposedRatioVBox.getChildren().get(0)).getChildren().get(0)).setText(" ? ");
        ((Text) ((VBox) proposedRatioVBox.getChildren().get(0)).getChildren().get(2)).setText(" ? ");
        addRatioButton.setDisable(true);
    }

    private void invertProposedRatio() {
        SpeciesRecordInterface num = (SpeciesRecordInterface) ((VBox) proposedRatioVBox.getChildren().get(0)).getChildren().get(0).getUserData();
        SpeciesRecordInterface den = (SpeciesRecordInterface) ((VBox) proposedRatioVBox.getChildren().get(0)).getChildren().get(2).getUserData();
        ((Text) ((VBox) proposedRatioVBox.getChildren().get(0)).getChildren().get(0)).setText(null == den ? " ? " : den.prettyPrintShortForm());
        ((VBox) proposedRatioVBox.getChildren().get(0)).getChildren().get(0).setUserData(den);
        ((Text) ((VBox) proposedRatioVBox.getChildren().get(0)).getChildren().get(2)).setText(null == num ? " ? " : num.prettyPrintShortForm());
        ((VBox) proposedRatioVBox.getChildren().get(0)).getChildren().get(2).setUserData(num);
        addRatioButton.setDisable(!checkLegalityOfProposedRatio());
    }

    private void populateBlocksStatus() {
        blockStatusHBox.getChildren().clear();
        if (null != analysis.getAnalysisMethod()) {
            var massSpecExtractedData = analysis.getMassSpecExtractedData();
            Map<Integer, MassSpecOutputBlockRecordFull> blocksData = massSpecExtractedData.getBlocksDataFull();
            for (MassSpecOutputBlockRecordFull block : blocksData.values()) {
                Button blockStatusButton = blockStatusButtonFactory(block.blockID());
                blockStatusHBox.getChildren().add(blockStatusButton);
            }
        }
    }

    private Button blockStatusButtonFactory(int blockID) {
        Button blockStatusButton = new Button();
        blockStatusButton.setPrefSize(45.0, 25.0);
        blockStatusButton.setPadding(new Insets(0003));
        blockStatusButton.setFont(Font.font("Monospaced", FontWeight.EXTRA_BOLD, 10));
        blockStatusButton.setId(String.valueOf(blockID));
        blockStatusButton.setPadding(new Insets(0, -1, 0, -1));
        if (null != analysis.getMapOfBlockIdToProcessStatus().get(blockID)) {
            tuneButton(blockStatusButton, analysis.getMapOfBlockIdToProcessStatus().get(blockID));
        }
        blockStatusButton.setOnAction(e -> {
            switch ((int) blockStatusButton.getUserData()) {
                case RUN -> {
                    if (null != analysis.getMapOfBlockIdToPlots().get(blockID)) {
                        tuneButton(blockStatusButton, SHOW);
                    } else {
                        tuneButton(blockStatusButton, SKIP);
                    }
                }
                case SHOW -> tuneButton(blockStatusButton, SKIP);
                case SKIP -> tuneButton(blockStatusButton, RUN);
            }
        });

        return blockStatusButton;
    }

    private void tuneButton(Button blockStatusButton, int blockStatus) {
        Color stateColor = Color.BLACK;
        switch (blockStatus) {
            case SKIP -> {
                stateColor = TRIPOLI_ANALYSIS_RED;
                blockStatusButton.setUserData(SKIP);
                blockStatusButton.setText("Skip " + blockStatusButton.getId());
                analysis.getMapOfBlockIdToProcessStatus().put(Integer.parseInt(blockStatusButton.getId()), SKIP);
            }
            case RUN -> {
                stateColor = Color.WHITE;
                blockStatusButton.setUserData(RUN);
                blockStatusButton.setText("Run " + blockStatusButton.getId());
                analysis.getMapOfBlockIdToProcessStatus().put(Integer.parseInt(blockStatusButton.getId()), RUN);
            }
            case SHOW -> {
                stateColor = TRIPOLI_ANALYSIS_GREEN;
                blockStatusButton.setUserData(SHOW);
                blockStatusButton.setText("Show " + blockStatusButton.getId());
                analysis.getMapOfBlockIdToProcessStatus().put(Integer.parseInt(blockStatusButton.getId()), SHOW);
            }
        }
        blockStatusButton.setStyle("-fx-background-color: " + convertColorToHex(stateColor) + ";-fx-border-color: BLACK");
    }

    @FXML
    public void selectDataFileButtonAction() {
        try {
            File selectedFile = selectDataFile(TripoliGUI.primaryStage);
            if (null != selectedFile) {
                removeAnalysisMethod();
                try {
                    analysis.extractMassSpecDataFromPath(Path.of(selectedFile.toURI()));
                } catch (TripoliException e) {
                    //TripoliMessageDialog.showWarningDialog(e.getMessage(), TripoliGUI.primaryStage);
                }

                // Proceed based on analysis case per https://docs.google.com/drawings/d/1U6-8LC55mHjHv8N7p6MAfKcdW8NibJSei3iTMT7E1A8/edit?usp=sharing
                populateAnalysisManagerGridPane(analysis.getAnalysisCaseNumber());

                processingToolBar.setDisable(null == analysis.getAnalysisMethod());

                try {
                    previewAndSculptDataAction();
                } catch (TripoliException e) {
                    throw new RuntimeException(e);
                }

            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException |
                 JAXBException | TripoliException e) {
            TripoliMessageDialog.showWarningDialog(e.getMessage(), TripoliGUI.primaryStage);
        }
    }

    private void removeAnalysisMethod() {
        analysis.resetAnalysis();
        populateAnalysisMethodGridPane();
        populateBlocksStatus();

    }

    @FXML
    private void selectMethodFileButtonAction() {
        try {
            File selectedFile = selectMethodFile(null);
            if ((null != selectedFile) && (selectedFile.exists())) {
                AnalysisMethod analysisMethod = analysis.extractAnalysisMethodfromPath(Path.of(selectedFile.toURI()));
                String compareInfo = compareAnalysisMethodToDataFileSpecs(analysisMethod, analysis.getMassSpecExtractedData());
                if (compareInfo.isBlank()) {
                    analysis.setMethod(analysisMethod);
                    ((Analysis) analysis).initializeBlockProcessing();
                    TripoliPersistentState.getExistingPersistentState().setMRUMethodXMLFolderPath(selectedFile.getParent());
                } else {
                    boolean choice = showChoiceDialog(
                            "The chosen analysis method does not meet the specifications in the data file.\n\n"
                                    + compareInfo
                                    + "\n\nProceed?", TripoliGUI.primaryStage);
                    if (choice) {
                        analysis.setMethod(analysisMethod);
                        ((Analysis) analysis).initializeBlockProcessing();
                        TripoliPersistentState.getExistingPersistentState().setMRUMethodXMLFolderPath(selectedFile.getParent());
                    }
                }
            }
        } catch (TripoliException | IOException | JAXBException e) {
            TripoliMessageDialog.showWarningDialog(e.getMessage(), TripoliGUI.primaryStage);
        }
        processingToolBar.setDisable(null == analysis.getAnalysisMethod());
        // initialize block processing state
        for (Integer blockID : analysis.getMassSpecExtractedData().getBlocksDataFull().keySet()) {
            analysis.getMapOfBlockIdToProcessStatus().put(blockID, RUN);
        }
        populateAnalysisManagerGridPane(analysis.getAnalysisCaseNumber());
    }

    @FXML
    final void initializeMonteCarloTechniqueAction() {
        for (Node button : blockStatusHBox.getChildren()) {
            if (button instanceof Button) {
                analysis.getMapOfBlockIdToProcessStatus().put(Integer.parseInt(button.getId()), (int) button.getUserData());
            }
        }
        if (null != MCMCPlotsWindow) {
            MCMCPlotsWindow.close();
        }
        MCMCPlotsWindow = new MCMCPlotsWindow(TripoliGUI.primaryStage, this);
        MCMCPlotsWindow.loadPlotsWindow();
    }


    public void previewAndSculptDataAction() throws TripoliException {
        // ogTripoli view
        if (null != ogTripoliPreviewPlotsWindow) {
            ogTripoliPreviewPlotsWindow.close();
        }

        AllBlockInitForMCMC.PlottingData plottingData = null;
        switch (analysis.getAnalysisCaseNumber()) {
            case 0 -> {
            }
            case 1 -> {
                plottingData = AllBlockInitForDataLiteOne.initBlockModels(analysis);
            }
            case 2 -> {
            }
            case 3 -> {
            }
            case 4 -> {
                if (analysis.getAnalysisMethod() != null) {
                    plottingData = AllBlockInitForMCMC.initBlockModels(analysis);
                }
            }
        }

        if (plottingData != null) {
            ogTripoliPreviewPlotsWindow = new OGTripoliPlotsWindow(TripoliGUI.primaryStage, this, plottingData);
            ogTripoliPreviewPlotsWindow.loadPlotsWindow();
        }

    }

    public void reviewAndSculptDataAction() {
        // fire up OGTripoli style session plots
        if (null != ogTripoliReviewPlotsWindow) {
            ogTripoliReviewPlotsWindow.close();
        }
        AllBlockInitForMCMC.PlottingData plottingData = analysis.assemblePostProcessPlottingData();
        ogTripoliReviewPlotsWindow = new OGTripoliPlotsWindow(TripoliGUI.primaryStage, this, plottingData);
        ogTripoliReviewPlotsWindow.loadPlotsWindow();
    }

    public void selectRunAllAction() {
        for (Node button : blockStatusHBox.getChildren()) {
            if (button instanceof Button) {
                tuneButton((Button) button, RUN);
            }
        }
        mcmcButton.setDisable(false);
    }

    public void selectRunNoneAction() {
        for (Node button : blockStatusHBox.getChildren()) {
            if (button instanceof Button) {
                tuneButton((Button) button, SKIP);
            }
        }
    }

    public void selectShowsAction() {
        for (Node button : blockStatusHBox.getChildren()) {
            if ((button instanceof Button) && (null != analysis.getMapOfBlockIdToPlots().get(Integer.parseInt(button.getId())))) {
                tuneButton((Button) button, SHOW);
            } else if (null == analysis.getMapOfBlockIdToPlots().get(Integer.parseInt(button.getId()))) {
                tuneButton((Button) button, SKIP);
            }
        }
    }

    /**
     * Restores block status buttons to their saved state
     */
    public void refreshAllBlocksStatusAction() {
        for (Node button : blockStatusHBox.getChildren()) {
            if ((button instanceof Button) && (null != analysis.getMapOfBlockIdToProcessStatus().get(Integer.parseInt(button.getId())))) {
                tuneButton((Button) button, analysis.getMapOfBlockIdToProcessStatus().get(Integer.parseInt(button.getId())));
            }
        }
    }

    /**
     *
     */
    @Override
    public void callbackRefreshBlocksStatus() {
        refreshAllBlocksStatusAction();
    }

    @Override
    public void callBackSetBlockIncludedStatus(int blockID, boolean included) {
        boolean isProcessed = (null != analysis.getMapOfBlockIdToPlots().get(blockID));
        analysis.getMapOfBlockIdToProcessStatus().put(blockID, included ? (isProcessed ? SHOW : RUN) : SKIP);
        populateBlocksStatus();
        if (null != ogTripoliReviewPlotsWindow) {
            ogTripoliReviewPlotsWindow.getOgTripoliViewController().populatePlots();
        }
        if (null != ogTripoliPreviewPlotsWindow) {
            ogTripoliPreviewPlotsWindow.getOgTripoliViewController().populatePlots();
        }
    }

    private void updateAnalysisRatios(IsotopicRatio ratio, boolean displayed) {
        AnalysisMethod analysisMethod = analysis.getAnalysisMethod();
        int indexOfIsotopicRatio = analysisMethod.getIsotopicRatiosList().indexOf(ratio);
        if (0 <= indexOfIsotopicRatio) {
            analysisMethod.getIsotopicRatiosList().get(indexOfIsotopicRatio).setDisplayed(displayed);
            analysis.updateRatiosPlotBuilderDisplayStatus(indexOfIsotopicRatio, displayed);
        }
        int indexOfDerivedIsotopicRatio = analysisMethod.getDerivedIsotopicRatiosList().indexOf(ratio);
        if (0 <= indexOfDerivedIsotopicRatio) {
            analysisMethod.getDerivedIsotopicRatiosList().get(indexOfDerivedIsotopicRatio).setDisplayed(displayed);
            analysis.updateRatiosPlotBuilderDisplayStatus(indexOfDerivedIsotopicRatio + analysisMethod.getIsotopicRatiosList().size(), displayed);
        }
    }

    public void knotsChoiceAction() {
        analysis.getAnalysisMethod().toggleKnotsMethod();
    }

    class RatioClickHandler implements EventHandler<MouseEvent> {
        IsotopicRatio ratio;
        VBox ratioVBox;

        public RatioClickHandler(IsotopicRatio ratio, VBox ratioVBox) {
            this.ratio = ratio;
            this.ratioVBox = ratioVBox;
        }

        /**
         * @param event the event which occurred
         */
        @Override
        public void handle(MouseEvent event) {
            if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
                ratiosListTextFlow.getChildren().remove(ratioVBox);
                activeRatiosList.remove(ratio);
                updateAnalysisRatios(ratio, false);
            }
        }
    }

    class FlipRatioClickHandler implements EventHandler<MouseEvent> {
        IsotopicRatio ratio;
        VBox ratioVBox;

        public FlipRatioClickHandler(IsotopicRatio ratio, VBox ratioVBox) {
            this.ratio = ratio;
            this.ratioVBox = ratioVBox;
        }

        /**
         * @param event the event which occurred
         */
        @Override
        public void handle(MouseEvent event) {
            if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
                IsotopicRatio invertedRatio = new IsotopicRatio(
                        ratio.getDenominator(),
                        ratio.getNumerator(),
                        true);
                VBox myVBox = makeRatioVBox(invertedRatio.prettyPrint(), Color.BLACK);
                ((VBox) myVBox.getChildren().get(0)).getChildren().get(0).setUserData(invertedRatio.getNumerator());
                ((VBox) myVBox.getChildren().get(0)).getChildren().get(2).setUserData(invertedRatio.getDenominator());
                if (checkLegalityOfProposedRatio(myVBox)) {
                    ratiosListTextFlow.getChildren().remove(ratioVBox);
                    activeRatiosList.remove(ratio);
                    ratio.setDisplayed(false);
                    updateAnalysisRatios(ratio, false);
                    addRatioAction(invertedRatio, myVBox);
                }
            }
        }
    }

    private class CheckBoxChangeListener implements ChangeListener<Boolean> {
        private final CheckBox checkBox;

        public CheckBoxChangeListener(CheckBox checkBox) {
            this.checkBox = checkBox;
        }

        /**
         * @param observable The {@code ObservableValue} which value changed
         * @param oldValue   The old value
         * @param newValue   The new value
         */
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            boolean displayed = newValue;
            IsotopicRatio ratio = (IsotopicRatio) checkBox.getUserData();
            updateAnalysisRatios(ratio, displayed);
        }
    }
}