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

import com.google.common.primitives.Booleans;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.cirdles.tripoli.gui.AnalysisManagerCallbackI;
import org.cirdles.tripoli.gui.constants.ConstantsTripoliApp;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots.MCMCPlotsControllerInterface;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.analysisPlots.AnalysisBlockCyclesPlotI;
import org.cirdles.tripoli.gui.utilities.BrowserControl;
import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.EnsemblesStore;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.cirdles.tripoli.constants.TripoliConstants.*;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.BlockEnsemblesPlotter.blockEnsemblePlotEngine;

/**
 * @author James F. Bowring
 */
public class PlotWallPane extends Pane implements PlotWallPaneInterface {

    public static final double gridCellDim = 2.0;
    public static double menuOffset = 30.0;
    private final String iD;
    private final boolean[] zoomFlagsXY = new boolean[2];
    private final AnalysisInterface analysis;
    private final MCMCPlotsControllerInterface mcmcPlotsController;
    AnalysisManagerCallbackI analysisManagerCallbackI;
    private double toolBarHeight;
    private int toolBarCount;
    private boolean logScale;
    private boolean blockMode;
    ChangeListener<Boolean> cycleCBChangeListener = (observable, oldValue, newValue) -> {
        blockMode = !newValue;
        rebuildPlot(false, true);
    };
    private ConstantsTripoliApp.PlotLayoutStyle plotLayoutStyle;
    private ToolBar scaleControlsToolbar;
    private TripoliPlotPane zoomedPlot;
    private Button chauvenetButton;
    private CheckBox cycleCB;

    private PlotWallPane(String iD, AnalysisInterface analysis, MCMCPlotsControllerInterface mcmcPlotsController, AnalysisManagerCallbackI analysisManagerCallbackI) {
        this.iD = iD;
        zoomFlagsXY[0] = true;
        zoomFlagsXY[1] = true;
        this.analysis = analysis;
        this.mcmcPlotsController = mcmcPlotsController;
        this.analysisManagerCallbackI = analysisManagerCallbackI;
        plotLayoutStyle = ConstantsTripoliApp.PlotLayoutStyle.TILE;
        this.blockMode = false;
    }

    public static PlotWallPaneInterface createPlotWallPane(
            String iD,
            AnalysisInterface analysis,
            MCMCPlotsControllerInterface mcmcPlotsController,
            AnalysisManagerCallbackI analysisManagerCallbackI) {
        if (null == iD) {
            return new PlotWallPane("NONE", analysis, mcmcPlotsController, analysisManagerCallbackI);
        } else {
            return new PlotWallPane(iD, analysis, mcmcPlotsController, analysisManagerCallbackI);
        }
    }

    public void setZoomedTripoliPlotPane(TripoliPlotPane zoomedPlot) {
        this.zoomedPlot = zoomedPlot;
    }

    public AnalysisInterface getAnalysis() {
        return analysis;
    }

    public void tilePlots() {
        List<Node> plotPanes = getChildren()
                .stream()
                .filter(plot -> plot instanceof TripoliPlotPane)
                .collect(Collectors.toList());

        // preserves display order
        plotPanes.sort(Comparator.comparing(o -> ((TripoliPlotPane) o)));

        double rowTileCount = Math.floor(Math.sqrt(plotPanes.size()));
        int columnTileCount = (int) Math.ceil(plotPanes.size() / rowTileCount);

        double parentWidth = Math.max(((AnchorPane) getParent()).getWidth(), ((AnchorPane) getParent()).getMinWidth());
        double displayWidth = (parentWidth - gridCellDim * 2.0) / columnTileCount;
        double tileWidth = displayWidth - displayWidth % gridCellDim;

        double parentHeight = Math.max(((AnchorPane) getParent()).getHeight(), ((AnchorPane) getParent()).getMinHeight());
        double displayHeight = (parentHeight - toolBarHeight - gridCellDim * rowTileCount) / rowTileCount;
        double tileHeight = displayHeight - displayHeight % gridCellDim;

        int plotIndex = 0;
        for (Node plot : plotPanes) {
            plot.setLayoutY(gridCellDim + toolBarHeight + tileHeight * Math.floor(plotIndex / columnTileCount));
            ((Pane) plot).setPrefHeight(tileHeight);
            plot.setLayoutX(gridCellDim + tileWidth * (plotIndex % columnTileCount));
            ((Pane) plot).setPrefWidth(tileWidth);

            ((TripoliPlotPane) plot).snapToGrid();

            plotIndex++;
        }

        plotLayoutStyle = ConstantsTripoliApp.PlotLayoutStyle.TILE;
    }

    public void clearTripoliPanes() {
        List<Node> plotPanes = getChildren()
                .stream()
                .filter(plot -> plot instanceof TripoliPlotPane)
                .collect(Collectors.toList());
        for (Node plotPane : plotPanes) {
            getChildren().remove(plotPane);
        }
    }

    public void stackPlots() {
        if (null != getParent()) {
            List<Node> plotPanes = getChildren()
                    .stream()
                    .filter(plot -> plot instanceof TripoliPlotPane)
                    .collect(Collectors.toList());

            // preserves display order
            plotPanes.sort(Comparator.comparing(o -> ((TripoliPlotPane) o)));
            double tileWidth;
            double displayHeight;
            if (0 == iD.compareToIgnoreCase("OGTripoliSession")) {
                double parentWidth = Math.max(((AnchorPane) getParent()).getPrefWidth(), ((AnchorPane) getParent()).getMinWidth());
                tileWidth = parentWidth - gridCellDim * 2.0;
                double parentHeight = Math.max(((AnchorPane) getParent()).getPrefHeight(), ((AnchorPane) getParent()).getMinHeight());
                displayHeight = (parentHeight - toolBarHeight) / getCountOfPlots();
            } else {
                double parentWidth = Math.max(((AnchorPane) getParent()).getPrefWidth(), ((AnchorPane) getParent()).getMinWidth());
                tileWidth = (parentWidth - gridCellDim * 2.0);

                double parentHeight = Math.max(((AnchorPane) getParent()).getPrefHeight(), ((AnchorPane) getParent()).getMinHeight());
                displayHeight = (parentHeight - toolBarHeight) / getCountOfPlots();
            }

            double tileHeight = displayHeight - displayHeight % gridCellDim;

            int plotIndex = 0;
            for (Node plotPane : plotPanes) {
                plotPane.setLayoutY(gridCellDim + toolBarHeight + tileHeight * plotIndex);
                ((Pane) plotPane).setPrefHeight(tileHeight);
                plotPane.setLayoutX(gridCellDim);
                ((Pane) plotPane).setPrefWidth(tileWidth);

                ((TripoliPlotPane) plotPane).snapToGrid();

                plotIndex++;
            }
        }
        plotLayoutStyle = ConstantsTripoliApp.PlotLayoutStyle.STACK;
    }

    public void repeatLayoutStyle() {
        if (zoomedPlot != null) {
            zoomedPlot.showTripoliPlotPaneFullSize();
        } else {
            switch (plotLayoutStyle) {
                case TILE -> tilePlots();
                case STACK -> stackPlots();
            }
        }
    }

    public void replotAll() {
        for (Node plotPane : getChildren()) {
            if (plotPane instanceof TripoliPlotPane) {
                ((TripoliPlotPane) plotPane).replot();
            }
        }
    }

    public void resetDataAll() {
        for (Node plotPane : getChildren()) {
            if (plotPane instanceof TripoliPlotPane) {
                ((TripoliPlotPane) plotPane).resetData();
            }
        }
    }

    public void performChauvenets() {
        for (Node plotPane : getChildren()) {
            if (plotPane instanceof TripoliPlotPane) {
                ((TripoliPlotPane) plotPane).performChauvenets();
            }
        }
    }

    public void performIgnoreRejects(boolean ignoreRejects) {
        for (Node plotPane : getChildren()) {
            if (plotPane instanceof TripoliPlotPane) {
                ((TripoliPlotPane) plotPane).performIgnoreRejects(ignoreRejects);
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

    public void toggleSculptingMode() {
        for (Node plotPane : getChildren()) {
            if (plotPane instanceof TripoliPlotPane) {
                ((TripoliPlotPane) plotPane).toggleSculptingMode();
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

    public void applyBurnIn() {
        int burnIn = (int) analysis.getMapOfBlockIdToPlots().get(mcmcPlotsController.getCurrentBlockID())[5][0].getShadeWidthForModelConvergence();
        int blockID = mcmcPlotsController.getCurrentBlockID();
        analysis.getMapOfBlockIdToModelsBurnCount().put(blockID, burnIn);

        blockEnsemblePlotEngine(blockID, analysis);
        mcmcPlotsController.plotEnsemblesEngine(analysis.getMapOfBlockIdToPlots().get(blockID));
        ((Analysis) analysis).analysisRatioEngine();
        mcmcPlotsController.plotAnalysisRatioEngine();
        EnsemblesStore.produceSummaryModelFromEnsembleStore(blockID, analysis);

        // fire up OGTripoli style analysis plots
        if (null != analysisManagerCallbackI) {
            analysisManagerCallbackI.reviewAndSculptDataAction();
        }
    }

    public void applyBurnInAllBlocks() {
        int burnIn = (int) analysis.getMapOfBlockIdToPlots().get(mcmcPlotsController.getCurrentBlockID())[5][0].getShadeWidthForModelConvergence();
        int blockIDCount = analysis.getMapOfBlockIdToPlots().keySet().size() + 1;

        for (int blockID = 1; blockID < blockIDCount; blockID++) {
            ((Analysis) analysis).updateShadeWidthsForConvergenceLinePlots(blockID, burnIn);
            analysis.getMapOfBlockIdToModelsBurnCount().put(blockID, burnIn);
            blockEnsemblePlotEngine(blockID, analysis);
            mcmcPlotsController.plotEnsemblesEngine(analysis.getMapOfBlockIdToPlots().get(blockID));
            EnsemblesStore.produceSummaryModelFromEnsembleStore(blockID, analysis);
        }

        ((Analysis) analysis).analysisRatioEngine();
        mcmcPlotsController.plotAnalysisRatioEngine();

        // fire up OGTripoli style analysis plots
        if (null != analysisManagerCallbackI) {
            analysisManagerCallbackI.reviewAndSculptDataAction();
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

        Button restoreButton = new Button("Restore Plots");
        restoreButton.setOnAction(event -> replotAll());
        toolBar.getItems().add(restoreButton);

        if ((0 != iD.compareToIgnoreCase(PLOT_TAB_CONVERGE))
                && (0 != iD.compareToIgnoreCase(PLOT_TAB_CONVERGE_INTENSITY))) {
            Button statsButton = new Button("Toggle Plots' Stats");
            statsButton.setOnAction(event -> toggleShowStatsAllPlots());
            toolBar.getItems().add(statsButton);

            Button ratioLogsButton = new Button("Toggle Ratios / LogRatios");
            ratioLogsButton.setOnAction(event -> toggleRatiosLogRatios());
            toolBar.getItems().add(ratioLogsButton);
        }

        if (0 != iD.compareToIgnoreCase(PLOT_TAB_CONVERGE_INTENSITY)) {
            Button tileButton = new Button("Tile Plots");
            tileButton.setOnAction(event -> tilePlots());
            toolBar.getItems().add(tileButton);

            Button stackButton = new Button("Stack Plots");
            stackButton.setOnAction(event -> stackPlots());
            toolBar.getItems().add(stackButton);
        }

        if (0 == iD.compareToIgnoreCase(PLOT_TAB_ENSEMBLES)) {
            Button applyBurnInButton = new Button("Apply BurnIn");
            applyBurnInButton.setOnAction(event -> applyBurnIn());
            toolBar.getItems().addAll(applyBurnInButton);

            Button applyBurnAllBlocksButton = new Button("Apply BurnIn All Blocks");
            applyBurnAllBlocksButton.setOnAction(event -> applyBurnInAllBlocks());
            toolBar.getItems().addAll(applyBurnAllBlocksButton);
        }

        getChildren().addAll(toolBar);
    }

    public void buildScaleControlsToolbar() {
        Font commandFont = Font.font("SansSerif", FontWeight.BOLD, 12);
        scaleControlsToolbar = new ToolBar();
        scaleControlsToolbar.setPrefHeight(toolBarHeight);
        scaleControlsToolbar.setStyle(scaleControlsToolbar.getStyle() + ";-fx-background-color:LINEN");
        scaleControlsToolbar.setLayoutY(0.0);

        Button infoButton = new Button("?");
        infoButton.setFont(commandFont);
        infoButton.setOnAction(event -> {
            Path resourcePath = Path.of(DOCS_FOLDER.getAbsolutePath() + File.separator + "ogTripoliHelp.md.html");
            BrowserControl.showURI(resourcePath.toString());
        });
        scaleControlsToolbar.getItems().add(infoButton);

        Button stackButton = new Button("Stack Plots");
        stackButton.setFont(commandFont);
        stackButton.setOnAction(event -> stackPlots());
        scaleControlsToolbar.getItems().add(stackButton);

//        Button toggleStatsButton = new Button("Toggle Stats");
//        toggleStatsButton.setFont(commandFont);
//        toggleStatsButton.setOnAction(event -> toggleShowStatsAllPlots());
//        scaleControlsToolbar.getItems().add(toggleStatsButton);

        Button tileButton = new Button("Tile Plots");
        tileButton.setFont(commandFont);
        tileButton.setOnAction(event -> tilePlots());
        scaleControlsToolbar.getItems().add(tileButton);

        Button replotAllButton = new Button("Replot All");
        replotAllButton.setFont(commandFont);
        replotAllButton.setOnAction(event -> replotAll());
        scaleControlsToolbar.getItems().add(replotAllButton);

        Button resetAllDataButton = new Button("Reset All Data");
        resetAllDataButton.setFont(commandFont);
        resetAllDataButton.setOnAction(event -> {
            resetDataAll();
            chauvenetButton.setDisable(false);
        });
        scaleControlsToolbar.getItems().add(resetAllDataButton);

        chauvenetButton = new Button("Chauvenet Rejection");
        chauvenetButton.setFont(commandFont);
        chauvenetButton.setOnAction(event -> {
            performChauvenets();
            chauvenetButton.setDisable(true);
        });
        scaleControlsToolbar.getItems().add(chauvenetButton);

        CheckBox ignoreCB = new CheckBox("Ignore Rejects");
        scaleControlsToolbar.getItems().add(ignoreCB);
        ignoreCB.setSelected(true);
        ignoreCB.selectedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                    performIgnoreRejects(newVal);
                });


        Label labelMode = new Label("Mode:");
        labelMode.setFont(commandFont);
        labelMode.setAlignment(Pos.CENTER_RIGHT);
        labelMode.setPrefWidth(50);
        scaleControlsToolbar.getItems().add(labelMode);

        cycleCB = new CheckBox("Cycle");
        scaleControlsToolbar.getItems().add(cycleCB);
        cycleCB.setSelected(true);
        cycleCB.selectedProperty().addListener(cycleCBChangeListener);
        updateStatusOfCycleCheckBox();

        Label labelScale = new Label("Ratio Scale:");
        labelScale.setFont(commandFont);
        labelScale.setAlignment(Pos.CENTER_RIGHT);
        labelScale.setPrefWidth(80);
        scaleControlsToolbar.getItems().add(labelScale);

        CheckBox logCB = new CheckBox("Log");
        scaleControlsToolbar.getItems().add(logCB);
        logCB.selectedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                    logScale = newVal;
                    rebuildPlot(false, true);
                });

        Label labelZoom = new Label("Zoom:");
        labelZoom.setFont(commandFont);
        labelZoom.setAlignment(Pos.CENTER_RIGHT);
        labelZoom.setPrefWidth(50);
        scaleControlsToolbar.getItems().add(labelZoom);

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

        RadioButton xOnlyRB = new RadioButton("X-only");
        xOnlyRB.setToggleGroup(toggleScaleY);
        scaleControlsToolbar.getItems().add(xOnlyRB);
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
        scaleControlsToolbar.getItems().add(yOnlyRB);
        yOnlyRB.selectedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                    if (newVal) {
                        zoomFlagsXY[0] = false;
                        zoomFlagsXY[1] = true;
                    }
                    resetZoom();
                });


        getChildren().add(scaleControlsToolbar);
    }

    @Override
    public double getToolBarHeight() {
        return toolBarHeight;
    }

    public void setToolBarHeight(double toolBarHeight) {
        this.toolBarHeight = toolBarHeight;
    }

    @Override
    public int getToolBarCount() {
        return toolBarCount;
    }

    public void setToolBarCount(int toolBarCount) {
        this.toolBarCount = toolBarCount;
    }

    private void rebuildPlot(boolean reScaleX, boolean reScaleY) {
        for (Node plotPane : getChildren()) {
            if (plotPane instanceof TripoliPlotPane) {
                ((TripoliPlotPane) plotPane).updateAnalysisRatiosPlotted(blockMode, logScale, reScaleX, reScaleY);
            }
        }
    }

    private void resetZoom() {
        for (Node plotPane : getChildren()) {
            if (plotPane instanceof TripoliPlotPane) {
                ((TripoliPlotPane) plotPane).resetAnalysisRatioZoom(zoomFlagsXY);
            }
        }
    }

    public void synchronizeRatioPlotsScroll(AnalysisBlockCyclesPlotI sourceAnalysisBlockCyclesPlot, double zoomChunkX, double zoomChunkY) {
        ObservableList<Node> children = getChildren();
        for (Node child : children) {
            if (child instanceof TripoliPlotPane) {
                AnalysisBlockCyclesPlotI childPlot = (AnalysisBlockCyclesPlotI) ((TripoliPlotPane) child).getPlot();
                if (childPlot != sourceAnalysisBlockCyclesPlot) {
                    childPlot.setZoomChunkX(zoomChunkX);
                    childPlot.setZoomChunkY(zoomChunkY);
                    childPlot.adjustZoom();
                } else {
                    childPlot.adjustZoomSelf();
                }
            }
        }
    }

    public void synchronizeRatioPlotsDrag(double x, double y) {
        ObservableList<Node> children = getChildren();
        for (Node child : children) {
            if (child instanceof TripoliPlotPane) {
                AnalysisBlockCyclesPlotI childPlot = (AnalysisBlockCyclesPlotI) ((TripoliPlotPane) child).getPlot();
                childPlot.adjustOffsetsForDrag(x, y);
            }
        }
    }

    public void synchronizeConvergencePlotsShade(int blockID, double shadeWidth) {
        ((Analysis) analysis).updateShadeWidthsForConvergenceLinePlots(blockID, shadeWidth);
        ObservableList<Node> children = getChildren();
        for (Node child : children) {
            if ((child instanceof TripoliPlotPane) && (((TripoliPlotPane) child).getPlot() instanceof AbstractPlot)) {
                ((TripoliPlotPane) child).getPlot().repaint();
            }
        }
    }

    public void synchronizeMouseStartsOnPress(double x, double y) {
        ObservableList<Node> children = getChildren();
        for (Node child : children) {
            if (child instanceof TripoliPlotPane) {
                AnalysisBlockCyclesPlotI childPlot = (AnalysisBlockCyclesPlotI) ((TripoliPlotPane) child).getPlot();
                childPlot.adjustMouseStartsForPress(x, y);
            }
        }
    }

    public void synchronizeBlockToggle(int blockID) {
        ObservableList<Node> children = getChildren();
        boolean included = false;
        for (Node child : children) {
            if (child instanceof TripoliPlotPane) {
                AnalysisBlockCyclesPlotI childPlot = (AnalysisBlockCyclesPlotI) ((TripoliPlotPane) child).getPlot();
                if (childPlot.getMapBlockIdToBlockCyclesRecord().get(blockID) != null) {
                    childPlot.getMapBlockIdToBlockCyclesRecord().put(
                            blockID,
                            childPlot.getMapBlockIdToBlockCyclesRecord().get(blockID).toggleBlockIncluded());
                    childPlot.repaint();
                    included = childPlot.getMapBlockIdToBlockCyclesRecord().get(blockID).blockIncluded();
                }
            }
        }
        analysisManagerCallbackI.callBackSetBlockIncludedStatus(blockID, included);
    }

    public void updateStatusOfCycleCheckBox() {
        ObservableList<Node> children = getChildren();
        List<Boolean> allShowCycle = new ArrayList<>();
        for (Node child : children) {
            if (child instanceof TripoliPlotPane) {
                AnalysisBlockCyclesPlotI childPlot = (AnalysisBlockCyclesPlotI) ((TripoliPlotPane) child).getPlot();
                allShowCycle.add(!childPlot.getBlockMode());
            }
        }
        int countOfShowCycles = Booleans.countTrue(Booleans.toArray(allShowCycle));
        cycleCB.selectedProperty().removeListener(cycleCBChangeListener);
        cycleCB.selectedProperty().setValue(countOfShowCycles == allShowCycle.size());
        cycleCB.setIndeterminate(false);
        if ((countOfShowCycles != allShowCycle.size()) && (countOfShowCycles > 0)) {
            cycleCB.selectedProperty().setValue(true);
            cycleCB.setIndeterminate(true);
        }
        cycleCB.selectedProperty().addListener(cycleCBChangeListener);
    }
}