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

package org.cirdles.tripoli.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.io.IOException;

import static org.cirdles.tripoli.gui.OGTripoliViewController.plottingData;

/**
 * @author James F. Bowring
 */
public class OGTripoliPlotsWindow {

    public static final double PLOT_WINDOW_WIDTH = 1000.0;
    public static final double PLOT_WINDOW_HEIGHT = 700.0;
//    public static final double SCROLLBAR_THICKNESS = 15.0;

    //    private final double xOffset = 0;
//    private final double yOffset = 0;
    public Stage plottingStage;
    public Window plottingWindow;
    private Stage primaryStage;

//    private OGTripoliPlotsWindow() {
//    }

    public OGTripoliPlotsWindow(Stage primaryStage, AnalysisManagerCallbackI analysisManagerCallbackI) {
        this.primaryStage = primaryStage;
        plottingStage = new Stage();
        plottingStage.setMinWidth(PLOT_WINDOW_WIDTH);
        plottingStage.setMinHeight(PLOT_WINDOW_HEIGHT);

        plottingStage.setOnCloseRequest((WindowEvent e) -> {
            plottingStage.hide();
            plottingStage.setScene(null);
            e.consume();
        });

        OGTripoliViewController.analysisManagerCallbackI = analysisManagerCallbackI;
    }

    public void close() {
        plottingStage.close();
    }

    public void loadPlotsWindow() {
        if (!plottingStage.isShowing()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/cirdles/tripoli/gui/OGTripoliView.fxml"));
                Scene scene = new Scene(loader.load());
                plottingStage.setScene(scene);

            } catch (IOException iOException) {
                iOException.printStackTrace();
            }
            plottingWindow = plottingStage.getScene().getWindow();
            plottingStage.setTitle("Tripoli " + (plottingData.preview() ? "PREVIEW" : "REVIEW") + " and Sculpt Data");
            plottingStage.show();
        }

        // center on app window
        plottingStage.setX(primaryStage.getX() + (primaryStage.getWidth() - plottingStage.getWidth()) / 2);
        plottingStage.setY(primaryStage.getY() + (primaryStage.getHeight() - plottingStage.getHeight()) / 2);
        plottingStage.requestFocus();
    }
}