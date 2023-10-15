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
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.analysisPlots.HistogramAnalysisPlot;
import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.plots.analysisPlotBuilders.HistogramAnalysisBuilder;
import org.cirdles.tripoli.plots.analysisPlotBuilders.PeakCentreAnalysisBuilder;
import org.cirdles.tripoli.plots.histograms.HistogramBuilder;
import org.cirdles.tripoli.plots.histograms.HistogramRecord;
import org.cirdles.tripoli.plots.histograms.RatioHistogramBuilder;
import org.cirdles.tripoli.plots.linePlots.*;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.AllBlockInitForOGTripoli;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.MCMCProcess;
import org.cirdles.tripoli.species.IsotopicRatio;
import org.cirdles.tripoli.utilities.IntuitiveStringComparator;

import java.net.URL;
import java.util.*;

import static org.cirdles.tripoli.constants.TripoliConstants.*;
import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotHeight;
import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotWidth;
import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots.MCMCPlotsWindow.PLOT_WINDOW_HEIGHT;
import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots.MCMCPlotsWindow.PLOT_WINDOW_WIDTH;
import static org.cirdles.tripoli.plots.analysisPlotBuilders.HistogramAnalysisBuilder.initializeHistogramAnalysis;
import static org.cirdles.tripoli.plots.analysisPlotBuilders.PeakCentreAnalysisBuilder.initializeAnalysisPeakCentres;
import static org.cirdles.tripoli.sessions.analysis.Analysis.*;

public class MCMCPlotsController implements MCMCPlotsControllerInterface {

    private static final int TOOLBAR_HEIGHT = 30;
    public static AnalysisInterface analysis;
    public static AnalysisManagerCallbackI analysisManagerCallbackI;
    private static int MAX_BLOCK_COUNT = 2000;
    private int currentBlockID = 0;
    @FXML
    private AnchorPane logAnchorPane;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Tab convergencesTab;
    @FXML
    private Tab convergeErrorTab;
    @FXML
    private Tab convergeIntensityTab;
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
    private AnchorPane convergePlotsAnchorPane;
    @FXML
    private AnchorPane convergeErrorPlotsAnchorPane;
    @FXML
    private AnchorPane convergeIntensityAnchorPane;
    @FXML
    private AnchorPane ensemblePlotsAnchorPane;
    @FXML
    private AnchorPane dataFitPlotsAnchorPane;
    @FXML
    private AnchorPane beamShapeAnchorPane;
    @FXML
    private AnchorPane ratioSessionAnchorPane;
    @FXML
    private AnchorPane peakAnalysisAnchorPane;
    private PlotWallPane convergePlotsWallPane;
    private PlotWallPane convergeErrorPlotsWallPane;
    private PlotWallPane convergeIntensityPlotsWallPane;
    private PlotWallPane ensemblePlotsWallPane;
    private PlotWallPane dataFitPlotsWallPane;
    private PlotWallPane ratiosSessionPlotsWallPane;
    private PlotWallPane peakShapeOverlayPlotWallPane;
    private PlotWallPane peakAnalysisPlotWallPlane;
    private ListView<String> listViewOfBlocks = new ListView<>();

    public int getCurrentBlockID() {
        return currentBlockID;
    }

    public void plotIncomingAction() {
        processDataFileAndShowPlotsOfMCMC(analysis);
    }

    @FXML
    void initialize() {
        eventLogTextArea.setText("Using MCMC with max of 100000 iterations");
        masterVBox.setPrefSize(PLOT_WINDOW_WIDTH, PLOT_WINDOW_HEIGHT);
        toolbar.setPrefSize(PLOT_WINDOW_WIDTH, TOOLBAR_HEIGHT);

        plotTabPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            convergePlotsAnchorPane.setMinWidth((Double) newValue);
            convergeErrorPlotsAnchorPane.setMinWidth((Double) newValue);
            convergeIntensityAnchorPane.setMinWidth((Double) newValue);
            ensemblePlotsAnchorPane.setMinWidth((Double) newValue);
            dataFitPlotsAnchorPane.setMinWidth((Double) newValue);
            ratioSessionAnchorPane.setMinWidth((Double) newValue);
            beamShapeAnchorPane.setMinWidth((Double) newValue);
            peakAnalysisAnchorPane.setMinWidth((Double) newValue);
        });

        plotTabPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            convergePlotsAnchorPane.setMinHeight(((Double) newValue) - TOOLBAR_HEIGHT);
            convergeErrorPlotsAnchorPane.setMinHeight(((Double) newValue) - TOOLBAR_HEIGHT);
            convergeIntensityAnchorPane.setMinHeight((Double) newValue - TOOLBAR_HEIGHT);
            ensemblePlotsAnchorPane.setMinHeight((Double) newValue - TOOLBAR_HEIGHT);
            dataFitPlotsAnchorPane.setMinHeight((Double) newValue - TOOLBAR_HEIGHT);
            ratioSessionAnchorPane.setMinHeight((Double) newValue - TOOLBAR_HEIGHT);
            beamShapeAnchorPane.setMinHeight((Double) newValue - TOOLBAR_HEIGHT);
            peakAnalysisAnchorPane.setMinHeight((Double) newValue - TOOLBAR_HEIGHT);
        });

        plotTabPane.prefWidthProperty().bind(masterVBox.widthProperty());
        plotTabPane.prefHeightProperty().bind(masterVBox.heightProperty());
        plotIncomingAction();
        populateListOfAvailableBlocks();

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
                        showLogsEngine(finalBlockIndex);
                    }
                    if (activeServices.isEmpty()) {
                        //if (blocksToProcess.size() > 1) plotRatioSessionEngine();
                        listViewOfBlocks.setDisable(false);
                        listViewOfBlocks.getSelectionModel().selectFirst();
                        progressBar.setProgress(1.0);

                        // fire up OGTripoli style session plots
                        AllBlockInitForOGTripoli.PlottingData plottingData = analysis.assemblePostProcessPlottingData();
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
    public void plotRatioSessionEngine() {
        Map<Integer, PlotBuilder[][]> mapOfBlockIdToPlots = analysis.getMapOfBlockIdToPlots();
        Map<IsotopicRatio, List<HistogramRecord>> mapRatioNameToAnalysisRecords = new TreeMap<>();
        Iterator<Map.Entry<Integer, PlotBuilder[][]>> iterator = mapOfBlockIdToPlots.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, PlotBuilder[][]> entry = iterator.next();
            if (analysis.getMapOfBlockIdToProcessStatus().get(entry.getKey()) == SHOW) {
                PlotBuilder[] ratiosPlotBuilder = entry.getValue()[PLOT_INDEX_RATIOS];
                for (PlotBuilder ratioPlotBuilder : ratiosPlotBuilder) {
                    IsotopicRatio ratio = ((RatioHistogramBuilder) ratioPlotBuilder).getRatio();
                    if (ratioPlotBuilder.isDisplayed()) {
                        String ratioName = ratioPlotBuilder.getTitle()[0];
                        mapRatioNameToAnalysisRecords.computeIfAbsent(ratio, k -> new ArrayList<>());
                        boolean useInvertedRatio = analysis.getAnalysisMethod().getMapOfRatioNamesToInvertedFlag().get(ratioName);
                        mapRatioNameToAnalysisRecords.get(ratio).add(
                                useInvertedRatio ?
                                        ((RatioHistogramBuilder) ratioPlotBuilder).getInvertedRatioHistogramRecord()
                                        : ((RatioHistogramBuilder) ratioPlotBuilder).getHistogramRecord());
                    }
                }
            }
        }

        plotTabPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            ratiosSessionPlotsWallPane.repeatLayoutStyle();
        });
        plotTabPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            ratiosSessionPlotsWallPane.repeatLayoutStyle();
        });


        if (ratioSessionAnchorPane.getChildren().isEmpty()) {
            ratiosSessionPlotsWallPane = PlotWallPane.createPlotWallPane(null, analysis, this, analysisManagerCallbackI);
            ratiosSessionPlotsWallPane.buildToolBar();
            ratiosSessionPlotsWallPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));
            ratiosSessionPlotsWallPane.prefWidthProperty().bind(ratioSessionAnchorPane.widthProperty());
            ratiosSessionPlotsWallPane.prefHeightProperty().bind(ratioSessionAnchorPane.heightProperty());
            ratioSessionAnchorPane.getChildren().add(ratiosSessionPlotsWallPane);
        } else {
            ratiosSessionPlotsWallPane = (PlotWallPane) ratioSessionAnchorPane.getChildren().get(0);
            ratiosSessionPlotsWallPane.clearTripoliPanes();
        }

        for (Map.Entry<IsotopicRatio, List<HistogramRecord>> entry : mapRatioNameToAnalysisRecords.entrySet()) {
            HistogramAnalysisBuilder histogramAnalysisBuilder = initializeHistogramAnalysis(
                    analysis.getMapOfBlockIdToProcessStatus().size(), entry.getKey(), entry.getValue(), entry.getValue().get(0).title(), "Block ID", "Ratio");
            TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(ratiosSessionPlotsWallPane);
            AbstractPlot plot = HistogramAnalysisPlot.generatePlot(new Rectangle(minPlotWidth, minPlotHeight), histogramAnalysisBuilder.getHistogramAnalysisRecord());
            tripoliPlotPane.addPlot(plot);
        }
        ratiosSessionPlotsWallPane.stackPlots();
    }

    // plotting engine for peak sessions
    @FXML
    private void plotPeakSessionEngine() {
        Map<Integer, PlotBuilder[]> mapOfPeakPlotsToBlock = analysis.getMapOfBlockIdToPeakPlots();
        Map<String, List<PeakShapesOverlayRecord>> mapPeakNameToAnalysisRecords = new TreeMap<>();
        Iterator<Map.Entry<Integer, PlotBuilder[]>> iterator = mapOfPeakPlotsToBlock.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, PlotBuilder[]> entry = iterator.next();
            if (analysis.getMapOfBlockIdToProcessStatus().get(entry.getKey()) == SHOW) {
                PlotBuilder[] peaksPlotBuilder = entry.getValue();
                for (PlotBuilder peakPlotBuilder : peaksPlotBuilder) {
                    if (peakPlotBuilder.isDisplayed()) {
                        String peakName = peakPlotBuilder.getTitle()[1];
                        mapPeakNameToAnalysisRecords.computeIfAbsent(peakName, k -> new ArrayList<>());
                        mapPeakNameToAnalysisRecords.get(peakName).add(((PeakShapesOverlayBuilder) peakPlotBuilder).getPeakShapesOverlayRecord());

                    }
                }
            }
        }
        plotTabPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            peakAnalysisPlotWallPlane.repeatLayoutStyle();
        });
        plotTabPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            peakAnalysisPlotWallPlane.repeatLayoutStyle();
        });
        // TODO add peak analysis pane to fxml and controller

        if (peakAnalysisAnchorPane.getChildren().isEmpty()) {
            peakAnalysisPlotWallPlane = PlotWallPane.createPlotWallPane(null, analysis, this, null);
            peakAnalysisPlotWallPlane.buildToolBar();
            peakAnalysisPlotWallPlane.setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));
            peakAnalysisAnchorPane.getChildren().add(peakAnalysisPlotWallPlane);
        } else {
            peakAnalysisPlotWallPlane = (PlotWallPane) peakAnalysisAnchorPane.getChildren().get(0);
            peakAnalysisPlotWallPlane.clearTripoliPanes();
        }
        for (Map.Entry<String, List<PeakShapesOverlayRecord>> entry : mapPeakNameToAnalysisRecords.entrySet()) {
            PeakCentreAnalysisBuilder peakCentreAnalysisBuilder = initializeAnalysisPeakCentres(analysis.getMapOfBlockIdToPeakPlots().size(),
                    entry.getValue(),
                    new String[]{entry.getValue().get(0).title()[1]},
                    "Block ID",
                    "Peak Widths");

            TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(peakAnalysisPlotWallPlane);
            AbstractPlot plot = PeakCentresLinePlot.generatePlot(new Rectangle(minPlotWidth, minPlotHeight), peakCentreAnalysisBuilder.getPeakCentreAnalysisRecord());
            tripoliPlotPane.addPlot(plot);
        }
        peakAnalysisPlotWallPlane.stackPlots();
    }

    private synchronized void plotBlockEngine(Task<String> plotBuildersTaska) {
        analysisManagerCallbackI.callbackRefreshBlocksStatus();

        PlotBuildersTaskInterface plotBuildersTask = (PlotBuildersTaskInterface) plotBuildersTaska;

        PlotBuilder[] convergeRatioPlotBuilder = plotBuildersTask.getConvergeRatioLineBuilder();
        PlotBuilder[] convergeBLFaradayLineBuilder = plotBuildersTask.getConvergeBLFaradayLineBuilder();
        PlotBuilder[] convergeErrRawMisfitBuilder = plotBuildersTask.getConvergeErrRawMisfitLineBuilder();
        PlotBuilder[] convergeErrWeightedMisfitBuilder = plotBuildersTask.getConvergeErrWeightedMisfitLineBuilder();
        PlotBuilder[] convergeIntensityLinesBuilder = plotBuildersTask.getConvergeIntensityLinesBuilder();
        PlotBuilder[] observedDataPlotBuilder = plotBuildersTask.getObservedDataLineBuilder();
        PlotBuilder[] observedDataWithSubsetsLineBuilder = plotBuildersTask.getObservedDataWithSubsetsLineBuilder();
        PlotBuilder[] residualDataPlotBuilder = plotBuildersTask.getResidualDataLineBuilder();

        PlotBuilder[] peakShapeOverlayBuilder = plotBuildersTask.getPeakShapesBuilder();

        // plotting ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        plotTabPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            convergePlotsWallPane.repeatLayoutStyle();
            convergeErrorPlotsWallPane.repeatLayoutStyle();
            convergeIntensityPlotsWallPane.repeatLayoutStyle();
            ensemblePlotsWallPane.repeatLayoutStyle();
            dataFitPlotsWallPane.repeatLayoutStyle();
            peakShapeOverlayPlotWallPane.restoreAllPlots();
        });
        plotTabPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            convergePlotsWallPane.repeatLayoutStyle();
            convergeErrorPlotsWallPane.repeatLayoutStyle();
            convergeIntensityPlotsWallPane.repeatLayoutStyle();
            ensemblePlotsWallPane.repeatLayoutStyle();
            dataFitPlotsWallPane.repeatLayoutStyle();
            peakShapeOverlayPlotWallPane.repeatLayoutStyle();
        });


        if (convergePlotsAnchorPane.getChildren().isEmpty()) {
            convergePlotsWallPane = PlotWallPane.createPlotWallPane(PLOT_TAB_CONVERGE, analysis, this, null);
            convergePlotsWallPane.buildToolBar();
            convergePlotsWallPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));
            convergePlotsWallPane.prefWidthProperty().bind(convergePlotsAnchorPane.widthProperty());
            convergePlotsWallPane.prefHeightProperty().bind(convergePlotsAnchorPane.heightProperty());
            convergePlotsAnchorPane.getChildren().add(convergePlotsWallPane);
        } else {
            convergePlotsWallPane = (PlotWallPane) convergePlotsAnchorPane.getChildren().get(0);
            convergePlotsWallPane.clearTripoliPanes();
        }
        produceTripoliLinePlots(convergeRatioPlotBuilder, convergePlotsWallPane);
        produceTripoliLinePlots(convergeBLFaradayLineBuilder, convergePlotsWallPane);
        convergePlotsWallPane.tilePlots();


        if (convergeErrorPlotsAnchorPane.getChildren().isEmpty()) {
            convergeErrorPlotsWallPane = PlotWallPane.createPlotWallPane(PLOT_TAB_CONVERGE, analysis, this, null);
            convergeErrorPlotsWallPane.buildToolBar();
            convergeErrorPlotsWallPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));
            convergeErrorPlotsWallPane.prefWidthProperty().bind(convergeErrorPlotsAnchorPane.widthProperty());
            convergeErrorPlotsWallPane.prefHeightProperty().bind(convergeErrorPlotsAnchorPane.heightProperty());
            convergeErrorPlotsAnchorPane.getChildren().add(convergeErrorPlotsWallPane);
        } else {
            convergeErrorPlotsWallPane = (PlotWallPane) convergeErrorPlotsAnchorPane.getChildren().get(0);
            convergeErrorPlotsWallPane.clearTripoliPanes();
        }
        produceTripoliLinePlots(convergeErrRawMisfitBuilder, convergeErrorPlotsWallPane);
        produceTripoliLinePlots(convergeErrWeightedMisfitBuilder, convergeErrorPlotsWallPane);
        convergeErrorPlotsWallPane.tilePlots();


        if (convergeIntensityAnchorPane.getChildren().isEmpty()) {
            convergeIntensityPlotsWallPane = PlotWallPane.createPlotWallPane(PLOT_TAB_CONVERGE_INTENSITY, analysis, this, null);
            convergeIntensityPlotsWallPane.buildToolBar();
            convergeIntensityPlotsWallPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));
            convergeIntensityPlotsWallPane.prefWidthProperty().bind(convergeIntensityAnchorPane.widthProperty());
            convergeIntensityPlotsWallPane.prefHeightProperty().bind(convergeIntensityAnchorPane.heightProperty());
            convergeIntensityAnchorPane.getChildren().add(convergeIntensityPlotsWallPane);
        } else {
            convergeIntensityPlotsWallPane = (PlotWallPane) convergeIntensityAnchorPane.getChildren().get(0);
            convergeIntensityPlotsWallPane.clearTripoliPanes();
        }
        produceTripoliMultiLineIntensityPlots(convergeIntensityLinesBuilder, convergeIntensityPlotsWallPane);
        convergeIntensityPlotsWallPane.tilePlots();


        plotEnsemblesEngine(((MCMCPlotBuildersTask) plotBuildersTask).getPlotBuilders());


        if (dataFitPlotsAnchorPane.getChildren().isEmpty()) {
            dataFitPlotsWallPane = PlotWallPane.createPlotWallPane(null, analysis, this, null);
            dataFitPlotsWallPane.buildToolBar();
            dataFitPlotsWallPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));
            dataFitPlotsWallPane.prefWidthProperty().bind(dataFitPlotsAnchorPane.widthProperty());
            dataFitPlotsWallPane.prefHeightProperty().bind(dataFitPlotsAnchorPane.heightProperty());
            dataFitPlotsAnchorPane.getChildren().add(dataFitPlotsWallPane);
        } else {
            dataFitPlotsWallPane = (PlotWallPane) dataFitPlotsAnchorPane.getChildren().get(0);
            dataFitPlotsWallPane.clearTripoliPanes();
        }
        produceTripoliBasicScatterAndLinePlots(observedDataPlotBuilder, dataFitPlotsWallPane);
        produceTripoliBasicScatterAndLinePlots(observedDataWithSubsetsLineBuilder, dataFitPlotsWallPane);
        produceTripoliBasicScatterAndLinePlots(residualDataPlotBuilder, dataFitPlotsWallPane);
        dataFitPlotsWallPane.stackPlots();

        if (beamShapeAnchorPane.getChildren().isEmpty()) {
            peakShapeOverlayPlotWallPane = PlotWallPane.createPlotWallPane(null, analysis, this, null);
            peakShapeOverlayPlotWallPane.buildToolBar();
            peakShapeOverlayPlotWallPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));
            peakShapeOverlayPlotWallPane.prefWidthProperty().bind(beamShapeAnchorPane.widthProperty());
            peakShapeOverlayPlotWallPane.prefHeightProperty().bind(beamShapeAnchorPane.heightProperty());
            beamShapeAnchorPane.getChildren().add(peakShapeOverlayPlotWallPane);
        } else {
            peakShapeOverlayPlotWallPane = (PlotWallPane) beamShapeAnchorPane.getChildren().get(0);
            peakShapeOverlayPlotWallPane.clearTripoliPanes();
        }
        producePeakShapesOverlayPlot(peakShapeOverlayBuilder, peakShapeOverlayPlotWallPane);
        peakShapeOverlayPlotWallPane.tilePlots();
    }


    public void plotEnsemblesEngine(PlotBuilder[][] plotBuilders) {
        PlotBuilder[] ratiosHistogramBuilder = plotBuilders[PLOT_INDEX_RATIOS];
        PlotBuilder[] baselineHistogramBuilder = plotBuilders[PLOT_INDEX_BASELINES];
        PlotBuilder[] dalyFaradayHistogramBuilder = plotBuilders[PLOT_INDEX_DFGAINS];
        PlotBuilder[] intensityLinePlotBuilder = plotBuilders[PLOT_INDEX_MEANINTENSITIES];

        if (ensemblePlotsAnchorPane.getChildren().isEmpty()) {
            ensemblePlotsWallPane = PlotWallPane.createPlotWallPane(PLOT_TAB_ENSEMBLES, analysis, this, analysisManagerCallbackI);
            ensemblePlotsWallPane.buildToolBar();
            ensemblePlotsWallPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));
            ensemblePlotsWallPane.prefWidthProperty().bind(ensemblePlotsAnchorPane.widthProperty());
            ensemblePlotsWallPane.prefHeightProperty().bind(ensemblePlotsAnchorPane.heightProperty());
            ensemblePlotsAnchorPane.getChildren().add(ensemblePlotsWallPane);
        } else {
            ensemblePlotsWallPane = (PlotWallPane) ensemblePlotsAnchorPane.getChildren().get(0);
            ensemblePlotsWallPane.clearTripoliPanes();
        }
        produceTripoliRatioHistogramPlots(ratiosHistogramBuilder, ensemblePlotsWallPane);
        produceTripoliHistogramPlots(baselineHistogramBuilder, ensemblePlotsWallPane);
        produceTripoliHistogramPlots(dalyFaradayHistogramBuilder, ensemblePlotsWallPane);
        produceTripoliMultiLinePlots(intensityLinePlotBuilder, ensemblePlotsWallPane);
        ensemblePlotsWallPane.repeatLayoutStyle();

        listViewOfBlocks.refresh();
    }

    private synchronized void showLogsEngine(int blockNumber) {
        String log = analysis.uppdateLogsByBlock(blockNumber + 1, "");
        TextArea logTextArea = new TextArea(log);
        logTextArea.setPrefSize(logAnchorPane.getWidth(), logAnchorPane.getHeight());
        logAnchorPane.getChildren().removeAll();
        logAnchorPane.getChildren().add(logTextArea);
    }


    private void produceTripoliRatioHistogramPlots(PlotBuilder[] plotBuilder, PlotWallPane plotWallPane) {
        for (int i = 0; i < plotBuilder.length; i++) {
            if (plotBuilder[i].isDisplayed()) {
                HistogramRecord plotRecord = ((RatioHistogramBuilder) plotBuilder[i]).getHistogramRecord();
                HistogramRecord invertedPlotRecord = ((RatioHistogramBuilder) plotBuilder[i]).getInvertedRatioHistogramRecord();
                HistogramRecord logRatioHistogramRecord = ((RatioHistogramBuilder) plotBuilder[i]).getLogRatioHistogramRecord();
                HistogramRecord logInvertedRatioHistogramRecord = ((RatioHistogramBuilder) plotBuilder[i]).getLogInvertedRatioHistogramRecord();
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
            showLogsEngine(blockIndex);
            listViewOfBlocks.refresh();
        }
    }

    public void convergeTabSelected(Event event) {
        Tab tab = (Tab) event.getSource();
        if (tab.isSelected() && !((AnchorPane) tab.getContent()).getChildren().isEmpty()) {
            ((PlotWallPane) ((AnchorPane) tab.getContent()).getChildren().get(0)).restoreAllPlots();
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
                    setText("Block# " + blockID
                            + " {BurnIn = " + analysis.getMapOfBlockIdToModelsBurnCount().get(blockID)
                            + " of " + analysis.getMapBlockIDToEnsembles().get(blockID).size() + " models}");
                }
            }
        }
    }

}