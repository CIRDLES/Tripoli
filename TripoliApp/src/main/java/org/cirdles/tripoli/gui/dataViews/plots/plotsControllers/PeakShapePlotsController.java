package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractDataView;
import org.cirdles.tripoli.gui.dataViews.plots.BeamShapeLinePlot;
import org.cirdles.tripoli.gui.dataViews.plots.GBeamLinePlot;
import org.cirdles.tripoli.utilities.file.FileUtilities;
import org.cirdles.tripoli.visualizationUtilities.AbstractPlotBuilder;
import org.cirdles.tripoli.visualizationUtilities.linePlots.BeamShapeLinePlotBuilder;
import org.cirdles.tripoli.visualizationUtilities.linePlots.GBeamLinePlotBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static org.cirdles.tripoli.gui.dataViews.plots.PeakShapePlotsWindow.plottingWindow;
import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.RJMCMCPlots.RJMCMCPlotsWindow.*;

public class PeakShapePlotsController {

    public static List<File> resourceFilesInFolder;

    public static File resourceBrowserTarget;

    public static String resourceBrowserType = ".txt";

    private ListView<File> listViewOfResourcesInFolder;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;
    @FXML
    private ScrollPane resourceListScrollPane;

    @FXML
    private AnchorPane resourceListAnchorPane;

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
    private Button demo2Button;

    @FXML
    private Button browseResourceButton;

    @FXML
    void demo2ButtonAction(ActionEvent event) throws IOException {
        processDataFileAndShowPlotsOfPeakShapes();
        ((Button) event.getSource()).setDisable(true);
    }

    @FXML
    void initialize() {

        masterVBox.setPrefSize(PLOT_WINDOW_WIDTH, PLOT_WINDOW_HEIGHT);
        toolbar.setPrefSize(PLOT_WINDOW_WIDTH, 20.0);

        populateListOfResources();
        gBeamPlotScrollPane.setPrefSize(PLOT_WINDOW_WIDTH, PLOT_WINDOW_HEIGHT - toolbar.getHeight());
        gBeamPlotScrollPane.setPrefViewportWidth(PLOT_WINDOW_WIDTH - SCROLLBAR_THICKNESS);
        gBeamPlotScrollPane.setPrefViewportHeight(gBeamPlotScrollPane.getPrefHeight() - SCROLLBAR_THICKNESS);

        masterVBox.prefWidthProperty().bind(plotsAnchorPane.widthProperty());
        masterVBox.prefHeightProperty().bind(plotsAnchorPane.heightProperty());

        gBeamPlotScrollPane.prefWidthProperty().bind(masterVBox.widthProperty());
        gBeamPlotScrollPane.prefHeightProperty().bind(masterVBox.heightProperty().subtract(toolbar.getHeight()));


        resourceListAnchorPane.prefHeightProperty().bind(resourceListScrollPane.heightProperty());
        resourceListAnchorPane.prefWidthProperty().bind(resourceListScrollPane.widthProperty());
        listViewOfResourcesInFolder.prefHeightProperty().bind(resourceListAnchorPane.prefHeightProperty());
        listViewOfResourcesInFolder.prefWidthProperty().bind(resourceListAnchorPane.prefWidthProperty());

    }

    public void processDataFileAndShowPlotsOfPeakShapes() throws IOException {
//        ResourceExtractor RESOURCE_EXTRACTOR = new ResourceExtractor(Tripoli.class);
//        Path dataFile = RESOURCE_EXTRACTOR
//               .extractResourceAsFile(String.valueOf(resourceBrowserTarget)).toPath();
        if (resourceBrowserTarget != null && resourceBrowserTarget.isFile()) {
            final PeakShapesService service = new PeakShapesService(resourceBrowserTarget.toPath());
            eventLogTextArea.textProperty().bind(service.valueProperty());
            service.start();
            service.setOnSucceeded(evt -> {
                AbstractPlotBuilder gBeamPlotBuilder = ((PeakShapesTask) service.getPeakShapesTask()).getGBeamPlotBuilder();

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


                AbstractPlotBuilder beamShapePlotBuilder = ((PeakShapesTask) service.getPeakShapesTask()).getBeamShapePlotBuilder();

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
        } else {
            eventLogTextArea.setText("Please Choose File");
        }

    }

    @FXML
    private void browseResourceFileAction(ActionEvent event) {
        resourceBrowserTarget = FileUtilities.selectPeakShapeResourceFileForBrowsing(plottingWindow);
        demo2Button.setDisable(false);
    }

    private void populateListOfResources() {

        resourceFilesInFolder = new ArrayList<>();
        resourceBrowserTarget = new File("TripoliResources/PeakCentres");

        // Filter has not been applied yet
        if (resourceBrowserTarget != null) {
            if (resourceBrowserType.compareToIgnoreCase(".txt") == 0) {
                resourceFilesInFolder.addAll(Arrays.asList(Objects.requireNonNull(resourceBrowserTarget.listFiles())));
            }
        }

        if (resourceFilesInFolder.isEmpty()) {
            if (resourceBrowserType.compareToIgnoreCase(".txt") == 0) {
                eventLogTextArea.setText("No Valid Resources");
            }
        } else {
            listViewOfResourcesInFolder = new ListView<>();
            listViewOfResourcesInFolder.setCellFactory(
                    (parameter)
                            -> new ResourceDisplayName()
            );


            ObservableList<File> items = FXCollections.observableArrayList(resourceFilesInFolder);
            listViewOfResourcesInFolder.setItems(items);

            listViewOfResourcesInFolder.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<File>() {
                @Override
                public void changed(ObservableValue<? extends File> observable, File oldValue, File newValue) {
                    resourceBrowserTarget = newValue;
                    demo2Button.setDisable(false);
                }
            });

            if (resourceFilesInFolder.size() > 0) {
                listViewOfResourcesInFolder.getSelectionModel().selectFirst();
            }

            resourceListAnchorPane.getChildren().add(listViewOfResourcesInFolder);
        }

    }

    static class ResourceDisplayName extends ListCell<File> {

        @Override
        protected void updateItem(File resource, boolean empty) {
            super.updateItem(resource, empty);
            if (resource == null || empty) {
                setText(null);
            } else {
                setText(resource.getName());
            }
        }
    }
}
