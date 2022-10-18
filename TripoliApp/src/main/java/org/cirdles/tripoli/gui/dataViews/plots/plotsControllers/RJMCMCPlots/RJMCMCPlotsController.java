package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.RJMCMCPlots;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.gui.dataViews.plots.*;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethodBuiltinFactory;
import org.cirdles.tripoli.visualizationUtilities.AbstractPlotBuilder;
import org.cirdles.tripoli.visualizationUtilities.histograms.HistogramBuilder;
import org.cirdles.tripoli.visualizationUtilities.histograms.HistogramRecord;
import org.cirdles.tripoli.visualizationUtilities.linePlots.ComboPlotBuilder;
import org.cirdles.tripoli.visualizationUtilities.linePlots.LinePlotBuilder;
import org.cirdles.tripoli.visualizationUtilities.linePlots.MultiLinePlotBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.cirdles.tripoli.TripoliConstants.SYNTHETIC_DATA_FOLDER_2ISOTOPE;
import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotHeight;
import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotWidth;
import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.RJMCMCPlots.RJMCMCPlotsWindow.PLOT_WINDOW_HEIGHT;
import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.RJMCMCPlots.RJMCMCPlotsWindow.PLOT_WINDOW_WIDTH;

public class RJMCMCPlotsController {

    private static final int TAB_HEIGHT = 35;
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private GridPane convergeBLGridPane;

    @FXML
    private TextArea convergeBLLegendTextBox;

    @FXML
    private GridPane convergeErrGridPane;

    @FXML
    private TextArea convergeErrLegendTextBox;

    @FXML
    private AnchorPane convergeIntensityAnchorPane;

    @FXML
    private TextArea convergeIntensityLegendTextBox;

    @FXML
    private GridPane convergeNoiseGridPane;

    @FXML
    private TextArea convergeNoiseLegendTextBox;

    @FXML
    private AnchorPane convergeRatioAnchorPane;

    @FXML
    private TextArea convergeRatioLegendTextBox;

    @FXML
    private GridPane dataFitGridPane;

    @FXML
    private TextArea dataFitLegendTextBox;

    @FXML
    private GridPane ensembleGridPane;

    @FXML
    private TextArea ensembleLegendTextBox;

    @FXML
    private TextArea eventLogTextArea;

    @FXML
    private ScrollPane listOfFilesScrollPane;

    @FXML
    private VBox masterVBox;

    @FXML
    private TabPane plotTabPane;

    @FXML
    private AnchorPane plotsAnchorPane;

    @FXML
    private Button processFileButton;

    @FXML
    private Button processFileButton2;

    @FXML
    private ToolBar toolbar;
    @FXML
    private TabPane ratioHistogramsTabPane;



    private ListView<File> listViewOfSyntheticFiles = new ListView<>();


    @FXML
    void demo1_2IsotopeButtonAction(ActionEvent event) throws IOException {
        processDataFileAndShowPlotsOfRJMCMC(
                listViewOfSyntheticFiles.getSelectionModel().selectedItemProperty().getValue().toPath(),
                AnalysisMethodBuiltinFactory.analysisMethodsBuiltinMap.get("BurdickBlSyntheticData"));
        ((Button) event.getSource()).setDisable(true);
        processFileButton2.setDisable(true);
    }
    @FXML
    void demo1_5IsotopeButtonAction(ActionEvent event) throws IOException {
        // Jim's playground for 5 isotopes
        ResourceExtractor RESOURCE_EXTRACTOR = new ResourceExtractor(Tripoli.class);
        Path dataFile = RESOURCE_EXTRACTOR
                .extractResourceAsFile("/org/cirdles/tripoli/dataProcessors/dataSources/synthetic/fiveIsotopeSyntheticData/SyntheticDataset_01R.txt").toPath();
        processDataFileAndShowPlotsOfRJMCMC(
                dataFile,
                AnalysisMethodBuiltinFactory.analysisMethodsBuiltinMap.get("KU_204_5_6_7_8_Daly_AllFaradayPb"));

        ((Button) event.getSource()).setDisable(true);
        processFileButton.setDisable(true);
    }

    @FXML
    void initialize() {

        masterVBox.setPrefSize(PLOT_WINDOW_WIDTH, PLOT_WINDOW_HEIGHT);
        toolbar.setPrefSize(PLOT_WINDOW_WIDTH, 30.0);

        masterVBox.prefWidthProperty().bind(plotsAnchorPane.widthProperty());
        masterVBox.prefHeightProperty().bind(plotsAnchorPane.heightProperty());

        plotTabPane.prefWidthProperty().bind(masterVBox.widthProperty());
        plotTabPane.prefHeightProperty().bind(masterVBox.heightProperty());

        ensembleGridPane.prefWidthProperty().bind(plotTabPane.widthProperty().subtract(ensembleLegendTextBox.getWidth()));
        ensembleGridPane.prefHeightProperty().bind(plotTabPane.heightProperty().subtract(TAB_HEIGHT));

        convergeRatioAnchorPane.prefWidthProperty().bind(plotTabPane.widthProperty().subtract(convergeRatioLegendTextBox.getWidth()));
        convergeRatioAnchorPane.prefHeightProperty().bind(plotTabPane.heightProperty().subtract(TAB_HEIGHT));

        dataFitGridPane.prefWidthProperty().bind(plotTabPane.widthProperty().subtract(dataFitLegendTextBox.getWidth()));
        dataFitGridPane.prefHeightProperty().bind(plotTabPane.heightProperty().subtract(TAB_HEIGHT));

        convergeBLGridPane.prefWidthProperty().bind(plotTabPane.widthProperty().subtract(convergeBLLegendTextBox.getWidth()));
        convergeBLGridPane.prefHeightProperty().bind(plotTabPane.heightProperty().subtract(TAB_HEIGHT));

        convergeErrGridPane.prefWidthProperty().bind(plotTabPane.widthProperty().subtract(convergeErrLegendTextBox.getWidth()));
        convergeErrGridPane.prefHeightProperty().bind(plotTabPane.heightProperty().subtract(TAB_HEIGHT));

        convergeIntensityAnchorPane.prefWidthProperty().bind(plotTabPane.widthProperty().subtract(convergeIntensityLegendTextBox.getWidth()));
        convergeIntensityAnchorPane.prefHeightProperty().bind(plotTabPane.heightProperty().subtract(TAB_HEIGHT));

        convergeNoiseGridPane.prefWidthProperty().bind(plotTabPane.widthProperty().subtract(convergeNoiseLegendTextBox.getWidth()));
        convergeNoiseGridPane.prefHeightProperty().bind(plotTabPane.heightProperty().subtract(TAB_HEIGHT));

        populateListOfSyntheticData2IsotopesFiles();

        processFileButton.setDisable(listViewOfSyntheticFiles.getItems().isEmpty());
        processFileButton2.setDisable(listViewOfSyntheticFiles.getItems().isEmpty());
    }

    private void populateListOfSyntheticData2IsotopesFiles() {
        List<File> filesInFolder = new ArrayList<>();
        File[] allFiles;
        for (File file : Objects.requireNonNull(SYNTHETIC_DATA_FOLDER_2ISOTOPE.listFiles((file, name) -> name.toLowerCase().endsWith(".txt")))) {
            try {
                List<String> contentsByLine = new ArrayList<>(Files.readAllLines(file.toPath(), Charset.defaultCharset()));
                if (contentsByLine.size() > 5 && (contentsByLine.get(3).startsWith("Sample ID,SyntheticDataSet1"))) {
                    filesInFolder.add(file);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(filesInFolder);

        listViewOfSyntheticFiles = new ListView<>();
        listViewOfSyntheticFiles.setCellFactory((parameter) -> new FileDisplayName());

        ObservableList<File> items = FXCollections.observableArrayList(filesInFolder);
        listViewOfSyntheticFiles.setItems(items);
        listViewOfSyntheticFiles.getSelectionModel().selectFirst();
        // todo: complete listener
//        listViewOfSyntheticFiles.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<File>() {
//            @Override
//            public void changed(ObservableValue<? extends File> observable, File oldValue, File selectedFile) {
//                Path filePath = selectedFile.toPath();
//            }
//        });

        listViewOfSyntheticFiles.prefWidthProperty().bind(listOfFilesScrollPane.widthProperty());
        listViewOfSyntheticFiles.prefHeightProperty().bind(listOfFilesScrollPane.heightProperty());
        listOfFilesScrollPane.setContent(listViewOfSyntheticFiles);
    }

    public void processDataFileAndShowPlotsOfRJMCMC(Path dataFile, AnalysisMethod analysisMethod) throws IOException {
//        org.cirdles.commons.util.ResourceExtractor RESOURCE_EXTRACTOR = new ResourceExtractor(Tripoli.class);
//        Path dataFile = RESOURCE_EXTRACTOR
//                .extractResourceAsFile("/org/cirdles/tripoli/dataProcessors/dataSources/synthetic/twoIsotopeSyntheticData/SyntheticDataset_01.txt").toPath();
        final RJMCMCUpdatesService service = new RJMCMCUpdatesService(dataFile, analysisMethod);
        eventLogTextArea.textProperty().bind(service.valueProperty());
        service.start();
        service.setOnSucceeded(evt -> {
            RJMCMCPlotBuildersTask plotBuildersTask = ((RJMCMCPlotBuildersTask) service.getPlotBuildersTask());

            AbstractPlotBuilder[] ratiosHistogramBuilder = plotBuildersTask.getRatiosHistogramBuilder();
            AbstractPlotBuilder baselineHistogramBuilder = plotBuildersTask.getBaselineHistogramBuilder();
            AbstractPlotBuilder dalyFaradayHistogramBuilder = plotBuildersTask.getDalyFaradayGainHistogramBuilder();
            AbstractPlotBuilder signalNoiseHistogramBuilder = plotBuildersTask.getSignalNoiseHistogramBuilder();
            AbstractPlotBuilder intensityLinePlotBuilder = plotBuildersTask.getMeanIntensityLineBuilder();

            AbstractPlotBuilder convergeRatioPlotBuilder = plotBuildersTask.getConvergeRatioLineBuilder();

            AbstractPlotBuilder observedDataPlotBuilder = plotBuildersTask.getObservedDataLineBuilder();
            AbstractPlotBuilder residualDataPlotBuilder = plotBuildersTask.getResidualDataLineBuilder();

            AbstractPlotBuilder convergeBLFaradayL1LineBuilder = plotBuildersTask.getConvergeBLFaradayL1LineBuilder();
            AbstractPlotBuilder convergeBLFaradayH1LineBuilder = plotBuildersTask.getConvergeBLFaradayH1LineBuilder();

            AbstractPlotBuilder convergeErrWeightedMisfitBuilder = plotBuildersTask.getConvergeErrWeightedMisfitLineBuilder();
            AbstractPlotBuilder convergeErrRawMisfitBuilder = plotBuildersTask.getConvergeErrRawMisfitLineBuilder();

            AbstractPlotBuilder convergeIntensityLinesBuilder = plotBuildersTask.getConvergeIntensityLinesBuilder();

            AbstractPlotBuilder convergeNoiseFaradayL1LineBuilder = plotBuildersTask.getConvergeNoiseFaradayL1LineBuilder();
            AbstractPlotBuilder convergeNoiseFaradayH1LineBuilder = plotBuildersTask.getConvergeNoiseFaradayH1LineBuilder();

            AbstractDataView baselineHistogramPlot = new HistogramPlot(
                    new Rectangle(ensembleGridPane.getWidth() / ensembleGridPane.getColumnCount(),
                            (plotTabPane.getHeight() - TAB_HEIGHT) / ensembleGridPane.getRowCount()),
                    (HistogramBuilder) baselineHistogramBuilder);

            AbstractDataView dalyFaradayHistogramPlot = new HistogramPlot(
                    new Rectangle(ensembleGridPane.getWidth() / ensembleGridPane.getColumnCount(),
                            (plotTabPane.getHeight() - TAB_HEIGHT) / ensembleGridPane.getRowCount()),
                    (HistogramBuilder) dalyFaradayHistogramBuilder);

            AbstractDataView intensityLinePlot = new MultiLineLinePlot(
                    new Rectangle(ensembleGridPane.getWidth(),
                            (plotTabPane.getHeight() - TAB_HEIGHT) / ensembleGridPane.getRowCount()),
                    (MultiLinePlotBuilder) intensityLinePlotBuilder
            );

            AbstractDataView signalNoiseHistogramPlot = new HistogramPlot(
                    new Rectangle(ensembleGridPane.getWidth(),
                            (plotTabPane.getHeight() - TAB_HEIGHT) / ensembleGridPane.getRowCount()),
                    (HistogramBuilder) signalNoiseHistogramBuilder);


            AbstractDataView convergeRatioLinePlot = new BasicLinePlotLogX(
                    new Rectangle(convergeRatioAnchorPane.getWidth(),
                            plotTabPane.getHeight() - TAB_HEIGHT),
                    (LinePlotBuilder) convergeRatioPlotBuilder);


            AbstractDataView observedDataLinePlot = new BasicScatterAndLinePlot(
                    new Rectangle(dataFitGridPane.getWidth(),
                            (plotTabPane.getHeight() - TAB_HEIGHT) / dataFitGridPane.getRowCount()),
                    (ComboPlotBuilder) observedDataPlotBuilder);

            AbstractDataView residualDataLinePlot = new BasicScatterAndLinePlot(
                    new Rectangle(dataFitGridPane.getWidth(),
                            (plotTabPane.getHeight() - TAB_HEIGHT) / dataFitGridPane.getRowCount()),
                    (ComboPlotBuilder) residualDataPlotBuilder);

            AbstractDataView convergeBLFaradayL1LinePlot = new BasicLinePlotLogX(
                    new Rectangle(convergeBLGridPane.getWidth(),
                            (plotTabPane.getHeight() - TAB_HEIGHT) / convergeBLGridPane.getRowCount()),
                    (LinePlotBuilder) convergeBLFaradayL1LineBuilder);

            AbstractDataView convergeBLFaradayH1LinePlot = new BasicLinePlotLogX(
                    new Rectangle(convergeBLGridPane.getWidth(),
                            (plotTabPane.getHeight() - TAB_HEIGHT) / convergeBLGridPane.getRowCount()),
                    (LinePlotBuilder) convergeBLFaradayH1LineBuilder);

            AbstractDataView convergeErrWeightedMisfitPlot = new BasicLinePlotLogX(
                    new Rectangle(convergeErrGridPane.getWidth(),
                            (plotTabPane.getHeight() - TAB_HEIGHT) / convergeErrGridPane.getRowCount()),
                    (LinePlotBuilder) convergeErrWeightedMisfitBuilder);

            AbstractDataView convergeErrRawMisfitPlot = new BasicLinePlotLogX(
                    new Rectangle(convergeErrGridPane.getWidth(),
                            (plotTabPane.getHeight() - TAB_HEIGHT) / convergeErrGridPane.getRowCount()),
                    (LinePlotBuilder) convergeErrRawMisfitBuilder);

            AbstractDataView convergeIntensityLinesPlot = new MultiLinePlotLogX(
                    new Rectangle(convergeIntensityAnchorPane.getWidth(),
                            plotTabPane.getHeight() - TAB_HEIGHT),
                    (MultiLinePlotBuilder) convergeIntensityLinesBuilder);

            AbstractDataView convergeNoiseFaradayL1LinePlot = new BasicLinePlotLogX(
                    new Rectangle(convergeNoiseGridPane.getWidth(),
                            (plotTabPane.getHeight() - TAB_HEIGHT) / convergeNoiseGridPane.getRowCount()),
                    (LinePlotBuilder) convergeNoiseFaradayL1LineBuilder);

            AbstractDataView convergeNoiseFaradayH1LinePlot = new BasicLinePlotLogX(
                    new Rectangle(convergeNoiseGridPane.getWidth(),
                            (plotTabPane.getHeight() - TAB_HEIGHT) / convergeNoiseGridPane.getRowCount()),
                    (LinePlotBuilder) convergeNoiseFaradayH1LineBuilder);


            plotTabPane.widthProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.intValue() > 100) {
                    double newWidth = newValue.intValue() - ensembleLegendTextBox.getWidth();
                    baselineHistogramPlot.setMyWidth(newWidth / ensembleGridPane.getColumnCount());
                    baselineHistogramPlot.repaint();
                    dalyFaradayHistogramPlot.setMyWidth(newWidth / ensembleGridPane.getColumnCount());
                    dalyFaradayHistogramPlot.repaint();
                    intensityLinePlot.setMyWidth(newWidth);
                    intensityLinePlot.repaint();
                    signalNoiseHistogramPlot.setMyWidth(newWidth);
                    signalNoiseHistogramPlot.repaint();

                    convergeRatioLinePlot.setMyWidth(newWidth);
                    convergeRatioLinePlot.repaint();

                    observedDataLinePlot.setMyWidth(newWidth);
                    observedDataLinePlot.repaint();
                    residualDataLinePlot.setMyWidth(newWidth);
                    residualDataLinePlot.repaint();

                    convergeBLFaradayL1LinePlot.setMyWidth(newWidth);
                    convergeBLFaradayL1LinePlot.repaint();
                    convergeBLFaradayH1LinePlot.setMyWidth(newValue.intValue());
                    convergeBLFaradayH1LinePlot.repaint();

                    convergeErrWeightedMisfitPlot.setMyWidth(newWidth);
                    convergeErrWeightedMisfitPlot.repaint();
                    convergeErrRawMisfitPlot.setMyWidth(newWidth);
                    convergeErrRawMisfitPlot.repaint();

                    convergeIntensityLinesPlot.setMyWidth(newWidth);
                    convergeIntensityLinesPlot.repaint();

                    convergeNoiseFaradayL1LinePlot.setMyWidth(newWidth);
                    convergeNoiseFaradayL1LinePlot.repaint();
                    convergeNoiseFaradayH1LinePlot.setMyWidth(newWidth);
                    convergeNoiseFaradayH1LinePlot.repaint();


                }
            });

            plotTabPane.heightProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.intValue() > 100) {
                    baselineHistogramPlot.setMyHeight((newValue.intValue() - TAB_HEIGHT) / ensembleGridPane.getRowCount());
                    baselineHistogramPlot.repaint();
                    dalyFaradayHistogramPlot.setMyHeight((newValue.intValue() - TAB_HEIGHT) / ensembleGridPane.getRowCount());
                    dalyFaradayHistogramPlot.repaint();
                    intensityLinePlot.setMyHeight((newValue.intValue() - TAB_HEIGHT) / ensembleGridPane.getRowCount());
                    intensityLinePlot.repaint();
                    signalNoiseHistogramPlot.setMyHeight((newValue.intValue() - TAB_HEIGHT) / ensembleGridPane.getRowCount());
                    signalNoiseHistogramPlot.repaint();

                    convergeRatioLinePlot.setMyHeight(newValue.intValue() - TAB_HEIGHT);
                    convergeRatioLinePlot.repaint();

                    observedDataLinePlot.setMyHeight((newValue.intValue() - TAB_HEIGHT) / dataFitGridPane.getRowCount());
                    observedDataLinePlot.repaint();
                    residualDataLinePlot.setMyHeight((newValue.intValue() - TAB_HEIGHT) / dataFitGridPane.getRowCount());
                    residualDataLinePlot.repaint();

                    convergeBLFaradayL1LinePlot.setMyHeight((newValue.intValue() - TAB_HEIGHT) / convergeBLGridPane.getRowCount());
                    convergeBLFaradayL1LinePlot.repaint();
                    convergeBLFaradayH1LinePlot.setMyHeight((newValue.intValue() - TAB_HEIGHT) / convergeBLGridPane.getRowCount());
                    convergeBLFaradayH1LinePlot.repaint();

                    convergeErrWeightedMisfitPlot.setMyHeight((newValue.intValue() - TAB_HEIGHT) / convergeErrGridPane.getRowCount());
                    convergeErrWeightedMisfitPlot.repaint();
                    convergeErrRawMisfitPlot.setMyHeight((newValue.intValue() - TAB_HEIGHT) / convergeErrGridPane.getRowCount());
                    convergeErrRawMisfitPlot.repaint();

                    convergeIntensityLinesPlot.setMyHeight(newValue.intValue() - TAB_HEIGHT);
                    convergeIntensityLinesPlot.repaint();

                    convergeNoiseFaradayL1LinePlot.setMyHeight((newValue.intValue() - TAB_HEIGHT) / convergeNoiseGridPane.getRowCount());
                    convergeNoiseFaradayL1LinePlot.repaint();
                    convergeNoiseFaradayH1LinePlot.setMyHeight((newValue.intValue() - TAB_HEIGHT) / convergeNoiseGridPane.getRowCount());
                    convergeNoiseFaradayH1LinePlot.repaint();
                }
            });

            baselineHistogramPlot.preparePanel();
            ensembleGridPane.add(baselineHistogramPlot, 0, 1, 1, 1);
            dalyFaradayHistogramPlot.preparePanel();
            ensembleGridPane.add(dalyFaradayHistogramPlot, 1, 1, 1, 1);
            intensityLinePlot.preparePanel();
            ensembleGridPane.add(intensityLinePlot, 0, 2, 2, 1);
            signalNoiseHistogramPlot.preparePanel();
            ensembleGridPane.add(signalNoiseHistogramPlot, 0, 3, 2, 1);

            convergeRatioLinePlot.preparePanel();
            convergeRatioAnchorPane.getChildren().add(convergeRatioLinePlot);

            observedDataLinePlot.preparePanel();
            dataFitGridPane.add(observedDataLinePlot, 0, 0);
            residualDataLinePlot.preparePanel();
            dataFitGridPane.add(residualDataLinePlot, 0, 1);

            convergeBLFaradayL1LinePlot.preparePanel();
            convergeBLGridPane.add(convergeBLFaradayL1LinePlot, 0, 0);
            convergeBLFaradayH1LinePlot.preparePanel();
            convergeBLGridPane.add(convergeBLFaradayH1LinePlot, 0, 1);

            convergeErrWeightedMisfitPlot.preparePanel();
            convergeErrGridPane.add(convergeErrWeightedMisfitPlot, 0, 0);
            convergeErrRawMisfitPlot.preparePanel();
            convergeErrGridPane.add(convergeErrRawMisfitPlot, 0, 1);

            convergeIntensityLinesPlot.preparePanel();
            convergeIntensityAnchorPane.getChildren().add(convergeIntensityLinesPlot);

            convergeNoiseFaradayL1LinePlot.preparePanel();
            convergeNoiseGridPane.add(convergeNoiseFaradayL1LinePlot, 0, 0);
            convergeNoiseFaradayH1LinePlot.preparePanel();
            convergeNoiseGridPane.add(convergeNoiseFaradayH1LinePlot, 0, 1);

            processFileButton.setDisable(false);

            // ratio histograms revision
            PlotWallPane plotWallPane = new PlotWallPane();
            plotWallPane.buildToolBar();
            plotWallPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"),null,null)));
            ratioHistogramsTabPane.getTabs().get(0).setContent(plotWallPane);

            for (int i = 0; i < ratiosHistogramBuilder.length; i++){
                HistogramRecord plotRecord = ((HistogramBuilder) ratiosHistogramBuilder[i]).getHistograms()[0];
                TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(plotWallPane);
                HistogramSinglePlot ratiosHistogramSinglePlot = new HistogramSinglePlot(
                        new Rectangle(minPlotWidth, minPlotHeight),
                        plotRecord, plotRecord.title(), "Ratios", "Counts");
                tripoliPlotPane.addPlot(ratiosHistogramSinglePlot);
            }

            plotWallPane.tilePlots();

        });

    }

    static class FileDisplayName extends ListCell<File> {

        @Override
        protected void updateItem(File file, boolean empty) {
            super.updateItem(file, empty);
            if (file == null || empty) {
                setText(null);
            } else {
                setText(file.getName());
            }
        }
    }

}