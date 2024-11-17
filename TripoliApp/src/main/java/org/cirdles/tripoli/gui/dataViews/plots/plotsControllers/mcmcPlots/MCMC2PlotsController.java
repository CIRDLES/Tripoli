package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import org.cirdles.tripoli.gui.AnalysisManagerCallbackI;
import org.cirdles.tripoli.gui.AnalysisManagerController;
import org.cirdles.tripoli.gui.TripoliGUI;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.PlotWallPane;
import org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.OGTripoliPlotsWindow;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.OGTripoliViewController;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.*;
import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.plots.histograms.HistogramBuilder;
import org.cirdles.tripoli.plots.histograms.HistogramRecord;
import org.cirdles.tripoli.plots.histograms.RatioHistogramBuilder;
import org.cirdles.tripoli.plots.linePlots.*;
import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.MCMCProcess;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.initializers.AllBlockInitForMCMC;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc2.TestDriver;
import org.cirdles.tripoli.utilities.IntuitiveStringComparator;

import java.net.URL;
import java.util.*;

import static org.cirdles.tripoli.constants.TripoliConstants.PLOT_TAB_ENSEMBLES;
import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotHeight;
import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotWidth;
import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots.MCMCPlotsWindow.PLOT_WINDOW_HEIGHT;
import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots.MCMCPlotsWindow.PLOT_WINDOW_WIDTH;
import static org.cirdles.tripoli.sessions.analysis.Analysis.RUN;
import static org.cirdles.tripoli.sessions.analysis.Analysis.SKIP;

public class MCMC2PlotsController implements MCMCPlotsControllerInterface {

    private static final int TOOLBAR_HEIGHT = 30;
    private static final int MAX_BLOCK_COUNT = 2000;
    public static AnalysisInterface analysis;
    public static AnalysisManagerCallbackI analysisManagerCallbackI;
    private int currentBlockID = 0;
    @FXML
    private ProgressBar progressBar;
    private Service[] services;
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
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
    private AnchorPane ensemblePlotsAnchorPane;
    private PlotWallPane ensemblePlotsWallPane;
    private ListView<String> listViewOfBlocks = new ListView<>();

    public int getCurrentBlockID() {
        return currentBlockID;
    }

    public void plotIncomingAction() {
        processDataFileAndShowPlotsOfMCMC2(analysis);

    }

    @FXML
    void initialize() {
        eventLogTextArea.setText("Using MCMC2");
        masterVBox.setPrefSize(PLOT_WINDOW_WIDTH, PLOT_WINDOW_HEIGHT);
        toolbar.setPrefSize(PLOT_WINDOW_WIDTH, TOOLBAR_HEIGHT);

        plotTabPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            ensemblePlotsAnchorPane.setMinWidth((Double) newValue);
        });

        plotTabPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            ensemblePlotsAnchorPane.setMinHeight((Double) newValue - TOOLBAR_HEIGHT);
        });

        plotTabPane.prefWidthProperty().bind(masterVBox.widthProperty());
        plotTabPane.prefHeightProperty().bind(masterVBox.heightProperty());

        plotIncomingAction();
//        populateListOfAvailableBlocks();

    }

    private void populateListOfAvailableBlocks() {
        List<String> blocksByName = new ArrayList<>();
        for (Integer blockID : analysis.getMapOfBlockIdToProcessStatus().keySet()) {
            if (SKIP != analysis.getMapOfBlockIdToProcessStatus().get(blockID)) {
                blocksByName.add("" + blockID);
            }
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

        listViewOfBlocks.setOnMouseClicked(event -> {
            if (MouseButton.PRIMARY == event.getButton() && 2 == event.getClickCount()) {
                viewSelectedBlockAction();
            }
        });

        listViewOfBlocks.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> viewSelectedBlockAction());

        listViewOfBlocks.setDisable(true);
    }

    public synchronized void processDataFileAndShowPlotsOfMCMC2(AnalysisInterface analysis) {
        PlotBuilder[] test = TestDriver.simulationsOfMCMC2();
        PlotBuilder[][] plots = new PlotBuilder[1][];
        plots[0] = test;
        plotEnsemblesEngine(plots);
    }

    @SuppressWarnings("unchecked")
    public synchronized void processDataFileAndShowPlotsOfMCMC(AnalysisInterface analysis) {
        MCMCPlotBuildersTask.analysis = analysis;
        // check process status
        List<Integer> blocksToProcess = new ArrayList<>();

        for (Integer blockID : analysis.getMapOfBlockIdToProcessStatus().keySet()) {
            if (SKIP != analysis.getMapOfBlockIdToProcessStatus().get(blockID)) {
                blocksToProcess.add(blockID);
            }
        }

        int countOfBlocks = blocksToProcess.size();
        services = new Service[blocksToProcess.size()];

        Set<Integer> activeServices = new TreeSet<>();
        for (int blockIndex = 0; blockIndex < countOfBlocks; blockIndex++) {
            activeServices.add(blockIndex);
        }

        int indexOfFirstRunningBlockProcess = MAX_BLOCK_COUNT;
        for (int blockIndex = 0; blockIndex < countOfBlocks; blockIndex++) {
            services[blockIndex] = new MCMCUpdatesService(blocksToProcess.get(blockIndex));

            if (analysis.getMapOfBlockIdToProcessStatus().get(blocksToProcess.get(blockIndex)) == RUN) {
                indexOfFirstRunningBlockProcess = Math.min(indexOfFirstRunningBlockProcess, blockIndex);
            }
            int finalBlockIndex = blockIndex;
            services[finalBlockIndex].setOnSucceeded(evt -> {
                activeServices.remove(finalBlockIndex);
                Task<String> plotBuildersTask = ((MCMCUpdatesService) services[finalBlockIndex]).getPlotBuilderTask();
                if (null != plotBuildersTask) {
                    if (((MCMCPlotBuildersTask) plotBuildersTask).healthyPlotbuilder()) {
                        plotBlockEngine(plotBuildersTask);
                    }
                    if (activeServices.isEmpty()) {
                        listViewOfBlocks.setDisable(false);
                        listViewOfBlocks.getSelectionModel().selectFirst();
                        progressBar.setProgress(1.0);
                        ((Analysis) analysis).analysisRatioEngine();

                        // fire up OGTripoli style session plots
                        AllBlockInitForMCMC.PlottingData plottingData = analysis.assemblePostProcessPlottingData();
                        if (null != AnalysisManagerController.ogTripoliReviewPlotsWindow) {
                            AnalysisManagerController.ogTripoliReviewPlotsWindow.close();
                        }
                        AnalysisManagerController.ogTripoliReviewPlotsWindow =
                                new OGTripoliPlotsWindow(TripoliGUI.primaryStage, analysisManagerCallbackI, plottingData);
                        OGTripoliViewController.analysis = analysis;
                        AnalysisManagerController.ogTripoliReviewPlotsWindow.loadPlotsWindow();
                    }
                }
            });
            services[finalBlockIndex].setOnFailed(evt -> {
                listViewOfBlocks.setDisable(false);
                listViewOfBlocks.getSelectionModel().selectFirst();
                progressBar.setProgress(1.0);
                ((Analysis) analysis).analysisRatioEngine();
            });
        }

        if (MAX_BLOCK_COUNT > indexOfFirstRunningBlockProcess) {
            progressBar.accessibleTextProperty().bind(((MCMCUpdatesService) services[indexOfFirstRunningBlockProcess]).valueProperty());
            progressBar.accessibleTextProperty().addListener((observable, oldValue, newValue) -> {
                if (null != newValue) {
                    String[] data = newValue.split(">%");
                    try {
                        double percent = Double.parseDouble(data[0]) / MCMCProcess.getModelCount();
                        progressBar.setProgress(percent);
                    } catch (NumberFormatException e) {
                    }
                }
            });
        } else {
            progressBar.setProgress(1.0);
        }

        for (int blockIndex = 0; blockIndex < countOfBlocks; blockIndex++) {
            services[blockIndex].start();
        }
    }

    @FXML
    public void plotAnalysisRatioEngine() {

    }


    private synchronized void plotBlockEngine(Task<String> plotBuildersTaska) {
        analysisManagerCallbackI.callbackRefreshBlocksStatus();

        MCMCPlotBuildersTaskInterface plotBuildersTask = (MCMCPlotBuildersTaskInterface) plotBuildersTaska;

        // plotting ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        plotTabPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            ensemblePlotsWallPane.repeatLayoutStyle();
        });
        plotTabPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            ensemblePlotsWallPane.repeatLayoutStyle();
        });

        plotEnsemblesEngine(((MCMCPlotBuildersTask) plotBuildersTask).getPlotBuilders());

    }

    public void plotEnsemblesEngine(PlotBuilder[][] plotBuilders) {
//        PlotBuilder[] ratiosHistogramBuilder = plotBuilders[PLOT_INDEX_RATIOS];
        PlotBuilder[] baselineHistogramBuilder = plotBuilders[0];
//        PlotBuilder[] dalyFaradayHistogramBuilder = plotBuilders[PLOT_INDEX_DFGAINS];
//        PlotBuilder[] intensityLinePlotBuilder = plotBuilders[PLOT_INDEX_MEANINTENSITIES];

        if (ensemblePlotsAnchorPane.getChildren().isEmpty()) {
            ensemblePlotsWallPane = (PlotWallPane) PlotWallPane.createPlotWallPane(PLOT_TAB_ENSEMBLES, analysis, this, analysisManagerCallbackI);
            ensemblePlotsWallPane.buildToolBar();
            ensemblePlotsWallPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));
            ensemblePlotsWallPane.prefWidthProperty().bind(ensemblePlotsAnchorPane.widthProperty());
            ensemblePlotsWallPane.prefHeightProperty().bind(ensemblePlotsAnchorPane.heightProperty());
            ensemblePlotsWallPane.setToolBarCount(2);
            ensemblePlotsWallPane.setToolBarHeight(35.0);
            ensemblePlotsAnchorPane.getChildren().add(ensemblePlotsWallPane);
        } else {
            ensemblePlotsWallPane = (PlotWallPane) ensemblePlotsAnchorPane.getChildren().get(0);
            ensemblePlotsWallPane.clearTripoliPanes();
        }
//        produceTripoliRatioHistogramPlots(ratiosHistogramBuilder, ensemblePlotsWallPane);
        produceTripoliHistogramPlots(baselineHistogramBuilder, ensemblePlotsWallPane);
//        produceTripoliHistogramPlots(dalyFaradayHistogramBuilder, ensemblePlotsWallPane);
//        produceTripoliMultiLinePlots(intensityLinePlotBuilder, ensemblePlotsWallPane);
        ensemblePlotsWallPane.repeatLayoutStyle();

        listViewOfBlocks.refresh();
    }

    private void produceTripoliRatioHistogramPlots(PlotBuilder[] plotBuilder, PlotWallPane plotWallPane) {
        for (int i = 0; i < plotBuilder.length; i++) {
            if (plotBuilder[i].isDisplayed()) {
                HistogramRecord plotRecord = ((RatioHistogramBuilder) plotBuilder[i]).getHistogramRecord();
                HistogramRecord invertedPlotRecord = ((RatioHistogramBuilder) plotBuilder[i]).getInvertedRatioHistogramRecord();
                HistogramRecord logRatioHistogramRecord = ((RatioHistogramBuilder) plotBuilder[i]).getLogRatioHistogramRecord();
                HistogramRecord logInvertedRatioHistogramRecord = ((RatioHistogramBuilder) plotBuilder[i]).getInvertedLogRatioHistogramRecord();
                TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(plotWallPane);
                AbstractPlot plot = RatioHistogramPlot.generatePlot(
                        new Rectangle(minPlotWidth, minPlotHeight),
                        plotRecord, invertedPlotRecord,
                        logRatioHistogramRecord,
                        logInvertedRatioHistogramRecord,
                        analysis.getAnalysisMethod(),
                        plotWallPane);
                tripoliPlotPane.addPlot(plot);
            }
        }
    }

    private void produceTripoliHistogramPlots(PlotBuilder[] plotBuilder, PlotWallPane plotWallPane) {
        for (int i = 0; i < plotBuilder.length; i++) {
            if (plotBuilder[i].isDisplayed()) {
                HistogramRecord plotRecord = ((HistogramBuilder) plotBuilder[i]).getHistogramRecord();
                TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(plotWallPane);
                AbstractPlot plot = HistogramSinglePlot.generatePlot(new Rectangle(minPlotWidth, minPlotHeight), plotRecord, plotWallPane);
                tripoliPlotPane.addPlot(plot);
            }
        }
    }

    private void produceTripoliLinePlots(PlotBuilder[] plotBuilder, PlotWallPane plotWallPane) {
        for (int i = 0; i < plotBuilder.length; i++) {
            TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(plotWallPane);
            AbstractPlot plot = LinePlot.generatePlot(new Rectangle(minPlotWidth, minPlotHeight), (LinePlotBuilder) plotBuilder[i], plotWallPane);
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

    private void produceTripoliMultiLineIntensityPlots(PlotBuilder[] plotBuilder, PlotWallPane plotWallPane) {
        for (int i = 0; i < plotBuilder.length; i++) {
            TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(plotWallPane);
            AbstractPlot plot = MultiLineIntensityPlot.generatePlot(new Rectangle(minPlotWidth, minPlotHeight), (MultiLinePlotBuilder) plotBuilder[i], plotWallPane);
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

    private void producePeakShapesOverlayPlot(PlotBuilder[] plotBuilder, PlotWallPane plotWallPane) {
        for (int i = 0; i < plotBuilder.length; i++) {
            if (plotBuilder[i].isDisplayed()) {
                PeakShapesOverlayRecord peakShapesOverlayRecord = ((PeakShapesOverlayBuilder) plotBuilder[i]).getPeakShapesOverlayRecord();
                TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(plotWallPane);
                AbstractPlot plot = PeakShapesOverlayPlot.generatePlot(new Rectangle(minPlotWidth, minPlotHeight), peakShapesOverlayRecord);
                tripoliPlotPane.addPlot(plot);
            }
        }
    }

    public void viewSelectedBlockAction() {
        int blockIndex = listViewOfBlocks.getSelectionModel().getSelectedIndex();
        viewSelectedBlock(blockIndex);
    }

    public void viewSelectedBlock(int blockIndex) {
        currentBlockID = blockIndex + 1;
        Task<String> mcmcPlotBuildersTask = ((MCMCUpdatesService) services[blockIndex]).getPlotBuilderTask();
        if ((null != mcmcPlotBuildersTask)
                && mcmcPlotBuildersTask.isDone()
                && ((MCMCPlotBuildersTask) mcmcPlotBuildersTask).healthyPlotbuilder()) {
            plotBlockEngine(mcmcPlotBuildersTask);
            listViewOfBlocks.refresh();
        }
    }

    public void convergeTabSelected(Event event) {
        Tab tab = (Tab) event.getSource();
        if (tab.isSelected() && !((AnchorPane) tab.getContent()).getChildren().isEmpty()) {
            ((PlotWallPane) ((AnchorPane) tab.getContent()).getChildren().get(0)).replotAll();
        }
    }

    static class BlockDisplayID extends ListCell<String> {
        @Override
        protected void updateItem(String blockIDtext, boolean empty) {
            super.updateItem(blockIDtext, empty);
            if (null == blockIDtext || empty) {
                setText(null);
            } else {
                int blockID = Integer.parseInt(blockIDtext);
                if (analysis.getMapBlockIDToEnsembles().isEmpty()) {
                    setText("Block# " + blockID);
                } else {
                    Integer burnCount = analysis.getMapOfBlockIdToModelsBurnCount().get(blockID);
                    setText("Block# " + blockID
                            + " {BurnIn = " + ((burnCount == null) ? 0 : burnCount)
                            + " of " + analysis.getMapBlockIDToEnsembles().get(blockID).size() + " models}");
                }
            }
        }
    }

}