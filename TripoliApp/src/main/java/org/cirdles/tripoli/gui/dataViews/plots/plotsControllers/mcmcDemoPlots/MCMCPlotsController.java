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
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.PlotWallPane;
import org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmc2Plots.MCMC2UpdatesService;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmc2Plots.PlotBuildersTaskInterface;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.HistogramSinglePlot;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.LinePlot;
import org.cirdles.tripoli.plots.AbstractPlotBuilder;
import org.cirdles.tripoli.plots.histograms.HistogramBuilder;
import org.cirdles.tripoli.plots.histograms.HistogramRecord;
import org.cirdles.tripoli.plots.linePlots.LinePlotBuilder;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmcV2.MCMCProcess;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotHeight;
import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotWidth;
import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcDemoPlots.MCMCPlotsWindow.PLOT_WINDOW_HEIGHT;
import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcDemoPlots.MCMCPlotsWindow.PLOT_WINDOW_WIDTH;

public class MCMCPlotsController {

    private static final int TAB_HEIGHT = 35;
    public static AnalysisInterface analysis;
    private Service<String> service;
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
    private Button processFileButton2;

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

        populateListOfAvailableBlocks();
    }

    private void populateListOfAvailableBlocks() {
        int blockCount = analysis.getMassSpecExtractedData().getBlocksData().size();
        List<String> blocksByName = new ArrayList<>();
        for (int i = 0; i < blockCount; i++) {
            blocksByName.add("Block # " + (i + 1));
        }

        listViewOfBlocks = new ListView<>();
        listViewOfBlocks.setCellFactory((parameter) -> new BlockDisplayName());

        ObservableList<String> items = FXCollections.observableArrayList(blocksByName);
        listViewOfBlocks.setItems(items);
        listViewOfBlocks.getSelectionModel().selectFirst();
        listViewOfBlocks.prefWidthProperty().bind(listOfFilesScrollPane.widthProperty());
        listViewOfBlocks.prefHeightProperty().bind(listOfFilesScrollPane.heightProperty());
        listOfFilesScrollPane.setContent(listViewOfBlocks);
    }

    public void processDataFileAndShowPlotsOfMCMC2(AnalysisInterface analysis) {
        int blockNumber = listViewOfBlocks.getSelectionModel().getSelectedIndex() + 1;
        service = new MCMC2UpdatesService(analysis, blockNumber);
        eventLogTextArea.setText("");
        eventLogTextArea.accessibleTextProperty().bind(service.valueProperty());
        eventLogTextArea.accessibleTextProperty().addListener((observable, oldValue, newValue) -> {
            if (null != newValue) {
                eventLogTextArea.setText(eventLogTextArea.getText() + "\n" + newValue);
                eventLogTextArea.selectEnd();
                eventLogTextArea.deselect();
            }
        });
        processFileButton2.setDisable(true);
        service.start();
        service.setOnSucceeded(evt -> {
            Task<String> plotBuildersTask = ((MCMC2UpdatesService) service).getPlotBuildersTask();
            plotEngine(plotBuildersTask);
        });

        MCMCProcess.ALLOW_EXECUTION = true;
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


////        AbstractPlotBuilder observedDataPlotBuilder = plotBuildersTask.getObservedDataLineBuilder();
////        AbstractPlotBuilder residualDataPlotBuilder = plotBuildersTask.getResidualDataLineBuilder();
////
////        AbstractPlotBuilder convergeIntensityLinesBuilder = plotBuildersTask.getConvergeIntensityLinesBuilder();
////
////        AbstractDataView observedDataLinePlot = new BasicScatterAndLinePlot(
////                new Rectangle(dataFitGridPane.getWidth(),
////                        (plotTabPane.getHeight() - TAB_HEIGHT) / dataFitGridPane.getRowCount()),
////                (ComboPlotBuilder) observedDataPlotBuilder);
////
////        AbstractDataView residualDataLinePlot = new BasicScatterAndLinePlot(
////                new Rectangle(dataFitGridPane.getWidth(),
////                        (plotTabPane.getHeight() - TAB_HEIGHT) / dataFitGridPane.getRowCount()),
////                (ComboPlotBuilder) residualDataPlotBuilder);
////
////        AbstractDataView convergeIntensityLinesPlot = new MultiLinePlotLogX(
////                new Rectangle(convergeIntensityAnchorPane.getWidth(),
////                        plotTabPane.getHeight() - TAB_HEIGHT),
////                (MultiLinePlotBuilder) convergeIntensityLinesBuilder);
////
////        plotTabPane.widthProperty().addListener((observable, oldValue, newValue) -> {
////            if (newValue.intValue() > 100) {
////                double newWidth = newValue.intValue();
////
////                observedDataLinePlot.setMyWidth(newWidth);
////                observedDataLinePlot.repaint();
////                residualDataLinePlot.setMyWidth(newWidth);
////                residualDataLinePlot.repaint();
////                convergeIntensityLinesPlot.setMyWidth(newWidth);
////                convergeIntensityLinesPlot.repaint();
////
////            }
//        });
//
//        plotTabPane.heightProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue.intValue() > 100) {
//
//                observedDataLinePlot.setMyHeight((newValue.intValue() - TAB_HEIGHT) / dataFitGridPane.getRowCount());
//                observedDataLinePlot.repaint();
//                residualDataLinePlot.setMyHeight((newValue.intValue() - TAB_HEIGHT) / dataFitGridPane.getRowCount());
//                residualDataLinePlot.repaint();
//
//                convergeIntensityLinesPlot.setMyHeight(newValue.intValue() - TAB_HEIGHT);
//                convergeIntensityLinesPlot.repaint();
//            }
//        });

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

    public void stopProcess2(ActionEvent actionEvent) {
        MCMCProcess.ALLOW_EXECUTION = false;
        ((MCMCUpdatesService) service).getPlotBuildersTask().cancel();
        if (((MCMCUpdatesService) service).getPlotBuildersTask().isCancelled()) {
            service.cancel();
        }
    }


    static class BlockDisplayName extends ListCell<String> {
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