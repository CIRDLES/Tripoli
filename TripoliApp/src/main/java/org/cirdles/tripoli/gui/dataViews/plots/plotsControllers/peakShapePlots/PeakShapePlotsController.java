package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.peakShapePlots;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractDataView;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.BeamShapeLinePlot;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.GBeamLinePlot;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.PeakCentresLinePlot;
import org.cirdles.tripoli.gui.utilities.fileUtilities.FileHandlerUtil;
import org.cirdles.tripoli.plots.AbstractPlotBuilder;
import org.cirdles.tripoli.plots.linePlots.BeamShapeLinePlotBuilder;
import org.cirdles.tripoli.plots.linePlots.GBeamLinePlotBuilder;
import org.cirdles.tripoli.plots.linePlots.LinePlotBuilder;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.peakShapes.BeamDataOutputDriverExperiment;
import org.cirdles.tripoli.utilities.IntuitiveStringComparator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcDemoPlots.MCMCPlotsWindow.*;
import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.peakShapePlots.PeakShapePlotsWindow.plottingWindow;

public class PeakShapePlotsController {

    public static List<File> resourceFilesInFolder;

    public static File resourceBrowserTarget;

    public static String currentGroup;

    public static int currentGroupIndex;

    public static int remSize;

    public static List<ImageView> beamImageSet;

    public static List<ImageView> gBeamImageSet;

    static Map<String, List<File>> resourceGroups;

    ListView<File> listViewOfResourcesInFolder;

    AbstractDataView peakCentreLinePlot;

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
    private ScrollPane peakCentrePlotScrollPane;

    @FXML
    private ScrollPane gBeamPlotScrollPane;

    @FXML
    private AnchorPane eventAnchorPane;

    @FXML
    private ScrollPane eventScrollPane;

    @FXML
    private TextArea eventLogTextArea;

    @FXML
    private ToolBar toolbar;

    @FXML
    private Button browseResourceButton;

    public static String getCurrentGroup() {
        return currentGroup;
    }

    public void setCurrentGroup(String currentGroup) {
        PeakShapePlotsController.currentGroup = currentGroup;
    }

    public static List<File> getResourceGroups(String group) {
        return resourceGroups.get(group);
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


        eventAnchorPane.prefHeightProperty().bind(eventScrollPane.heightProperty());
        eventAnchorPane.prefWidthProperty().bind(eventScrollPane.widthProperty());
        eventLogTextArea.prefHeightProperty().bind(eventAnchorPane.heightProperty());
        eventLogTextArea.prefWidthProperty().bind(eventAnchorPane.widthProperty());


        peakCentrePlotScrollPane.prefHeightProperty().bind(plotsAnchorPane.heightProperty().subtract(300));
        peakCentrePlotScrollPane.prefWidthProperty().bind(masterVBox.widthProperty());


    }

    @FXML
    public void browseResourceFileAction() {
        resourceBrowserTarget = FileHandlerUtil.selectPeakShapeResourceFolderForBrowsing(plottingWindow);
        populateListOfGroups();
    }

    private void populateListOfGroups() {

        resourceFilesInFolder = new ArrayList<>();
        File[] allFiles;
        resourceGroups = new TreeMap<>();

        if (resourceBrowserTarget != null) {
            for (File file : Objects.requireNonNull(resourceBrowserTarget.listFiles((file, name) -> name.toLowerCase().endsWith(".txt")))) {
                try {
                    List<String> contentsByLine = new ArrayList<>(Files.readAllLines(file.toPath(), Charset.defaultCharset()));
                    if (contentsByLine.size() > 5 && (contentsByLine.get(4).startsWith("Peak Centre Mass"))) {
                        resourceFilesInFolder.add(file);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }


        if (!resourceFilesInFolder.isEmpty()) {
            ListView<String> listViewOfGroupResourcesInFolder = new ListView<>();
            listViewOfGroupResourcesInFolder.setCellFactory(
                    (parameter)
                            -> new ResourceDisplayName()
            );
            allFiles = resourceFilesInFolder.toArray(new File[0]);
            eventLogTextArea.textProperty().unbind();
            eventLogTextArea.setText("");

            // Generates a map of groups
            Pattern groupPattern = Pattern.compile("C-(.*?)-S");

            for (File allFile : allFiles) {
                // Checks if substring in filename is already present in map
                Matcher groupMatch = groupPattern.matcher(allFile.getName());
                if (groupMatch.find()) {
                    if (resourceGroups.containsKey(groupMatch.group(1))) {
                        resourceGroups.get(groupMatch.group(1)).add(allFile);
                    } else {
                        resourceGroups.put(groupMatch.group(1), new ArrayList<>());
                        resourceGroups.get(groupMatch.group(1)).add(allFile);
                    }
                }

            }


            IntuitiveStringComparator<String> intuitiveStringComparator = new IntuitiveStringComparator<>();
            for (Map.Entry<String, List<File>> entry : resourceGroups.entrySet()) {
                List<File> files = entry.getValue();
                files.sort((file1, file2) -> intuitiveStringComparator.compare(file1.getName(), file2.getName()));
            }

            ObservableList<String> items = FXCollections.observableArrayList(resourceGroups.keySet().stream().toList());
            listViewOfGroupResourcesInFolder.setItems(items);


            listViewOfGroupResourcesInFolder.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                // Files will be manipulated here when group is selected
                setCurrentGroup(newValue);
                processFilesAndShowPeakCentre(newValue);
                populateListOfResources(newValue);
                eventLogTextArea.textProperty().unbind();
                eventLogTextArea.setText("Select File From Plot");
            });


            listViewOfGroupResourcesInFolder.getSelectionModel().selectFirst();
            listViewOfGroupResourcesInFolder.prefHeightProperty().bind(resourceListAnchorPane.prefHeightProperty());
            listViewOfGroupResourcesInFolder.prefWidthProperty().bind(resourceListAnchorPane.prefWidthProperty());
            resourceListAnchorPane.getChildren().add(listViewOfGroupResourcesInFolder);
            eventLogTextArea.setText("Select File From Plot");

        } else {
            eventLogTextArea.textProperty().unbind();
            eventLogTextArea.setText("No valid resources");


            resourceListAnchorPane.getChildren().removeAll();
            eventAnchorPane.getChildren().removeAll();


            gBeamPlotScrollPane.setContent(null);
            beamShapePlotScrollPane.setContent(null);
            peakCentrePlotScrollPane.setContent(null);

        }

    }

    public void processFilesAndShowPeakCentre(String groupValue) {

        double[] finalYAxis;
        double[] finalXAxis;
        beamImageSet = new ArrayList<>();
        gBeamImageSet = new ArrayList<>();

        if (plotsAnchorPane.getChildren().size() > 1) {
            plotsAnchorPane.getChildren().remove(1, remSize);
        }
        remSize = 1;

        double[] xAxis = new double[resourceGroups.get(groupValue).size()];
        double[] yAxis = new double[resourceGroups.get(groupValue).size()];
        for (int k = 0; k < resourceGroups.get(groupValue).size(); k++) {
            resourceBrowserTarget = resourceGroups.get(groupValue).get(k);
            if (resourceBrowserTarget != null && resourceBrowserTarget.isFile()) {
                try {
                    AbstractPlotBuilder[] plots = BeamDataOutputDriverExperiment.modelTest(resourceBrowserTarget.toPath(), this::processFilesAndShowPeakCentre);
                    xAxis[k] = k + 1;
                    yAxis[k] = BeamDataOutputDriverExperiment.getMeasBeamWidthAMU();
                    AbstractDataView gBeamLinePlot = new GBeamLinePlot(
                            new Rectangle(gBeamPlotScrollPane.getWidth(),
                                    gBeamPlotScrollPane.getHeight()),
                            (GBeamLinePlotBuilder) plots[1]
                    );

                    AbstractDataView beamShapeLinePlot = new BeamShapeLinePlot(
                            new Rectangle(beamShapePlotScrollPane.getWidth(),
                                    beamShapePlotScrollPane.getHeight()),
                            (BeamShapeLinePlotBuilder) plots[0]
                    );

                    gBeamLinePlot.preparePanel();
                    beamShapeLinePlot.preparePanel();

                    // Creates a rendered image of the beam shape and g-beam line plots
                    WritableImage writableImage1 = new WritableImage((int) beamShapeLinePlot.getWidth(), (int) beamShapeLinePlot.getHeight());
                    beamShapeLinePlot.snapshot(null, writableImage1);
                    ImageView image1 = new ImageView(writableImage1);
                    image1.setFitWidth(95);
                    image1.setFitHeight(51);
                    //image1.setPreserveRatio(true);
                    //RenderedImage renderedImage1 = SwingFXUtils.fromFXImage(image1.snapshot(null, null), null);

                    WritableImage writableImage2 = new WritableImage((int) gBeamLinePlot.getWidth(), (int) gBeamLinePlot.getHeight());
                    gBeamLinePlot.snapshot(null, writableImage2);
                    ImageView image2 = new ImageView(writableImage2);
                    image2.setFitWidth(95);
                    image2.setFitHeight(51);
                    //image2.setPreserveRatio(true);
                    //RenderedImage renderedImage2 = SwingFXUtils.fromFXImage(image2.snapshot(null, null), null);

                    // adds the rendered images to a list that will be used later
                    beamImageSet.add(image1);
                    gBeamImageSet.add(image2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        finalYAxis = yAxis;
        finalXAxis = xAxis;

        LinePlotBuilder peakCentrePlotBuilder = LinePlotBuilder.initializeLinePlot(finalXAxis, finalYAxis, "PeakCentre Plot", "", "");

        peakCentreLinePlot = new PeakCentresLinePlot(new Rectangle(peakCentrePlotScrollPane.getWidth(), peakCentrePlotScrollPane.getHeight()), peakCentrePlotBuilder);

        peakCentrePlotScrollPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            peakCentreLinePlot.setMyWidth(newValue.intValue());
            peakCentreLinePlot.repaint();
        });

        peakCentrePlotScrollPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            peakCentreLinePlot.setMyHeight(newValue.intValue());
            peakCentreLinePlot.repaint();
        });

        peakCentreLinePlot.preparePanel();
        peakCentrePlotScrollPane.setContent(peakCentreLinePlot);
        int size = 1;
        for (int i = 0; i < gBeamImageSet.size(); i++) {
            plotsAnchorPane.getChildren().add(gBeamImageSet.get(i));
            ImageView pos = (ImageView) plotsAnchorPane.getChildren().get(i + 1);
            pos.setX(peakCentreLinePlot.mapX(peakCentreLinePlot.getxAxisData()[i]) - 35);
            pos.setY(210);
            pos.setVisible(false);
            size++;
            remSize++;


        }
        for (int i = 0; i < beamImageSet.size(); i++) {
            plotsAnchorPane.getChildren().add(beamImageSet.get(i));
            ImageView pos = (ImageView) plotsAnchorPane.getChildren().get(size + i);
            pos.setX(peakCentreLinePlot.mapX(peakCentreLinePlot.getxAxisData()[i]) - 35);
            pos.setY(250);
            pos.setVisible(false);
            remSize++;
        }


        // Selects file from peakCentre plot
        peakCentrePlotScrollPane.setOnMouseClicked(click -> {
            peakCentreLinePlot.getOnMouseClicked();
            processDataFileAndShowPlotsOfPeakShapes();

            listViewOfResourcesInFolder.getSelectionModel().select(currentGroupIndex);
        });

        int finalSize = size;

        peakCentrePlotScrollPane.setOnMouseMoved(mouse -> {


            int index = (int) peakCentreLinePlot.convertMouseXToValue(mouse.getX());


            if (peakCentreLinePlot.mouseInHouse(mouse) && index >= 1 && mouse.getY() > 10) {
                for (int i = 0; i < peakCentreLinePlot.getxAxisData().length; i++) {
                    ImageView pos1 = (ImageView) plotsAnchorPane.getChildren().get(finalSize + (i));
                    pos1.setVisible(false);
                    ImageView pos2 = (ImageView) plotsAnchorPane.getChildren().get(i + 1);
                    pos2.setVisible(false);
                    if (peakCentreLinePlot.getxAxisData()[i] == index) {
                        pos1 = (ImageView) plotsAnchorPane.getChildren().get(finalSize + (index - 1));
                        pos1.setVisible(true);
                        pos2 = (ImageView) plotsAnchorPane.getChildren().get(index);
                        pos2.setVisible(true);
                    }

                }


            } else {
                for (int i = 1; i < plotsAnchorPane.getChildren().size(); i++) {
                    plotsAnchorPane.getChildren().get(i).setVisible(false);
                }
            }

        });
    }

    private void populateListOfResources(String groupValue) {
        listViewOfResourcesInFolder = new ListView<>();
        listViewOfResourcesInFolder.setCellFactory(param -> new ResourceDisplayName2());
        eventLogTextArea.textProperty().unbind();
        int initialIndex;

        ObservableList<File> items = FXCollections.observableArrayList(resourceGroups.get(groupValue));
        listViewOfResourcesInFolder.setItems(items);


        listViewOfResourcesInFolder.setOnMouseClicked(click -> {
            peakCentreLinePlot.repaint();
            int index;
            if (click.getClickCount() == 1) {
                resourceBrowserTarget = listViewOfResourcesInFolder.getSelectionModel().getSelectedItem();
                index = listViewOfResourcesInFolder.getSelectionModel().getSelectedIndex();
                peakCentreLinePlot.getGraphicsContext2D().setLineWidth(1.0);
                peakCentreLinePlot.getGraphicsContext2D().strokeOval(peakCentreLinePlot.mapX(peakCentreLinePlot.getxAxisData()[index]) - 6, peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[index]) - 6, 12, 12);
                processDataFileAndShowPlotsOfPeakShapes();
            }
        });

        listViewOfResourcesInFolder.setOnKeyPressed(key -> {
            peakCentreLinePlot.repaint();
            int index;
            if (key.getCode() == KeyCode.DOWN || key.getCode() == KeyCode.UP) {
                resourceBrowserTarget = listViewOfResourcesInFolder.getSelectionModel().getSelectedItem();
                index = listViewOfResourcesInFolder.getSelectionModel().getSelectedIndex();
                processDataFileAndShowPlotsOfPeakShapes();
                peakCentreLinePlot.getGraphicsContext2D().setLineWidth(1.0);
                peakCentreLinePlot.getGraphicsContext2D().strokeOval(peakCentreLinePlot.mapX(peakCentreLinePlot.getxAxisData()[index]) - 6, peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[index]) - 6, 12, 12);
            }
        });

        listViewOfResourcesInFolder.getSelectionModel().selectFirst();
        initialIndex = listViewOfResourcesInFolder.getSelectionModel().getSelectedIndex();
        resourceBrowserTarget = listViewOfResourcesInFolder.getSelectionModel().getSelectedItem();
        peakCentreLinePlot.getGraphicsContext2D().setLineWidth(1.0);
        peakCentreLinePlot.getGraphicsContext2D().strokeOval(peakCentreLinePlot.mapX(peakCentreLinePlot.getxAxisData()[initialIndex]) - 6, peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[initialIndex]) - 6, 12, 12);
        processDataFileAndShowPlotsOfPeakShapes();

        listViewOfResourcesInFolder.prefHeightProperty().bind(eventAnchorPane.heightProperty());
        listViewOfResourcesInFolder.prefWidthProperty().bind(eventAnchorPane.widthProperty());
        eventAnchorPane.getChildren().add(listViewOfResourcesInFolder);
    }


    public void processDataFileAndShowPlotsOfPeakShapes() {


        if (resourceBrowserTarget != null && resourceBrowserTarget.isFile()) {
            final PeakShapesService service = new PeakShapesService(resourceBrowserTarget.toPath());
            eventLogTextArea.textProperty().bind(service.valueProperty());
            try {
                AbstractPlotBuilder[] plots = BeamDataOutputDriverExperiment.modelTest(resourceBrowserTarget.toPath(), this::processFilesAndShowPeakCentre);

                AbstractDataView gBeamLinePlot = new GBeamLinePlot(
                        new Rectangle(gBeamPlotScrollPane.getWidth(),
                                gBeamPlotScrollPane.getHeight()),
                        (GBeamLinePlotBuilder) plots[1]
                );

                gBeamPlotScrollPane.widthProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue.intValue() > 100) {
                        gBeamLinePlot.setMyWidth(newValue.intValue() - SCROLLBAR_THICKNESS);
                        gBeamLinePlot.repaint();
                    }
                });

                gBeamPlotScrollPane.heightProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue.intValue() > 100) {
                        gBeamLinePlot.setMyHeight(newValue.intValue() - SCROLLBAR_THICKNESS);
                        gBeamLinePlot.repaint();
                    }
                });

                gBeamLinePlot.preparePanel();
                gBeamPlotScrollPane.setContent(gBeamLinePlot);


                AbstractDataView beamShapeLinePlot = new BeamShapeLinePlot(
                        new Rectangle(beamShapePlotScrollPane.getWidth(),
                                beamShapePlotScrollPane.getHeight()),
                        (BeamShapeLinePlotBuilder) plots[0]
                );

                beamShapePlotScrollPane.widthProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue.intValue() > 100) {
                        beamShapeLinePlot.setMyWidth(newValue.intValue() - SCROLLBAR_THICKNESS);
                        beamShapeLinePlot.repaint();
                    }
                });

                beamShapePlotScrollPane.heightProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue.intValue() > 100) {
                        beamShapeLinePlot.setMyHeight(newValue.intValue() - SCROLLBAR_THICKNESS);
                        beamShapeLinePlot.repaint();
                    }
                });

                beamShapeLinePlot.preparePanel();
                beamShapeLinePlot.getGraphicsContext2D();


                beamShapePlotScrollPane.setContent(beamShapeLinePlot);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            eventLogTextArea.textProperty().unbind();
            eventLogTextArea.setText("Please Choose Folder");
        }

    }

    static class ResourceDisplayName2 extends ListCell<File> {

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

    static class ResourceDisplayName extends ListCell<String> {

        @Override
        protected void updateItem(String resource, boolean empty) {

            super.updateItem(resource, empty);
            if (resource == null || empty) {
                setText(null);
            } else {
                setText(resource);
            }
        }
    }


}