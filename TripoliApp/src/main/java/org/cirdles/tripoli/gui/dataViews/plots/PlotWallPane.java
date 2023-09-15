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
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.sessionPlots.BlockRatioCyclesSessionPlot;
import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author James F. Bowring
 */
public class PlotWallPane extends Pane {

    public static final double gridCellDim = 2.0;
    public static final double toolBarHeight = 35.0;
    public static double menuOffset = 30.0;
    private String iD;
    private boolean logScale;

    private boolean[] zoomFlagsXY = new boolean[2];

    private AnalysisInterface analysis;

    private PlotWallPane(String iD, AnalysisInterface analysis) {
        this.iD = iD;
        zoomFlagsXY[0] = true;
        zoomFlagsXY[1] = true;
        this.analysis = analysis;
    }

    public static PlotWallPane createPlotWallPane(String iD, AnalysisInterface analysis) {
        if (iD == null) {
            return new PlotWallPane("NONE", analysis);
        } else {
            return new PlotWallPane(iD, analysis);
        }
    }

    public AnalysisInterface getAnalysis() {
        return analysis;
    }

    public void tilePlots() {
        List<Node> plotPanes = getChildren()
                .stream()
                .filter(plot -> plot instanceof TripoliPlotPane)
                .collect(Collectors.toList());

        double widthTileCount = Math.min(5, plotPanes.size());
        double heightTileCount = Math.ceil(plotPanes.size() / widthTileCount);

        double displayWidth = ((getParent().getBoundsInParent().getWidth() - gridCellDim * 2.0) / widthTileCount);
        double tileWidth = displayWidth - displayWidth % gridCellDim;

        double displayHeight = ((getParent().getBoundsInParent().getHeight() - toolBarHeight - gridCellDim * heightTileCount) / heightTileCount);
        double tileHeight = displayHeight - displayHeight % gridCellDim;

        int plotIndex = 0;
        for (Node plot : plotPanes) {
            plot.setLayoutY(gridCellDim + toolBarHeight + tileHeight * Math.floor(plotIndex / widthTileCount));
            ((Pane) plot).setPrefHeight(tileHeight);
            plot.setLayoutX(gridCellDim + tileWidth * (plotIndex % widthTileCount));
            ((Pane) plot).setPrefWidth(tileWidth);

            ((TripoliPlotPane) plot).snapToGrid();

            plotIndex++;
        }
    }

    public void stackPlots() {
        double tileWidth;
        double displayHeight;
        if (iD.compareToIgnoreCase("OGTripoliSession") == 0) {
            tileWidth = ((AnchorPane) getParent()).getPrefWidth() - gridCellDim * 2.0;
            displayHeight = (((AnchorPane) getParent()).getPrefHeight() - toolBarHeight) / getCountOfPlots();
        } else {
            tileWidth = (getParent().getBoundsInParent().getWidth() - gridCellDim * 2.0);
            displayHeight = (getParent().getBoundsInParent().getHeight() - toolBarHeight) / getCountOfPlots();
        }

        double tileHeight = displayHeight - displayHeight % gridCellDim;

        int plotIndex = 0;
        for (Node plotPane : getChildren()) {
            if (plotPane instanceof TripoliPlotPane) {
                plotPane.setLayoutY(gridCellDim + toolBarHeight + tileHeight * plotIndex);
                ((Pane) plotPane).setPrefHeight(tileHeight);
                plotPane.setLayoutX(gridCellDim);
                ((Pane) plotPane).setPrefWidth(tileWidth);

                ((TripoliPlotPane) plotPane).snapToGrid();

                plotIndex++;
            }
        }
    }

    public void cascadePlots() {

        double cascadeLap = 20.0;
        double displayWidth = getParent().getBoundsInParent().getWidth() - cascadeLap * (getCountOfPlots() - 1);
        double tileWidth = displayWidth - gridCellDim;

        double displayHeight = getParent().getBoundsInParent().getHeight() - toolBarHeight - cascadeLap * (getCountOfPlots() - 1);
        double tileHeight = displayHeight - gridCellDim;

        int plotIndex = 0;
        for (Node plotPane : getChildren()) {
            if (plotPane instanceof TripoliPlotPane) {
                plotPane.setLayoutY(gridCellDim + toolBarHeight + cascadeLap * plotIndex);
                ((Pane) plotPane).setPrefHeight(tileHeight);
                plotPane.setLayoutX(gridCellDim + cascadeLap * plotIndex);
                ((Pane) plotPane).setPrefWidth(tileWidth);

                ((TripoliPlotPane) plotPane).snapToGrid();

                plotIndex++;
            }
        }
    }

    public void restoreAllPlots() {
        for (Node plotPane : getChildren()) {
            if (plotPane instanceof TripoliPlotPane) {
                ((TripoliPlotPane) plotPane).restorePlot();
            }
        }
    }

    public void toggleShowStatsAllPlots() {
        for (Node plotPane : getChildren()) {
            if (plotPane instanceof TripoliPlotPane) {
                ((TripoliPlotPane) plotPane).toggleShowStats();
            }
        }
    }

    public void toggleRatiosLogRatios() {
        for (Node plotPane : getChildren()) {
            if (plotPane instanceof TripoliPlotPane) {
                ((TripoliPlotPane) plotPane).toggleRatiosLogRatios();
            }
        }
    }

    private int getCountOfPlots() {
        List<Node> plots = getChildren()
                .stream()
                .filter(plot -> plot instanceof TripoliPlotPane)
                .collect(Collectors.toList());
        return plots.size();
    }

    public void buildToolBar() {
        ToolBar toolBar = new ToolBar();
        toolBar.setPrefHeight(toolBarHeight);

        Button button0 = new Button("Restore Plots");
        button0.setOnAction(event -> restoreAllPlots());

        Button button1 = new Button("Tile Plots");
        button1.setOnAction(event -> tilePlots());

        Button button2 = new Button("Stack Plots");
        button2.setOnAction(event -> stackPlots());

        Button button3 = new Button("Cascade Plots");
        button3.setOnAction(event -> cascadePlots());

        Button button4 = new Button("Toggle Plots' Stats");
        button4.setOnAction(event -> toggleShowStatsAllPlots());

        Button button5 = new Button("Toggle Ratios / LogRatios");
        button5.setOnAction(event -> toggleRatiosLogRatios());

        toolBar.getItems().addAll(button0, button5, button4, button1, button2, button3);
        getChildren().addAll(toolBar);
    }

    public void buildScaleControlsToolbar() {
        ToolBar toolBar = new ToolBar();
        toolBar.setPrefHeight(toolBarHeight);
        toolBar.setLayoutY(0.0);

        Button button0 = new Button("Restore Plots");
        button0.setOnAction(event -> restoreAllPlots());
        toolBar.getItems().add(button0);

        Button button1 = new Button("Toggle Stats");
        button1.setOnAction(event -> toggleShowStatsAllPlots());
        toolBar.getItems().add(button1);

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

        RadioButton xOnlyRB = new RadioButton("X-only");
        xOnlyRB.setToggleGroup(toggleScaleY);
        toolBar.getItems().add(xOnlyRB);
        xOnlyRB.selectedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                    if (newVal) {
                        zoomFlagsXY[0] = true;
                        zoomFlagsXY[1] = false;
                    }
                    resetZoom();
                });

        RadioButton yOnlyRB = new RadioButton("Y-only");
        yOnlyRB.setToggleGroup(toggleScaleY);
        toolBar.getItems().add(yOnlyRB);
        yOnlyRB.selectedProperty().addListener(
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
                ((TripoliPlotPane) plotPane).updateRatiosSessionPlotted(logScale, reScaleX, reScaleY);
            }
        }
    }

    private void resetZoom() {
        for (Node plotPane : getChildren()) {
            if (plotPane instanceof TripoliPlotPane) {
                ((TripoliPlotPane) plotPane).resetRatioSessionZoom(zoomFlagsXY);
            }
        }
    }

    public void synchronizeRatioPlotsScroll(double zoomChunkX, double zoomChunkY) {
        ObservableList<Node> children = getChildren();
        for (Node child : children) {
            if (child instanceof TripoliPlotPane) {
                BlockRatioCyclesSessionPlot childPlot = (BlockRatioCyclesSessionPlot) ((TripoliPlotPane) child).getChildren().get(0);
                childPlot.setZoomChunkX(zoomChunkX);
                childPlot.setZoomChunkY(zoomChunkY);
                childPlot.adjustZoom();
            }
        }
    }

    public void synchronizeRatioPlotsDrag(double x, double y) {
        ObservableList<Node> children = getChildren();
        for (Node child : children) {
            if (child instanceof TripoliPlotPane) {
                BlockRatioCyclesSessionPlot childPlot = (BlockRatioCyclesSessionPlot) ((TripoliPlotPane) child).getChildren().get(0);
                childPlot.adjustOffsetsForDrag(x, y);
            }
        }
    }

    public void synchronizeConvergencePlotsShade(int blockID, double shadeWidth) {
        ((Analysis) analysis).updateShadeWidthsForConvergenceLinePlots(blockID, shadeWidth);
        ObservableList<Node> children = getChildren();
        for (Node child : children) {
            if (child instanceof TripoliPlotPane) {
                if (((TripoliPlotPane) child).getChildren().get(0) instanceof AbstractPlot) {
                    ((AbstractPlot) ((TripoliPlotPane) child).getChildren().get(0)).repaint();
                }
            }
        }
    }

    public void synchronizeMouseStartsOnPress(double x, double y) {
        ObservableList<Node> children = getChildren();
        for (Node child : children) {
            if (child instanceof TripoliPlotPane) {
                BlockRatioCyclesSessionPlot childPlot = (BlockRatioCyclesSessionPlot) ((TripoliPlotPane) child).getChildren().get(0);
                childPlot.adjustMouseStartsForPress(x, y);
            }
        }
    }

    public void synchronizeBlockToggle(int blockID) {
        ObservableList<Node> children = getChildren();
        for (Node child : children) {
            if (child instanceof TripoliPlotPane) {
                if (child instanceof TripoliPlotPane) {
                    BlockRatioCyclesSessionPlot childPlot = (BlockRatioCyclesSessionPlot) ((TripoliPlotPane) child).getChildren().get(0);
                    childPlot.getMapBlockIdToBlockRatioCyclesRecord().put(
                            blockID,
                            childPlot.getMapBlockIdToBlockRatioCyclesRecord().get(blockID).toggleBlockIncluded());
                    childPlot.repaint();
                }
            }
        }
    }
}