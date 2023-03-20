package org.cirdles.tripoli.gui;

import jakarta.xml.bind.JAXBException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots.MCMCPlotsController;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots.MCMCPlotsWindow;
import org.cirdles.tripoli.gui.dialogs.TripoliMessageDialog;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputSingleBlockRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.sessions.analysis.methods.baseline.BaselineCell;
import org.cirdles.tripoli.sessions.analysis.methods.sequence.SequenceCell;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.cirdles.tripoli.constants.ConstantsTripoliCore.MISSING_STRING_FIELD;
import static org.cirdles.tripoli.gui.constants.ConstantsTripoliApp.*;
import static org.cirdles.tripoli.gui.dialogs.TripoliMessageDialog.showChoiceDialog;
import static org.cirdles.tripoli.gui.utilities.fileUtilities.FileHandlerUtil.selectDataFile;
import static org.cirdles.tripoli.gui.utilities.fileUtilities.FileHandlerUtil.selectMethodFile;
import static org.cirdles.tripoli.sessions.analysis.Analysis.*;
import static org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod.compareAnalysisMethodToDataFileSpecs;

public class AnalysisManagerController implements Initializable {

    public static AnalysisInterface analysis;
    public static MCMCPlotsWindow MCMCPlotsWindow;
    private final Map<String, boolean[][]> mapOfGridPanesToCellUse = new TreeMap<>();
    public Tab detectorDetailTab;
    public TabPane analysiMethodTabPane;
    @FXML
    public HBox blockStatusHBox;
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
        analysisManagerGridPane.setStyle("-fx-background-color: " + convertColorToHex(TRIPOLI_ANALYSIS_YELLOW));
        setupListeners();
        populateAnalysisManagerGridPane();
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

    private void populateAnalysisManagerGridPane() {
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

        populateAnalysisMethodGridPane();

        populateBlocksStatus();
    }

    private void populateAnalysisDataFields() {
        metaDataTextArea.setText(analysis.prettyPrintAnalysisMetaData());
        dataSummaryTextArea.setText(analysis.prettyPrintAnalysisDataSummary());
    }

    private void populateAnalysisMethodGridPane() {
        // column 0 of GridPanes is reserved for row labels
        AnalysisMethod analysisMethod = analysis.getMethod();
        Map<String, Detector> mapOfDetectors = analysis.getMassSpecExtractedData().getDetectorSetup().getMapOfDetectors();
        List<Detector> detectorsInOrderList = mapOfDetectors.values().stream().sorted(Comparator.comparing(Detector::getOrdinalIndex)).collect(Collectors.toList());

        setUpGridPaneRows(analysisDetectorsGridPane, 7, detectorsInOrderList.size() + 1);
        prepareAnalysisMethodGridPanes(analysisDetectorsGridPane, detectorsInOrderList);

        aboutAnalysisTextArea.setText((null == analysisMethod) ? "No analysis method loaded" : analysisMethod.prettyPrintMethodSummary(true));

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
                populateDetectorDetailRow(methodGridPane, (0 < col) ? String.valueOf(detectorsInOrderList.get(col - 1).getAmplifierGain()) : "gain", col, 4);
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

    private void populateBlocksStatus() {
        blockStatusHBox.getChildren().clear();
        var massSpecExtractedData = analysis.getMassSpecExtractedData();
        Map<Integer, MassSpecOutputSingleBlockRecord> blocksData = massSpecExtractedData.getBlocksData();
        for (MassSpecOutputSingleBlockRecord block : blocksData.values()) {
            Button blockStatusButton = blockStatusButtonFactory(block.blockID());
            blockStatusHBox.getChildren().add(blockStatusButton);
        }
    }

    private Button blockStatusButtonFactory(int blockID) {
        Button blockStatusButton = new Button();
        blockStatusButton.setPrefSize(45.0, 25.0);
        blockStatusButton.setPadding(new Insets(0003));
        blockStatusButton.setFont(Font.font("Monospaced", FontWeight.EXTRA_BOLD, 10));
        blockStatusButton.setId(String.valueOf(blockID));
        blockStatusButton.setPadding(new Insets(0,-1,0,-1));
        tuneButton(blockStatusButton, analysis.getMapOfBlocksToProcessStatus().get(blockID));

        blockStatusButton.setOnAction(e -> {
            switch ((int) blockStatusButton.getUserData()) {
                case RUN -> {
                    if (null != analysis.getMapOfBlockToPlots().get(blockID)) {
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
            }
            case RUN -> {
                stateColor = Color.WHITE;
                blockStatusButton.setUserData(RUN);
                blockStatusButton.setText("Run " + blockStatusButton.getId());
            }
            case SHOW -> {
                stateColor = TRIPOLI_ANALYSIS_GREEN;
                blockStatusButton.setUserData(SHOW);
                blockStatusButton.setText("Show " + blockStatusButton.getId());
            }
        }
        blockStatusButton.setStyle("-fx-background-color: " + convertColorToHex(stateColor) + ";-fx-border-color: BLACK");
    }

    @FXML
    private void selectDataFileButtonAction() {
        try {
            File selectedFile = selectDataFile(TripoliGUI.primaryStage);
            if (null != selectedFile) {
                analysis.setAnalysisMethod(null);
                try {
                    analysis.extractMassSpecDataFromPath(Path.of(selectedFile.toURI()));
                } catch (TripoliException e) {
                    TripoliMessageDialog.showWarningDialog(e.getMessage(), TripoliGUI.primaryStage);
                }
                populateAnalysisManagerGridPane();
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException |
                 JAXBException | TripoliException e) {
            TripoliMessageDialog.showWarningDialog(e.getMessage(), TripoliGUI.primaryStage);
        }
    }

    @FXML
    private void selectMethodFileButtonAction() {
        try {
            File selectedFile = selectMethodFile(TripoliGUI.primaryStage);
            if ((null != selectedFile) && (selectedFile.exists())) {
                AnalysisMethod analysisMethod = analysis.extractAnalysisMethodfromPath(Path.of(selectedFile.toURI()));
                String compareInfo = compareAnalysisMethodToDataFileSpecs(analysisMethod, analysis.getMassSpecExtractedData());
                if (compareInfo.isBlank()) {
                    analysis.setMethod(analysisMethod);
                } else {
                    boolean choice = showChoiceDialog(
                            "The chosen analysis method does not meet the specifications in the data file.\n\n"
                                    + compareInfo
                                    + "\n\nProceed?", TripoliGUI.primaryStage);
                    if (choice) analysis.setMethod(analysisMethod);
                }
            }
        } catch (TripoliException | IOException | JAXBException e) {
            TripoliMessageDialog.showWarningDialog(e.getMessage(), TripoliGUI.primaryStage);
        }
        populateAnalysisManagerGridPane();
    }

    public void initializeMonteCarloTechniqueAction() {
        for (Node button : blockStatusHBox.getChildren()){
            if (button instanceof Button){
                analysis.getMapOfBlocksToProcessStatus().put(Integer.parseInt(button.getId()), (int)button.getUserData());
            }
        }

        MCMCPlotsWindow = new MCMCPlotsWindow(TripoliGUI.primaryStage);
        MCMCPlotsController.analysis = analysis;
        MCMCPlotsWindow.loadPlotsWindow();
    }

    public void selectRunAllAction() {
        for (Node button : blockStatusHBox.getChildren()){
            if (button instanceof Button){
                tuneButton((Button)button, RUN);
            }
        }
    }

    public void selectRunNoneAction() {
        for (Node button : blockStatusHBox.getChildren()){
            if (button instanceof Button){
                tuneButton((Button)button, SKIP);
            }
        }
    }

    public void selectShowsAction() {
        for (Node button : blockStatusHBox.getChildren()){
            if ((button instanceof Button) && (analysis.getMapOfBlocksToProcessStatus().get(Integer.parseInt(button.getId())) == 0)){
                tuneButton((Button)button, SHOW);
            }
        }
    }

    public void restoreAllAction() {
        for (Node button : blockStatusHBox.getChildren()){
            if (button instanceof Button){
                tuneButton((Button)button, analysis.getMapOfBlocksToProcessStatus().get(Integer.parseInt(button.getId())));
            }
        }
    }
}