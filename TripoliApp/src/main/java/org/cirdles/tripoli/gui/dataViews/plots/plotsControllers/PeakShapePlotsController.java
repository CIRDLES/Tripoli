package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
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
import java.nio.charset.Charset;
import java.nio.file.Files;
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
    void demo2ButtonAction(ActionEvent event) {
        if (resourceBrowserTarget != null) {
            processDataFileAndShowPlotsOfPeakShapes();
            ((Button) event.getSource()).setDisable(true);
        } else {
            eventLogTextArea.setText("No file has been selected");
        }
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


        resourceListAnchorPane.prefHeightProperty().bind(resourceListScrollPane.heightProperty());
        resourceListAnchorPane.prefWidthProperty().bind(resourceListScrollPane.widthProperty());


    }

    public void processDataFileAndShowPlotsOfPeakShapes() {
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

                gBeamPlotScrollPane.widthProperty().addListener(new ChangeListener<>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        if (newValue.intValue() > 100) {
                            gBeamLinePlot.setMyWidth(newValue.intValue() - SCROLLBAR_THICKNESS);
                            gBeamLinePlot.repaint();
                        }
                    }
                });

                gBeamPlotScrollPane.heightProperty().addListener(new ChangeListener<>() {
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

                beamShapePlotScrollPane.widthProperty().addListener(new ChangeListener<>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        if (newValue.intValue() > 100) {
                            beamShapeLinePlot.setMyWidth(newValue.intValue() - SCROLLBAR_THICKNESS);
                            beamShapeLinePlot.repaint();
                        }
                    }
                });

                beamShapePlotScrollPane.heightProperty().addListener(new ChangeListener<>() {
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
            eventLogTextArea.textProperty().unbind();
            eventLogTextArea.setText("Please Choose Folder");
        }

    }

    @FXML
    public void browseResourceFileAction() {
        resourceBrowserTarget = FileUtilities.selectPeakShapeResourceFolderForBrowsing(plottingWindow);
        populateListOfResources();
    }

    private void populateListOfResources() {

        resourceFilesInFolder = new ArrayList<>();
        // resourceBrowserTarget = new File("TripoliResources/PeakCentres");


        if (resourceBrowserTarget != null && (resourceBrowserType.compareToIgnoreCase(".txt") == 0)) {
            for (File file : Objects.requireNonNull(resourceBrowserTarget.listFiles(File::isFile))) {
                try {
                    List<String> contentsByLine = new ArrayList<>(Files.readAllLines(file.toPath(), Charset.defaultCharset()));
                    List<String[]> headerLine = new ArrayList<>();

                    for (String line : contentsByLine) {
                        if (!line.isEmpty()) {
                            headerLine.add(line.split("\\s*,\\s*"));

                            if (line.startsWith("#START")) {
                                break;
                            }
                        }
                    }
                    // Filters out files
                    if (headerLine.size() >= 2 && (headerLine.get(0)[0].equalsIgnoreCase("timestamp")) && ((headerLine.get(1)[1].equalsIgnoreCase("PhotoMultiplier") || headerLine.get(1)[1].equalsIgnoreCase("Axial")))) {
                        resourceFilesInFolder.add(file);
                    } else {
                        eventLogTextArea.textProperty().unbind();
                        eventLogTextArea.setText("No valid resources");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } else {
            eventLogTextArea.textProperty().unbind();
            eventLogTextArea.setText("No valid resources");
        }
        // Checks if there are no files in folder
        if (resourceFilesInFolder.isEmpty()) {
            if (resourceBrowserType.compareToIgnoreCase(".txt") == 0) {
                eventLogTextArea.textProperty().unbind();
                eventLogTextArea.setText("No valid resources");
                resourceListAnchorPane.getChildren().remove(listViewOfResourcesInFolder);
            }
        } else {
            listViewOfResourcesInFolder = new ListView<>();
            listViewOfResourcesInFolder.setCellFactory(
                    (parameter)
                            -> new ResourceDisplayName()
            );


            ObservableList<File> items = FXCollections.observableArrayList(resourceFilesInFolder);
            // Sorts by file last modified
            // Sort by date within file not implemented yet
            items.sort(Comparator.comparing(File::lastModified));
            listViewOfResourcesInFolder.setItems(items);

            // Selects the file and list and displays details of selected file
            listViewOfResourcesInFolder.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
                @Override
                public void changed(ObservableValue<? extends File> observable, File oldValue, File newValue) {
                    resourceBrowserTarget = newValue;
                    try {
                        List<String> contentsByLine = new ArrayList<>(Files.readAllLines(resourceBrowserTarget.toPath(), Charset.defaultCharset()));
                        List<String[]> headerLine = new ArrayList<>();

                        for (String line : contentsByLine) {
                            if (!line.isEmpty()) {
                                headerLine.add(line.split("\\s*,\\s*"));

                                if (line.startsWith("#START")) {
                                    break;
                                }
                            }
                        }

                        String collector = headerLine.get(1)[1];
                        String massID = headerLine.get(2)[1];
                        String initialMass = headerLine.get(3)[1];
                        String peakCentreMass = headerLine.get(4)[1];
                        eventLogTextArea.textProperty().unbind();
                        eventLogTextArea.setText("Collector: " + collector + "\n" + "Mass ID: " + massID + "\n" + "Initial Mass: " + initialMass + "\n" +
                                "Peak Centre Mass: " + peakCentreMass);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    demo2Button.setDisable(false);

                }
            });

            listViewOfResourcesInFolder.setOnMouseClicked(new EventHandler<>() {
                @Override
                public void handle(MouseEvent click) {
                    if (click.getClickCount() == 2) {
                        resourceBrowserTarget = listViewOfResourcesInFolder.getSelectionModel().getSelectedItem();
                        processDataFileAndShowPlotsOfPeakShapes();
                    }
                }
            });

            if (resourceFilesInFolder.size() > 0) {
                listViewOfResourcesInFolder.getSelectionModel().selectFirst();
            }

            listViewOfResourcesInFolder.prefHeightProperty().bind(resourceListAnchorPane.prefHeightProperty());
            listViewOfResourcesInFolder.prefWidthProperty().bind(resourceListAnchorPane.prefWidthProperty());
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
