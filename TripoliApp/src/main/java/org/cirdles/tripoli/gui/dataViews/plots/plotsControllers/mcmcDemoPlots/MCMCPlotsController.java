package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcDemoPlots;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractDataView;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.PlotWallPane;
import org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.*;
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
import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcDemoPlots.MCMCPlotsWindow.PLOT_WINDOW_HEIGHT;
import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcDemoPlots.MCMCPlotsWindow.PLOT_WINDOW_WIDTH;

public class MCMCPlotsController {

    private static final int TAB_HEIGHT = 35;
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private GridPane convergeErrGridPane;

    @FXML
    private TextArea convergeErrLegendTextBox;

    @FXML
    private AnchorPane convergeIntensityAnchorPane;

    @FXML
    private TextArea convergeIntensityLegendTextBox;

    @FXML
    private GridPane dataFitGridPane;

    @FXML
    private TextArea dataFitLegendTextBox;

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
    private AnchorPane convergePlotsAnchorPane;
    @FXML
    private AnchorPane ensemblePlotsAnchorPane;

    private ListView<File> listViewOfSyntheticFiles = new ListView<>();


    @FXML
    void demo1_2IsotopeButtonAction(ActionEvent event) throws IOException {
        processDataFileAndShowPlotsOfMCMC(
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
        processDataFileAndShowPlotsOfMCMC(
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

        dataFitGridPane.prefWidthProperty().bind(plotTabPane.widthProperty().subtract(dataFitLegendTextBox.getWidth()));
        dataFitGridPane.prefHeightProperty().bind(plotTabPane.heightProperty().subtract(TAB_HEIGHT));

        convergeErrGridPane.prefWidthProperty().bind(plotTabPane.widthProperty().subtract(convergeErrLegendTextBox.getWidth()));
        convergeErrGridPane.prefHeightProperty().bind(plotTabPane.heightProperty().subtract(TAB_HEIGHT));

        convergeIntensityAnchorPane.prefWidthProperty().bind(plotTabPane.widthProperty().subtract(convergeIntensityLegendTextBox.getWidth()));
        convergeIntensityAnchorPane.prefHeightProperty().bind(plotTabPane.heightProperty().subtract(TAB_HEIGHT));

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
        listViewOfSyntheticFiles.prefWidthProperty().bind(listOfFilesScrollPane.widthProperty());
        listViewOfSyntheticFiles.prefHeightProperty().bind(listOfFilesScrollPane.heightProperty());
        listOfFilesScrollPane.setContent(listViewOfSyntheticFiles);
    }

    public void processDataFileAndShowPlotsOfMCMC(Path dataFile, AnalysisMethod analysisMethod) throws IOException {
        final MCMCUpdatesService service = new MCMCUpdatesService(dataFile, analysisMethod);
        eventLogTextArea.textProperty().bind(service.valueProperty());
        service.start();
        service.setOnSucceeded(evt -> {
            MCMCPlotBuildersTask plotBuildersTask = ((MCMCPlotBuildersTask) service.getPlotBuildersTask());

            AbstractPlotBuilder[] ratiosHistogramBuilder = plotBuildersTask.getRatiosHistogramBuilder();
            AbstractPlotBuilder[] baselineHistogramBuilder = plotBuildersTask.getBaselineHistogramBuilder();
            AbstractPlotBuilder[] dalyFaradayHistogramBuilder = plotBuildersTask.getDalyFaradayGainHistogramBuilder();
            AbstractPlotBuilder[] signalNoiseHistogramBuilder = plotBuildersTask.getSignalNoiseHistogramBuilder();
            AbstractPlotBuilder[] intensityLinePlotBuilder = plotBuildersTask.getMeanIntensityLineBuilder();

            AbstractPlotBuilder[] convergeRatioPlotBuilder = plotBuildersTask.getConvergeRatioLineBuilder();
            AbstractPlotBuilder[] convergeBLFaradayLineBuilder = plotBuildersTask.getConvergeBLFaradayLineBuilder();
            AbstractPlotBuilder[] convergeNoiseFaradayLineBuilder = plotBuildersTask.getConvergeNoiseFaradayLineBuilder();

            AbstractPlotBuilder observedDataPlotBuilder = plotBuildersTask.getObservedDataLineBuilder();
            AbstractPlotBuilder residualDataPlotBuilder = plotBuildersTask.getResidualDataLineBuilder();

            AbstractPlotBuilder convergeErrWeightedMisfitBuilder = plotBuildersTask.getConvergeErrWeightedMisfitLineBuilder();
            AbstractPlotBuilder convergeErrRawMisfitBuilder = plotBuildersTask.getConvergeErrRawMisfitLineBuilder();

            AbstractPlotBuilder convergeIntensityLinesBuilder = plotBuildersTask.getConvergeIntensityLinesBuilder();


            AbstractDataView observedDataLinePlot = new BasicScatterAndLinePlot(
                    new Rectangle(dataFitGridPane.getWidth(),
                            (plotTabPane.getHeight() - TAB_HEIGHT) / dataFitGridPane.getRowCount()),
                    (ComboPlotBuilder) observedDataPlotBuilder);

            AbstractDataView residualDataLinePlot = new BasicScatterAndLinePlot(
                    new Rectangle(dataFitGridPane.getWidth(),
                            (plotTabPane.getHeight() - TAB_HEIGHT) / dataFitGridPane.getRowCount()),
                    (ComboPlotBuilder) residualDataPlotBuilder);

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

            plotTabPane.widthProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.intValue() > 100) {
                    double newWidth = newValue.intValue();

                    observedDataLinePlot.setMyWidth(newWidth);
                    observedDataLinePlot.repaint();
                    residualDataLinePlot.setMyWidth(newWidth);
                    residualDataLinePlot.repaint();

                    convergeErrWeightedMisfitPlot.setMyWidth(newWidth);
                    convergeErrWeightedMisfitPlot.repaint();
                    convergeErrRawMisfitPlot.setMyWidth(newWidth);
                    convergeErrRawMisfitPlot.repaint();

                    convergeIntensityLinesPlot.setMyWidth(newWidth);
                    convergeIntensityLinesPlot.repaint();

                }
            });

            plotTabPane.heightProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.intValue() > 100) {

                    observedDataLinePlot.setMyHeight((newValue.intValue() - TAB_HEIGHT) / dataFitGridPane.getRowCount());
                    observedDataLinePlot.repaint();
                    residualDataLinePlot.setMyHeight((newValue.intValue() - TAB_HEIGHT) / dataFitGridPane.getRowCount());
                    residualDataLinePlot.repaint();

                    convergeErrWeightedMisfitPlot.setMyHeight((newValue.intValue() - TAB_HEIGHT) / convergeErrGridPane.getRowCount());
                    convergeErrWeightedMisfitPlot.repaint();
                    convergeErrRawMisfitPlot.setMyHeight((newValue.intValue() - TAB_HEIGHT) / convergeErrGridPane.getRowCount());
                    convergeErrRawMisfitPlot.repaint();

                    convergeIntensityLinesPlot.setMyHeight(newValue.intValue() - TAB_HEIGHT);
                    convergeIntensityLinesPlot.repaint();
                }
            });

            observedDataLinePlot.preparePanel();
            dataFitGridPane.add(observedDataLinePlot, 0, 0);
            residualDataLinePlot.preparePanel();
            dataFitGridPane.add(residualDataLinePlot, 0, 1);

            convergeErrWeightedMisfitPlot.preparePanel();
            convergeErrGridPane.add(convergeErrWeightedMisfitPlot, 0, 0);
            convergeErrRawMisfitPlot.preparePanel();
            convergeErrGridPane.add(convergeErrRawMisfitPlot, 0, 1);

            convergeIntensityLinesPlot.preparePanel();
            convergeIntensityAnchorPane.getChildren().add(convergeIntensityLinesPlot);

            processFileButton.setDisable(false);

            // plotting revision +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
            PlotWallPane ensemblePlotsWallPane = new PlotWallPane();
            ensemblePlotsWallPane.buildToolBar();
            ensemblePlotsWallPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));
            ensemblePlotsAnchorPane.getChildren().add(ensemblePlotsWallPane);
            produceTripoliHistogramPlots(ratiosHistogramBuilder, ensemblePlotsWallPane);
            produceTripoliHistogramPlots(baselineHistogramBuilder, ensemblePlotsWallPane);
            produceTripoliHistogramPlots(dalyFaradayHistogramBuilder, ensemblePlotsWallPane);
            produceTripoliHistogramPlots(signalNoiseHistogramBuilder, ensemblePlotsWallPane);

            PlotWallPane convergePlotsWallPane = new PlotWallPane();
            convergePlotsWallPane.buildToolBar();
            convergePlotsWallPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));
            convergePlotsAnchorPane.getChildren().add(convergePlotsWallPane);
            produceTripoliLinePlots(convergeRatioPlotBuilder, convergePlotsWallPane);
            produceTripoliLinePlots(convergeBLFaradayLineBuilder, convergePlotsWallPane);
            produceTripoliLinePlots(convergeNoiseFaradayLineBuilder, convergePlotsWallPane);
            convergePlotsWallPane.tilePlots();

            TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(ensemblePlotsWallPane);
            AbstractPlot plot = MultiLinePlot.generatePlot(new Rectangle(minPlotWidth, minPlotHeight), (MultiLinePlotBuilder) intensityLinePlotBuilder[0]);
            tripoliPlotPane.addPlot(plot);

            ensemblePlotsWallPane.tilePlots();
        });
    }

    private void produceTripoliHistogramPlots(AbstractPlotBuilder[] plotBuilder, PlotWallPane plotWallPane) {
        for (int i = 0; i < plotBuilder.length; i++) {
            HistogramRecord plotRecord = ((HistogramBuilder) plotBuilder[i]).getHistogram();
            TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(plotWallPane);
            AbstractPlot plot = HistogramSinglePlot.generatePlot(new Rectangle(minPlotWidth, minPlotHeight), plotRecord);
            tripoliPlotPane.addPlot(plot);
        }
    }

    private void produceTripoliLinePlots(AbstractPlotBuilder[] plotBuilder, PlotWallPane plotWallPane) {
        for (int i = 0; i < plotBuilder.length; i++) {
            TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(plotWallPane);
            AbstractPlot plot = LinePlot.generatePlot(new Rectangle(minPlotWidth, minPlotHeight), (LinePlotBuilder) plotBuilder[i]);
            tripoliPlotPane.addPlot(plot);
        }
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