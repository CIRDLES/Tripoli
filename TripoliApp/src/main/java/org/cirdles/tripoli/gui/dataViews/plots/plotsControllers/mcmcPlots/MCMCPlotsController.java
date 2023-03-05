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
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractDataView;
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
    private Service[] services;
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
    private GridPane dataFitGridPane;

    @FXML
    private TextArea eventLogTextArea;

    @FXML
    private ScrollPane listOfFilesScrollPane;

    @FXML
    private VBox masterVBox;

    @FXML
    private TabPane plotTabPane;

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

        plotTabPane.prefWidthProperty().bind(masterVBox.widthProperty());
        plotTabPane.prefHeightProperty().bind(masterVBox.heightProperty());

        dataFitGridPane.prefWidthProperty().bind(plotTabPane.widthProperty());
        dataFitGridPane.prefHeightProperty().bind(plotTabPane.heightProperty().subtract(TAB_HEIGHT));

//        convergeErrGridPane.prefWidthProperty().bind(plotTabPane.widthProperty().subtract(convergeErrLegendTextBox.getWidth()));
//        convergeErrGridPane.prefHeightProperty().bind(plotTabPane.heightProperty().subtract(TAB_HEIGHT));

        convergeIntensityAnchorPane.prefWidthProperty().bind(plotTabPane.widthProperty());
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
        listViewOfBlocks.setCellFactory((parameter) -> new BlockDisplayID());

        Collections.sort(blocksByName, new IntuitiveStringComparator<>());
        ObservableList<String> items = FXCollections.observableArrayList(blocksByName);
        listViewOfBlocks.setItems(items);
        listViewOfBlocks.getSelectionModel().selectFirst();
        listViewOfBlocks.prefWidthProperty().bind(listOfFilesScrollPane.widthProperty());
        listViewOfBlocks.prefHeightProperty().bind(listOfFilesScrollPane.heightProperty());
        listOfFilesScrollPane.setContent(listViewOfBlocks);

        listViewOfBlocks.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 ) {
                    viewSelectedBlockAction();
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void processDataFileAndShowPlotsOfMCMC2(AnalysisInterface analysis) {
        services = new Service[analysis.getMassSpecExtractedData().getBlocksData().size()];
        MCMCPlotBuildersTask.analysis = analysis;

        for (int blockIndex = 0; blockIndex < services.length; blockIndex++) {
            services[blockIndex] = new MCMCUpdatesService(blockIndex + 1);
            eventLogTextArea.setText("");
            eventLogTextArea.accessibleTextProperty().bind(((MCMCUpdatesService) services[blockIndex]).valueProperty());
            eventLogTextArea.accessibleTextProperty().addListener((observable, oldValue, newValue) -> {
                if (null != newValue) {
                    eventLogTextArea.setText(eventLogTextArea.getText() + "\n" + newValue);
                    eventLogTextArea.selectEnd();
                    eventLogTextArea.deselect();
                }
            });

            int finalBlockIndex = blockIndex;
            services[finalBlockIndex].setOnSucceeded(evt -> {
                Task<String> plotBuildersTask = ((MCMCUpdatesService) services[finalBlockIndex]).getPlotBuilderTask();
                if (null != plotBuildersTask) {
                    plotEngine(plotBuildersTask);
                }
            });

        }

        for (int blockIndex = 0; blockIndex < services.length; blockIndex++) {
            services[blockIndex].start();
        }
        processFileButton2.setDisable(true);
        MCMCProcess.ALLOW_EXECUTION = true;
    }


    private void plotEngine(Task<String> plotBuildersTaska) {
        PlotBuildersTaskInterface plotBuildersTask = (PlotBuildersTaskInterface) plotBuildersTaska;
        PlotBuilder[] ratiosHistogramBuilder = plotBuildersTask.getRatiosHistogramBuilder();
        PlotBuilder[] baselineHistogramBuilder = plotBuildersTask.getBaselineHistogramBuilder();
        PlotBuilder[] dalyFaradayHistogramBuilder = plotBuildersTask.getDalyFaradayGainHistogramBuilder();
        PlotBuilder[] signalNoiseHistogramBuilder = plotBuildersTask.getSignalNoiseHistogramBuilder();
        PlotBuilder[] intensityLinePlotBuilder = plotBuildersTask.getMeanIntensityLineBuilder();

        PlotBuilder[] convergeRatioPlotBuilder = plotBuildersTask.getConvergeRatioLineBuilder();
        PlotBuilder[] convergeBLFaradayLineBuilder = plotBuildersTask.getConvergeBLFaradayLineBuilder();
        PlotBuilder[] convergeNoiseFaradayLineBuilder = plotBuildersTask.getConvergeNoiseFaradayLineBuilder();
        PlotBuilder[] convergeErrWeightedMisfitBuilder = plotBuildersTask.getConvergeErrWeightedMisfitLineBuilder();
        PlotBuilder[] convergeErrRawMisfitBuilder = plotBuildersTask.getConvergeErrRawMisfitLineBuilder();

//
//        PlotBuilder observedDataPlotBuilder = plotBuildersTask.getObservedDataLineBuilder();
//        PlotBuilder residualDataPlotBuilder = plotBuildersTask.getResidualDataLineBuilder();
//
//        PlotBuilder convergeIntensityLinesBuilder = plotBuildersTask.getConvergeIntensityLinesBuilder();
//
//        AbstractDataView observedDataLinePlot = new BasicScatterAndLinePlot(
//                new Rectangle(dataFitGridPane.getWidth(),
//                        (plotTabPane.getHeight() - TAB_HEIGHT) / dataFitGridPane.getRowCount()),
//                (ComboPlotBuilder) observedDataPlotBuilder);
//
//        AbstractDataView residualDataLinePlot = new BasicScatterAndLinePlot(
//                new Rectangle(dataFitGridPane.getWidth(),
//                        (plotTabPane.getHeight() - TAB_HEIGHT) / dataFitGridPane.getRowCount()),
//                (ComboPlotBuilder) residualDataPlotBuilder);
//
//        AbstractDataView convergeIntensityLinesPlot = new MultiLinePlotLogX(
//                new Rectangle(convergeIntensityAnchorPane.getWidth(),
//                        plotTabPane.getHeight() - TAB_HEIGHT),
//                (MultiLinePlotBuilder) convergeIntensityLinesBuilder);
//
//        plotTabPane.widthProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue.intValue() > 100) {
//                double newWidth = newValue.intValue();
//
//                observedDataLinePlot.setMyWidth(newWidth);
//                observedDataLinePlot.repaint();
//                residualDataLinePlot.setMyWidth(newWidth);
//                residualDataLinePlot.repaint();
//                convergeIntensityLinesPlot.setMyWidth(newWidth);
//                convergeIntensityLinesPlot.repaint();
//
//            }
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
//
//        observedDataLinePlot.preparePanel();
//        dataFitGridPane.add(observedDataLinePlot, 0, 0);
//        residualDataLinePlot.preparePanel();
//        dataFitGridPane.add(residualDataLinePlot, 0, 1);
//
//        convergeIntensityLinesPlot.preparePanel();
//        convergeIntensityAnchorPane.getChildren().add(convergeIntensityLinesPlot);

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
        ensemblePlotsWallPane.tilePlots();

        PlotWallPane convergePlotsWallPane = new PlotWallPane();
        convergePlotsWallPane.buildToolBar();
        convergePlotsWallPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));
        convergePlotsAnchorPane.getChildren().add(convergePlotsWallPane);
        produceTripoliLinePlots(convergeRatioPlotBuilder, convergePlotsWallPane);
        produceTripoliLinePlots(convergeBLFaradayLineBuilder, convergePlotsWallPane);
        produceTripoliLinePlots(convergeNoiseFaradayLineBuilder, convergePlotsWallPane);
        produceTripoliLinePlots(convergeErrRawMisfitBuilder, convergePlotsWallPane);
        produceTripoliLinePlots(convergeErrWeightedMisfitBuilder, convergePlotsWallPane);
        convergePlotsWallPane.tilePlots();
//
//        TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(ensemblePlotsWallPane);
//        AbstractPlot plot = MultiLinePlot.generatePlot(new Rectangle(minPlotWidth, minPlotHeight), (MultiLinePlotBuilder) intensityLinePlotBuilder[0]);
//        tripoliPlotPane.addPlot(plot);


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
        Task<String> mcmcPlotBuildersTask = ((MCMCUpdatesService) services[blockNumber]).getPlotBuilderTask();
        if (mcmcPlotBuildersTask.isDone()) {
            plotEngine(mcmcPlotBuildersTask);
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