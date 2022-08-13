package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.RJMCMCPlots;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractDataView;
import org.cirdles.tripoli.gui.dataViews.plots.BasicScatterPlot;
import org.cirdles.tripoli.gui.dataViews.plots.HistogramPlot;
import org.cirdles.tripoli.gui.dataViews.plots.BasicLinePlot;
import org.cirdles.tripoli.visualizationUtilities.AbstractPlotBuilder;
import org.cirdles.tripoli.visualizationUtilities.histograms.HistogramBuilder;
import org.cirdles.tripoli.visualizationUtilities.linePlots.LinePlotBuilder;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.RJMCMCPlots.RJMCMCPlotsWindow.PLOT_WINDOW_HEIGHT;
import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.RJMCMCPlots.RJMCMCPlotsWindow.PLOT_WINDOW_WIDTH;

public class RJMCMCPlotsController {

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
    private ScrollPane convergeRatioScrollPane;

    @FXML
    private GridPane dataFitGridPane;

    @FXML
    private TextArea dataFitLegendTextBox;

    @FXML
    void demo1ButtonAction(ActionEvent event) throws IOException {
        processDataFileAndShowPlotsOfRJMCMC();
        ((Button) event.getSource()).setDisable(true);
    }

    @FXML
    void initialize() {

        masterVBox.setPrefSize(PLOT_WINDOW_WIDTH, PLOT_WINDOW_HEIGHT);
        toolbar.setPrefSize(PLOT_WINDOW_WIDTH, 20.0);


        masterVBox.prefWidthProperty().bind(plotsAnchorPane.widthProperty());
        masterVBox.prefHeightProperty().bind(plotsAnchorPane.heightProperty());

        plotTabPane.prefWidthProperty().bind(masterVBox.widthProperty());
        plotTabPane.prefHeightProperty().bind(masterVBox.heightProperty().subtract(toolbar.getHeight()));

        ensembleGridPane.prefWidthProperty().bind(plotTabPane.widthProperty().subtract(ensembleLegendTextBox.getWidth()));
        ensembleGridPane.prefHeightProperty().bind(plotTabPane.heightProperty().subtract(toolbar.getHeight()));

        convergeRatioScrollPane.prefWidthProperty().bind(plotTabPane.widthProperty());
        convergeRatioScrollPane.prefHeightProperty().bind(plotTabPane.heightProperty().subtract(toolbar.getHeight()));

        dataFitGridPane.prefWidthProperty().bind(plotTabPane.widthProperty().subtract(dataFitLegendTextBox.getWidth()));
        dataFitGridPane.prefHeightProperty().bind(plotTabPane.heightProperty().subtract(toolbar.getHeight()));


    }

    public void processDataFileAndShowPlotsOfRJMCMC() throws IOException {
        org.cirdles.commons.util.ResourceExtractor RESOURCE_EXTRACTOR = new ResourceExtractor(Tripoli.class);
        Path dataFile = RESOURCE_EXTRACTOR
                .extractResourceAsFile("/org/cirdles/tripoli/dataProcessors/dataSources/synthetic/SyntheticDataset_05.txt").toPath();
        final RJMCMCUpdatesService service = new RJMCMCUpdatesService(dataFile);
        eventLogTextArea.textProperty().bind(service.valueProperty());
        service.start();
        service.setOnSucceeded(evt -> {
            AbstractPlotBuilder ratiosHistogramBuilder = ((RJMCMCPlotBuildersTask) service.getPlotBuildersTask()).getRatiosHistogramBuilder();
            AbstractPlotBuilder baselineHistogramBuilder = ((RJMCMCPlotBuildersTask) service.getPlotBuildersTask()).getBaselineHistogramBuilder();
            AbstractPlotBuilder dalyFaradayHistogramBuilder = ((RJMCMCPlotBuildersTask) service.getPlotBuildersTask()).getDalyFaradayGainHistogramBuilder();
            AbstractPlotBuilder signalNoiseHistogramBuilder = ((RJMCMCPlotBuildersTask) service.getPlotBuildersTask()).getSignalNoiseHistogramBuilder();
            AbstractPlotBuilder intensityLinePlotBuilder = ((RJMCMCPlotBuildersTask) service.getPlotBuildersTask()).getMeanIntensityLineBuilder();

            AbstractPlotBuilder convergeRatioPlotBuilder = ((RJMCMCPlotBuildersTask) service.getPlotBuildersTask()).getConvergeRatioLineBuilder();

            AbstractPlotBuilder observedDataPlotBuilder = ((RJMCMCPlotBuildersTask) service.getPlotBuildersTask()).getObserveDataLineBuilder();

            AbstractDataView ratiosHistogramPlot = new HistogramPlot(
                    new Rectangle(ensembleGridPane.getWidth(),
                            ensembleGridPane.getHeight() / ensembleGridPane.getRowCount()),
                    (HistogramBuilder) ratiosHistogramBuilder);

            AbstractDataView baselineHistogramPlot = new HistogramPlot(
                    new Rectangle(ensembleGridPane.getWidth() / ensembleGridPane.getColumnCount(),
                            ensembleGridPane.getHeight() / ensembleGridPane.getRowCount()),
                    (HistogramBuilder) baselineHistogramBuilder);

            AbstractDataView dalyFaradayHistogramPlot = new HistogramPlot(
                    new Rectangle(ensembleGridPane.getWidth() / ensembleGridPane.getColumnCount(),
                            ensembleGridPane.getHeight() / ensembleGridPane.getRowCount()),
                    (HistogramBuilder) dalyFaradayHistogramBuilder);

            AbstractDataView intensityLinePlot = new BasicLinePlot(
                    new Rectangle(ensembleGridPane.getWidth(),
                            ensembleGridPane.getHeight() / ensembleGridPane.getRowCount()),
                    (LinePlotBuilder) intensityLinePlotBuilder
            );

            AbstractDataView signalNoiseHistogramPlot = new HistogramPlot(
                    new Rectangle(ensembleGridPane.getWidth(),
                            ensembleGridPane.getHeight() / ensembleGridPane.getRowCount()),
                    (HistogramBuilder) signalNoiseHistogramBuilder);

            AbstractDataView convergeRatioLinePlot = new BasicLinePlot(
                    new Rectangle(convergeRatioScrollPane.getWidth(),
                            convergeRatioScrollPane.getHeight()),
                    (LinePlotBuilder) convergeRatioPlotBuilder);

            AbstractDataView observedDataLinePlot = new BasicScatterPlot(
                    new Rectangle(dataFitGridPane.getWidth(),
                            dataFitGridPane.getHeight() / dataFitGridPane.getRowCount()),
                    (LinePlotBuilder) observedDataPlotBuilder);


            plotTabPane.widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
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

                        convergeRatioLinePlot.setMyWidth(newValue.intValue());
                        convergeRatioLinePlot.repaint();

                        observedDataLinePlot.setMyWidth(newWidth);
                        observedDataLinePlot.repaint();
                    }
                }
            });

            plotTabPane.heightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    if (newValue.intValue() > 100) {
                        ratiosHistogramPlot.setMyHeight(newValue.intValue() / ensembleGridPane.getRowCount() - 5);
                        ratiosHistogramPlot.repaint();
                        baselineHistogramPlot.setMyHeight(newValue.intValue() / ensembleGridPane.getRowCount() - 5);
                        baselineHistogramPlot.repaint();
                        dalyFaradayHistogramPlot.setMyHeight(newValue.intValue() / ensembleGridPane.getRowCount() - 5);
                        dalyFaradayHistogramPlot.repaint();
                        intensityLinePlot.setMyHeight(newValue.intValue() / ensembleGridPane.getRowCount() - 5);
                        intensityLinePlot.repaint();
                        signalNoiseHistogramPlot.setMyHeight(newValue.intValue() / ensembleGridPane.getRowCount() - 5);
                        signalNoiseHistogramPlot.repaint();

                        convergeRatioLinePlot.setMyHeight(newValue.intValue());
                        convergeRatioLinePlot.repaint();

                        observedDataLinePlot.setMyHeight(newValue.intValue());
                        observedDataLinePlot.repaint();
                    }
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
            convergeRatioScrollPane.setContent(convergeRatioLinePlot);

            observedDataLinePlot.preparePanel();
            dataFitGridPane.add(observedDataLinePlot, 0,0);


        });

    }

}