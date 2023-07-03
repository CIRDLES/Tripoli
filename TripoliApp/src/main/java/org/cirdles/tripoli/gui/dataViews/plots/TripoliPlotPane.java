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
import javafx.scene.control.ColorPicker;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.RatioHistogramPlot;
import org.cirdles.tripoli.gui.utilities.TripoliColor;

import static org.cirdles.tripoli.gui.dataViews.plots.PlotWallPane.*;

/**
 * @author James F. Bowring
 */
public class TripoliPlotPane extends Pane {

    public static double minPlotWidth = 225.0;
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
        if (oneEdgesNorthWest) {
            if (0.0 <= deltaX) {
                targetPane.setLayoutX(Math.min(targetPane.getLayoutX() + deltaX, plotWallPane.getWidth() - targetPane.getWidth() - gridCellDim));
            } else {
                targetPane.setLayoutX(Math.max(targetPane.getLayoutX() + deltaX, gridCellDim));
            }

            if (0.0 <= deltaY) {
                targetPane.setLayoutY(Math.min(targetPane.getLayoutY() + deltaY, plotWallPane.getHeight() - targetPane.getHeight() - gridCellDim));
            } else {
                targetPane.setLayoutY(Math.max(targetPane.getLayoutY() + deltaY, gridCellDim + toolBarHeight));
            }
        }

        if (onEdgeEast) {
            if (0.0 <= deltaX) {
                targetPane.setPrefWidth(Math.min(plotWallPane.getWidth() - targetPane.getLayoutX() - gridCellDim, targetPane.getWidth() + deltaX));
            } else {
                targetPane.setPrefWidth(Math.max(minPlotWidth, targetPane.getWidth() + deltaX));
            }
        }

        if (onEdgeSouth) {
            if (0.0 <= deltaY) {
                targetPane.setPrefHeight(Math.min(plotWallPane.getHeight() - targetPane.getLayoutY() - gridCellDim, targetPane.getHeight() + deltaY));
//                mouseStartY = Math.max(e.getSceneY(), mouseStartY - deltaX);
            } else {
                targetPane.setPrefHeight(Math.max(minPlotHeight, targetPane.getHeight() + deltaY));
//                mouseStartY = Math.min(e.getSceneY(), mouseStartY + deltaX);
            }
        }

//        mouseStartX = e.getSceneX();
//        mouseStartY = e.getSceneY();

        ((AbstractPlot) getChildren().get(0)).updatePlotSize(getPrefWidth(), getPrefHeight());
        ((AbstractPlot) getChildren().get(0)).calculateTics();
        mouseStartX = e.getSceneX();
        mouseStartY = e.getSceneY();
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
        tripoliPlotPane.setPrefSize(minPlotWidth, minPlotHeight);
        tripoliPlotPane.setLayoutX(40.0);
        tripoliPlotPane.setLayoutY(40.0);
        tripoliPlotPane.initializePlotPane();

        plotWallPane.getChildren().addAll(tripoliPlotPane);

        return tripoliPlotPane;
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
        ((AbstractPlot) getChildren().get(0)).updatePlotSize(getPrefWidth(), getPrefHeight());
        ((AbstractPlot) getChildren().get(0)).calculateTics();
    }

    public void snapToGrid() {
        setLayoutX(getLayoutX() - (getLayoutX() % gridCellDim));
        setPrefWidth(getPrefWidth() - (getPrefWidth() % gridCellDim));
        setLayoutY(getLayoutY() - (getLayoutY() % gridCellDim));
        setPrefHeight(getPrefHeight() - (getPrefHeight() % gridCellDim));

        ((AbstractPlot) getChildren().get(0)).updatePlotSize(getPrefWidth(), getPrefHeight());
        ((AbstractPlot) getChildren().get(0)).calculateTics();
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

        plot.widthProperty().bind(prefWidthProperty());
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

        plot.preparePanel();
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
        ((AbstractPlot) getChildren().get(0)).toggleShowStats();
        ((AbstractPlot) getChildren().get(0)).repaint();
    }

    public void toggleRatiosLogRatios() {
        if (getChildren().get(0) instanceof RatioHistogramPlot) {
            ((RatioHistogramPlot) getChildren().get(0)).toggleRatiosLogRatios();
            ((RatioHistogramPlot) getChildren().get(0)).repaint();
        }
    }

    public void restorePlot() {
        ((AbstractPlot) getChildren().get(0)).refreshPanel(true, true);
    }

    private record PlotLocation(
            double x,
            double y,
            double w,
            double h
    ) {
    }
}