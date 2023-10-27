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

package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.peakShapePlots;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.io.IOException;

import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.peakShapePlots.PeakShapeDemoPlotsControllerTest.resourceBrowserTarget;

/**
 * @author James F. Bowring
 */
public class PeakShapePlotsWindow {

    public static final double PLOT_WINDOW_WIDTH = 900.0;
    public static final double PLOT_WINDOW_HEIGHT = 600.0;
    public static final double SCROLLBAR_THICKNESS = 15.0;
    public static Window plottingWindow;
    public Stage plottingStage;
    private Stage primaryStage;

    private PeakShapePlotsWindow() {
    }

    public PeakShapePlotsWindow(Stage primaryStage) {
        this.primaryStage = primaryStage;
        plottingStage = new Stage();
        plottingStage.setMinWidth(PLOT_WINDOW_WIDTH);
        plottingStage.setMinHeight(PLOT_WINDOW_HEIGHT);
        plottingStage.setTitle("Tripoli View of PeakShapes");

        plottingStage.setOnCloseRequest((WindowEvent e) -> {
            plottingStage.hide();
            plottingStage.setScene(null);
            resourceBrowserTarget = null;
            e.consume();
        });
    }

    public void loadPlotsWindow() {
        if (!plottingStage.isShowing()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/cirdles/tripoli/gui/dataViews/plots/plotsControllers/PeakShapeDemoPlotsTest.fxml"));
                Scene scene = new Scene(loader.load());
                plottingStage.setScene(scene);

            } catch (IOException iOException) {
                iOException.printStackTrace();
            }
            plottingWindow = plottingStage.getScene().getWindow();
            plottingStage.show();
        }

        // center on app window
        plottingStage.setX(primaryStage.getX() + (primaryStage.getWidth() - plottingStage.getWidth()) / 2);
        plottingStage.setY(primaryStage.getY() + (primaryStage.getHeight() - plottingStage.getHeight()) / 2);
        plottingStage.requestFocus();
    }
}