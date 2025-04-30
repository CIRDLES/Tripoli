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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
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
    public TabPane analysiMethodTabPane;
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
    private static final String NUMBER_STRING = "NUMBER";
    private static final String INVISIBLE_NEWLINE_PLACEHOLDER = " \n";
    private static final String VISIBLE_NEWLINE_PLACEHOLDER = "\u23CE\n";
    private static final String INVISIBLE_TAB_PLACEHOLDER = "  ";
    private static final String VISIBLE_TAB_PLACEHOLDER = " \u21E5";
    private static final String VISIBLE_WHITESPACE_PLACEHOLDER = "\u2423";
    private static final String INVISIBLE_WHITESPACE_PLACEHOLDER = " ";
    private List<String> listOperators = new ArrayList<>();
    private final Map<String, String> presentationMap = new HashMap<>();

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

        presentationMap.put("New line", INVISIBLE_NEWLINE_PLACEHOLDER);
        presentationMap.put("Tab", INVISIBLE_TAB_PLACEHOLDER);
        presentationMap.put("White space", INVISIBLE_WHITESPACE_PLACEHOLDER);


        populateCustomExpressions();
        setTextFlowListener();
        initExpressionTextFlowAndTextArea();
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
                showTab(analysiMethodTabPane, 2, selectColumnsToPlot);
                analysiMethodTabPane.getSelectionModel().select(2);
                populateAnalysisMethodColumnsSelectorPane();
                processingToolBar.setVisible(false);
            }
            case 2, 3, 4 -> {
                showTab(analysiMethodTabPane, 2, detectorDetailTab);
                showTab(analysiMethodTabPane, 3, baselineTableTab);
                showTab(analysiMethodTabPane, 4, sequenceTableTab);
                showTab(analysiMethodTabPane, 5, selectRatiosToPlotTab);
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
        tripoliPersistentState.updateTripoliPersistentState();
    }

    private void populateAnalysisMethodColumnsSelectorPane() {
        List<CheckBox> ratioCheckBoxList = new ArrayList<>();
        List<CheckBox> ratioInvertedCheckBoxList = new ArrayList<>();
        List<CheckBox> functionCheckBoxList = new ArrayList<>();
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

        ratiosScrollPane.prefHeightProperty().bind(analysiMethodTabPane.heightProperty());
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


        functionsHeaderHBox.prefWidthProperty().bind(ratiosScrollPane.widthProperty());
        functionsHeaderHBox.getChildren().clear();
        functionsScrollPane.prefHeightProperty().bind(analysiMethodTabPane.heightProperty());
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
            if (!userFunction.isTreatAsIsotopicRatio()) {
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

        userFunctions.sort(null);
        for (UserFunction userFunction : analysis.getUserFunctions()) {
            if (userFunction.isTreatAsIsotopicRatio()) {
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

    private void populateCustomExpressions() {
        List<UserFunction> userFunctions = analysis.getUserFunctions();
        List<Operation> operationList = new ArrayList<>(OPERATIONS_MAP.values());
        listOperators = OPERATIONS_MAP.keySet().stream().toList();

        ListView<ExpressionTreeInterface> userFunctionLV = new ListView<>();
        ListView<ExpressionTreeInterface> isotopicRatioLV = new ListView<>();
        ListView<ExpressionTreeInterface> operationLV = new ListView<>();
        ListView<ExpressionTreeInterface> customExpressionLV = new ListView<>();
        for (UserFunction userFunction : userFunctions) {
            if (userFunction.isTreatAsIsotopicRatio()) {
                isotopicRatioLV.getItems().add(new UserFunctionNode(userFunction.getName()));
            } else if(userFunction.isTreatAsCustomExpression()) {
                customExpressionLV.getItems().add(userFunction.getCustomExpression());
            } else {
                userFunctionLV.getItems().add(new UserFunctionNode(userFunction.getName()));
            }
        }
        operationLV.getItems().addAll(operationList);
        
        setAccordionListViewListener(userFunctionLV);
        setAccordionListViewListener(isotopicRatioLV);
        setAccordionListViewListener(operationLV);
        setAccordionListViewListener(customExpressionLV);

        TitledPane ufPane = new TitledPane("User Functions", userFunctionLV);
        TitledPane irPane = new TitledPane("Isotopic Ratios", isotopicRatioLV);
        TitledPane opPane = new TitledPane("Operations", operationLV);
        TitledPane cePane = new TitledPane("Custom Expressions", customExpressionLV);

        expressionAccordion.getPanes().addAll(ufPane, irPane, opPane, cePane);

        expressionAccordion.setExpandedPane(expressionAccordion.getPanes().get(0));
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
                        setText(item.getName());
                    }
                }
            };

            cell.setOnDragDetected(event -> {
                ExpressionTreeInterface selectedItem = accordionLV.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    Dragboard dragboard = cell.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    if (selectedItem instanceof Operation) { // get operator rather than name
                        String key = OPERATIONS_MAP.entrySet()
                                .stream()
                                .filter(entry -> entry.getValue().equals(selectedItem))
                                .map(Map.Entry::getKey)
                                .findFirst()
                                .orElse(null);
                        content.putString(key);
                    } else {
                        content.putString(selectedItem.getName());
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

    private void setTextFlowListener(){
        insertIndicator.setFill(Color.RED);
        insertIndicator.setFont(Font.font("SansSerif", FontWeight.BOLD, 12));
        expressionScrollPane.setOnDragOver(event -> {
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        expressionScrollPane.setOnDragEntered(event -> {
            expressionTextFlow.getChildren().remove(insertIndicator);
            expressionTextFlow.getChildren().add(insertIndicator);
        });

        expressionScrollPane.setOnDragExited(event -> {
            expressionTextFlow.getChildren().remove(insertIndicator);
        });

        expressionScrollPane.setOnDragDropped((DragEvent event) -> {

            expressionTextFlow.getChildren().remove(insertIndicator);

            Dragboard db = event.getDragboard();
            boolean success = false;

            int index = expressionTextFlow.getChildren().size();

            if (db.hasString()) {
                String content = db.getString();
                if (OPERATIONS_MAP.containsKey(content)) {
                    // case of operation
                    insertOperationIntoExpressionTextFlow(content, index);
                } else if ( content.contains(NUMBER_STRING)) {
                    // case of "NUMBER"
                    insertNumberIntoExpressionTextFlow(index);
                } else if (presentationMap.containsKey(content)) {
                    // case of presentation (new line, tab)
                    insertPresentationIntoExpressionTextFlow(presentationMap.get(content), index);
                } else {
                    // case of expression
                    insertExpressionIntoExpressionTextFlow(content, index);
                }

                success = true;
            }

            event.setDropCompleted(success);

            event.consume();
        });
    }

    private void insertOperationIntoExpressionTextFlow(String content, int index) {
        //Add spaces
        ExpressionTextNode exp = new OperationTextNode(' ' + content.trim() + ' ');
        exp.setIndex(index);
        expressionTextFlow.getChildren().add(exp);
        updateExpressionTextFlowChildren();
    }
    private void insertNumberIntoExpressionTextFlow(int index) {
        //Add spaces
        ExpressionTextNode exp = new NumberTextNode(' ' + NUMBER_STRING.trim() + ' ');
        exp.setIndex(index);
        expressionTextFlow.getChildren().add(exp);
        updateExpressionTextFlowChildren();
    }
    private void insertExpressionIntoExpressionTextFlow(String content, int index) {
        //Add spaces
        ExpressionTextNode exp = new ExpressionTextNode(' ' + content.trim() + ' ');
        exp.setIndex(index);
        expressionTextFlow.getChildren().add(exp);
        updateExpressionTextFlowChildren();
    }
    private void insertPresentationIntoExpressionTextFlow(String content, int index) {
        ExpressionTextNode exp = new PresentationTextNode(content);
        exp.setIndex(index);
        expressionTextFlow.getChildren().add(exp);
        updateExpressionTextFlowChildren();
    }

    private void updateExpressionTextFlowChildren() {
        // extract and sort
        List<Node> children = new ArrayList<>(expressionTextFlow.getChildren());
        // sort
        children.sort((Node o1, Node o2) -> {
            int retVal = 0;
            if (o1 instanceof ExpressionTextNode && o2 instanceof ExpressionTextNode) {
                retVal = Double.compare(((ExpressionTextNode) o1).getIndex(), ((ExpressionTextNode) o2).getIndex());
            }
            return retVal;
        });

        // reset ordinals to integer values
        int index = 0;
        for (Node etn : children) {
            ((ExpressionTextNode) etn).setIndex(index);
            index++;
        }

        expressionTextFlow.getChildren().setAll(children);

        expressionString.set(makeStringFromExpressionTextNodeList(expressionTextFlow.getChildren()));

    }

    private void initExpressionTextFlowAndTextArea() {

        //Init of the textarea
        expressionAsTextArea.setFont(Font.font("Monospaced"));

        expressionAsTextArea.textProperty().bindBidirectional(expressionString);
        expressionString.addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !makeStringFromExpressionTextNodeList(expressionTextFlow.getChildren()).equals(newValue)) {
                makeTextFlowFromString(newValue);
            }
        });

        expressionNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if ((newValue != null) && newValue.compareTo(oldValue) != 0) {
                // remove spaces
                //expressionNameTextField.setText(FileNameFixer.fixFileName(newValue));
                //updateEditor();
                //refreshSaved();
            }
        });

    }

    private String makeStringFromExpressionTextNodeList(List<Node> list) {
        StringBuilder sb = new StringBuilder();
        for (Node node : list) {
            if (node instanceof ExpressionTextNode) {
                switch (((ExpressionTextNode) node).getText()) {
                    case VISIBLE_NEWLINE_PLACEHOLDER:
                    case INVISIBLE_NEWLINE_PLACEHOLDER:
                        sb.append("\n");
                        break;
                    case VISIBLE_TAB_PLACEHOLDER:
                    case INVISIBLE_TAB_PLACEHOLDER:
                        sb.append("\t");
                        break;
                    case INVISIBLE_WHITESPACE_PLACEHOLDER:
                    case VISIBLE_WHITESPACE_PLACEHOLDER:
                        sb.append(" ");
                        break;
                    default:
                        String txt = ((ExpressionTextNode) node).getText().trim();
                        String nonLetter = "\t\n\r [](),+-*/<>=^\"";
                        if (sb.length() == 0 || nonLetter.indexOf(sb.charAt(sb.length() - 1)) != -1 || nonLetter.indexOf(txt.charAt(0)) != -1) {
                            sb.append(txt);
                        } else {
                            sb.append(" ").append(txt);
                        }
                        break;
                }
            }
        }
        return sb.toString();
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

    public void newCustomExpressionOnAction(ActionEvent actionEvent) {
    }

    public void editCustomExpressionOnAction(ActionEvent actionEvent) {
    }

    public void cancelCustomExpressionOnAction(ActionEvent actionEvent) {
    }

    public void saveCustomExpressionOnAction(ActionEvent actionEvent) {
    }

    public void expressionClearAction(ActionEvent actionEvent) {
    }

    public void expressionUndoAction(ActionEvent actionEvent) {
    }

    public void expressionRedoAction(ActionEvent actionEvent) {
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

            expressionTextFlow.getChildren().clear();
            String expression = expressionString.get();
            expression = expression.replaceAll("( )*\\[", "[");
            makeTextFlowFromString(expression);
        }
    }

    private void makeTextFlowFromString(String string) {
        List<Node> children = new ArrayList<>();

        try {
            InputStream stream = new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
            ExpressionsForTripoliLexer lexer = new ExpressionsForTripoliLexer(CharStreams.fromStream(stream, StandardCharsets.UTF_8));
            List<? extends Token> tokens = lexer.getAllTokens();

            for (int i = 0; i < tokens.size(); i++) {
                Token token = tokens.get(i);
                String nodeText = token.getText();

                ExpressionTextNode etn;

                if (ShuntingYard.isNumber(nodeText) || NUMBER_STRING.equals(nodeText)) {
                    etn = new NumberTextNode(' ' + nodeText + ' ');
                } else if (listOperators.contains(nodeText)) {
                    etn = new OperationTextNode(' ' + nodeText + ' ');
                } else if (nodeText.equals("\n") || nodeText.equals("\r")) {
                    etn = new PresentationTextNode(INVISIBLE_NEWLINE_PLACEHOLDER);
                } else if (nodeText.equals("\t")) {
                    etn = new PresentationTextNode(INVISIBLE_TAB_PLACEHOLDER);
                } else if (nodeText.equals(" ")) {
                    etn = new PresentationTextNode(INVISIBLE_WHITESPACE_PLACEHOLDER);
                } else {
                    etn = new ExpressionTextNode(' ' + nodeText + ' ');
                }

                etn.setIndex(i);
                children.add(etn);
            }
        } catch (IOException ignored) {
        }
        expressionTextFlow.getChildren().setAll(children);
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
                if (event.getGestureSource() != this && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            setOnDragEntered(event -> {
                if (!expressionTextFlow.getChildren().contains(insertIndicator)) {
                    expressionTextFlow.getChildren().add(insertIndicator);
                }
                moveInsertIndicatorToIndex(this.index);
                event.consume();
            });

            setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    String droppedText = db.getString();

                    ExpressionTextNode newText = new ExpressionTextNode(" " + droppedText + " ");
                    newText.setIndex(index);
                    expressionTextFlow.getChildren().add(index, newText);
                    reindexTextFlow();

                    success = true;
                }
                event.setDropCompleted(success);
                event.consume();
            });
        }
        private void moveInsertIndicatorToIndex(int index) {
            expressionTextFlow.getChildren().remove(insertIndicator);
            expressionTextFlow.getChildren().add(index, insertIndicator);
        }

        private void reindexTextFlow() {
            for (int i = 0; i < expressionTextFlow.getChildren().size(); i++) {
                Node node = expressionTextFlow.getChildren().get(i);
                if (node instanceof ExpressionTextNode et) {
                    et.setIndex(i);
                }
            }
        }
        private void setupContextMenu() {
            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(event -> {
                expressionTextFlow.getChildren().remove(this);
                reindexTextFlow();
            });

            ContextMenu contextMenu = new ContextMenu(deleteItem);

            setOnContextMenuRequested(event -> {
                contextMenu.show(this, event.getScreenX(), event.getScreenY());
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
    private class PresentationTextNode extends ExpressionTextNode {

        public PresentationTextNode(String text) {
            super(text);
            this.fontSize = EXPRESSION_BUILDER_DEFAULT_FONTSIZE + 3;
            updateFontSize();
        }
    }
}