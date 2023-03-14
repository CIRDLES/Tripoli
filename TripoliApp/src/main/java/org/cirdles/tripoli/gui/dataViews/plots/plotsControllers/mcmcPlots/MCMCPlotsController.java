package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.PlotWallPane;
import org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.*;
import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.plots.histograms.HistogramBuilder;
import org.cirdles.tripoli.plots.histograms.HistogramRecord;
import org.cirdles.tripoli.plots.linePlots.ComboPlotBuilder;
import org.cirdles.tripoli.plots.linePlots.LinePlotBuilder;
import org.cirdles.tripoli.plots.linePlots.MultiLinePlotBuilder;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.MCMCProcess;
import org.cirdles.tripoli.utilities.IntuitiveStringComparator;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotHeight;
import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotWidth;
import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots.MCMCPlotsWindow.PLOT_WINDOW_HEIGHT;
import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots.MCMCPlotsWindow.PLOT_WINDOW_WIDTH;

public class MCMCPlotsController {

    private static final int TAB_HEIGHT = 35;
    public static AnalysisInterface analysis;
    @FXML
    public AnchorPane dataFitPlotsAnchorPane;
    @FXML
    public AnchorPane convergeErrorPlotsAnchorPane;
    @FXML
    public AnchorPane beamShapeAnchorPane;
    @FXML
    public AnchorPane summaryAnchorPane;
    @FXML
    public AnchorPane logAnchorPane;
    @FXML
    public ProgressBar progressBar;
    private Service[] services;
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private AnchorPane convergeIntensityAnchorPane;

    @FXML
    private TextArea eventLogTextArea;

    @FXML
    private ScrollPane listOfFilesScrollPane;

    @FXML
    private VBox masterVBox;

    @FXML
    private TabPane plotTabPane;

    @FXML
    private ToolBar toolbar;

    @FXML
    private AnchorPane convergePlotsAnchorPane;
    @FXML
    private AnchorPane ensemblePlotsAnchorPane;

    private ListView<String> listViewOfBlocks = new ListView<>();

    public void plotIncomingAction() {
        MCMCProcess.ALLOW_EXECUTION = true;
        processDataFileAndShowPlotsOfMCMC2(analysis);
    }

    @FXML
    void initialize() {
        masterVBox.setPrefSize(PLOT_WINDOW_WIDTH, PLOT_WINDOW_HEIGHT);
        toolbar.setPrefSize(PLOT_WINDOW_WIDTH, 30.0);

        plotTabPane.prefWidthProperty().bind(masterVBox.widthProperty());
        plotTabPane.prefHeightProperty().bind(masterVBox.heightProperty());
        plotIncomingAction();
        populateListOfAvailableBlocks();

    }

    private void populateListOfAvailableBlocks() {
        int blockCount = analysis.getMassSpecExtractedData().getBlocksData().size();
        List<String> blocksByName = new ArrayList<>();
        for (int i = 0; i < blockCount; i++) {
            blocksByName.add("Block # " + (i + 1));
        }

        listViewOfBlocks = new ListView<>();
        listViewOfBlocks.setCellFactory((parameter) -> new BlockDisplayID());

        Collections.sort(blocksByName, new IntuitiveStringComparator<>());
        ObservableList<String> items = FXCollections.observableArrayList(blocksByName);
        listViewOfBlocks.setItems(items);
        listViewOfBlocks.getSelectionModel().selectFirst();
        listViewOfBlocks.prefWidthProperty().bind(listOfFilesScrollPane.widthProperty());
        listViewOfBlocks.prefHeightProperty().bind(listOfFilesScrollPane.heightProperty());
        listViewOfBlocks.getSelectionModel().select(-1);
        listOfFilesScrollPane.setContent(listViewOfBlocks);

        listViewOfBlocks.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (MouseButton.PRIMARY == event.getButton() && 2 == event.getClickCount()) {
                    viewSelectedBlockAction();
                }
            }
        });

        listViewOfBlocks.setDisable(true);
    }

    @SuppressWarnings("unchecked")
    public void processDataFileAndShowPlotsOfMCMC2(AnalysisInterface analysis) {
        services = new Service[analysis.getMassSpecExtractedData().getBlocksData().size()];
        MCMCPlotBuildersTask.analysis = analysis;

        for (int blockIndex = 0; blockIndex < services.length; blockIndex++) {
            services[blockIndex] = new MCMCUpdatesService(blockIndex + 1);
            progressBar.accessibleTextProperty().bind(((MCMCUpdatesService) services[blockIndex]).valueProperty());
            progressBar.accessibleTextProperty().addListener((observable, oldValue, newValue) -> {
                if (null != newValue) {
                    String[] data = newValue.split(">%");
                    try {
                        double percent = Double.parseDouble(data[0]) / MCMCProcess.getModelCount();
                        //if (progressBar.getProgress() < percent) {
                        progressBar.setProgress(percent);
                        // }
                    } catch (NumberFormatException e) {
                        //
                    }
                }
            });


            int finalBlockIndex = blockIndex;
            services[finalBlockIndex].setOnSucceeded(evt -> {
                Task<String> plotBuildersTask = ((MCMCUpdatesService) services[finalBlockIndex]).getPlotBuilderTask();
                if (null != plotBuildersTask) {
                    plotEngine(plotBuildersTask);
                    showLogsEngine(finalBlockIndex);
                    listViewOfBlocks.setDisable(false);
                    listViewOfBlocks.getSelectionModel().selectFirst();
                }
            });
        }

        for (int blockIndex = 0; blockIndex < services.length; blockIndex++) {
            services[blockIndex].start();
        }
        MCMCProcess.ALLOW_EXECUTION = true;
    }


    private void plotEngine(Task<String> plotBuildersTaska) {
        ensemblePlotsAnchorPane.getChildren().removeAll();
        convergePlotsAnchorPane.getChildren().removeAll();
        dataFitPlotsAnchorPane.getChildren().removeAll();
        convergeErrorPlotsAnchorPane.getChildren().removeAll();
        convergeIntensityAnchorPane.getChildren().removeAll();

        PlotBuildersTaskInterface plotBuildersTask = (PlotBuildersTaskInterface) plotBuildersTaska;
        PlotBuilder[] ratiosHistogramBuilder = plotBuildersTask.getRatiosHistogramBuilder();
        PlotBuilder[] baselineHistogramBuilder = plotBuildersTask.getBaselineHistogramBuilder();
        PlotBuilder[] dalyFaradayHistogramBuilder = plotBuildersTask.getDalyFaradayGainHistogramBuilder();
        PlotBuilder[] signalNoiseHistogramBuilder = plotBuildersTask.getSignalNoiseHistogramBuilder();
        PlotBuilder[] intensityLinePlotBuilder = plotBuildersTask.getMeanIntensityVsKnotsMultiLineBuilder();

        PlotBuilder[] convergeRatioPlotBuilder = plotBuildersTask.getConvergeRatioLineBuilder();
        PlotBuilder[] convergeBLFaradayLineBuilder = plotBuildersTask.getConvergeBLFaradayLineBuilder();
        PlotBuilder[] convergeNoiseFaradayLineBuilder = plotBuildersTask.getConvergeNoiseFaradayLineBuilder();
        PlotBuilder[] convergeErrWeightedMisfitBuilder = plotBuildersTask.getConvergeErrWeightedMisfitLineBuilder();
        PlotBuilder[] convergeErrRawMisfitBuilder = plotBuildersTask.getConvergeErrRawMisfitLineBuilder();

        PlotBuilder[] observedDataPlotBuilder = plotBuildersTask.getObservedDataLineBuilder();
        PlotBuilder[] residualDataPlotBuilder = plotBuildersTask.getResidualDataLineBuilder();

        PlotBuilder[] convergeIntensityLinesBuilder = plotBuildersTask.getConvergeIntensityLinesBuilder();

        PlotBuilder[] observedDataWithSubsetsLineBuilder = plotBuildersTask.getObservedDataWithSubsetsLineBuilder();


        // plotting revision +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        PlotWallPane ensemblePlotsWallPane = new PlotWallPane();
        ensemblePlotsWallPane.buildToolBar();
        ensemblePlotsWallPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));
        ensemblePlotsAnchorPane.getChildren().add(ensemblePlotsWallPane);
        produceTripoliHistogramPlots(ratiosHistogramBuilder, ensemblePlotsWallPane);
        produceTripoliHistogramPlots(baselineHistogramBuilder, ensemblePlotsWallPane);
        produceTripoliHistogramPlots(dalyFaradayHistogramBuilder, ensemblePlotsWallPane);
        produceTripoliHistogramPlots(signalNoiseHistogramBuilder, ensemblePlotsWallPane);
        produceTripoliMultiLinePlots(intensityLinePlotBuilder, ensemblePlotsWallPane);
        ensemblePlotsWallPane.tilePlots();

        PlotWallPane convergePlotsWallPane = new PlotWallPane();
        convergePlotsWallPane.buildToolBar();
        convergePlotsWallPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));
        convergePlotsAnchorPane.getChildren().add(convergePlotsWallPane);
        produceTripoliLinePlots(convergeRatioPlotBuilder, convergePlotsWallPane);
        produceTripoliLinePlots(convergeBLFaradayLineBuilder, convergePlotsWallPane);
        produceTripoliLinePlots(convergeNoiseFaradayLineBuilder, convergePlotsWallPane);
        convergePlotsWallPane.tilePlots();

        PlotWallPane dataFitPlotsWallPane = new PlotWallPane();
        dataFitPlotsWallPane.buildToolBar();
        dataFitPlotsWallPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));
        dataFitPlotsAnchorPane.getChildren().add(dataFitPlotsWallPane);
        produceTripoliBasicScatterAndLinePlots(observedDataPlotBuilder, dataFitPlotsWallPane);
        produceTripoliBasicScatterAndLinePlots(observedDataWithSubsetsLineBuilder, dataFitPlotsWallPane);
        produceTripoliBasicScatterAndLinePlots(residualDataPlotBuilder, dataFitPlotsWallPane);
        dataFitPlotsWallPane.stackPlots();

        PlotWallPane convergeErrorPlotsWallPane = new PlotWallPane();
        convergeErrorPlotsWallPane.buildToolBar();
        convergeErrorPlotsWallPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));
        convergeErrorPlotsAnchorPane.getChildren().add(convergeErrorPlotsWallPane);
        produceTripoliLinePlots(convergeErrRawMisfitBuilder, convergeErrorPlotsWallPane);
        produceTripoliLinePlots(convergeErrWeightedMisfitBuilder, convergeErrorPlotsWallPane);
        convergeErrorPlotsWallPane.tilePlots();

        PlotWallPane convergeIntensityPlotsWallPane = new PlotWallPane();
        convergeIntensityPlotsWallPane.buildToolBar();
        convergeIntensityPlotsWallPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));
        convergeIntensityAnchorPane.getChildren().add(convergeIntensityPlotsWallPane);
        produceTripoliMultiLineLogXPlots(convergeIntensityLinesBuilder, convergeIntensityPlotsWallPane);
        convergeIntensityPlotsWallPane.tilePlots();
    }

    private void showLogsEngine(int blockNumber) {
        String log = analysis.uppdateLogsByBlock(blockNumber + 1, "");
        TextArea logTextArea = new TextArea(log);
        logTextArea.setPrefSize(logAnchorPane.getWidth(), logAnchorPane.getHeight());
        logAnchorPane.getChildren().removeAll();
        logAnchorPane.getChildren().add(logTextArea);
    }

    private void produceTripoliHistogramPlots(PlotBuilder[] plotBuilder, PlotWallPane plotWallPane) {
        for (int i = 0; i < plotBuilder.length; i++) {
            HistogramRecord plotRecord = ((HistogramBuilder) plotBuilder[i]).getHistogram();
            TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(plotWallPane);
            AbstractPlot plot = HistogramSinglePlot.generatePlot(new Rectangle(minPlotWidth, minPlotHeight), plotRecord);
            tripoliPlotPane.addPlot(plot);
        }
    }

    private void produceTripoliLinePlots(PlotBuilder[] plotBuilder, PlotWallPane plotWallPane) {
        for (int i = 0; i < plotBuilder.length; i++) {
            TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(plotWallPane);
            AbstractPlot plot = LinePlot.generatePlot(new Rectangle(minPlotWidth, minPlotHeight), (LinePlotBuilder) plotBuilder[i]);
            tripoliPlotPane.addPlot(plot);
        }
    }

    private void produceTripoliMultiLinePlots(PlotBuilder[] plotBuilder, PlotWallPane plotWallPane) {
        for (int i = 0; i < plotBuilder.length; i++) {
            TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(plotWallPane);
            AbstractPlot plot = MultiLinePlot.generatePlot(new Rectangle(minPlotWidth, minPlotHeight), (MultiLinePlotBuilder) plotBuilder[i]);
            tripoliPlotPane.addPlot(plot);
        }
    }

    private void produceTripoliMultiLineLogXPlots(PlotBuilder[] plotBuilder, PlotWallPane plotWallPane) {
        for (int i = 0; i < plotBuilder.length; i++) {
            TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(plotWallPane);
            AbstractPlot plot = MultiLinePlotLogX.generatePlot(new Rectangle(minPlotWidth, minPlotHeight), (MultiLinePlotBuilder) plotBuilder[i]);
            tripoliPlotPane.addPlot(plot);
        }
    }

    private void produceTripoliBasicScatterAndLinePlots(PlotBuilder[] plotBuilder, PlotWallPane plotWallPane) {
        for (int i = 0; i < plotBuilder.length; i++) {
            TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(plotWallPane);
            AbstractPlot plot = BasicScatterAndLinePlot.generatePlot(new Rectangle(minPlotWidth, minPlotHeight), (ComboPlotBuilder) plotBuilder[i]);
            tripoliPlotPane.addPlot(plot);
        }
    }

    public void stopProcess2(ActionEvent actionEvent) {
        MCMCProcess.ALLOW_EXECUTION = false;

        for (int blockIndex = 0; blockIndex < services.length; blockIndex++) {
            ((MCMCUpdatesService) services[blockIndex]).getPlotBuilderTask().cancel();
            if (((MCMCUpdatesService) services[blockIndex]).getPlotBuilderTask().isCancelled()) {
                services[blockIndex].cancel();
            }
        }
    }

    public void viewSelectedBlockAction() {
        int blockNumber = listViewOfBlocks.getSelectionModel().getSelectedIndex();
        viewSelectedBlock(blockNumber);
    }

    public void viewSelectedBlock(int blockNumber) {
        Task<String> mcmcPlotBuildersTask = ((MCMCUpdatesService) services[blockNumber]).getPlotBuilderTask();
        if ((null != mcmcPlotBuildersTask) && mcmcPlotBuildersTask.isDone()) {
            plotEngine(mcmcPlotBuildersTask);
            showLogsEngine(blockNumber);
        }
    }

    static class BlockDisplayID extends ListCell<String> {
        @Override
        protected void updateItem(String blockID, boolean empty) {
            super.updateItem(blockID, empty);
            if (null == blockID || empty) {
                setText(null);
            } else {
                setText(blockID);
            }
        }
    }

}