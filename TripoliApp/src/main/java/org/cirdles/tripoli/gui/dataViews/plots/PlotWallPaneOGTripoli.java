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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.cirdles.tripoli.constants.TripoliConstants;
import org.cirdles.tripoli.species.SpeciesRecordInterface;

import java.util.List;

/**
 * @author James F. Bowring
 */
public class PlotWallPaneOGTripoli extends Pane {

    public static final double gridCellDim = 2.0;
    public static final double toolBarHeight = 35.0;
    public static double menuOffset = 30.0;
    CheckBox baseLineCB;
    CheckBox gainCB;
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

    private PlotWallPaneOGTripoli(String iD) {
        this.iD = iD;
        zoomFlagsXY[0] = true;
        zoomFlagsXY[1] = true;
    }

    public static PlotWallPaneOGTripoli createPlotWallPane(String iD) {
        if (iD == null) {
            return new PlotWallPaneOGTripoli("NONE");
        } else {
            return new PlotWallPaneOGTripoli(iD);
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

        double tileHeight = displayHeight;// - displayHeight % gridCellDim;

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

    public void buildOGTripoliToolBar(List<SpeciesRecordInterface> species) {
        ToolBar toolBar = new ToolBar();
        toolBar.setPrefHeight(toolBarHeight);
        speciesChecked = new boolean[species.size()];

        CheckBox[] speciesCheckBoxes = new CheckBox[species.size()];
        for (int speciesIndex = 0; speciesIndex < species.size(); speciesIndex++) {
            speciesCheckBoxes[speciesIndex] = new CheckBox(species.get(speciesIndex).prettyPrintShortForm().trim());
            toolBar.getItems().add(speciesCheckBoxes[speciesIndex]);
            int finalSpeciesIndex = speciesIndex;
            speciesCheckBoxes[speciesIndex].selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                        speciesChecked[finalSpeciesIndex] = newVal;
                        rebuildPlot(false, true);
                    });
        }
        speciesCheckBoxes[0].setSelected(true);

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

        CheckBox showModel = new CheckBox("Model");
        showModel.setSelected(true);
        toolBar.getItems().add(showModel);
        showModel.selectedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                    showModels = newVal;
                    rebuildPlot(false, false);
                });

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

        getChildren().addAll(toolBar);
    }

    public void buildScaleControlsToolbar() {
        ToolBar toolBar = new ToolBar();
        toolBar.setPrefHeight(toolBarHeight);
        toolBar.setLayoutY(toolBarHeight);

        Button restoreButton = new Button("Restore Plot");
        restoreButton.setOnAction(event -> rebuildPlot(true, true));
        toolBar.getItems().add(restoreButton);

        Label labelScale = new Label("Scale:");
        labelScale.setAlignment(Pos.CENTER_RIGHT);
        labelScale.setPrefWidth(60);
        toolBar.getItems().add(labelScale);

        CheckBox logCB = new CheckBox("Log");
        toolBar.getItems().add(logCB);
        logCB.selectedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                    logScale = newVal;
                    rebuildPlot(false, true);
                });

        Label labelViews = new Label("Zoom:");
        labelViews.setAlignment(Pos.CENTER_RIGHT);
        labelViews.setPrefWidth(50);
        toolBar.getItems().add(labelViews);

        ToggleGroup toggleScaleY = new ToggleGroup();

        RadioButton countsRB = new RadioButton("Both");
        countsRB.setToggleGroup(toggleScaleY);
        countsRB.setSelected(true);
        toolBar.getItems().add(countsRB);
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
        toolBar.getItems().add(voltsRB);
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
        toolBar.getItems().add(ampsRB);
        ampsRB.selectedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                    if (newVal) {
                        zoomFlagsXY[0] = false;
                        zoomFlagsXY[1] = true;
                    }
                    resetZoom();
                });


        getChildren().add(toolBar);
    }

    private void rebuildPlot(boolean reScaleX, boolean reScaleY) {
        for (Node plotPane : getChildren()) {
            if (plotPane instanceof TripoliPlotPane) {
                ((TripoliPlotPane) plotPane).updateSpeciesPlotted(speciesChecked, showFaradays, showPMs, showModels, intensityUnits, baselineCorr, gainCorr, logScale, reScaleX, reScaleY);
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
}