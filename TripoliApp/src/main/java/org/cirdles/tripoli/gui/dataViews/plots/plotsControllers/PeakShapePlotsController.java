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
import org.cirdles.tripoli.utilities.IntuitiveStringComparator;
import org.cirdles.tripoli.utilities.file.FileUtilities;
import org.cirdles.tripoli.visualizationUtilities.AbstractPlotBuilder;
import org.cirdles.tripoli.visualizationUtilities.linePlots.BeamShapeLinePlotBuilder;
import org.cirdles.tripoli.visualizationUtilities.linePlots.GBeamLinePlotBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        File[] allFiles;
        ArrayList<ArrayList<File>> resourceGroups = new ArrayList<>();

        if (resourceBrowserTarget != null && (resourceBrowserType.compareToIgnoreCase(".txt") == 0)) {
            for (File file : Objects.requireNonNull(resourceBrowserTarget.listFiles(File::isFile))) {
                if (file.getName().endsWith(".txt") || file.getName().endsWith(".TXT")) {
                    try {
                        List<String> contentsByLine = new ArrayList<>(Files.readAllLines(file.toPath(), Charset.defaultCharset()));
                        List<String[]> headerLine = new ArrayList<>();

                        for (String line : contentsByLine) {
                            if (!line.isEmpty()) {
                                headerLine.add(line.split("\\s*,\\s*"));
                            } else {
                                break;
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


            // Sorted by date within file
            resourceFilesInFolder.sort(new Comparator<File>() {
                DateFormat f = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

                @Override
                public int compare(File o1, File o2) {
                    try {
                        String file1 = new BufferedReader(new FileReader(o1)).readLine().replaceFirst("Timestamp, ", "");
                        String file2 = new BufferedReader(new FileReader(o2)).readLine().replaceFirst("Timestamp, ", "");
                        return f.parse(file1).compareTo(f.parse(file2));
                    } catch (IOException | ParseException e) {
                        e.printStackTrace();
                    }
                    return 0;
                }
            });

            if (resourceFilesInFolder.get(0).getName().contains("NBS987")) {
                // Working on grouping and sorting
                allFiles = resourceFilesInFolder.toArray(new File[resourceFilesInFolder.size()]);

                // Generates a map of groups
                Map<Integer, String> fileGroups = new HashMap<>();
                Pattern groupPattern = Pattern.compile("C-(.*?)-S");
                Pattern endPattern = Pattern.compile("-(.*?).");

                int j = 0;
                for (int i = 0; i < allFiles.length; i++) {
                    // Checks if substring in filename is already present in map
                    Matcher groupMatch = groupPattern.matcher(allFiles[i].getName());
                    if (groupMatch.find()) {
                        if (!fileGroups.containsValue(groupMatch.group(1))) {
                            fileGroups.put(j, groupMatch.group(1));
                            j++;
                        }
                    }

                }

                // Generates groups of list that contain a list of files
                for (int i = 0; i < fileGroups.size(); i++) {
                    resourceGroups.add(new ArrayList<>());
                }

                for (int i = 0; i < resourceGroups.size(); i++) {
                    for (int k = 0; k < allFiles.length; k++) {
                        // Adds to list according to key and value in fileGroups
                        Matcher matcher = groupPattern.matcher(allFiles[k].getName());
                        if (matcher.find()) {
                            if (matcher.group(1).equals(fileGroups.get(i))) {
                                resourceGroups.get(i).add(allFiles[k]);
                            }
                        }
                    }
                }


                IntuitiveStringComparator<String> intuitiveStringComparator = new IntuitiveStringComparator<>();
                for (int i = 0; i < resourceGroups.size(); i++) {
                    resourceGroups.get(i).sort(new Comparator<File>() {

                        @Override
                        public int compare(File o1, File o2) {
                            Matcher endMatch1 = endPattern.matcher(o1.getName());
                            Matcher endMatch2 = endPattern.matcher(o2.getName());
                            if (endMatch1.find() && endMatch2.find()) {
                                return intuitiveStringComparator.compare(endMatch1.group(1), endMatch2.group(1));
                            }
                            return 0;
                        }
                    });
                }

                for (int i = 0; i < fileGroups.size(); i++) {
                    System.out.println("Group: " + fileGroups.get(i));
                    for (int k = 0; k < resourceGroups.get(i).size(); k++) {
                        System.out.println("File: " + resourceGroups.get(i).get(k));
                    }
                    System.out.println();
                }


            }

            ObservableList<File> items = FXCollections.observableArrayList(resourceFilesInFolder);
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
                            } else {
                                break;
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
