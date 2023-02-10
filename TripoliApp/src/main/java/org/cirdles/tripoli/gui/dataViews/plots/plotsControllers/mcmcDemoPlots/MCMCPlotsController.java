package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcDemoPlots;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractDataView;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.PlotWallPane;
import org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmc2Plots.MCMC2UpdatesService;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmc2Plots.PlotBuildersTaskInterface;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.BasicScatterAndLinePlot;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.HistogramSinglePlot;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.LinePlot;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.MultiLinePlotLogX;
import org.cirdles.tripoli.plots.AbstractPlotBuilder;
import org.cirdles.tripoli.plots.histograms.HistogramBuilder;
import org.cirdles.tripoli.plots.histograms.HistogramRecord;
import org.cirdles.tripoli.plots.linePlots.ComboPlotBuilder;
import org.cirdles.tripoli.plots.linePlots.LinePlotBuilder;
import org.cirdles.tripoli.plots.linePlots.MultiLinePlotBuilder;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.DataModelDriverExperiment;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethodBuiltinFactory;

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
    public static AnalysisInterface analysis;
    private Service service;
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
    void demo1_2IsotopeButtonAction(ActionEvent event) {
        DataModelDriverExperiment.ALLOW_EXECUTION = true;
        processDataFileAndShowPlotsOfMCMC(
                listViewOfSyntheticFiles.getSelectionModel().selectedItemProperty().getValue().toPath(),
                AnalysisMethodBuiltinFactory.analysisMethodsBuiltinMap.get(AnalysisMethodBuiltinFactory.BURDICK_BL_SYNTHETIC_DATA));
        ((Button) event.getSource()).setDisable(true);
        processFileButton2.setDisable(true);
    }

    @FXML
    void testAction(ActionEvent event) {
        DataModelDriverExperiment.ALLOW_EXECUTION = false;
        service.cancel();
        processFileButton.setDisable(listViewOfSyntheticFiles.getItems().isEmpty());
        processFileButton2.setDisable(listViewOfSyntheticFiles.getItems().isEmpty());

    }

    public void plotIncomingAction() {
        processDataFileAndShowPlotsOfMCMC2(analysis);
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

    public void processDataFileAndShowPlotsOfMCMC(Path dataFile, AnalysisMethod analysisMethod) {
        service = new MCMCUpdatesService(dataFile, analysisMethod);
        eventLogTextArea.textProperty().bind(service.valueProperty());
        service.start();
        service.setOnSucceeded(evt -> {
            Task<String> plotBuildersTask = ((MCMCUpdatesService) service).getPlotBuildersTask();

            plotEngine(plotBuildersTask);
        });
    }

    public void processDataFileAndShowPlotsOfMCMC2(AnalysisInterface analysis) {
        service = new MCMC2UpdatesService(analysis);
        eventLogTextArea.textProperty().bind(service.valueProperty());
        service.start();
        service.setOnSucceeded(evt -> {
            Task<String> plotBuildersTask = ((MCMC2UpdatesService) service).getPlotBuildersTask();

            plotEngine(plotBuildersTask);
        });
    }


    private void plotEngine(Task<String> plotBuildersTaska) {
        PlotBuildersTaskInterface plotBuildersTask = (PlotBuildersTaskInterface) plotBuildersTaska;
        AbstractPlotBuilder[] ratiosHistogramBuilder = plotBuildersTask.getRatiosHistogramBuilder();
        AbstractPlotBuilder[] baselineHistogramBuilder = plotBuildersTask.getBaselineHistogramBuilder();
        AbstractPlotBuilder[] dalyFaradayHistogramBuilder = plotBuildersTask.getDalyFaradayGainHistogramBuilder();
        AbstractPlotBuilder[] signalNoiseHistogramBuilder = plotBuildersTask.getSignalNoiseHistogramBuilder();
        AbstractPlotBuilder[] intensityLinePlotBuilder = plotBuildersTask.getMeanIntensityLineBuilder();

        AbstractPlotBuilder[] convergeRatioPlotBuilder = plotBuildersTask.getConvergeRatioLineBuilder();
        AbstractPlotBuilder[] convergeBLFaradayLineBuilder = plotBuildersTask.getConvergeBLFaradayLineBuilder();
        AbstractPlotBuilder[] convergeNoiseFaradayLineBuilder = plotBuildersTask.getConvergeNoiseFaradayLineBuilder();
        AbstractPlotBuilder[] convergeErrWeightedMisfitBuilder = plotBuildersTask.getConvergeErrWeightedMisfitLineBuilder();
        AbstractPlotBuilder[] convergeErrRawMisfitBuilder = plotBuildersTask.getConvergeErrRawMisfitLineBuilder();


        AbstractPlotBuilder observedDataPlotBuilder = plotBuildersTask.getObservedDataLineBuilder();
        AbstractPlotBuilder residualDataPlotBuilder = plotBuildersTask.getResidualDataLineBuilder();

        AbstractPlotBuilder convergeIntensityLinesBuilder = plotBuildersTask.getConvergeIntensityLinesBuilder();

        AbstractDataView observedDataLinePlot = new BasicScatterAndLinePlot(
                new Rectangle(dataFitGridPane.getWidth(),
                        (plotTabPane.getHeight() - TAB_HEIGHT) / dataFitGridPane.getRowCount()),
                (ComboPlotBuilder) observedDataPlotBuilder);

        AbstractDataView residualDataLinePlot = new BasicScatterAndLinePlot(
                new Rectangle(dataFitGridPane.getWidth(),
                        (plotTabPane.getHeight() - TAB_HEIGHT) / dataFitGridPane.getRowCount()),
                (ComboPlotBuilder) residualDataPlotBuilder);

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

                convergeIntensityLinesPlot.setMyHeight(newValue.intValue() - TAB_HEIGHT);
                convergeIntensityLinesPlot.repaint();
            }
        });

//        observedDataLinePlot.preparePanel();
//        dataFitGridPane.add(observedDataLinePlot, 0, 0);
//        residualDataLinePlot.preparePanel();
//        dataFitGridPane.add(residualDataLinePlot, 0, 1);
//
//        convergeIntensityLinesPlot.preparePanel();
//        convergeIntensityAnchorPane.getChildren().add(convergeIntensityLinesPlot);
//
//        processFileButton.setDisable(false);

        // plotting revision +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        PlotWallPane ensemblePlotsWallPane = new PlotWallPane();
        ensemblePlotsWallPane.buildToolBar();
        ensemblePlotsWallPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));
        ensemblePlotsAnchorPane.getChildren().add(ensemblePlotsWallPane);
        produceTripoliHistogramPlots(ratiosHistogramBuilder, ensemblePlotsWallPane);
        produceTripoliHistogramPlots(baselineHistogramBuilder, ensemblePlotsWallPane);
        produceTripoliHistogramPlots(dalyFaradayHistogramBuilder, ensemblePlotsWallPane);
        produceTripoliHistogramPlots(signalNoiseHistogramBuilder, ensemblePlotsWallPane);

//        PlotWallPane convergePlotsWallPane = new PlotWallPane();
//        convergePlotsWallPane.buildToolBar();
//        convergePlotsWallPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));
//        convergePlotsAnchorPane.getChildren().add(convergePlotsWallPane);
//        produceTripoliLinePlots(convergeRatioPlotBuilder, convergePlotsWallPane);
//        produceTripoliLinePlots(convergeBLFaradayLineBuilder, convergePlotsWallPane);
//        produceTripoliLinePlots(convergeNoiseFaradayLineBuilder, convergePlotsWallPane);
//        produceTripoliLinePlots(convergeErrRawMisfitBuilder, convergePlotsWallPane);
//        produceTripoliLinePlots(convergeErrWeightedMisfitBuilder, convergePlotsWallPane);
//        convergePlotsWallPane.tilePlots();
//
//        TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(ensemblePlotsWallPane);
//        AbstractPlot plot = MultiLinePlot.generatePlot(new Rectangle(minPlotWidth, minPlotHeight), (MultiLinePlotBuilder) intensityLinePlotBuilder[0]);
//        tripoliPlotPane.addPlot(plot);

        ensemblePlotsWallPane.tilePlots();
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