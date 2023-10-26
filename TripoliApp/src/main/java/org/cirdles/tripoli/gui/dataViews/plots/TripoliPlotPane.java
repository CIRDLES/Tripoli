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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.ColorPicker;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.cirdles.tripoli.constants.TripoliConstants;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.analysisPlots.BlockRatioCyclesAnalysisPlot;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.analysisPlots.SpeciesIntensityAnalysisPlot;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.RatioHistogramPlot;
import org.cirdles.tripoli.gui.utilities.TripoliColor;

import static org.cirdles.tripoli.gui.dataViews.plots.PlotWallPane.*;

/**
 * @author James F. Bowring
 */
public class TripoliPlotPane extends Pane {

    public static double minPlotWidth = 175.0;
    public static double minPlotHeight = 100.0;
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

    private final EventHandler<MouseEvent> mouseReleasedEventHandler = e -> {
        snapToGrid();
    };
    private Pane plotWallPane;
    private final EventHandler<MouseEvent> mouseDraggedEventHandler = e -> {
        Pane targetPane = (Pane) e.getSource();
        double deltaX = e.getSceneX() - mouseStartX;
        double deltaY = e.getSceneY() - mouseStartY;
        if ((deltaX != 0) && (deltaY != 0)) {
            if (oneEdgesNorthWest) {
                if (0.0 < deltaX) {
                    targetPane.setLayoutX(Math.min(targetPane.getLayoutX() + deltaX, plotWallPane.getWidth() - targetPane.getWidth() - gridCellDim));
                } else {
                    targetPane.setLayoutX(Math.max(targetPane.getLayoutX() + deltaX, gridCellDim));
                }

                if (0.0 < deltaY) {
                    targetPane.setLayoutY(Math.min(targetPane.getLayoutY() + deltaY, plotWallPane.getHeight() - targetPane.getHeight() - gridCellDim));
                } else {
                    targetPane.setLayoutY(Math.max(targetPane.getLayoutY() + deltaY, gridCellDim + toolBarHeight));
                }
            }

            if (onEdgeEast) {
                if (0.0 < deltaX) {
                    targetPane.setPrefWidth(Math.min(plotWallPane.getWidth() - targetPane.getLayoutX() - gridCellDim, targetPane.getWidth() + deltaX + 0.4));
                } else {
                    targetPane.setPrefWidth(Math.max(minPlotWidth, targetPane.getWidth() + deltaX - 0.6));
                }
            }

            if (onEdgeSouth) {
                if (0.0 < deltaY) {
                    targetPane.setPrefHeight(Math.min(plotWallPane.getHeight() - targetPane.getLayoutY() - gridCellDim, targetPane.getHeight() + deltaY + 0.4));
                } else {
                    // shrinking
                    targetPane.setPrefHeight(Math.max(minPlotHeight, targetPane.getHeight() + deltaY - 0.6));
                }
            }

            updatePlot();

            mouseStartX = e.getSceneX(); // use deltas??
            mouseStartY = e.getSceneY();
        }
    };
    private PlotLocation plotLocation;
    private final EventHandler<MouseEvent> mouseClickedEventHandler = e -> {
        if (e.isPrimaryButtonDown() && 1 == e.getClickCount()) {
            mouseStartX = e.getSceneX();
            mouseStartY = e.getSceneY();
        }
        if (e.isPrimaryButtonDown() && 2 == e.getClickCount()) {
            toggleFullSize();
        }
        toFront();
    };

    private TripoliPlotPane(Pane plotWallPane) {
        this.plotWallPane = plotWallPane;
    }

    public static TripoliPlotPane makePlotPane(Pane plotWallPane) {
        TripoliPlotPane tripoliPlotPane = new TripoliPlotPane(plotWallPane);

        tripoliPlotPane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                ((AbstractPlot) tripoliPlotPane.getChildren().get(0)).setWidthF((Double) newValue);
                ((AbstractPlot) tripoliPlotPane.getChildren().get(0)).refreshPanel(false, false);
            }
        });

        tripoliPlotPane.setLayoutX(0.0);
        tripoliPlotPane.setLayoutY(40.0);
        tripoliPlotPane.initializePlotPane();

        tripoliPlotPane.setStyle(tripoliPlotPane.getStyle() + ";-fx-background-color:RED;");
        plotWallPane.getChildren().addAll(tripoliPlotPane);

        return tripoliPlotPane;
    }

    private void updatePlot() {
        if (!getChildren().isEmpty()) {
            ((AbstractPlot) getChildren().get(0)).updatePlotSize(getPrefWidth(), getPrefHeight());
            ((AbstractPlot) getChildren().get(0)).calculateTics();
        }
    }

    private void toggleFullSize() {
        if (null == plotLocation) {
            plotLocation = new PlotLocation(getLayoutX(), getLayoutY(), getPrefWidth(), getPrefHeight());
            setLayoutX(gridCellDim);
            setPrefWidth(plotWallPane.getWidth() - 2 * gridCellDim);
            setLayoutY(gridCellDim);
            setPrefHeight(plotWallPane.getHeight() - 2 * gridCellDim);
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
        getChildren().add(plot);

        plot.setLayoutX(0.0);
        plot.setLayoutY(0.0);
        plot.setWidthF(getWidth());
        plot.setHeightF(getHeight());

        plot.widthProperty().bind(widthProperty());
        widthProperty().addListener((observable, oldValue, newValue) -> {
            plot.setWidthF(newValue.doubleValue());
            plot.updatePlotSize();
            plot.repaint();
        });

        plot.heightProperty().bind(prefHeightProperty());
        heightProperty().addListener((observable, oldValue, newValue) -> {
            plot.setHeightF(newValue.doubleValue());
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
        if (!getChildren().isEmpty()) {
            ((AbstractPlot) getChildren().get(0)).toggleShowStats();
            ((AbstractPlot) getChildren().get(0)).repaint();
        }
    }

    public void toggleRatiosLogRatios() {
        if (!getChildren().isEmpty() && (getChildren().get(0) instanceof RatioHistogramPlot)) {
            ((RatioHistogramPlot) getChildren().get(0)).toggleRatiosLogRatios();
            ((RatioHistogramPlot) getChildren().get(0)).repaint();
        }
    }

    public void restorePlot() {
        if (!getChildren().isEmpty()) {
            ((AbstractPlot) getChildren().get(0)).refreshPanel(true, true);
        }
    }

    public void updateSpeciesPlotted(
            boolean[] speciesChecked, boolean showFaradays, boolean showPMs, boolean showModels, TripoliConstants.IntensityUnits intensityUnits, boolean baselineCorr, boolean gainCorr, boolean logScale, boolean reScaleX, boolean reScaleY) {
        if (!getChildren().isEmpty() && (getChildren().get(0) instanceof SpeciesIntensityAnalysisPlot)) {
            ((SpeciesIntensityAnalysisPlot) getChildren().get(0)).setSpeciesChecked(speciesChecked);
            ((SpeciesIntensityAnalysisPlot) getChildren().get(0)).setShowFaradays(showFaradays);
            ((SpeciesIntensityAnalysisPlot) getChildren().get(0)).setShowPMs(showPMs);
            ((SpeciesIntensityAnalysisPlot) getChildren().get(0)).setShowModels(showModels);
            ((SpeciesIntensityAnalysisPlot) getChildren().get(0)).setIntensityUnits(intensityUnits);
            ((SpeciesIntensityAnalysisPlot) getChildren().get(0)).setBaselineCorr(baselineCorr);
            ((SpeciesIntensityAnalysisPlot) getChildren().get(0)).setGainCorr(gainCorr);
            ((SpeciesIntensityAnalysisPlot) getChildren().get(0)).setLogScale(logScale);

            ((SpeciesIntensityAnalysisPlot) getChildren().get(0)).refreshPanel(reScaleX, reScaleY);
        }
    }

    public void updateAnalysisRatiosPlotted(boolean logScale, boolean reScaleX, boolean reScaleY) {
        if (!getChildren().isEmpty() && (getChildren().get(0) instanceof BlockRatioCyclesAnalysisPlot)) {
            ((BlockRatioCyclesAnalysisPlot) getChildren().get(0)).setLogScale(logScale);

            ((BlockRatioCyclesAnalysisPlot) getChildren().get(0)).refreshPanel(reScaleX, reScaleY);
        }
    }

    public void resetAnalysisIntensityZoom(boolean[] zoomFlagsXY) {
        ((SpeciesIntensityAnalysisPlot) getChildren().get(0)).setZoomFlagsXY(zoomFlagsXY);
    }

    public void resetAnalysisRatioZoom(boolean[] zoomFlagsXY) {
        ((BlockRatioCyclesAnalysisPlot) getChildren().get(0)).setZoomFlagsXY(zoomFlagsXY);
    }

    private record PlotLocation(
            double x,
            double y,
            double w,
            double h
    ) {
    }
}