package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.RJMCMCPlots;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractDataView;
import org.cirdles.tripoli.gui.dataViews.plots.HistogramPlot;
import org.cirdles.tripoli.visualizationUtilities.AbstractPlotBuilder;
import org.cirdles.tripoli.visualizationUtilities.histograms.HistogramBuilder;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.RJMCMCPlots.RJMCMCPlotsWindow.*;

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

        ensembleGridPane.prefWidthProperty().bind(plotTabPane.widthProperty());
        ensembleGridPane.prefHeightProperty().bind(plotTabPane.heightProperty().subtract(toolbar.getHeight()));

    }

    public void processDataFileAndShowPlotsOfRJMCMC() throws IOException {
        org.cirdles.commons.util.ResourceExtractor RESOURCE_EXTRACTOR = new ResourceExtractor(Tripoli.class);
        Path dataFile = RESOURCE_EXTRACTOR
                .extractResourceAsFile("/org/cirdles/tripoli/dataProcessors/dataSources/synthetic/SyntheticDataset_05.txt").toPath();
        final RJMCMCUpdatesService service = new RJMCMCUpdatesService(dataFile);
        eventLogTextArea.textProperty().bind(service.valueProperty());
        service.start();
        service.setOnSucceeded(evt -> {
            AbstractPlotBuilder ratiosHistogramBuilder = ((RJMCMCUpdatesTask) service.getHistogramTask()).getRatiosHistogramBuilder();
            AbstractPlotBuilder baselineHistogramBuilder = ((RJMCMCUpdatesTask) service.getHistogramTask()).getBaselineHistogramBuilder();
            AbstractPlotBuilder dalyFaradayHistogramBuilder = ((RJMCMCUpdatesTask) service.getHistogramTask()).getDalyFaradayGainHistogramBuilder();

            AbstractDataView ratiosHistogramPlot = new HistogramPlot(
                    new Rectangle(ensembleGridPane.getWidth(),
                            ensembleGridPane.getHeight() / ensembleGridPane.getRowCount()),
                    (HistogramBuilder)ratiosHistogramBuilder);

            AbstractDataView baselineHistogramPlot = new HistogramPlot(
                    new Rectangle(ensembleGridPane.getWidth() / ensembleGridPane.getColumnCount(),
                            ensembleGridPane.getHeight() / ensembleGridPane.getRowCount()),
                    (HistogramBuilder)baselineHistogramBuilder);

            AbstractDataView dalyFaradayHistogramPlot = new HistogramPlot(
                    new Rectangle(ensembleGridPane.getWidth() / ensembleGridPane.getColumnCount(),
                            ensembleGridPane.getHeight() / ensembleGridPane.getRowCount()),
                    (HistogramBuilder)dalyFaradayHistogramBuilder);

            plotTabPane.widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    if (newValue.intValue() > 100) {
                        ratiosHistogramPlot.setMyWidth(newValue.intValue());
                        ratiosHistogramPlot.repaint();
                        baselineHistogramPlot.setMyWidth(newValue.intValue() / ensembleGridPane.getColumnCount());
                        baselineHistogramPlot.repaint();
                        dalyFaradayHistogramPlot.setMyWidth(newValue.intValue() / ensembleGridPane.getColumnCount());
                        dalyFaradayHistogramPlot.repaint();
                    }
                }
            });

            plotTabPane.heightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    if (newValue.intValue() > 100) {
                        ratiosHistogramPlot.setMyHeight(newValue.intValue() / ensembleGridPane.getRowCount());
                        ratiosHistogramPlot.repaint();
                        baselineHistogramPlot.setMyHeight(newValue.intValue() / ensembleGridPane.getRowCount());
                        baselineHistogramPlot.repaint();
                        dalyFaradayHistogramPlot.setMyHeight(newValue.intValue() / ensembleGridPane.getRowCount());
                        dalyFaradayHistogramPlot.repaint();
                    }
                }
            });

            ratiosHistogramPlot.preparePanel();
            ensembleGridPane.add(ratiosHistogramPlot, 0, 0, 2, 1);
            baselineHistogramPlot.preparePanel();
            ensembleGridPane.add(baselineHistogramPlot, 0, 1,1,1);
            dalyFaradayHistogramPlot.preparePanel();
            ensembleGridPane.add(dalyFaradayHistogramPlot, 1, 1,1,1);

        });

    }
    
}