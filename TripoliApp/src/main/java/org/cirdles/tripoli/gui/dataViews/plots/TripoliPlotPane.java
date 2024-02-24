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

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.cirdles.tripoli.constants.TripoliConstants;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.analysisPlots.AnalysisBlockCyclesPlot;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.analysisPlots.SpeciesIntensityAnalysisPlot;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.RatioHistogramPlot;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.analysisPlots.AnalysisRatioPlot;
import org.cirdles.tripoli.gui.utilities.TripoliColor;

import static org.cirdles.tripoli.gui.dataViews.plots.PlotWallPane.gridCellDim;
import static org.cirdles.tripoli.gui.dataViews.plots.PlotWallPane.menuOffset;

/**
 * @author James F. Bowring
 */
public class TripoliPlotPane extends BorderPane {

    public static double minPlotWidth = 175.0;
    public static double minPlotHeight = 100.0;
    private double plotToolBarHeight = 0;
    static double mouseStartX;
    static double mouseStartY;
    static boolean onEdgeEast;
    static boolean onEdgeSouth;
    static boolean oneEdgesNorthWest;
    private final EventHandler<MouseEvent> mouseMovedEventHandler = e -> {
        Pane targetPane = (Pane) e.getSource();
        targetPane.setCursor(Cursor.DEFAULT);

        onEdgeEast = (e.getSceneX() >= targetPane.getLayoutX() + targetPane.getWidth() - 2.0)
                && (e.getSceneX() <= targetPane.getLayoutX() + targetPane.getWidth() + 2.0);
        if (onEdgeEast) {
            targetPane.setCursor(Cursor.E_RESIZE);
        }
        onEdgeSouth = (e.getSceneY() - menuOffset >= targetPane.getLayoutY() + targetPane.getHeight() - 2.0)
                && (e.getSceneY() - menuOffset <= targetPane.getLayoutY() + targetPane.getHeight() + 2.0);
        if (onEdgeSouth) {
            targetPane.setCursor(Cursor.S_RESIZE);
        }
        if (onEdgeEast && onEdgeSouth) {
            targetPane.setCursor(Cursor.SE_RESIZE);
        }

        oneEdgesNorthWest =
                ((e.getSceneX() >= targetPane.getLayoutX() - 2.0)
                        && (e.getSceneX() <= targetPane.getLayoutX() + 2.0))
                        ||
                        ((e.getSceneY() - menuOffset >= targetPane.getLayoutY() - 2.0)
                                && (e.getSceneY() - menuOffset <= targetPane.getLayoutY() + 2.0));

        if (oneEdgesNorthWest) {
            targetPane.setCursor(Cursor.OPEN_HAND);
        }
    };
    private PlotWallPaneInterface plotWallPane;
    private AbstractPlot plot;
    private final EventHandler<MouseEvent> mouseReleasedEventHandler = e -> {
        snapToGrid();
    };
    private final EventHandler<MouseEvent> mouseDraggedEventHandler = e -> {
        Pane targetPane = (Pane) e.getSource();
        double deltaX = e.getSceneX() - mouseStartX;
        double deltaY = e.getSceneY() - mouseStartY;
        if ((deltaX != 0) && (deltaY != 0)) {
            if (oneEdgesNorthWest) {
                if (0.0 < deltaX) {
                    targetPane.setLayoutX(Math.min(targetPane.getLayoutX() + deltaX, ((Pane) plotWallPane).getWidth() - targetPane.getWidth() - gridCellDim));
                } else {
                    targetPane.setLayoutX(Math.max(targetPane.getLayoutX() + deltaX, gridCellDim));
                }

                if (0.0 < deltaY) {
                    targetPane.setLayoutY(Math.min(targetPane.getLayoutY() + deltaY, ((Pane) plotWallPane).getHeight() - targetPane.getHeight() - gridCellDim));
                } else {
                    targetPane.setLayoutY(Math.max(targetPane.getLayoutY() + deltaY, gridCellDim + plotWallPane.getToolBarHeight()));
                }
            }

            if (onEdgeEast) {
                if (0.0 < deltaX) {
                    targetPane.setPrefWidth(Math.min(((Pane) plotWallPane).getWidth() - targetPane.getLayoutX() - gridCellDim, targetPane.getWidth() + deltaX + 0.4));
                } else {
                    targetPane.setPrefWidth(Math.max(minPlotWidth, targetPane.getWidth() + deltaX - 0.6));
                }
            }

            if (onEdgeSouth) {
                if (0.0 < deltaY) {
                    targetPane.setPrefHeight(Math.min(((Pane) plotWallPane).getHeight() - targetPane.getLayoutY() - gridCellDim, targetPane.getHeight() + deltaY + 0.4));
                } else {
                    // shrinking
                    targetPane.setPrefHeight(Math.max(minPlotHeight, targetPane.getHeight() + deltaY - 0.6));
                }
            }

            updatePlot();

            mouseStartX = e.getSceneX();
            mouseStartY = e.getSceneY();
        }
    };
    private PlotLocation plotLocation;
    private final EventHandler<MouseEvent> mouseClickedEventHandler = e -> {
        if (getPlot().mouseInHouse(e.getX(), e.getY())) {
            if (e.isPrimaryButtonDown() && 1 == e.getClickCount()) {
                mouseStartX = e.getSceneX();
                mouseStartY = e.getSceneY();
            }
            if (e.isPrimaryButtonDown() && 2 == e.getClickCount()) {
                if (plot instanceof SpeciesIntensityAnalysisPlot) {

                } else {
                    toggleFullSize();
                }
            }
            toFront();
        }
    };

    private TripoliPlotPane(PlotWallPaneInterface plotWallPane) {
        this.plotWallPane = plotWallPane;
    }

    public static TripoliPlotPane makePlotPane(PlotWallPaneInterface plotWallPane) {
        TripoliPlotPane tripoliPlotPane = new TripoliPlotPane(plotWallPane);

        tripoliPlotPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            tripoliPlotPane.getPlot().setWidthF((Double) newValue);
            tripoliPlotPane.getPlot().refreshPanel(false, false);
        });

        tripoliPlotPane.setLayoutX(0.0);
        tripoliPlotPane.setLayoutY(40.0);
        tripoliPlotPane.initializePlotPane();

        tripoliPlotPane.setStyle(tripoliPlotPane.getStyle() + ";-fx-background-color:RED;");
        ((Pane) plotWallPane).getChildren().addAll(tripoliPlotPane);

        return tripoliPlotPane;
    }

    public AbstractPlot getPlot() {
        return plot;
    }

    private void updatePlot() {
        if (plot != null) {
            plot.updatePlotSize(getPrefWidth(), getPrefHeight() - plotToolBarHeight);
            plot.calculateTics();
        }
    }

    private void toggleFullSize() {
        if (null == plotLocation) {
            plotLocation = new PlotLocation(getLayoutX(), getLayoutY(), getPrefWidth(), getPrefHeight());
            setLayoutX(gridCellDim);
            setPrefWidth(((Pane) plotWallPane).getWidth() - 2 * gridCellDim);
            setLayoutY(gridCellDim + plotWallPane.getToolBarCount() * plotWallPane.getToolBarHeight());
            setPrefHeight(((Pane) plotWallPane).getHeight() - 2 * gridCellDim - plotWallPane.getToolBarCount() * plotWallPane.getToolBarHeight());
        } else {
            setLayoutX(plotLocation.x());
            setPrefWidth(plotLocation.w());
            setLayoutY(plotLocation.y());
            setPrefHeight(plotLocation.h());
            plotLocation = null;
        }
        updatePlot();
    }

    public void snapToGrid() {
        setLayoutX(getLayoutX() - (getLayoutX() % gridCellDim));
        setPrefWidth(getPrefWidth() - (getPrefWidth() % gridCellDim));
        setLayoutY(getLayoutY() - (getLayoutY() % gridCellDim));
        setPrefHeight(getPrefHeight() - (getPrefHeight() % gridCellDim));

        updatePlot();
    }

    private void initializePlotPane() {
        addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseDraggedEventHandler);
        addEventFilter(MouseEvent.MOUSE_PRESSED, mouseClickedEventHandler);
        addEventFilter(MouseEvent.MOUSE_MOVED, mouseMovedEventHandler);
        addEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleasedEventHandler);
    }

    public void addPlot(AbstractPlot plot) {
        this.plot = plot;

        Pane plotPane = new Pane();
        plotPane.getChildren().add(plot);
        setCenter(plotPane);

        boolean isBlockCyclesPlot = (plot instanceof AnalysisBlockCyclesPlot);
        plotToolBarHeight = isBlockCyclesPlot ? 30 : 0;
        if (isBlockCyclesPlot) {
            Font toolBarFont = Font.font("SansSerif", FontWeight.BOLD, 10);

            ToolBar plotToolBar = new ToolBar();
            plotToolBar.setMinHeight(plotToolBarHeight);
            plotToolBar.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
            plotToolBar.setStyle(plotToolBar.getStyle() + ";-fx-background-color:WHITE");

            Button restoreButton = new Button("Restore");
            restoreButton.setFont(toolBarFont);
            restoreButton.setOnAction(event -> restorePlot());
            plotToolBar.getItems().add(restoreButton);

            Button synchButton = new Button("Synch");
            synchButton.setFont(toolBarFont);
            plotToolBar.getItems().add(synchButton);

            Button ignoreDiscardsButton = new Button("Ignore Discards");
            ignoreDiscardsButton.setFont(toolBarFont);
            plotToolBar.getItems().add(ignoreDiscardsButton);

            Button chauvenetButton = new Button("Chauvenet");
            chauvenetButton.setFont(toolBarFont);
            plotToolBar.getItems().add(chauvenetButton);

            Button toggleStatsButton = new Button("Toggle Stats");
            toggleStatsButton.setFont(toolBarFont);
            toggleStatsButton.setOnAction(event -> toggleShowStats());
            plotToolBar.getItems().add(toggleStatsButton);

            setBottom(plotToolBar);
        }

        plot.widthProperty().bind(widthProperty());
        widthProperty().addListener((observable, oldValue, newValue) -> {
            plot.setWidthF(newValue.doubleValue());
            plot.updatePlotSize();
            plot.repaint();
        });

        plot.heightProperty().bind(heightProperty().subtract(plotToolBarHeight));
        heightProperty().addListener((observable, oldValue, newValue) -> {
            plot.setHeightF(newValue.doubleValue() - plotToolBarHeight);
            plot.updatePlotSize();
            plot.repaint();
        });

        plot.preparePanel(true, true);
        plot.repaint();
    }

    public void changeDataColor(AbstractPlot plot) {
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setLayoutX(getPrefWidth() / 2.0 - 50.0);
        colorPicker.setLayoutY(10.0);

        colorPicker.setValue(plot.getDataColor().color());
        getChildren().add(colorPicker);
        colorPicker.setVisible(true);

        colorPicker.setOnAction(t -> {
            plot.setDataColor(TripoliColor.create(colorPicker.getValue()));
            plot.repaint();
            getChildren().remove(colorPicker);
        });
    }

    public void toggleShowStats() {
        if (plot != null) {
            plot.toggleShowStats();
            plot.repaint();
        }
    }

    public void toggleRatiosLogRatios() {
        if (plot != null && (plot instanceof RatioHistogramPlot)) {
            ((RatioHistogramPlot) plot).toggleRatiosLogRatios();
        }
        if (plot != null && (plot instanceof AnalysisRatioPlot)) {
            ((AnalysisRatioPlot) plot).toggleRatiosLogRatios();
        }

    }

    public void restorePlot() {
        if (plot != null) {
            plot.refreshPanel(true, true);
        }
    }

    public void updateSpeciesPlotted(
            boolean[] speciesChecked, boolean showFaradays, boolean showPMs, boolean showModels,
            boolean showUncertainties, TripoliConstants.IntensityUnits intensityUnits, boolean baselineCorr, boolean gainCorr, boolean logScale, boolean reScaleX, boolean reScaleY) {
        if (plot != null && (plot instanceof SpeciesIntensityAnalysisPlot)) {
            ((SpeciesIntensityAnalysisPlot) plot).setSpeciesChecked(speciesChecked);
            ((SpeciesIntensityAnalysisPlot) plot).setShowFaradays(showFaradays);
            ((SpeciesIntensityAnalysisPlot) plot).setShowPMs(showPMs);
            ((SpeciesIntensityAnalysisPlot) plot).setShowModels(showModels);
            ((SpeciesIntensityAnalysisPlot) plot).setIntensityUnits(intensityUnits);
            ((SpeciesIntensityAnalysisPlot) plot).setBaselineCorr(baselineCorr);
            ((SpeciesIntensityAnalysisPlot) plot).setGainCorr(gainCorr);
            ((SpeciesIntensityAnalysisPlot) plot).setLogScale(logScale);
            ((SpeciesIntensityAnalysisPlot) plot).setShowUncertainties(showUncertainties);
            plot.refreshPanel(reScaleX, reScaleY);
        }
    }

    public void updateAnalysisRatiosPlotted(boolean blockMode, boolean logScale, boolean reScaleX, boolean reScaleY) {
        if (plot != null && (plot instanceof AnalysisBlockCyclesPlot)) {
            ((AnalysisBlockCyclesPlot) plot).setBlockMode(blockMode);
            ((AnalysisBlockCyclesPlot) plot).setLogScale(logScale);
            plot.refreshPanel(reScaleX, reScaleY);
        }
    }

    public void resetAnalysisIntensityZoom(boolean[] zoomFlagsXY) {
        ((SpeciesIntensityAnalysisPlot) plot).setZoomFlagsXY(zoomFlagsXY);
    }

    public void resetAnalysisRatioZoom(boolean[] zoomFlagsXY) {
        ((AnalysisBlockCyclesPlot) plot).setZoomFlagsXY(zoomFlagsXY);
    }

    private record PlotLocation(
            double x,
            double y,
            double w,
            double h
    ) {
    }
}