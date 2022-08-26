package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.RJMCMCPlots;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.gui.dataViews.plots.*;
import org.cirdles.tripoli.visualizationUtilities.AbstractPlotBuilder;
import org.cirdles.tripoli.visualizationUtilities.histograms.HistogramBuilder;
import org.cirdles.tripoli.visualizationUtilities.linePlots.ComboPlotBuilder;
import org.cirdles.tripoli.visualizationUtilities.linePlots.LinePlotBuilder;
import org.cirdles.tripoli.visualizationUtilities.linePlots.MultiLinePlotBuilder;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.RJMCMCPlots.RJMCMCPlotsWindow.PLOT_WINDOW_HEIGHT;
import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.RJMCMCPlots.RJMCMCPlotsWindow.PLOT_WINDOW_WIDTH;

public class RJMCMCPlotsController {

    private static final int TAB_HEIGHT = 35;
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane plotsAnchorPane;

    @FXML
    private VBox masterVBox;

    @FXML
    private TextArea eventLogTextArea;

    @FXML
    private ToolBar toolbar;

    @FXML
    private TabPane plotTabPane;

    @FXML
    private GridPane ensembleGridPane;

    @FXML
    private TextArea ensembleLegendTextBox;

    @FXML
    private AnchorPane convergeRatioAnchorPane;

    @FXML
    private TextArea convergeRatioLegendTextBox;

    @FXML
    private GridPane dataFitGridPane;

    @FXML
    private TextArea dataFitLegendTextBox;

    @FXML
    private GridPane convergeBLGridPane;

    @FXML
    private TextArea convergeBLLegendTextBox;

    @FXML
    private GridPane convergeErrGridPane;

    @FXML
    private TextArea convergeErrLegendTextBox;

    @FXML
    private TextArea convergeIntensityLegendTextBox;

    @FXML
    private AnchorPane convergeIntensityAnchorPane;

    @FXML
    private GridPane convergeNoiseGridPane;

    @FXML
    private TextArea convergeNoiseLegendTextBox;


    @FXML
    void demo1ButtonAction(ActionEvent event) throws IOException {
        processDataFileAndShowPlotsOfRJMCMC();
        ((Button) event.getSource()).setDisable(true);
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

    }

    public void processDataFileAndShowPlotsOfRJMCMC() throws IOException {
        org.cirdles.commons.util.ResourceExtractor RESOURCE_EXTRACTOR = new ResourceExtractor(Tripoli.class);
        Path dataFile = RESOURCE_EXTRACTOR
                .extractResourceAsFile("/org/cirdles/tripoli/dataProcessors/dataSources/synthetic/twoIsotopeSyntheticData/SyntheticDataset_05.txt").toPath();
        final RJMCMCUpdatesService service = new RJMCMCUpdatesService(dataFile);
        eventLogTextArea.textProperty().bind(service.valueProperty());
        service.start();
        service.setOnSucceeded(evt -> {
            RJMCMCPlotBuildersTask plotBuildersTask = ((RJMCMCPlotBuildersTask) service.getPlotBuildersTask());

            AbstractPlotBuilder ratiosHistogramBuilder = plotBuildersTask.getRatiosHistogramBuilder();
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


            AbstractDataView ratiosHistogramPlot = new HistogramPlot(
                    new Rectangle(ensembleGridPane.getWidth(),
                            (plotTabPane.getHeight() - TAB_HEIGHT) / ensembleGridPane.getRowCount()),
                    (HistogramBuilder) ratiosHistogramBuilder);

            AbstractDataView baselineHistogramPlot = new HistogramPlot(
                    new Rectangle(ensembleGridPane.getWidth() / ensembleGridPane.getColumnCount(),
                            (plotTabPane.getHeight() - TAB_HEIGHT) / ensembleGridPane.getRowCount()),
                    (HistogramBuilder) baselineHistogramBuilder);

            AbstractDataView dalyFaradayHistogramPlot = new HistogramPlot(
                    new Rectangle(ensembleGridPane.getWidth() / ensembleGridPane.getColumnCount(),
                            (plotTabPane.getHeight() - TAB_HEIGHT) / ensembleGridPane.getRowCount()),
                    (HistogramBuilder) dalyFaradayHistogramBuilder);

            AbstractDataView intensityLinePlot = new BasicLinePlot(
                    new Rectangle(ensembleGridPane.getWidth(),
                            (plotTabPane.getHeight() - TAB_HEIGHT) / ensembleGridPane.getRowCount()),
                    (LinePlotBuilder) intensityLinePlotBuilder
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
                    ratiosHistogramPlot.setMyWidth(newWidth);
                    ratiosHistogramPlot.repaint();
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
                    ratiosHistogramPlot.setMyHeight((newValue.intValue() - TAB_HEIGHT) / ensembleGridPane.getRowCount());
                    ratiosHistogramPlot.repaint();
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

            ratiosHistogramPlot.preparePanel();
            ensembleGridPane.add(ratiosHistogramPlot, 0, 0, 2, 1);
            baselineHistogramPlot.preparePanel();
            ensembleGridPane.add(baselineHistogramPlot, 0, 1, 1, 1);
            dalyFaradayHistogramPlot.preparePanel();
            ensembleGridPane.add(dalyFaradayHistogramPlot, 1, 1, 1, 1);
            intensityLinePlot.preparePanel();
            ensembleGridPane.add(intensityLinePlot, 0, 2, 2, 1);
            signalNoiseHistogramPlot.preparePanel();
            ensembleGridPane.add(signalNoiseHistogramPlot, 0, 4, 2, 1);

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

        });

    }

}