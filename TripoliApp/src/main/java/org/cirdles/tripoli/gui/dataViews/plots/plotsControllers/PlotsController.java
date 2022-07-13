package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractDataView;
import org.cirdles.tripoli.gui.dataViews.plots.HistogramPlot;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.DataModelDriverExperiment;
import org.cirdles.tripoli.visualizationUtilities.Histogram;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class PlotsController {

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
    private ToolBar toolbar;

    @FXML
    private Button button;

    @FXML
    void buttonAction(ActionEvent event) throws IOException {
        loadPlot();
        ((Button)event.getSource()).setDisable(true);
    }


    @FXML
    void initialize() {

        masterVBox.setPrefSize(500.0, 500.0);
        toolbar.setPrefSize(500, 20.0);
        plotScrollPane.setPrefSize(500.0, 500.0 - toolbar.getHeight());
        plotScrollPane.setPrefViewportWidth(485.0);
        plotScrollPane.setPrefViewportHeight(465.0);

        masterVBox.prefWidthProperty().bind(plotsAnchorPane.widthProperty());
        masterVBox.prefHeightProperty().bind(plotsAnchorPane.heightProperty());

        plotScrollPane.prefWidthProperty().bind(masterVBox.widthProperty());
        plotScrollPane.prefHeightProperty().bind(masterVBox.heightProperty().subtract(toolbar.getHeight()));

    }

    public void loadPlot() throws IOException {
        org.cirdles.commons.util.ResourceExtractor RESOURCE_EXTRACTOR = new ResourceExtractor(Tripoli.class);
        Path dataFile = RESOURCE_EXTRACTOR
                .extractResourceAsFile("/org/cirdles/tripoli/dataProcessors/dataSources/synthetic/SyntheticDataset_05.txt").toPath();
        Histogram histogram = DataModelDriverExperiment.driveModelTest(dataFile);

        AbstractDataView histogramPlot = new HistogramPlot(
                new Rectangle(plotScrollPane.getWidth(),
                        plotScrollPane.getHeight()),
                histogram);

        plotScrollPane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.intValue() > 100) {
                    histogramPlot.setMyWidth(newValue.intValue() - 15);
                    histogramPlot.preparePanel();
                    histogramPlot.repaint();
                }
            }
        });

        plotScrollPane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.intValue() > 100) {
                    histogramPlot.setMyHeight(newValue.intValue() - 15);
                    histogramPlot.preparePanel();
                    histogramPlot.repaint();
                }
            }
        });

        histogramPlot.preparePanel();
        plotScrollPane.setContent(histogramPlot);

    }


}