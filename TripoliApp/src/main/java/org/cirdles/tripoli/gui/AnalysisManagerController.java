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

package org.cirdles.tripoli.gui;

import jakarta.xml.bind.JAXBException;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.text.*;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.cirdles.tripoli.ExpressionsForTripoliLexer;
import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;
import org.cirdles.tripoli.expressions.constants.ConstantNode;
import org.cirdles.tripoli.expressions.expressionTrees.ExpressionTree;
import org.cirdles.tripoli.expressions.expressionTrees.ExpressionTreeInterface;
import org.cirdles.tripoli.expressions.operations.Operation;
import org.cirdles.tripoli.expressions.parsing.ShuntingYard;
import org.cirdles.tripoli.expressions.species.IsotopicRatio;
import org.cirdles.tripoli.expressions.species.SpeciesRecordInterface;
import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.expressions.userFunctions.UserFunctionDisplay;
import org.cirdles.tripoli.expressions.userFunctions.UserFunctionNode;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots.MCMC2PlotsController;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots.MCMC2PlotsWindow;
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
import org.cirdles.tripoli.sessions.analysis.outputs.etRedux.ETReduxFraction;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.stateUtilities.AnalysisMethodPersistance;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliPersistentState;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.cirdles.tripoli.constants.TripoliConstants.MISSING_STRING_FIELD;
import static org.cirdles.tripoli.expressions.operations.Operation.OPERATIONS_MAP;
import static org.cirdles.tripoli.gui.SessionManagerController.tripoliSession;
import static org.cirdles.tripoli.gui.TripoliGUI.primaryStageWindow;
import static org.cirdles.tripoli.gui.constants.ConstantsTripoliApp.*;
import static org.cirdles.tripoli.gui.dialogs.TripoliMessageDialog.showChoiceDialog;
import static org.cirdles.tripoli.gui.utilities.UIUtilities.showTab;
import static org.cirdles.tripoli.gui.utilities.fileUtilities.FileHandlerUtil.*;
import static org.cirdles.tripoli.sessions.analysis.Analysis.*;
import static org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod.compareAnalysisMethodToDataFileSpecs;

public class AnalysisManagerController implements Initializable, AnalysisManagerCallbackI {

    public static boolean readingFile = false;
    public static AnalysisInterface analysis;
    public static MCMCPlotsWindow MCMCPlotsWindow;
    public static MCMC2PlotsWindow MCMC2PlotsWindow;
    public static OGTripoliPlotsWindow ogTripoliReviewPlotsWindow;
    public static OGTripoliPlotsWindow ogTripoliPreviewPlotsWindow;
    private final Map<String, boolean[][]> mapOfGridPanesToCellUse = new TreeMap<>();
    public Tab detectorDetailTab;
    public TabPane analysisMethodTabPane;
    @FXML
    public HBox blockStatusHBox;
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
    public TextField fractionNameTextField;
    public ScrollPane ratiosScrollPane;
    @FXML
    public ScrollPane functionsScrollPane;
    @FXML
    public Button exportToETReduxButton;
    @FXML
    public Button exportToClipBoardButton;
    @FXML
    public Button mcmc2Button;
    @FXML
    public Spinner<Integer> defaultCyclesPerBlockSpinner;
    @FXML
    public Button reloadDataForCyclesPerBlockBtn;
    public HBox ratiosHeaderHBox;
    public HBox functionsHeaderHBox;
    @FXML
    public Accordion expressionAccordion;
    @FXML
    public TextFlow expressionTextFlow;
    @FXML
    public ScrollPane expressionScrollPane;
    @FXML
    public Button createExpressionButton;
    @FXML
    public Button editExpressionButton;
    @FXML
    public Button cancelExpressionButton;
    @FXML
    public Button saveExpressionButton;
    @FXML
    public Button deleteExpressionButton;
    @FXML
    public TextField expressionNameTextField;
    @FXML
    public Button expressionClearBtn;
    @FXML
    public Button expressionUndoBtn;
    @FXML
    public Button expressionRedoBtn;
    @FXML
    public Button expressionAsTextBtn;
    @FXML
    public AnchorPane expressionPane;
    @FXML
    public HBox expressionsHeaderHBox;
    @FXML
    public ScrollPane expressionsScrollPane;
    @FXML
    public VBox expressionsVBox;
    @FXML
    public Label expressionInvalidLabel;
    @FXML
    public Label expressionUnsavedLabel;
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
    Text insertIndicator = new Text("|");
    private final TextArea expressionAsTextArea = new TextArea();
    private final BooleanProperty editAsText = new SimpleBooleanProperty(false);
    private final StringProperty expressionString = new SimpleStringProperty();
    private final int EXPRESSION_BUILDER_DEFAULT_FONTSIZE = 13;
    private int fontSizeModifier = 0; // todo: remove if not implementing font resizing
    private List<String> listOperators = new ArrayList<>();
    private ObservableList<ExpressionTreeInterface> customExpressionsList = FXCollections.observableArrayList();
    private StateManager<String> expressionStateManager = new StateManager<>();
    private final ObjectProperty<Mode> currentMode = new SimpleObjectProperty<>(Mode.EDIT);
    private ListView<ExpressionTreeInterface> customExpressionLV = new ListView<>();

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
        mcmc2Button.setDisable(false);
        // March 2024 implement drag n drop of files ===================================================================
        analysisManagerGridPane.setOnDragOver(event -> {
            event.acceptTransferModes(TransferMode.MOVE);
        });
        analysisManagerGridPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (event.getDragboard().hasFiles()) {
                for (int i = 0; i < db.getFiles().size(); i++) {
                    File dataFile = db.getFiles().get(i);

                    AnalysisInterface analysisProposed;
                    try {
                        analysisProposed = AnalysisInterface.initializeNewAnalysis(0);
                        String analysisName = analysisProposed.extractMassSpecDataFromPath(Path.of(dataFile.toURI()));
                        if (analysisProposed.getMassSpecExtractedData().getMassSpectrometerContext().compareTo(MassSpectrometerContextEnum.UNKNOWN) != 0) {
                            analysisProposed.setAnalysisName(analysisName);
                            analysisProposed.setAnalysisStartTime(analysisProposed.getMassSpecExtractedData().getHeader().analysisStartTime());
                            tripoliSession.getMapOfAnalyses().put(analysisProposed.getAnalysisName(), analysisProposed);
                            analysis = analysisProposed;
                            // manage analysis
                            readingFile = true;
                            MenuItem menuItemAnalysesManager = ((MenuBar) TripoliGUI.primaryStage.getScene()
                                    .getRoot().getChildrenUnmodifiable().get(0)).getMenus().get(1).getItems().get(0);
                            menuItemAnalysesManager.fire();
                        } else {
                            analysis = null;
                            TripoliMessageDialog.showWarningDialog("Tripoli does not recognize this file format.", primaryStageWindow);
                        }
                    } catch (JAXBException | IOException | InvocationTargetException | NoSuchMethodException e) {
//                    throw new RuntimeException(e);
                    } catch (IllegalAccessException | TripoliException e) {
//                    throw new RuntimeException(e);
                    }
                }
            }
        });
        // end implement drag n drop of files ===================================================================

        MCMCPlotsController.analysis = analysis;
        MCMC2PlotsController.analysis = analysis;
        OGTripoliViewController.analysis = analysis;
        analysisManagerGridPane.setStyle("-fx-background-color: " + convertColorToHex(TRIPOLI_ANALYSIS_YELLOW));

        setupListeners();

//        try {
//            if (readingFile) {
//                readingFile = false;
//                previewAndSculptDataFromFile();
//            } else {
//                previewAndSculptDataAction();
//            }
            populateAnalysisManagerGridPane(analysis.getAnalysisCaseNumber());
//        } catch (TripoliException e) {
//TODO: ALL need fixing:           throw new RuntimeException(e);
//        }

        ImageView imageView = new ImageView(getClass().getResource("/" + TRIPOLI_CLIPBOARD_ICON).toExternalForm());
        exportToClipBoardButton.setGraphic(imageView);
        exportToClipBoardButton.setPadding(new Insets(0, 0, 0, 0));
        exportToClipBoardButton.setMaxHeight(35);

        exportToETReduxButton.setDisable(analysis.getMassSpecExtractedData().getBlocksDataLite().isEmpty());
        reviewSculptData.setDisable(
                analysis.getMassSpecExtractedData().getBlocksDataLite().isEmpty()
                        && analysis.getMassSpecExtractedData().getBlocksDataFull().isEmpty());
        exportToClipBoardButton.setDisable(analysis.getMassSpecExtractedData().getBlocksDataLite().isEmpty());

        populateCustomExpressionTab();
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
        fractionNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            assert null != analysis;
            analysis.setAnalysisFractionName(newValue.isBlank() ? MISSING_STRING_FIELD : newValue);
        });
        sampleDescriptionTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            assert null != analysis;
            analysis.setAnalysisSampleDescription(newValue.isBlank() ? MISSING_STRING_FIELD : newValue);
        });

        int cyclesPerBlock = analysis.getMassSpecExtractedData().getHeader().cyclesPerBlock();
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 100, cyclesPerBlock);
        defaultCyclesPerBlockSpinner.setValueFactory(valueFactory);

        defaultCyclesPerBlockSpinner.setEditable(false);
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

        fractionNameTextField.setEditable(analysis.isMutable());
        fractionNameTextField.setText(analysis.getAnalysisFractionName());

        sampleDescriptionTextField.setEditable(analysis.isMutable());
        sampleDescriptionTextField.setText(analysis.getAnalysisSampleDescription());

        dataFilePathNameTextField.setEditable(false);
        dataFilePathNameTextField.setText(analysis.getDataFilePathString());

        if (0 != analysis.getDataFilePathString().compareToIgnoreCase(MISSING_STRING_FIELD)) {
            populateAnalysisDataFields();
            setupDefaults();
        }

        switch (caseNumber) {
            case 0 -> {
                analysisMethodTabPane.getTabs().remove(detectorDetailTab);
                analysisMethodTabPane.getTabs().remove(baselineTableTab);
                analysisMethodTabPane.getTabs().remove(sequenceTableTab);
                analysisMethodTabPane.getTabs().remove(selectRatiosToPlotTab);
                analysisMethodTabPane.getTabs().remove(selectColumnsToPlot);
            }
            case 1 -> {
                analysisMethodTabPane.getTabs().remove(detectorDetailTab);
                analysisMethodTabPane.getTabs().remove(baselineTableTab);
                analysisMethodTabPane.getTabs().remove(sequenceTableTab);
                analysisMethodTabPane.getTabs().remove(selectRatiosToPlotTab);
                showTab(analysisMethodTabPane, 2, selectColumnsToPlot);
                analysisMethodTabPane.getSelectionModel().select(2);
                populateAnalysisMethodColumnsSelectorPane();
                processingToolBar.setVisible(false);
            }
            case 2, 3, 4 -> {
                showTab(analysisMethodTabPane, 2, detectorDetailTab);
                showTab(analysisMethodTabPane, 3, baselineTableTab);
                showTab(analysisMethodTabPane, 4, sequenceTableTab);
                showTab(analysisMethodTabPane, 5, selectRatiosToPlotTab);
                analysisMethodTabPane.getTabs().remove(selectColumnsToPlot);
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

    private void setupDefaults() {
        // determine saved display status for userfunctions
        TripoliPersistentState tripoliPersistentState = null;
        try {
            tripoliPersistentState = TripoliPersistentState.getExistingPersistentState();
        } catch (TripoliException e) {
//            throw new RuntimeException(e);
        }

        List<UserFunction> userFunctions = analysis.getUserFunctions();

        AnalysisMethodPersistance analysisMethodPersistance =
                tripoliPersistentState.getMapMethodNamesToDefaults().get(analysis.getMethod().getMethodName());
        if (analysisMethodPersistance == null) {
            analysisMethodPersistance = new AnalysisMethodPersistance(10);
            tripoliPersistentState.getMapMethodNamesToDefaults().put(analysis.getMethod().getMethodName(), analysisMethodPersistance);
        }

        Map<String, UserFunctionDisplay> userFunctionDisplayMap = analysisMethodPersistance.getUserFunctionDisplayMap();
        if (userFunctionDisplayMap.isEmpty()) {
            for (int i = 0; i < userFunctions.size(); i++) {
                userFunctionDisplayMap.put(userFunctions.get(i).getName(), userFunctions.get(i).calcUserFunctionDisplay());
            }
        } else {
            for (int i = 0; i < userFunctions.size(); i++) {
                UserFunctionDisplay userFunctionDisplay = userFunctionDisplayMap.get(userFunctions.get(i).getName());
                if (userFunctionDisplay != null) {
                    userFunctions.get(i).setDisplayed(userFunctionDisplay.isDisplayed());
                    userFunctions.get(i).setInverted(userFunctionDisplay.isInverted());
                }
            }
        }

        populateAnalysisMethodColumnsSelectorPane();
    }

    private void populateAnalysisMethodColumnsSelectorPane() {
        List<CheckBox> ratioCheckBoxList = new ArrayList<>();
        List<CheckBox> ratioInvertedCheckBoxList = new ArrayList<>();
        List<CheckBox> functionCheckBoxList = new ArrayList<>();
        List<CheckBox> expressionCheckBoxList = new ArrayList<>();
        List<Label> exportLabelList = new ArrayList<>();
        List<RadioButton> cycleMeanRBs = new ArrayList<>();

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
        ChangeListener<Boolean> allExpressionsChangeListener = (observable, oldValue, newValue) -> {
            for (CheckBox checkBoxRatio : expressionCheckBoxList) {
                checkBoxRatio.setSelected(newValue);
            }
        };

        // Init Isotopic Ratio Box
        ratiosScrollPane.prefHeightProperty().bind(analysisMethodTabPane.heightProperty());
        ratiosVBox.prefWidthProperty().bind(ratiosScrollPane.widthProperty());
        ratiosVBox.prefHeightProperty().bind(ratiosScrollPane.heightProperty());
        ratiosVBox.getChildren().clear();

        ratiosHeaderHBox.prefWidthProperty().bind(ratiosScrollPane.widthProperty());
        ratiosHeaderHBox.getChildren().clear();
        HBox hBox = new HBox();
        hBox.setSpacing(25);
        hBox.setPadding(new Insets(5, 5, 5, 5));
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.prefWidthProperty().bind(ratiosVBox.widthProperty());


        List<UserFunction> userFunctions = analysis.getUserFunctions();
        CheckBox checkBoxSelectAllRatios = new CheckBox("Plot all Isotopic Ratios");
        checkBoxSelectAllRatios.setPrefWidth(225);
        int count = 0;
        int selected = 0;
        for (UserFunction userFunction : userFunctions) {
            if (userFunction.isTreatAsIsotopicRatio()) {
                count++;
                selected += userFunction.isDisplayed() ? 1 : 0;
            }
            checkBoxSelectAllRatios.setSelected(selected == count);
            checkBoxSelectAllRatios.setIndeterminate((0 < selected) && (selected < count));
        }
        checkBoxSelectAllRatios.selectedProperty().addListener(allRatiosChangeListener);
        hBox.getChildren().add(checkBoxSelectAllRatios);

        CheckBox checkBoxSelectAllRatiosInverted = new CheckBox("Invert all");
        checkBoxSelectAllRatiosInverted.setPrefWidth(110);
        count = 0;
        selected = 0;
        for (UserFunction userFunction : userFunctions) {
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

        Label exportHeaderLabel = new Label("Exported as");
        exportHeaderLabel.setPrefWidth(100);
        hBox.getChildren().add(exportHeaderLabel);

        Label toggleCycleMeansLabel = new Label("All Cycle Means");
        toggleCycleMeansLabel.setPrefWidth(100);
        toggleCycleMeansLabel.setPadding(new Insets(0, 0, 0, 0));

        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle(refreshButton.getStyle() + ";-fx-text-fill: RED;");
        refreshButton.setPrefWidth(65);
        refreshButton.setPadding(new Insets(0, 0, 0, 0));
        refreshButton.setOnAction(event -> populateAnalysisMethodColumnsSelectorPane());

        hBox.getChildren().addAll(toggleCycleMeansLabel, refreshButton);
        ratiosHeaderHBox.getChildren().add(hBox);
        // ---------- end IR

        // Init UserFunction Box
        functionsHeaderHBox.prefWidthProperty().bind(ratiosScrollPane.widthProperty());
        functionsHeaderHBox.getChildren().clear();
        functionsScrollPane.prefHeightProperty().bind(analysisMethodTabPane.heightProperty());
        functionsVBox.prefWidthProperty().bind(ratiosScrollPane.widthProperty());
        functionsVBox.prefHeightProperty().bind(ratiosScrollPane.heightProperty());
        functionsVBox.getChildren().clear();

        hBox = new HBox();
        hBox.setSpacing(5);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 5));
        hBox.prefWidthProperty().bind(functionsVBox.widthProperty());
        CheckBox checkBoxSelectAllFunctions = new CheckBox("Plot all User Functions");
        count = 0;
        selected = 0;
        for (UserFunction userFunction : userFunctions) {
            if (!userFunction.isTreatAsIsotopicRatio() && !userFunction.isTreatAsCustomExpression()) {
                count++;
                selected += userFunction.isDisplayed() ? 1 : 0;
            }
            checkBoxSelectAllFunctions.setSelected(selected == count);
            checkBoxSelectAllFunctions.setIndeterminate((0 < selected) && (selected < count));
        }
        checkBoxSelectAllFunctions.selectedProperty().addListener(allFunctionsChangeListener);
        hBox.getChildren().add(checkBoxSelectAllFunctions);
        hBox.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
        functionsHeaderHBox.getChildren().add(hBox);
        // ---------- end UF

        // Init Custom Expression Box -------------------
        expressionsHeaderHBox.prefWidthProperty().bind(functionsHeaderHBox.widthProperty());
        expressionsHeaderHBox.getChildren().clear();
        expressionsScrollPane.prefHeightProperty().bind(analysisMethodTabPane.heightProperty());
        expressionsVBox.prefWidthProperty().bind(functionsHeaderHBox.widthProperty());
        expressionsVBox.prefHeightProperty().bind(functionsHeaderHBox.heightProperty());
        expressionsVBox.getChildren().clear();

        hBox = new HBox();
        hBox.setSpacing(5);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 5));
        hBox.prefWidthProperty().bind(expressionsVBox.widthProperty());
        CheckBox checkBoxSelectAllExpressions = new CheckBox("Plot all Custom Expressions");
        count = 0;
        selected = 0;
        for (UserFunction userFunction : userFunctions) {
            if (userFunction.isTreatAsCustomExpression()) {
                count++;
                selected += userFunction.isDisplayed() ? 1 : 0;
            }
            checkBoxSelectAllExpressions.setSelected(selected == count);
            checkBoxSelectAllExpressions.setIndeterminate((0 < selected) && (selected < count));
        }
        checkBoxSelectAllExpressions.selectedProperty().addListener(allExpressionsChangeListener);
        hBox.getChildren().add(checkBoxSelectAllExpressions);
        hBox.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
        expressionsHeaderHBox.getChildren().add(hBox);
        // ---------- end CE

        userFunctions.sort(null);
        for (UserFunction userFunction : analysis.getUserFunctions()) {
            if (userFunction.isTreatAsIsotopicRatio() && !userFunction.isTreatAsCustomExpression()) {
                hBox = new HBox();
                CheckBox checkBoxRatio = new CheckBox(userFunction.getName());
                checkBoxRatio.setPrefWidth(500);
                checkBoxRatio.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
                checkBoxRatio.setUserData(userFunction);
                checkBoxRatio.setSelected(userFunction.isDisplayed());
                checkBoxRatio.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    userFunction.setDisplayed(newValue);
                    userFunction.setInverted(false);
                    if (newValue) {
                        int indexOfCheckBox = ratioCheckBoxList.indexOf(checkBoxRatio);
                        ratioInvertedCheckBoxList.get(indexOfCheckBox).setSelected(false);
                    }
                    int selectedR = 0;
                    for (CheckBox checkBoxRatioSingleton : ratioCheckBoxList) {
                        selectedR += (checkBoxRatioSingleton.isSelected() ? 1 : 0);
                    }
                    checkBoxSelectAllRatios.selectedProperty().removeListener(allRatiosChangeListener);
                    checkBoxSelectAllRatios.setSelected(selectedR == ratioCheckBoxList.size());
                    checkBoxSelectAllRatios.setIndeterminate((0 < selectedR) && (selectedR < ratioCheckBoxList.size()));
                    checkBoxSelectAllRatios.selectedProperty().addListener(allRatiosChangeListener);
                    populateAnalysisMethodColumnsSelectorPane();
                });
                ratioCheckBoxList.add(checkBoxRatio);
                checkBoxRatio.setPrefWidth(200);

                CheckBox checkBoxInvert = new CheckBox("Inverted");
                checkBoxInvert.setPrefWidth(100);
                checkBoxInvert.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
                checkBoxInvert.setUserData(userFunction);
                checkBoxInvert.setSelected(userFunction.isInverted());
                checkBoxInvert.setDisable(!userFunction.isDisplayed());
                checkBoxInvert.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    userFunction.setInverted(newValue);
                    int row = ratioInvertedCheckBoxList.indexOf(checkBoxInvert);
                    exportLabelList.get(row).setText(userFunction.getCorrectETReduxName());
                    int selectedI = 0;
                    for (CheckBox checkBoxRatioInverted : ratioInvertedCheckBoxList) {
                        selectedI += (checkBoxRatioInverted.isSelected() ? 1 : 0);
                    }
                    checkBoxSelectAllRatiosInverted.selectedProperty().removeListener(allRatiosInvertedChangeListener);
                    checkBoxSelectAllRatiosInverted.setSelected(selectedI == ratioInvertedCheckBoxList.size());
                    checkBoxSelectAllRatiosInverted.setIndeterminate((0 < selectedI) && (selectedI < ratioCheckBoxList.size()));
                    populateAnalysisMethodColumnsSelectorPane();
                    checkBoxSelectAllRatiosInverted.selectedProperty().addListener(allRatiosInvertedChangeListener);

                });
                ratioInvertedCheckBoxList.add(checkBoxInvert);

                Label exportLabel = new Label(userFunction.getCorrectETReduxName());
                exportLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
                exportLabel.setPrefWidth(65);
                exportLabelList.add(exportLabel);

                Label cycleMeanLabel = new Label(userFunction.showCycleMean());
                cycleMeanLabel.setUserData(userFunction);
                cycleMeanLabel.setDisable(!userFunction.isDisplayed());
                cycleMeanLabel.setPrefWidth(100);

                hBox.getChildren().addAll(checkBoxRatio, checkBoxInvert, exportLabel);
                if (!userFunction.getCorrectETReduxName().isEmpty()) {
                    hBox.getChildren().addAll(cycleMeanLabel);
                }
                hBox.setSpacing(45);
                hBox.setPadding(new Insets(1, 1, 1, 25));

                ratiosVBox.getChildren().add(hBox);
            } else if (userFunction.isTreatAsCustomExpression()){
                hBox = new HBox();
                CheckBox checkBoxExpression = new CheckBox(userFunction.getName());
                checkBoxExpression.setPrefWidth(500);
                checkBoxExpression.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
                checkBoxExpression.setUserData(userFunction);
                checkBoxExpression.setSelected(userFunction.isDisplayed());
                checkBoxExpression.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    ((UserFunction) checkBoxExpression.getUserData()).setDisplayed(newValue);
                    int selectedE = 0;
                    for (CheckBox checkBoxExpressionSingleton : expressionCheckBoxList) {
                        selectedE += (checkBoxExpressionSingleton.isSelected() ? 1 : 0);
                    }
                    checkBoxSelectAllExpressions.selectedProperty().removeListener(allExpressionsChangeListener);
                    checkBoxSelectAllExpressions.setSelected(selectedE == expressionCheckBoxList.size());
                    checkBoxSelectAllExpressions.setIndeterminate((0 < selectedE) && (selectedE < expressionCheckBoxList.size()));
                    checkBoxSelectAllExpressions.selectedProperty().addListener(allExpressionsChangeListener);
                });
                expressionCheckBoxList.add(checkBoxExpression);
                checkBoxExpression.setPrefWidth(175);
                hBox.getChildren().add(checkBoxExpression);
                hBox.setSpacing(45);
                hBox.setPadding(new Insets(1, 1, 1, 15));
                expressionsVBox.getChildren().add(hBox);
            } else {
                hBox = new HBox();
                CheckBox checkBoxFunction = new CheckBox(userFunction.getName());
                checkBoxFunction.setUserData(userFunction);
                checkBoxFunction.setSelected(userFunction.isDisplayed());
                checkBoxFunction.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    ((UserFunction) checkBoxFunction.getUserData()).setDisplayed(newValue);
                    int selectedF = 0;
                    for (CheckBox checkBoxFunctionSingleton : functionCheckBoxList) {
                        selectedF += (checkBoxFunctionSingleton.isSelected() ? 1 : 0);
                    }
                    checkBoxSelectAllFunctions.selectedProperty().removeListener(allFunctionsChangeListener);
                    checkBoxSelectAllFunctions.setSelected(selectedF == functionCheckBoxList.size());
                    checkBoxSelectAllFunctions.setIndeterminate((0 < selectedF) && (selectedF < functionCheckBoxList.size()));
                    checkBoxSelectAllFunctions.selectedProperty().addListener(allFunctionsChangeListener);

                });
                functionCheckBoxList.add(checkBoxFunction);
                checkBoxFunction.setPrefWidth(175);
                hBox.getChildren().add(checkBoxFunction);
                hBox.setSpacing(5);
                hBox.setPadding(new Insets(1, 1, 1, 15));
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

    private void populateCustomExpressionTab() {
        currentMode.set(Mode.VIEW);
        // List View inits ---------------------------------------
        List<UserFunction> userFunctions = analysis.getUserFunctions();
        List<Operation> operationList = new ArrayList<>(OPERATIONS_MAP.values());
        listOperators = OPERATIONS_MAP.keySet().stream().toList();

        ListView<ExpressionTreeInterface> userFunctionLV = new ListView<>();
        ListView<ExpressionTreeInterface> isotopicRatioLV = new ListView<>();
        ListView<ExpressionTreeInterface> operationLV = new ListView<>();

        for (UserFunction userFunction : userFunctions) {
            if (userFunction.isTreatAsIsotopicRatio() && !userFunction.isTreatAsCustomExpression()) {
                isotopicRatioLV.getItems().add(new UserFunctionNode(userFunction.getName()));
            } else if(userFunction.isTreatAsCustomExpression()) {
                customExpressionsList.add(userFunction.getCustomExpression());
            } else {
                userFunctionLV.getItems().add(new UserFunctionNode(userFunction.getName()));
            }
        }

        customExpressionLV.setItems(customExpressionsList);

        operationLV.getItems().add(new ConstantNode("# : Number", null));
        for (Operation op : operationList){
            Operation newOp = op.copy();
            newOp.setName(listOperators.get(operationList.indexOf(op)) + " : "+ op.getName());
            operationLV.getItems().add(newOp);
        }
        
        setAccordionListViewListener(userFunctionLV);
        setAccordionListViewListener(isotopicRatioLV);
        setAccordionListViewListener(operationLV);
        setAccordionListViewListener(customExpressionLV);

        TitledPane cePane = new TitledPane("Custom Expressions", customExpressionLV);
        TitledPane ufPane = new TitledPane("User Functions", userFunctionLV);
        TitledPane irPane = new TitledPane("Isotopic Ratios", isotopicRatioLV);
        TitledPane opPane = new TitledPane("Operations", operationLV);

        expressionAccordion.getPanes().addAll(cePane, ufPane, irPane, opPane);
        expressionAccordion.setExpandedPane(expressionAccordion.getPanes().get(0));
        // ------------------------- end LV inits

        initCustomExpressionListeners();

    }

    private void setAccordionListViewListener(ListView<ExpressionTreeInterface> accordionLV) {
        accordionLV.setCellFactory(param -> {
            ListCell<ExpressionTreeInterface> cell = new ListCell<>() {
                @Override
                protected void updateItem(ExpressionTreeInterface item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String displayName = item.getName().replaceAll("[\\[\\]]", "");
                        setText(displayName);
                    }
                }
            };

            cell.setOnDragDetected(event -> {
                ExpressionTreeInterface selectedItem = accordionLV.getSelectionModel().getSelectedItem();
                if (selectedItem != null && !currentMode.get().equals(Mode.VIEW)) {
                    Dragboard dragboard = cell.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    if (selectedItem instanceof Operation) { // OPERATION
                        String key = selectedItem.getName();
                        key = key.substring(0, key.indexOf(" : "));
                        if (((Operation) selectedItem).isSingleArg()){
                            key += "( )";
                        }
                        content.putString(key);
                    } else if (selectedItem instanceof UserFunctionNode) { // USER FUNCTION / RATIO
                        content.putString(((UserFunctionNode) selectedItem).getValue());
                    } else if (selectedItem instanceof ConstantNode && selectedItem.getName().contains(" : ")) { // CONSTANT
                        String key = selectedItem.getName();
                        key = key.substring(0, key.indexOf(" : "));
                        content.putString(key);
                    } else {// EXPRESSION TREE
                        content.putString(ExpressionTree.prettyPrint(selectedItem, analysis, false));
                    }
                    dragboard.setContent(content);
                    dragboard.setDragView(cell.snapshot(null, null));
                    accordionLV.setUserData(selectedItem);
                    event.consume();
                }
            });

            return cell;
        });
    }

    private void initCustomExpressionListeners() {
        insertIndicator.setFill(Color.RED);
        insertIndicator.setFont(Font.font("SansSerif", FontWeight.BOLD, 12));

        // TextFlow Drag/Drop ----------------------------
        expressionScrollPane.setOnDragOver(event -> {
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasString() && !currentMode.get().equals(Mode.VIEW)) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        expressionScrollPane.setOnDragEntered(event -> {
            if (!currentMode.get().equals(Mode.VIEW)){
                expressionTextFlow.getChildren().remove(insertIndicator);
                expressionTextFlow.getChildren().add(insertIndicator);
            }
        });

        expressionScrollPane.setOnDragExited(event -> {
            expressionTextFlow.getChildren().remove(insertIndicator);
        });

        expressionScrollPane.setOnDragDropped((DragEvent event) -> {
            int index = expressionTextFlow.getChildren().indexOf(insertIndicator);
            expressionTextFlow.getChildren().remove(insertIndicator);

            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString()) {
                String content = db.getString();

                expressionTextFlow.getChildren().add(index, new ExpressionTextNode(content));

                if (event.getGestureSource() instanceof ExpressionTextNode) {
                    expressionTextFlow.getChildren().remove(event.getGestureSource());
                }
                populateTextFlowFromString(makeStringFromExpressionTextNodeList());
                handleExpressionUpdate(true);

                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });
        // ------------ end textflow drag/drop

        // Existing expression selector
        customExpressionLV.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && currentMode.get().equals(Mode.VIEW)) {
                expressionNameTextField.setText(newValue.getName());
                if (newValue instanceof UserFunctionNode) {
                    populateTextFlowFromString(((UserFunctionNode) newValue).getValue());
                } else if (newValue instanceof ConstantNode) {
                    populateTextFlowFromString(((ConstantNode) newValue).getValue().toString());
                }else if (newValue instanceof ExpressionTree) {
                    populateTextFlowFromString(ExpressionTree.prettyPrint(newValue, analysis, false));
                }

            }
        });

        // Bind TextArea and TextFlow to expressionString property
        // This links all three objects to have the same text value
        expressionAsTextArea.setFont(Font.font("Monospaced"));
        expressionAsTextArea.textProperty().bindBidirectional(expressionString);

        expressionString.addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !makeStringFromExpressionTextNodeList().equals(newValue)) {
                populateTextFlowFromString(newValue);
            }
        });
        expressionTextFlow.getChildren().addListener((ListChangeListener<Node>) change -> {
            String textFlowContent = makeStringFromExpressionTextNodeList();
            if (!textFlowContent.equals(expressionString.get())) {
                expressionString.set(textFlowContent);
            }
        });

        // Property bindings ----------------------------------

        // Top menubar
        expressionUndoBtn.disableProperty().bind(expressionStateManager.canUndoProperty().not());
        expressionRedoBtn.disableProperty().bind(expressionStateManager.canRedoProperty().not());
        expressionClearBtn.disableProperty().bind(expressionString.isEmpty());
        expressionAsTextBtn.disableProperty().bind(currentMode.isEqualTo(Mode.VIEW));

        // Name field
        expressionNameTextField.editableProperty().bind(currentMode.isEqualTo(Mode.CREATE));

        // Unsaved Changes label
        expressionUnsavedLabel.visibleProperty().bind(expressionStateManager.hasChangesProperty());

        // Bottom menubar
        createExpressionButton.disableProperty().bind(currentMode.isNotEqualTo(Mode.VIEW));
        editExpressionButton.disableProperty().bind( // Enabled if in view mode and have a selected expression
                currentMode.isNotEqualTo(Mode.VIEW).or(customExpressionLV.getSelectionModel().selectedItemProperty().isNull())
        );
        cancelExpressionButton.disableProperty().bind(currentMode.isEqualTo(Mode.VIEW));
        saveExpressionButton.disableProperty().bind( // Enabled if has changes and be a valid expression
                expressionStateManager.hasChangesProperty().not().or(expressionInvalidLabel.visibleProperty()).or(expressionString.isEmpty()));
        deleteExpressionButton.disableProperty().bind( // Enabled if expression selected or editing
                currentMode.isEqualTo(Mode.CREATE).or(customExpressionLV.getSelectionModel().selectedItemProperty().isNull())
        );

        // ------------------------------ end property bindings
    }

    /**
     * Re-assign the indices for all the textflow nodes after inserting a new one based on their position in the textflow.
     */
    private void reindexExpressionTextFlowChildren() {
        for (int i = 0; i < expressionTextFlow.getChildren().size(); i++) {
            Node node = expressionTextFlow.getChildren().get(i);
            if (node instanceof ExpressionTextNode et) {
                et.setIndex(i);
            }
        }
        expressionTextFlow.getChildren().sort((Node o1, Node o2) -> {
            int retVal = 0;
            if (o1 instanceof ExpressionTextNode && o2 instanceof ExpressionTextNode) {
                retVal = Integer.compare(((ExpressionTextNode) o1).getIndex(), ((ExpressionTextNode) o2).getIndex());
            }
            return retVal;
        });
    }

    private String makeStringFromExpressionTextNodeList() {
        StringBuilder sb = new StringBuilder();
        for (Node node : expressionTextFlow.getChildren()) {
            if (node instanceof ExpressionTextNode) {
                ((ExpressionTextNode) node).getText();
                String txt = ((ExpressionTextNode) node).getText().trim();
                String nonLetter = "\t\n\r [](),+-*/<>=^\"";
                if (sb.isEmpty() || nonLetter.indexOf(sb.charAt(sb.length() - 1)) != -1 || nonLetter.indexOf(txt.charAt(0)) != -1) {
                    sb.append(txt);
                } else {
                    sb.append(" ").append(txt);
                }
            }
        }
        return sb.toString();
    }

    private void handleExpressionValidity() {
        List<String> rpnList = ShuntingYard.infixToPostfix(textFlowToList());
        try {
            ExpressionTree.buildTree(rpnList);
            expressionInvalidLabel.setVisible(false);
        } catch (Exception e) {
            expressionInvalidLabel.setVisible(true);
        }
        if (rpnList.isEmpty()) { expressionInvalidLabel.setVisible(false);}
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
                loadDataFile(selectedFile);
            }
        } catch (TripoliException e) {
            TripoliMessageDialog.showWarningDialog(e.getMessage(), TripoliGUI.primaryStage);
        }
    }

    private void loadDataFile(File selectedFile) {
        boolean legalFile = true;
        removeAnalysisMethod();
        String currentAnalysisName = analysis.getAnalysisName();
        if (tripoliSession.getMapOfAnalyses().containsKey(currentAnalysisName))
            tripoliSession.getMapOfAnalyses().remove(currentAnalysisName);
        try {
            String analysisName = analysis.extractMassSpecDataFromPath(Path.of(selectedFile.toURI()));

            if (analysis.getMassSpecExtractedData().getMassSpectrometerContext().compareTo(MassSpectrometerContextEnum.UNKNOWN) != 0) {
                analysis.setAnalysisName(analysisName);
                analysis.setAnalysisStartTime(analysis.getMassSpecExtractedData().getHeader().analysisStartTime());
                tripoliSession.getMapOfAnalyses().put(analysis.getAnalysisName(), analysis);
            } else {
                legalFile = false;
            }
        } catch (JAXBException | IOException | InvocationTargetException | NoSuchMethodException e) {
//                    throw new RuntimeException(e);
        } catch (IllegalAccessException | TripoliException e) {
//                    throw new RuntimeException(e);
        }

        if (legalFile) {
            // Proceed based on analysis case per https://docs.google.com/drawings/d/1U6-8LC55mHjHv8N7p6MAfKcdW8NibJSei3iTMT7E1A8/edit?usp=sharing
            populateAnalysisManagerGridPane(analysis.getAnalysisCaseNumber());

            try {
                previewAndSculptDataAction();
            } catch (TripoliException e) {
                throw new RuntimeException(e);
            }
            processingToolBar.setDisable(null == analysis.getAnalysisMethod());
            exportToETReduxButton.setDisable(analysis.getMassSpecExtractedData().getBlocksDataLite().isEmpty());
            reviewSculptData.setDisable(
                    analysis.getMassSpecExtractedData().getBlocksDataLite().isEmpty()
                            && analysis.getMassSpecExtractedData().getBlocksDataFull().isEmpty());
            exportToClipBoardButton.setDisable(analysis.getMassSpecExtractedData().getBlocksDataLite().isEmpty());
        } else {
            TripoliMessageDialog.showWarningDialog("Tripoli does not recognize this file format.", null);
        }
    }

    private void removeAnalysisMethod() {
        if (analysis != null) {
            analysis.resetAnalysis();
            populateAnalysisMethodGridPane();
            populateBlocksStatus();
        }
    }

    @FXML
    private void selectMethodFileButtonAction() {
        boolean switchedMethod = false;
        try {
            File selectedFile = selectMethodFile(null);
            if ((null != selectedFile) && (selectedFile.exists())) {
                AnalysisMethod analysisMethod = analysis.extractAnalysisMethodfromPath(Path.of(selectedFile.toURI()));
                String compareInfo = compareAnalysisMethodToDataFileSpecs(analysisMethod, analysis.getMassSpecExtractedData());
                if (compareInfo.isBlank()) {
                    analysis.setMethod(analysisMethod);
                    switchedMethod = true;
                    ((Analysis) analysis).initializeBlockProcessing();
                    TripoliPersistentState.getExistingPersistentState().setMRUMethodXMLFolderPath(selectedFile.getParent());
                } else {
                    boolean choice = showChoiceDialog(
                            "The chosen analysis method does not meet the specifications in the data file.\n\n"
                                    + compareInfo
                                    + "\n\nProceed?", TripoliGUI.primaryStage);
                    if (choice) {
                        analysis.setMethod(analysisMethod);
                        switchedMethod = true;
                        ((Analysis) analysis).initializeBlockProcessing();
                        TripoliPersistentState.getExistingPersistentState().setMRUMethodXMLFolderPath(selectedFile.getParent());
                    }
                }
            }
        } catch (TripoliException | IOException | JAXBException e) {
            TripoliMessageDialog.showWarningDialog(e.getMessage(), TripoliGUI.primaryStage);
        }
        if (switchedMethod) {
            processingToolBar.setDisable(null == analysis.getAnalysisMethod());
            // initialize block processing state
            for (Integer blockID : analysis.getMassSpecExtractedData().getBlocksDataFull().keySet()) {
                analysis.getMapOfBlockIdToProcessStatus().put(blockID, RUN);
            }
            populateAnalysisManagerGridPane(analysis.getAnalysisCaseNumber());
        }
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

    @FXML
    final void initializeMCMC2TechniqueAction() {
        if (null != MCMC2PlotsWindow) {
            MCMC2PlotsWindow.close();
        }
        MCMC2PlotsWindow = new MCMC2PlotsWindow(TripoliGUI.primaryStage, this);
        MCMC2PlotsWindow.loadPlotsWindow();
    }


    private void previewAndSculptDataFromFile() throws TripoliException {
        // first time opening file, suppress plotting
        TripoliPersistentState tripoliPersistentState = null;
        try {
            tripoliPersistentState = TripoliPersistentState.getExistingPersistentState();
        } catch (TripoliException e) {
//            throw new RuntimeException(e);
        }
        AnalysisMethodPersistance analysisMethodPersistance =
                tripoliPersistentState.getMapMethodNamesToDefaults().get(analysis.getMethod().getMethodName());
        if (analysisMethodPersistance != null) {
            Map<String, UserFunctionDisplay> userFunctionDisplayMap = analysisMethodPersistance.getUserFunctionDisplayMap();
            List<UserFunction> userFunctions = analysis.getUserFunctions();
            for (int i = 0; i < userFunctions.size(); i++) {
                UserFunctionDisplay userFunctionDisplay = userFunctionDisplayMap.get(userFunctions.get(i).getName());
                if (userFunctionDisplay != null) {
                    userFunctions.get(i).setDisplayed(userFunctionDisplay.isDisplayed());
                    userFunctions.get(i).setInverted(userFunctionDisplay.isInverted());
                }
            }
        }
        previewAndSculptDataAction();
    }

    public void previewAndSculptDataAction() throws TripoliException {
        // ogTripoli view
        // first time opening file, suppress plotting
        TripoliPersistentState tripoliPersistentState = null;
        try {
            tripoliPersistentState = TripoliPersistentState.getExistingPersistentState();
        } catch (TripoliException e) {
//            throw new RuntimeException(e);
        }

        if ((analysis.getMethod() != null) &&
                (tripoliPersistentState.getMapMethodNamesToDefaults().get(analysis.getMethod().getMethodName()) != null)) {
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
                populateAnalysisMethodColumnsSelectorPane();
                ogTripoliPreviewPlotsWindow = new OGTripoliPlotsWindow(TripoliGUI.primaryStage, this, plottingData);
                ogTripoliPreviewPlotsWindow.loadPlotsWindow();
            }
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

    public void exportToETReduxButtonAction() {

        ETReduxFraction etReduxFraction = analysis.prepareFractionForETReduxExport();
        String fileName = etReduxFraction.getSampleName() + "_" + etReduxFraction.getFractionID() + "_" + etReduxFraction.getEtReduxExportType() + ".xml";
        etReduxFraction.serializeXMLObject(fileName);
        try {
            saveExportFile(etReduxFraction, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TripoliException e) {
            throw new RuntimeException(e);
        }
    }

    public void exportToClipboardAction() {
        String clipBoardString = analysis.prepareFractionForClipboardExport();
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(clipBoardString);
        clipboard.setContent(content);
    }

    public void reloadDataForCyclesPerBlockBtnAction() throws TripoliException {
        TripoliPersistentState tripoliPersistentState = TripoliPersistentState.getExistingPersistentState();
        int cyclesPerBlock = defaultCyclesPerBlockSpinner.valueProperty().getValue();
        if (tripoliPersistentState.getMapMethodNamesToDefaults().containsKey(analysis.getAnalysisMethod().getMethodName())) {
            tripoliPersistentState.getMapMethodNamesToDefaults().get(analysis.getAnalysisMethod().getMethodName())
                    .setCyclesPerBlock(cyclesPerBlock);
        } else {
            AnalysisMethodPersistance analysisMethodPersistance = new AnalysisMethodPersistance(cyclesPerBlock);
            tripoliPersistentState.getMapMethodNamesToDefaults().put(analysis.getAnalysisMethod().getMethodName(), analysisMethodPersistance);
        }
        tripoliPersistentState.updateTripoliPersistentState();
        loadDataFile(new File(analysis.getDataFilePathString()));
    }

    public void saveDisplayedColumnsAsDefault() {
        // get saved display status for userfunctions
        TripoliPersistentState tripoliPersistentState = null;
        try {
            tripoliPersistentState = TripoliPersistentState.getExistingPersistentState();
        } catch (TripoliException e) {
//            throw new RuntimeException(e);
        }

        List<UserFunction> userFunctions = analysis.getUserFunctions();

        AnalysisMethodPersistance analysisMethodPersistance =
                tripoliPersistentState.getMapMethodNamesToDefaults().get(analysis.getMethod().getMethodName());

        Map<String, UserFunctionDisplay> userFunctionDisplayMap = analysisMethodPersistance.getUserFunctionDisplayMap();
        for (int i = 0; i < userFunctions.size(); i++) {
            UserFunctionDisplay userFunctionDisplay = userFunctionDisplayMap.get(userFunctions.get(i).getName());
            userFunctionDisplay.setDisplayed(userFunctions.get(i).isDisplayed());
            userFunctionDisplay.setInverted(userFunctions.get(i).isInverted());
        }

        tripoliPersistentState.updateTripoliPersistentState();
    }

    public void newCustomExpressionOnAction() {
        currentMode.set(Mode.CREATE);
        expressionString.set("");
        expressionNameTextField.clear();
        expressionStateManager.save("");

    }

    public void editCustomExpressionOnAction() {
        currentMode.set(Mode.EDIT);
        handleExpressionUpdate(true);
    }

    public void cancelCustomExpressionOnAction() {
        boolean proceed;
        if (expressionStateManager.hasChanges()) {
            proceed = showChoiceDialog("Unsaved changes detected. Proceed?", TripoliGUI.primaryStage);
            if (proceed) {
                expressionString.set(expressionStateManager.revert());
                if (currentMode.get() == Mode.CREATE) {
                    expressionNameTextField.clear();
                }
                expressionStateManager.clear();
                currentMode.set(Mode.VIEW);
            }
        } else {
            expressionStateManager.clear();
            currentMode.set(Mode.VIEW);
        }

    }

    public void saveCustomExpressionOnAction() {
        TripoliPersistentState tripoliPersistentState;
        try {
            tripoliPersistentState = TripoliPersistentState.getExistingPersistentState();
        } catch (TripoliException e) {
            e.printStackTrace();
            return;
        }

        String expressionName = expressionNameTextField.getText().trim();
        String expressionText = makeStringFromExpressionTextNodeList();

        if (expressionName.isBlank()) {
            TripoliMessageDialog.showWarningDialog("Please enter a name for the expression.", TripoliGUI.primaryStage);
            return;
        }
        if (expressionText.isBlank()) {
            TripoliMessageDialog.showWarningDialog("Please enter an expression.", TripoliGUI.primaryStage);
            return;
        }

        List<UserFunction> userFunctions = analysis.getUserFunctions();
        UserFunction existingFunction = userFunctions.stream()
                .filter(uf -> uf.getName().equalsIgnoreCase(expressionName))
                .findFirst()
                .orElse(null);

        if (existingFunction != null) {
            handleExistingExpression(existingFunction, expressionName, tripoliPersistentState);
        } else {
            handleNewExpression(expressionName, userFunctions, tripoliPersistentState);
        }
    }

    private void handleExistingExpression(UserFunction existingFunction, String expressionName, TripoliPersistentState persistentState) {
        if (!existingFunction.isTreatAsCustomExpression()) {
            TripoliMessageDialog.showWarningDialog("This name is already a built-in function. Please choose a different name.", TripoliGUI.primaryStage);
            return;
        }

        boolean proceed = currentMode.get() != Mode.CREATE || showChoiceDialog(
                "This name is already a custom expression. Would you like to overwrite?", TripoliGUI.primaryStage);
        if (!proceed) return;

        List<String> rpnList = ShuntingYard.infixToPostfix(textFlowToList());
        ExpressionTreeInterface expressionTree = ExpressionTree.buildTree(rpnList);
        expressionTree.setName(expressionName);

        existingFunction.setCustomExpression(expressionTree);
        checkExpressionForRenamedRatio(existingFunction);
        existingFunction.getMapBlockIdToBlockCyclesRecord().clear();

        analysis.getMassSpecExtractedData().populateCycleDataForCustomExpression(expressionTree);

        if (tripoliSession.isExpressionRefreshed()){
            persistentState.updateTripoliPersistentState();
        }

        populateAnalysisMethodColumnsSelectorPane();

        // replace old expression in list
        customExpressionsList.removeIf(e -> e.getName().equals(expressionTree.getName()));
        customExpressionsList.add(expressionTree);
        analysis.getMapOfBlockIdToRawDataLiteOne().clear(); // reset map for new data

        expressionStateManager.clear();
        currentMode.set(Mode.VIEW);
    }

    private void handleNewExpression(String expressionName, List<UserFunction> userFunctions, TripoliPersistentState persistentState) {
        List<String> rpnList = ShuntingYard.infixToPostfix(textFlowToList());
        ExpressionTreeInterface expressionTree = ExpressionTree.buildTree(rpnList);
        expressionTree.setName(expressionName);
        customExpressionsList.add(expressionTree);

        UserFunction newFunction = new UserFunction(expressionName, userFunctions.size(), false, true);
        newFunction.setTreatAsCustomExpression(true);
        checkExpressionForRenamedRatio(newFunction);
        newFunction.setCustomExpression(expressionTree);
        userFunctions.add(newFunction);

        analysis.getMassSpecExtractedData().populateCycleDataForCustomExpression(expressionTree);

        if (tripoliSession.isExpressionRefreshed()){
            AnalysisMethodPersistance methodPersistence =
                    persistentState.getMapMethodNamesToDefaults().get(analysis.getMethod().getMethodName());

            methodPersistence.getUserFunctionDisplayMap().put(
                    expressionName, new UserFunctionDisplay(expressionName, true, false));
            methodPersistence.getExpressionUserFunctionList().add(newFunction);

            persistentState.updateTripoliPersistentState();
        }

        populateAnalysisMethodColumnsSelectorPane();
        analysis.getMapOfBlockIdToRawDataLiteOne().clear(); // reset map for new data
        expressionStateManager.clear();
        currentMode.set(Mode.VIEW);
    }

    private void checkExpressionForRenamedRatio(UserFunction userFunction) {
        if (expressionTextFlow.getChildren().size() == 1){
            ExpressionTextNode expressionNode = (ExpressionTextNode) expressionTextFlow.getChildren().get(0);
            String ufName = expressionNode.getText().substring(2, expressionNode.getText().length() - 2);
            UserFunction existingFunction = analysis.getUserFunctions().stream()
                    .filter(uf -> uf.getName().equalsIgnoreCase(ufName))
                    .findFirst()
                    .orElse(null);
            if (existingFunction != null && existingFunction.isTreatAsIsotopicRatio()) {
                userFunction.setTreatAsIsotopicRatio(true);
            } else {
                userFunction.setTreatAsIsotopicRatio(false);
            }
        }
    }

    public void expressionClearAction() {
        if (currentMode.get().equals(Mode.VIEW)){
            expressionString.set("");
            expressionNameTextField.clear();

            expressionStateManager.clear();
            customExpressionLV.getSelectionModel().clearSelection();
        } else {
            expressionString.set("");

            handleExpressionUpdate(true);
        }
    }

    public void expressionUndoAction() {
        populateTextFlowFromString(expressionStateManager.undo());
    }

    public void expressionRedoAction() {
        populateTextFlowFromString(expressionStateManager.redo());
    }

    public void deleteCustomExpressionOnAction() {
        boolean proceed = showChoiceDialog("Are you sure you want to delete this expression?", TripoliGUI.primaryStage);
        if (proceed) {

            TripoliPersistentState tripoliPersistentState = null;
            try {
                tripoliPersistentState = TripoliPersistentState.getExistingPersistentState();
            } catch (TripoliException e) {
                e.printStackTrace();
            }
            String expressionName = expressionNameTextField.getText();
            List<UserFunction> userFunctions = analysis.getUserFunctions();

            UserFunction customExpression = userFunctions.stream().filter(uf -> uf.getName().equalsIgnoreCase(expressionName)).findFirst().get();
            customExpressionsList.remove(customExpression.getCustomExpression());

            userFunctions.remove(customExpression);
            analysis.getMassSpecExtractedData().removeCycleDataForDeletedExpression(customExpression.getCustomExpression());

            int columnIndex = customExpression.getColumnIndex();
            for (UserFunction uf : userFunctions) { // Reindex down
                if (uf.getColumnIndex() > columnIndex) {
                    uf.setColumnIndex(uf.getColumnIndex() - 1);
                }
            }

            if (tripoliSession.isExpressionRefreshed()){
                AnalysisMethodPersistance analysisMethodPersistance =
                        tripoliPersistentState.getMapMethodNamesToDefaults().get(analysis.getMethod().getMethodName());

                analysisMethodPersistance.getUserFunctionDisplayMap().remove(customExpression.getName());
                analysisMethodPersistance.getExpressionUserFunctionList().removeIf(e -> e.getName().equals(customExpression.getName()));
                tripoliPersistentState.updateTripoliPersistentState();
            }

            populateAnalysisMethodColumnsSelectorPane();

            currentMode.set(Mode.VIEW);
            expressionStateManager.clear();
            expressionString.set("");
            expressionNameTextField.clear();
            customExpressionLV.getSelectionModel().clearSelection();
        }
    }

    private List<String> textFlowToList(){
        List<String> retVal = new ArrayList<>();
        for (Node node : expressionTextFlow.getChildren()){
            String text = ((ExpressionTextNode) node).getText().trim();
            if (!text.isBlank()) {
                retVal.add(text);
            }
        }
        return retVal;
    }

    public void expressionAsTextAction() {
        if (!editAsText.get()) {

            editAsText.set(true);

            expressionPane.getChildren().setAll(expressionAsTextArea);
            AnchorPane.setBottomAnchor(expressionAsTextArea, 0.0);
            AnchorPane.setTopAnchor(expressionAsTextArea, 0.0);
            AnchorPane.setRightAnchor(expressionAsTextArea, 0.0);
            AnchorPane.setLeftAnchor(expressionAsTextArea, 0.0);
            expressionAsTextBtn.setText("Switch to d&d");
            expressionAsTextArea.requestFocus();

        } else {

            editAsText.set(false);

            expressionPane.getChildren().setAll(expressionScrollPane);
            AnchorPane.setBottomAnchor(expressionScrollPane, 0.0);
            AnchorPane.setTopAnchor(expressionScrollPane, 0.0);
            AnchorPane.setRightAnchor(expressionScrollPane, 0.0);
            AnchorPane.setLeftAnchor(expressionScrollPane, 0.0);
            expressionAsTextBtn.setText("Switch to text");

            handleExpressionUpdate(true);

        }
    }

    /**
     * Parses a given string into the correct nodes for the expressionTextFlow and then inserts them as children
     * @param expressionString new expression to be shown in the TextFlow
     */
    private void populateTextFlowFromString(String expressionString) {
        // Handle null as an empty string
        InputStream stream = new ByteArrayInputStream(Objects.requireNonNullElse(expressionString, "").getBytes(StandardCharsets.UTF_8));

        List<Node> children = new ArrayList<>();

        try {
            ExpressionsForTripoliLexer lexer = new ExpressionsForTripoliLexer(CharStreams.fromStream(stream, StandardCharsets.UTF_8));
            List<? extends Token> tokens = lexer.getAllTokens();

            for (int i = 0; i < tokens.size(); i++) {
                Token token = tokens.get(i);
                String nodeText = token.getText();

                ExpressionTextNode etn;

                if (ShuntingYard.isNumber(nodeText) || "#".equals(nodeText)) {
                    etn = new NumberTextNode(' ' + nodeText + ' ');
                } else if (listOperators.contains(nodeText)) {
                    etn = new OperationTextNode(' ' + nodeText + ' ');
                } else if (nodeText.contains("[")){
                    etn = new UserFunctionTextNode(' ' + nodeText + ' ');
                } else {
                    etn = new ExpressionTextNode(' ' + nodeText + ' ');
                }

                etn.setIndex(i);
                children.add(etn);
            }
        } catch (IOException ignored) {
        }
        expressionTextFlow.getChildren().setAll(children);
        handleExpressionUpdate(false);

    }

    private void handleExpressionUpdate(boolean saveCurrentState) {
        reindexExpressionTextFlowChildren();
        handleExpressionValidity();
        if (saveCurrentState) {
            expressionStateManager.save(expressionString.getValue());
        }
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

    private class ExpressionTextNode extends Text {
        private final String text;
        private int index;
        protected Color regularColor;
        protected Color selectedColor;
        protected Color oppositeColor;
        protected int fontSize;

        public ExpressionTextNode(String text) {
            super(text);

            setFontSmoothingType(FontSmoothingType.LCD);
            setFont(Font.font("SansSerif"));

            this.selectedColor = Color.RED;
            this.regularColor = Color.BLACK;
            this.oppositeColor = Color.LIME;

            setFill(regularColor);

            this.text = text;

            this.fontSize = EXPRESSION_BUILDER_DEFAULT_FONTSIZE;
            updateFontSize();
            setupDragHandlers();
            setupContextMenu();
        }
        public final void updateFontSize() {
            setFont(Font.font("SansSerif", FontWeight.SEMI_BOLD, fontSize + fontSizeModifier));
        }
        public int getIndex() { return index; }
        public void setIndex(int index) { this.index = index; }

        private void setupDragHandlers() {

            setOnDragOver(event -> {
                if (event.getDragboard().hasString() && !currentMode.get().equals(Mode.VIEW)) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            setOnDragEntered(event -> {
                if (!expressionTextFlow.getChildren().contains(insertIndicator) && !currentMode.get().equals(Mode.VIEW)) {
                    expressionTextFlow.getChildren().add(insertIndicator);
                }
                moveInsertIndicatorToIndex(this.index);

                event.consume();
            });

            setOnDragExited(event -> {
                expressionTextFlow.getChildren().remove(insertIndicator);
            });

            setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    String droppedText = db.getString();

                    int newIndex = expressionTextFlow.getChildren().indexOf(insertIndicator);
                    expressionTextFlow.getChildren().remove(insertIndicator);
                    expressionTextFlow.getChildren().add(newIndex, new ExpressionTextNode(droppedText));

                    if (event.getGestureSource() instanceof ExpressionTextNode) {
                        expressionTextFlow.getChildren().remove(event.getGestureSource());
                    }

                    populateTextFlowFromString(makeStringFromExpressionTextNodeList());
                    handleExpressionUpdate(true);

                    success = true;
                }
                event.setDropCompleted(success);
                event.consume();
            });

            setOnDragDetected(event -> {
                if (!currentMode.get().equals(Mode.VIEW)) {
                    setCursor(Cursor.CLOSED_HAND);
                    Dragboard db = startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(this.snapshot(null, null));
                    ClipboardContent cc = new ClipboardContent();
                    cc.putString(text);
                    db.setContent(cc);
                }
            });


        }
        private void moveInsertIndicatorToIndex(int index) {
            expressionTextFlow.getChildren().remove(insertIndicator);
            expressionTextFlow.getChildren().add(index, insertIndicator);
        }


        private void setupContextMenu() {
            // Delete -------------------
            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(event -> {
                expressionTextFlow.getChildren().remove(this);
                handleExpressionUpdate(true);
            });
            // --------------- end delete
            
            // Parenthesis -----------------------
            MenuItem addParenthesisItem = new MenuItem("Add Parentheses");
            addParenthesisItem.setOnAction(event -> {
                int index = expressionTextFlow.getChildren().indexOf(this);
                expressionTextFlow.getChildren().remove(this);

                expressionTextFlow.getChildren().add(index, new ExpressionTextNode("("));
                expressionTextFlow.getChildren().add(index + 1, this);
                expressionTextFlow.getChildren().add(index + 2, new ExpressionTextNode(")"));

                handleExpressionUpdate(true);
            });

            // ---------------- end parenthesis
            
            // value input (NumberTextNode only) -----------------------
            CustomMenuItem valueInputItem = new CustomMenuItem();
            valueInputItem.setHideOnClick(false);
        
            HBox inputContainer = new HBox(5);
            Label label = new Label("Value:");
            TextField valueField = new TextField();
            valueField.setPrefWidth(100);
        
            valueField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("-?\\d*\\.?\\d*")) {
                    valueField.setText(oldValue);
                }
            });
            
            // Create a method to handle the apply action
            EventHandler<ActionEvent> applyAction = e -> {
                if (valueField.getText().isEmpty()) {
                    int index = expressionTextFlow.getChildren().indexOf(this);
                    expressionTextFlow.getChildren().remove(this);
                    expressionTextFlow.getChildren().add(index, new NumberTextNode("#"));
                } else {
                    double numValue = Double.parseDouble(valueField.getText());
                    int index = expressionTextFlow.getChildren().indexOf(this);
                    expressionTextFlow.getChildren().remove(this);
                    expressionTextFlow.getChildren().add(index, new NumberTextNode(String.valueOf(numValue)));
                }
                handleExpressionUpdate(true);
            };
            
            valueField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    applyAction.handle(new ActionEvent());
                    event.consume();
                }
            });
            
            Button applyButton = new Button("Apply");
            applyButton.setOnAction(applyAction);
            
            inputContainer.getChildren().addAll(label, valueField, applyButton);
            valueInputItem.setContent(inputContainer);
            // -------------- end value input
            
            
            // Build context
            ContextMenu contextMenu = new ContextMenu(deleteItem, addParenthesisItem);

            if (this instanceof NumberTextNode || this.text.equals("#")) {
                contextMenu.getItems().add(valueInputItem);
            }
        
            setOnContextMenuRequested(event -> {
                if (!currentMode.get().equals(Mode.VIEW)) {
                    contextMenu.show(this, event.getScreenX(), event.getScreenY());

                    // Focus on the text field for immediate input
                    Platform.runLater(valueField::requestFocus);
                }
            });
        }

    }

    private class NumberTextNode extends ExpressionTextNode {
        public NumberTextNode(String text) {
            super(text);
        }
    }

    private class OperationTextNode extends ExpressionTextNode {

        public OperationTextNode(String text) {
            super(text);
            this.regularColor = Color.GREEN;
            setFill(regularColor);
            this.fontSize = EXPRESSION_BUILDER_DEFAULT_FONTSIZE + 3;
            updateFontSize();
        }
    }

    private class UserFunctionTextNode extends ExpressionTextNode {

        public UserFunctionTextNode(String text) {
            super(text);
            this.regularColor = Color.BLUE;
            setFill(regularColor);
        }
    }

    public static class StateManager<T> {

        private final BooleanProperty canUndo = new SimpleBooleanProperty(false);
        private final BooleanProperty canRedo = new SimpleBooleanProperty(false);
        private final BooleanProperty hasChanges = new SimpleBooleanProperty(false);

        private Node current;
        private Node tail;


        private class Node {
            T state;
            Node prev;
            Node next;

            Node(T state) {
                this.state = state;
            }
        }

        public void save(T state) {
            if (current != null && current.state.equals(state)) {
                return;
            }

            Node newNode = new Node(state);

            if (current != null) {
                current.next = newNode;
                newNode.prev = current;
            }
            current = newNode;

            if (tail == null) {
                tail = current;
            }

            updateProperties();
        }

        public T undo() {
            if (current != null && current.prev != null) {
                current = current.prev;
                updateProperties();
                return current.state;
            } else if (current.prev == null) {
                Node newNext = current;
                current = new Node(null);
                current.next = newNext;
                updateProperties();
            }
            return null;
        }

        public T redo() {
            if (current != null && current.next != null) {
                current = current.next;
                updateProperties();
                return current.state;
            }
            return null;
        }

        public void clear() {
            current = null;
            tail = null;
            canRedo.set(false);
            canUndo.set(false);
            hasChanges.set(false);
        }

        public boolean hasChanges() {
            if (current == null || tail == null) {
                return false;
            }

            return !Objects.equals(current.state, tail.state);
        }

        public T revert() {
            return tail.state;
        }


        public BooleanProperty canUndoProperty() {
            return canUndo;
        }
        public BooleanProperty canRedoProperty() {
            return canRedo;
        }
        public BooleanProperty hasChangesProperty() {return hasChanges;}

        private void updateProperties() {
            canUndo.set(current.prev != null);
            canRedo.set(current.next != null);
            hasChanges.set(hasChanges());
        }
    }

    private enum Mode {

        EDIT("Edit"),
        CREATE("Create"),
        VIEW("View");

        private final String printString;

        Mode(String printString) {
            this.printString = printString;
        }

        @Override
        public String toString() {
            return printString;
        }
    }
}