package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.peakShapePlots;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.PlotWallPane;
import org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.BeamShapeLinePlot;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.BeamShapeLinePlotX;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.GBeamLinePlot;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.PeakShapesOverlayPlot;
import org.cirdles.tripoli.gui.utilities.fileUtilities.FileHandlerUtil;
import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.plots.linePlots.*;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.peakShapes.BeamShapeTestDriver;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.peakShapes.PeakShapeOutputDataRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.phoenix.PeakShapeProcessor_PhoenixTextFile;
import org.cirdles.tripoli.utilities.IntuitiveStringComparator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotHeight;
import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotWidth;
import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots.MCMCPlotsWindow.PLOT_WINDOW_HEIGHT;
import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots.MCMCPlotsWindow.PLOT_WINDOW_WIDTH;
import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.peakShapePlots.PeakShapePlotsWindow.plottingWindow;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.MassSpectrometerBuiltinModelFactory.massSpectrometerModelBuiltinMap;

public class PeakShapeDemoPlotsControllerTest {
    public static List<File> resourceFilesInFolder;

    public static File resourceBrowserTarget;

    public static String currentGroup;

    public static int currentGroupIndex;

    public static int remSize;

    public static List<ImageView> beamImageSet;

    public static List<ImageView> gBeamImageSet;

    static Map<String, List<File>> resourceGroups;
    public Pane wallPlotsAnchorPane;

    ListView<File> listViewOfResourcesInFolder;

    AbstractPlot peakCentreLinePlot;

    PlotWallPane ensemblePlotsWallPane = (PlotWallPane) PlotWallPane.createPlotWallPane(null, null, null, null);
    @FXML
    private ScrollPane resourceListScrollPane;

    @FXML
    private AnchorPane plotsAnchorPane;

    @FXML
    private VBox masterVBox;

    @FXML
    private ToolBar toolbar;

    @FXML
    private AnchorPane eventAnchorPane;

    @FXML
    private ScrollPane eventScrollPane;

    @FXML
    private TextArea eventLogTextArea;


    public static String getCurrentGroup() {
        return currentGroup;
    }

    public void setCurrentGroup(String currentGroup) {
        PeakShapeDemoPlotsControllerTest.currentGroup = currentGroup;
    }

    public static List<File> getResourceGroups(String group) {
        return resourceGroups.get(group);
    }

    @FXML
    void initialize() {
        masterVBox.setPrefSize(PLOT_WINDOW_WIDTH, PLOT_WINDOW_HEIGHT);
        toolbar.setPrefSize(PLOT_WINDOW_WIDTH, 30.0);

        masterVBox.prefWidthProperty().bind(plotsAnchorPane.widthProperty());
        masterVBox.prefHeightProperty().bind(plotsAnchorPane.heightProperty());

        wallPlotsAnchorPane.prefWidthProperty().bind(masterVBox.widthProperty());
        wallPlotsAnchorPane.prefHeightProperty().bind(masterVBox.heightProperty());


        eventAnchorPane.prefHeightProperty().bind(eventScrollPane.heightProperty());
        eventAnchorPane.prefWidthProperty().bind(eventScrollPane.widthProperty());
        eventLogTextArea.prefHeightProperty().bind(eventAnchorPane.heightProperty());
        eventLogTextArea.prefWidthProperty().bind(eventAnchorPane.widthProperty());


    }


    @FXML
    public void browseResourceFileAction() {
        resourceBrowserTarget = FileHandlerUtil.selectPeakShapeResourceFolderForBrowsing(plottingWindow);
        if (resourceBrowserTarget == null) {
            System.out.println("File not chosen");
        } else {
            populateListOfGroups();
        }

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
                            -> new PeakShapeDemoPlotsControllerTest.ResourceDisplayName()
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


            listViewOfGroupResourcesInFolder.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                // Files will be manipulated here when group is selected
                setCurrentGroup(newValue);
                processFilesAndShowPeakCentre(newValue);
                populateListOfResources(newValue);

                eventLogTextArea.textProperty().unbind();
                eventLogTextArea.setText("Select File From Plot");
            });

            listViewOfGroupResourcesInFolder.setItems(items);
            listViewOfGroupResourcesInFolder.getSelectionModel().selectFirst();
            listViewOfGroupResourcesInFolder.prefWidthProperty().bind(resourceListScrollPane.widthProperty());
            listViewOfGroupResourcesInFolder.prefHeightProperty().bind(resourceListScrollPane.heightProperty());
            resourceListScrollPane.setContent(listViewOfGroupResourcesInFolder);
            eventLogTextArea.textProperty().unbind();
            eventLogTextArea.setText("Select File From Plot");

        } else {
            eventLogTextArea.textProperty().unbind();
            eventLogTextArea.setText("No valid resources");


            eventAnchorPane.getChildren().removeAll();

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
            // This can be changed to implement only image of peak shapes overlay
            if (resourceBrowserTarget != null && resourceBrowserTarget.isFile()) {
                try {
                    PlotBuilder[] plots = BeamShapeTestDriver.modelTest(resourceBrowserTarget.toPath(), this::populateListOfResources);
                    xAxis[k] = k + 1;
                    yAxis[k] = BeamShapeTestDriver.getMeasBeamWidthAMU();
                    AbstractPlot gBeamLinePlot = GBeamLinePlot.generatePlot(
                            new Rectangle(minPlotWidth,
                                    minPlotHeight),
                            (GBeamLinePlotBuilder) plots[1]
                    );

                    AbstractPlot beamShapeLinePlot = BeamShapeLinePlot.generatePlot(
                            new Rectangle(minPlotWidth,
                                    minPlotHeight),
                            (BeamShapeLinePlotBuilder) plots[0]
                    );

                    gBeamLinePlot.preparePanel(true, true);
                    beamShapeLinePlot.preparePanel(true, true);

                    // Creates a rendered image of the beam shape and g-beam line plots
                    WritableImage writableImage1 = new WritableImage((int) beamShapeLinePlot.getWidth(), (int) beamShapeLinePlot.getHeight());
                    beamShapeLinePlot.snapshot(null, writableImage1);
                    ImageView image1 = new ImageView(writableImage1);
                    image1.setFitWidth(85);
                    image1.setFitHeight(46);

                    WritableImage writableImage2 = new WritableImage((int) gBeamLinePlot.getWidth(), (int) gBeamLinePlot.getHeight());
                    gBeamLinePlot.snapshot(null, writableImage2);
                    ImageView image2 = new ImageView(writableImage2);
                    image2.setFitWidth(85);
                    image2.setFitHeight(46);

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
        if (wallPlotsAnchorPane.getChildren().size() == 0) {
            wallPlotsAnchorPane.getChildren().add(ensemblePlotsWallPane);
        } else {
            ensemblePlotsWallPane.getChildren().clear();
        }

        // Attempted to set up thumbnails
//        LinePlotBuilder peakCentrePlotBuilder = LinePlotBuilder.initializeLinePlot(finalXAxis, finalYAxis, new String[]{"PeakCentre Plot"}, "Blocks", "Peak Widths");
//        ensemblePlotsWallPane.buildToolBar();
//        ensemblePlotsWallPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));
//
//        peakCentreLinePlot = producePeakCentrePlot(peakCentrePlotBuilder, ensemblePlotsWallPane);


//        peakCentreGridPane.widthProperty().addListener((observable, oldValue, newValue) -> {
//            peakCentreLinePlot.setWidthF(newValue.intValue());
//            peakCentreLinePlot.repaint();
//        });

//        peakCentreGridPane.heightProperty().addListener((observable, oldValue, newValue) -> {
//            peakCentreLinePlot.setHeightF(newValue.intValue());
//            peakCentreLinePlot.repaint();
//            if (plotsAnchorPane.getChildren().size() > 1) {
//                plotsAnchorPane.getChildren().remove(1, remSize);
//            }
//            remSize = 1;
//
//            int size = 1;
//            for (int i = 0; i < gBeamImageSet.size(); i++) {
//                plotsAnchorPane.getChildren().add(gBeamImageSet.get(i));
//                ImageView pos = (ImageView) plotsAnchorPane.getChildren().get(i + 1);
//                pos.setX(peakCentreLinePlot.mapX(peakCentreLinePlot.getxAxisData()[i]) - 35);
//                if ((peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[i]) + 160) < 200) {
//                    pos.setY(peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[i]) + 300);
//                } else {
//                    pos.setY(peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[i]) + 155);
//                }
//
//                pos.setVisible(false);
//                size++;
//                remSize++;
//
//
//            }
//            for (int i = 0; i < beamImageSet.size(); i++) {
//                plotsAnchorPane.getChildren().add(beamImageSet.get(i));
//                ImageView pos = (ImageView) plotsAnchorPane.getChildren().get(size + i);
//                pos.setX(peakCentreLinePlot.mapX(peakCentreLinePlot.getxAxisData()[i]) - 35);
//                if ((peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[i]) + 160) < 200) {
//                    pos.setY(peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[i]) + 260);
//                } else {
//                    pos.setY(peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[i]) + 195);
//                }
//                pos.setVisible(false);
//                remSize++;
//            }
//        });

        //peakCentreLinePlot.preparePanel();
//        peakCentreGridPane.add(peakCentreLinePlot, 0, 0);
//        int size = 1;
//        for (int i = 0; i < gBeamImageSet.size(); i++) {
//            plotsAnchorPane.getChildren().add(gBeamImageSet.get(i));
//            ImageView pos = (ImageView) plotsAnchorPane.getChildren().get(i + 1);
//            pos.setX(peakCentreLinePlot.mapX(peakCentreLinePlot.getxAxisData()[i]) - 35);
//            if ((peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[i]) + 160) < 220) {
//                pos.setY(peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[i]) + 300);
//            } else {
//                pos.setY(peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[i]) + 155);
//            }
//
//            pos.setVisible(false);
//            size++;
//            remSize++;
//
//
//        }
//        for (int i = 0; i < beamImageSet.size(); i++) {
//            plotsAnchorPane.getChildren().add(beamImageSet.get(i));
//            ImageView pos = (ImageView) plotsAnchorPane.getChildren().get(size + i);
//            pos.setX(peakCentreLinePlot.mapX(peakCentreLinePlot.getxAxisData()[i]) - 35);
//            if ((peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[i]) + 160) < 220) {
//                pos.setY(peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[i]) + 260);
//            } else {
//                pos.setY(peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[i]) + 195);
//            }
//            pos.setVisible(false);
//            remSize++;
//        }

        // Selects file from peakCentre plot
//        peakCentreGridPane.setOnMouseClicked(click -> {
//            peakCentreLinePlot.getOnMouseClicked();
//            processDataFileAndShowPlotsOfPeakShapes();
//
//            listViewOfResourcesInFolder.getSelectionModel().select(currentGroupIndex);
//        });

//        int finalSize = size;

//        peakCentreGridPane.setOnMouseMoved(mouse -> {
//            int index = (int) peakCentreLinePlot.convertMouseXToValue(mouse.getX());
//
//            if (peakCentreLinePlot.mouseInHouse(mouse.getX(), mouse.getY()) && index >= 1 && mouse.getY() > 10) {
//                for (int i = 0; i < peakCentreLinePlot.getxAxisData().length; i++) {
//                    ImageView pos1 = (ImageView) plotsAnchorPane.getChildren().get(finalSize + (i));
//                    pos1.setVisible(false);
//                    ImageView pos2 = (ImageView) plotsAnchorPane.getChildren().get(i + 1);
//                    pos2.setVisible(false);
//                    if (peakCentreLinePlot.getxAxisData()[i] == index) {
//                        pos1 = (ImageView) plotsAnchorPane.getChildren().get(finalSize + (index - 1));
//                        pos1.setVisible(true);
//                        pos2 = (ImageView) plotsAnchorPane.getChildren().get(index);
//                        pos2.setVisible(true);
//                    }
//
//                }
//
//            } else {
//                for (int i = 1; i < plotsAnchorPane.getChildren().size(); i++) {
//                    plotsAnchorPane.getChildren().get(i).setVisible(false);
//                }
//            }
//
//        });
    }

    private void populateListOfResources(String groupValue) {
        listViewOfResourcesInFolder = new ListView<>();
        listViewOfResourcesInFolder.setCellFactory(param -> new PeakShapeDemoPlotsControllerTest.ResourceDisplayName2());
        eventLogTextArea.textProperty().unbind();
        //int initialIndex;

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
        resourceBrowserTarget = listViewOfResourcesInFolder.getSelectionModel().getSelectedItem();
        processDataFileAndShowPlotsOfPeakShapes();

        listViewOfResourcesInFolder.prefHeightProperty().bind(eventAnchorPane.heightProperty());
        listViewOfResourcesInFolder.prefWidthProperty().bind(eventAnchorPane.widthProperty());
        eventAnchorPane.getChildren().add(listViewOfResourcesInFolder);
    }


    public void processDataFileAndShowPlotsOfPeakShapes() {


        if (resourceBrowserTarget != null && resourceBrowserTarget.isFile()) {
            PeakShapesService service = new PeakShapesService(resourceBrowserTarget.toPath());
            eventLogTextArea.textProperty().bind(service.valueProperty());
            try {
                PlotBuilder[] plots = BeamShapeTestDriver.modelTest(resourceBrowserTarget.toPath(), this::populateListOfResources);
                PeakShapeOutputDataRecord peakShapeOutputDataRecord =
                        PeakShapeProcessor_PhoenixTextFile.initializeWithMassSpectrometer(
                                        massSpectrometerModelBuiltinMap.get(MassSpectrometerContextEnum.PHOENIX_FULL.getMassSpectrometerName()))
                                .prepareInputDataModelFromFile(resourceBrowserTarget.toPath());


//                        BeamShapeLinePlotBuilderX.getPeakData(resourceBrowserTarget.toPath());
                //BeamShapeLinePlotBuilderX beamShape = BeamShapeLinePlotBuilderX.initializeBeamShape(peakShapeOutputDataRecord, new String[]{peakShapeOutputDataRecord.massID()}, "Mass (amu)", "Beam Intensity");

                PeakShapesOverlayBuilder peakShapes = PeakShapesOverlayBuilder.initializePeakShape(1, peakShapeOutputDataRecord,
                        new String[]{"Peak Shapes " + peakShapeOutputDataRecord.massID() + " / Peak Mass: " + peakShapeOutputDataRecord.peakCenterMass()},
                        "Mass (amu)", "Peak Intensities");
                if (ensemblePlotsWallPane.getChildren().size() > 3) {
                    ensemblePlotsWallPane.getChildren().remove(2, ensemblePlotsWallPane.getChildren().size());

                }


                //produceBeamShapeLinePlot(beamShape.getBeamShapeRecord(), ensemblePlotsWallPane);
                producePeakShapesOverlayPlot(peakShapes.getPeakShapesOverlayRecord(), ensemblePlotsWallPane);
                produceGBeamShapeLinePlot(plots[1], ensemblePlotsWallPane);
                ensemblePlotsWallPane.tilePlots();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            eventLogTextArea.textProperty().unbind();
            eventLogTextArea.setText("Please Choose Folder");
        }

    }

    private void produceBeamShapeLinePlot(BeamShapeRecord beamShapeRecord, PlotWallPane plotWallPane) {
        TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(plotWallPane);
        AbstractPlot plot = BeamShapeLinePlotX.generatePlot(new Rectangle(minPlotWidth, minPlotHeight), beamShapeRecord);
        tripoliPlotPane.addPlot(plot);
    }

    private void produceGBeamShapeLinePlot(PlotBuilder plotBuilder, PlotWallPane plotWallPane) {
        TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(plotWallPane);
        AbstractPlot plot = GBeamLinePlot.generatePlot(new Rectangle(minPlotWidth, minPlotHeight), (GBeamLinePlotBuilder) plotBuilder);
        tripoliPlotPane.addPlot(plot);

    }

    private void producePeakShapesOverlayPlot(PeakShapesOverlayRecord peakShapesOverlayRecord, PlotWallPane plotWallPane) {
        TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(plotWallPane);
        AbstractPlot plot = PeakShapesOverlayPlot.generatePlot(new Rectangle(minPlotWidth, minPlotHeight), peakShapesOverlayRecord);
        tripoliPlotPane.addPlot(plot);

    }

//    private AbstractPlot producePeakCentrePlot(LinePlotBuilder plotBuilder, PlotWallPane plotWallPane) {
//        TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(plotWallPane);
//        AbstractPlot plot = PeakCentresLinePlotX.generatePlot(new Rectangle(minPlotWidth, minPlotHeight), plotBuilder);
//        tripoliPlotPane.addPlot(plot);
//
//        return plot;
//    }


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