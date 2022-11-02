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

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Pane;

/**
 * @author James F. Bowring
 */
public class PlotWallPane extends Pane {

    static double menuOffset = 64;
    static double gridCellDim = 5.0;
    static double toolBarHeight = 35.0;

    public void tilePlots() {
        // assume 2 x 4
        double widthTileCount = 4.0;
        double heightTileCount = 3.0;

        double displayWidth = ((getParent().getBoundsInParent().getWidth() - gridCellDim * 2.0) / widthTileCount);
        double tileWidth = displayWidth - displayWidth % gridCellDim;

        double displayHeight = ((getParent().getBoundsInParent().getHeight() - menuOffset - toolBarHeight - gridCellDim * 2.0) / heightTileCount);
        double tileHeight = displayHeight - displayHeight % gridCellDim;

        int plotIndex = 0;
        for (Node plot : getChildren()) {
            if (plot instanceof TripoliPlotPane) {
                plot.setLayoutY(gridCellDim + toolBarHeight + tileHeight * Math.floor(plotIndex / widthTileCount));
                ((Pane) plot).setPrefHeight(tileHeight);
                plot.setLayoutX(gridCellDim + tileWidth * (plotIndex % widthTileCount));
                ((Pane) plot).setPrefWidth(tileWidth);

                ((TripoliPlotPane) plot).snapToGrid();

                plotIndex++;
            }
        }
    }

    public void stackPlots() {
        double displayWidth = (getParent().getBoundsInParent().getWidth() - gridCellDim * 2.0);
        double tileWidth = displayWidth;

        double displayHeight = ((getParent().getBoundsInParent().getHeight() - toolBarHeight - gridCellDim * 2.0) / getCountOfPlots());
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
        double cascadeLap = 25.0;
        double displayWidth = (getParent().getBoundsInParent().getWidth() - gridCellDim * 2.0 - cascadeLap * getCountOfPlots());
        double tileWidth = displayWidth;

        double displayHeight = getParent().getBoundsInParent().getHeight() - toolBarHeight - gridCellDim * 2.0 - cascadeLap * getCountOfPlots();
        double tileHeight = displayHeight;

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

    private int getCountOfPlots() {
        int count = 0;
        for (Node plot : getChildren()) {
            if (plot instanceof TripoliPlotPane) {
                count++;
            }
        }
        return count;
    }

    public void buildToolBar() {
        ToolBar toolBar = new ToolBar();
        toolBar.setPrefHeight(toolBarHeight);

        Button button0 = new Button("Restore all Plots");
        button0.setOnAction(event -> restoreAllPlots());

        Button button1 = new Button("Tile Plots");
        button1.setOnAction(event -> tilePlots());

        Button button2 = new Button("Stack Plots");
        button2.setOnAction(event -> stackPlots());

        Button button3 = new Button("Cascade Plots");
        button3.setOnAction(event -> cascadePlots());

        toolBar.getItems().addAll(button0, button1, button2, button3);
        getChildren().addAll(toolBar);
    }
}