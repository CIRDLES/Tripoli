/*
 * Copyright 2022 James Bowring, Noah McLean, Scott Burdick, and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cirdles.tripoli.gui.dataViews.plots;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.cirdles.tripoli.constants.TripoliConstants;
import org.cirdles.tripoli.expressions.species.SpeciesRecordInterface;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.analysisPlots.SpeciesIntensityAnalysisPlot;

import java.util.List;

import static org.cirdles.tripoli.constants.TripoliConstants.TRIPOLI_MICHAELANGELO_URL;

/**
 * @author James F. Bowring
 */
public class PlotWallPaneIntensities extends Pane implements PlotWallPaneInterface {

    public static final double gridCellDim = 2.0;
    public static double menuOffset = 30.0;
    CheckBox baseLineCB;
    CheckBox gainCB;
    private double toolBarHeight;
    private int toolBarCount;
    private String iD;
    private boolean[] speciesChecked = new boolean[0];
    private boolean showFaradays = true;
    private boolean showPMs = true;
    private boolean showModels = true;
    private TripoliConstants.IntensityUnits intensityUnits = TripoliConstants.IntensityUnits.COUNTS;
    private boolean baselineCorr = true;
    private boolean gainCorr = true;
    private boolean logScale;

    private boolean[] zoomFlagsXY = new boolean[2];

    private ToolBar scaleControlsToolbar;
    private CheckBox[] speciesCheckBoxes;
    private boolean showUncertainties = false;

    private PlotWallPaneIntensities(String iD) {
        this.iD = iD;
        zoomFlagsXY[0] = true;
        zoomFlagsXY[1] = true;
    }

    public static PlotWallPaneInterface createPlotWallPane(String iD) {
        if (iD == null) {
            return new PlotWallPaneIntensities("NONE");
        } else {
            return new PlotWallPaneIntensities(iD);
        }
    }

    public void stackPlots() {
        double tileWidth;
        double displayHeight;
        if (iD.compareToIgnoreCase("OGTripoliSession") == 0) {
            double parentWidth = Math.max(((AnchorPane) getParent()).getPrefWidth(), ((AnchorPane) getParent()).getMinWidth());
            tileWidth = parentWidth - gridCellDim * 2.0;
            double parentHeight = Math.max(((AnchorPane) getParent()).getPrefHeight(), ((AnchorPane) getParent()).getMinHeight());
            displayHeight = (parentHeight - toolBarHeight);
        } else {
            tileWidth = (getParent().getBoundsInParent().getWidth() - gridCellDim * 1.0);
            displayHeight = (getParent().getBoundsInParent().getHeight() - toolBarHeight);
        }

        double tileHeight = displayHeight;

        int plotIndex = 0;
        for (Node plotPane : getChildren()) {
            if (plotPane instanceof TripoliPlotPane) {
                plotPane.setLayoutY(gridCellDim + toolBarHeight * 2.0 + tileHeight * plotIndex);
                ((Pane) plotPane).setPrefHeight(tileHeight);
                plotPane.setLayoutX(gridCellDim);
                ((Pane) plotPane).setPrefWidth(tileWidth);
                ((TripoliPlotPane) plotPane).snapToGrid();
                plotIndex++;
            }
        }
    }

    /**
     *
     */
    @Override
    public void tilePlots() {
        // not applicable
    }

    /**
     *
     */
    @Override
    public void toggleShowStatsAllPlots() {
        // not used
    }

    /**
     *
     */
    @Override
    public void repeatLayoutStyle() {

    }

    public void buildIntensitiesPlotToolBar(boolean showResiduals, List<SpeciesRecordInterface> species) {
        ToolBar toolBar = new ToolBar();
        toolBar.setPrefHeight(toolBarHeight);
        speciesChecked = new boolean[species.size()];

        speciesCheckBoxes = new CheckBox[species.size()];
        for (int speciesIndex = 0; speciesIndex < species.size(); speciesIndex++) {
            speciesCheckBoxes[speciesIndex] = new CheckBox(species.get(speciesIndex).prettyPrintShortForm().trim());
            toolBar.getItems().add(speciesCheckBoxes[speciesIndex]);
            int finalSpeciesIndex = speciesIndex;
            speciesCheckBoxes[speciesIndex].selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                        speciesChecked[finalSpeciesIndex] = newVal;
                        if (((SpeciesIntensityAnalysisPlot) ((Pane) ((TripoliPlotPane) getChildren().get(getChildren().size() - 1)).getCenter()).getChildren().get(0)).isInSculptorMode()) {
                            ((SpeciesIntensityAnalysisPlot) ((Pane) ((TripoliPlotPane) getChildren().get(getChildren().size() - 1)).getCenter()).getChildren().get(0)).setInSculptorMode(false);
                            ((SpeciesIntensityAnalysisPlot) ((Pane) ((TripoliPlotPane) getChildren().get(getChildren().size() - 1)).getCenter()).getChildren().get(0)).sculptBlock(false);
                        }
                        rebuildPlot(false, true);
                    });
        }
        speciesCheckBoxes[speciesCheckBoxes.length - 1].setSelected(true);

        CheckBox showFaraday = new CheckBox("F");
        showFaraday.setSelected(true);
        toolBar.getItems().add(showFaraday);
        showFaraday.selectedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                    showFaradays = newVal;
                    rebuildPlot(false, true);
                });

        CheckBox showPM = new CheckBox("PM");
        showPM.setSelected(true);
        toolBar.getItems().add(showPM);
        showPM.selectedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                    showPMs = newVal;
                    rebuildPlot(false, true);
                });

        if (showResiduals) {
            CheckBox showUnct = new CheckBox("Unct");
            showUnct.setSelected(false);
            toolBar.getItems().add(showUnct);
            showUnct.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                        showUncertainties = newVal;
                        rebuildPlot(false, false);
                    });
        } else {
            CheckBox showModel = new CheckBox("Model");
            showModel.setSelected(true);
            toolBar.getItems().add(showModel);
            showModel.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                        showModels = newVal;
                        rebuildPlot(false, false);
                    });
        }

        Label labelViews = new Label("Units:");
        labelViews.setAlignment(Pos.CENTER_RIGHT);
        labelViews.setPrefWidth(50);
        toolBar.getItems().add(labelViews);

        ToggleGroup toggleScaleY = new ToggleGroup();

        RadioButton countsRB = new RadioButton("Counts");
        countsRB.setToggleGroup(toggleScaleY);
        countsRB.setSelected(true);
        toolBar.getItems().add(countsRB);
        countsRB.selectedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                    if (newVal) {
                        intensityUnits = TripoliConstants.IntensityUnits.COUNTS;
                    }
                    rebuildPlot(false, true);
                });

        RadioButton voltsRB = new RadioButton("Volts");
        voltsRB.setToggleGroup(toggleScaleY);
        toolBar.getItems().add(voltsRB);
        voltsRB.selectedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                    if (newVal) {
                        intensityUnits = TripoliConstants.IntensityUnits.VOLTS;
                    }
                    rebuildPlot(false, true);
                });

        RadioButton ampsRB = new RadioButton("Amps");
        ampsRB.setToggleGroup(toggleScaleY);
        toolBar.getItems().add(ampsRB);
        ampsRB.selectedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                    if (newVal) {
                        intensityUnits = TripoliConstants.IntensityUnits.AMPS;
                    }
                    rebuildPlot(false, true);
                });


        Label labelCorr = new Label("Corr:");
        labelCorr.setAlignment(Pos.CENTER_RIGHT);
        labelCorr.setPrefWidth(50);
        toolBar.getItems().add(labelCorr);

        baseLineCB = new CheckBox("BL");
        baseLineCB.setSelected(true);
        toolBar.getItems().add(baseLineCB);
        baseLineCB.selectedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                    baselineCorr = newVal;
                    if (!newVal) {
                        gainCB.setSelected(false);
                    }
                    rebuildPlot(false, false);
                });

        gainCB = new CheckBox("Gain");
        gainCB.setSelected(true);
        toolBar.getItems().add(gainCB);
        gainCB.selectedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                    gainCorr = newVal;
                    if (newVal) {
                        baseLineCB.setSelected(true);
                    }
                    rebuildPlot(false, false);
                });

        getChildren().add(0, toolBar);
    }

    /**
     *
     */
    @Override
    public void buildToolBar() {

    }

    public void buildScaleControlsToolbar() {
        scaleControlsToolbar = new ToolBar();
        scaleControlsToolbar.setPrefHeight(toolBarHeight);
        scaleControlsToolbar.setLayoutY(toolBarHeight);

        Button restoreButton = new Button("Restore Plot");

        restoreButton.setOnAction(event -> {
            if (((SpeciesIntensityAnalysisPlot) ((Pane) ((TripoliPlotPane) getChildren().get(getChildren().size() - 1)).getCenter()).getChildren().get(0)).isInSculptorMode()) {
                ((SpeciesIntensityAnalysisPlot) ((Pane) ((TripoliPlotPane) getChildren().get(getChildren().size() - 1)).getCenter()).getChildren().get(0)).setInSculptorMode(false);
                ((SpeciesIntensityAnalysisPlot) ((Pane) ((TripoliPlotPane) getChildren().get(getChildren().size() - 1)).getCenter()).getChildren().get(0)).sculptBlock(false);
            } else {
                rebuildPlot(true, true);
            }
        });
        scaleControlsToolbar.getItems().add(restoreButton);

        Label labelScale = new Label("Scale:");
        labelScale.setAlignment(Pos.CENTER_RIGHT);
        labelScale.setPrefWidth(60);
        scaleControlsToolbar.getItems().add(labelScale);

        CheckBox logCB = new CheckBox("Log");
        scaleControlsToolbar.getItems().add(logCB);
        logCB.selectedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                    logScale = newVal;
                    rebuildPlot(false, true);
                });

        Label labelViews = new Label("Zoom:");
        labelViews.setAlignment(Pos.CENTER_RIGHT);
        labelViews.setPrefWidth(50);
        scaleControlsToolbar.getItems().add(labelViews);

        ToggleGroup toggleScaleY = new ToggleGroup();

        RadioButton countsRB = new RadioButton("Both");
        countsRB.setToggleGroup(toggleScaleY);
        countsRB.setSelected(true);
        scaleControlsToolbar.getItems().add(countsRB);
        countsRB.selectedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                    if (newVal) {
                        zoomFlagsXY[0] = true;
                        zoomFlagsXY[1] = true;
                    }
                    resetZoom();
                });

        RadioButton voltsRB = new RadioButton("X-only");
        voltsRB.setToggleGroup(toggleScaleY);
        scaleControlsToolbar.getItems().add(voltsRB);
        voltsRB.selectedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                    if (newVal) {
                        zoomFlagsXY[0] = true;
                        zoomFlagsXY[1] = false;
                    }
                    resetZoom();
                });

        RadioButton ampsRB = new RadioButton("Y-only");
        ampsRB.setToggleGroup(toggleScaleY);
        scaleControlsToolbar.getItems().add(ampsRB);
        ampsRB.selectedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                    if (newVal) {
                        zoomFlagsXY[0] = false;
                        zoomFlagsXY[1] = true;
                    }
                    resetZoom();
                });

        scaleControlsToolbar.setPadding(new Insets(0, 0, 0, 10));
        getChildren().add(1, scaleControlsToolbar);
    }

    public void builtSculptingHBox(String message) {
        // Michaelangelo sculpting
        final ImageView michaelangeloImageView = new ImageView();
        Image ratioFlipper = new Image(TRIPOLI_MICHAELANGELO_URL);
        michaelangeloImageView.setImage(ratioFlipper);
        michaelangeloImageView.setFitHeight(30);
        michaelangeloImageView.setFitWidth(30);
        HBox sculptHBox = new HBox(michaelangeloImageView);
        sculptHBox.setAlignment(Pos.CENTER);
        sculptHBox.setPadding(new Insets(0, 10, 0, 10));
        sculptHBox.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        sculptHBox.getChildren().add(new Label(message));
        sculptHBox.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, null, null)));
        scaleControlsToolbar.getItems().add(sculptHBox);
    }

    public void removeSculptingHBox() {
        Node targetHBox = null;
        for (Node node : scaleControlsToolbar.getItems()) {
            if (node instanceof HBox) {
                targetHBox = node;
            }
        }
        scaleControlsToolbar.getItems().remove(targetHBox);
    }


    private void rebuildPlot(boolean reScaleX, boolean reScaleY) {
        for (Node plotPane : getChildren()) {
            if (plotPane instanceof TripoliPlotPane) {
                ((TripoliPlotPane) plotPane).updateSpeciesPlotted(
                        speciesChecked, showFaradays, showPMs, showModels, showUncertainties, intensityUnits, baselineCorr, gainCorr, logScale, reScaleX, reScaleY);
            }
        }
    }

    private void resetZoom() {
        for (Node plotPane : getChildren()) {
            if (plotPane instanceof TripoliPlotPane) {
                ((TripoliPlotPane) plotPane).resetAnalysisIntensityZoom(zoomFlagsXY);
            }
        }
    }

    @Override
    public double getToolBarHeight() {
        return toolBarHeight;
    }

    @Override
    public void setToolBarHeight(double toolBarHeight) {
        this.toolBarHeight = toolBarHeight;
    }

    @Override
    public int getToolBarCount() {
        return toolBarCount;
    }

    @Override
    public void setToolBarCount(int toolBarCount) {
        this.toolBarCount = toolBarCount;
    }
}