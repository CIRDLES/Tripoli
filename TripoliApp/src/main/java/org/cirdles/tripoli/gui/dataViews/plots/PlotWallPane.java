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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Pane;

import javax.swing.*;

/**
 * @author James F. Bowring
 */
public class PlotWallPane extends Pane {

    static double menuOffset = 64;
    static double gridCellDim = 5.0;
    static double toolBarHeight = 35.0;

    private ToolBar toolBar;

    public void tilePlots(){
        // assume 2 x 4
        double displayWidth = ((getParent().getBoundsInParent().getWidth() - gridCellDim * 2.0)/ 4.0);
        double tileWidth = displayWidth - displayWidth % gridCellDim;

        int plotIndex = 0;
        for (Node plot : getChildren()){
            if (plot instanceof TripoliPlotPane) {
                plot.setLayoutY(gridCellDim + toolBarHeight);
                plot.setLayoutX(gridCellDim + tileWidth * plotIndex);
                ((Pane) plot).setPrefWidth(tileWidth);
                plotIndex++;
            }
        }
    }

    public void buildToolBar(){
        toolBar = new ToolBar();
        toolBar.setPrefHeight(toolBarHeight);
        Button button = new Button("Tile Plots");
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                tilePlots();
            }
        });

        toolBar.getItems().add(button);
        getChildren().addAll(toolBar);
    }
}