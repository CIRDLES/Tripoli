package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractDataView;
import org.cirdles.tripoli.gui.dataViews.plots.HistogramPlot;
import org.cirdles.tripoli.visualizationUtilities.histograms.HistogramBuilder;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

import static org.cirdles.tripoli.gui.dataViews.plots.RJMCMCPlotsWindow.*;

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
    private ScrollPane plotScrollPane;

    @FXML
    private TextArea eventLogTextArea;

    @FXML
    private ToolBar toolbar;

    @FXML
    void demo1ButtonAction(ActionEvent event) throws IOException {
        processDataFileAndShowPlotsOfRJMCMC();
        ((Button) event.getSource()).setDisable(true);
    }

    @FXML
    void initialize() {

        masterVBox.setPrefSize(PLOT_WINDOW_WIDTH, PLOT_WINDOW_HEIGHT);
        toolbar.setPrefSize(PLOT_WINDOW_WIDTH, 20.0);
        plotScrollPane.setPrefSize(PLOT_WINDOW_WIDTH, PLOT_WINDOW_HEIGHT - toolbar.getHeight());
        plotScrollPane.setPrefViewportWidth(PLOT_WINDOW_WIDTH - SCROLLBAR_THICKNESS);
        plotScrollPane.setPrefViewportHeight(plotScrollPane.getPrefHeight() - SCROLLBAR_THICKNESS);

        masterVBox.prefWidthProperty().bind(plotsAnchorPane.widthProperty());
        masterVBox.prefHeightProperty().bind(plotsAnchorPane.heightProperty());

        plotScrollPane.prefWidthProperty().bind(masterVBox.widthProperty());
        plotScrollPane.prefHeightProperty().bind(masterVBox.heightProperty().subtract(toolbar.getHeight()));

    }

    public void processDataFileAndShowPlotsOfRJMCMC() throws IOException {
        org.cirdles.commons.util.ResourceExtractor RESOURCE_EXTRACTOR = new ResourceExtractor(Tripoli.class);
        Path dataFile = RESOURCE_EXTRACTOR
                .extractResourceAsFile("/org/cirdles/tripoli/dataProcessors/dataSources/synthetic/SyntheticDataset_05.txt").toPath();
        final GetRJMCMCUpdatesService service = new GetRJMCMCUpdatesService(dataFile);
        eventLogTextArea.textProperty().bind(service.valueProperty());
        service.start();
        service.setOnSucceeded(evt -> {
            HistogramBuilder histogramBuilder = ((GetRJMCMCUpdatesTask) service.getHistogramTask()).getHistogram();

            AbstractDataView histogramPlot = new HistogramPlot(
                    new Rectangle(plotScrollPane.getWidth(),
                            plotScrollPane.getHeight()),
                    histogramBuilder);

            plotScrollPane.widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    if (newValue.intValue() > 100) {
                        histogramPlot.setMyWidth(newValue.intValue() - SCROLLBAR_THICKNESS);
                        histogramPlot.repaint();
                    }
                }
            });

            plotScrollPane.heightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    if (newValue.intValue() > 100) {
                        histogramPlot.setMyHeight(newValue.intValue() - SCROLLBAR_THICKNESS);
                        histogramPlot.repaint();
                    }
                }
            });

            histogramPlot.preparePanel();
            plotScrollPane.setContent(histogramPlot);
        });

    }
    
}