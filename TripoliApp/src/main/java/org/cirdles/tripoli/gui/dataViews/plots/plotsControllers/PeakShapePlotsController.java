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
import org.cirdles.tripoli.gui.dataViews.plots.BeamShapeLinePlot;
import org.cirdles.tripoli.gui.dataViews.plots.GBeamLinePlot;
import org.cirdles.tripoli.visualizationUtilities.linePlots.BeamShapeLinePlotBuilder;
import org.cirdles.tripoli.visualizationUtilities.linePlots.GBeamLinePlotBuilder;
import org.cirdles.tripoli.visualizationUtilities.linePlots.LinePlotBuilder;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

import static org.cirdles.tripoli.gui.dataViews.plots.RJMCMCPlotsWindow.*;

public class PeakShapePlotsController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane plotsAnchorPane;

    @FXML
    private VBox masterVBox;

    @FXML
    private ScrollPane beamShapePlotScrollPane;

    @FXML
    private ScrollPane gBeamPlotScrollPane;

    @FXML
    private TextArea eventLogTextArea;

    @FXML
    private ToolBar toolbar;

    @FXML
    void demo2ButtonAction(ActionEvent event) throws IOException {
        processDataFileAndShowPlotsOfPeakShapes();
        ((Button) event.getSource()).setDisable(true);
    }

    @FXML
    void initialize() {

        masterVBox.setPrefSize(PLOT_WINDOW_WIDTH, PLOT_WINDOW_HEIGHT);
        toolbar.setPrefSize(PLOT_WINDOW_WIDTH, 20.0);
        gBeamPlotScrollPane.setPrefSize(PLOT_WINDOW_WIDTH, PLOT_WINDOW_HEIGHT - toolbar.getHeight());
        gBeamPlotScrollPane.setPrefViewportWidth(PLOT_WINDOW_WIDTH - SCROLLBAR_THICKNESS);
        gBeamPlotScrollPane.setPrefViewportHeight(gBeamPlotScrollPane.getPrefHeight() - SCROLLBAR_THICKNESS);

        masterVBox.prefWidthProperty().bind(plotsAnchorPane.widthProperty());
        masterVBox.prefHeightProperty().bind(plotsAnchorPane.heightProperty());

        gBeamPlotScrollPane.prefWidthProperty().bind(masterVBox.widthProperty());
        gBeamPlotScrollPane.prefHeightProperty().bind(masterVBox.heightProperty().subtract(toolbar.getHeight()));

    }

    public void processDataFileAndShowPlotsOfPeakShapes() throws IOException {
        ResourceExtractor RESOURCE_EXTRACTOR = new ResourceExtractor(Tripoli.class);
        Path dataFile = RESOURCE_EXTRACTOR
                .extractResourceAsFile("/org/cirdles/tripoli/dataProcessors/dataSources/peakShapes/DVCC18-9 z9 Pb-570-PKC-205Pb-PM-S2B7C1.TXT").toPath();
        final PeakShapesService service = new PeakShapesService(dataFile);
        eventLogTextArea.textProperty().bind(service.valueProperty());
        service.start();
        service.setOnSucceeded(evt -> {
            LinePlotBuilder gBeamPlotBuilder = ((PeakShapesTask) service.getPeakShapesTask()).getGBeamPlotBuilder();

            AbstractDataView gBeamLinePlot = new GBeamLinePlot(
                    new Rectangle(gBeamPlotScrollPane.getWidth(),
                            gBeamPlotScrollPane.getHeight()),
                    (GBeamLinePlotBuilder) gBeamPlotBuilder
            );

            gBeamPlotScrollPane.widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    if (newValue.intValue() > 100) {
                        gBeamLinePlot.setMyWidth(newValue.intValue() - SCROLLBAR_THICKNESS);
                        gBeamLinePlot.repaint();
                    }
                }
            });

            gBeamPlotScrollPane.heightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    if (newValue.intValue() > 100) {
                        gBeamLinePlot.setMyHeight(newValue.intValue() - SCROLLBAR_THICKNESS);
                        gBeamLinePlot.repaint();
                    }
                }
            });

            gBeamLinePlot.preparePanel();
            gBeamPlotScrollPane.setContent(gBeamLinePlot);


            LinePlotBuilder beamShapePlotBuilder = ((PeakShapesTask) service.getPeakShapesTask()).getBeamShapePlotBuilder();

            AbstractDataView beamShapeLinePlot = new BeamShapeLinePlot(
                    new Rectangle(beamShapePlotScrollPane.getWidth(),
                            beamShapePlotScrollPane.getHeight()),
                    (BeamShapeLinePlotBuilder) beamShapePlotBuilder
            );

            beamShapePlotScrollPane.widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    if (newValue.intValue() > 100) {
                        beamShapeLinePlot.setMyWidth(newValue.intValue() - SCROLLBAR_THICKNESS);
                        beamShapeLinePlot.repaint();
                    }
                }
            });

            beamShapePlotScrollPane.heightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    if (newValue.intValue() > 100) {
                        beamShapeLinePlot.setMyHeight(newValue.intValue() - SCROLLBAR_THICKNESS);
                        beamShapeLinePlot.repaint();
                    }
                }
            });

            beamShapeLinePlot.preparePanel();
            beamShapePlotScrollPane.setContent(beamShapeLinePlot);
        });

    }
}