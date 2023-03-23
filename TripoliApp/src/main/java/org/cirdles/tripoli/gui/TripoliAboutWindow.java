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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * @author James F. Bowring
 */
public final class TripoliAboutWindow {

    private Stage primaryStage;
    private double xOffset;
    private double yOffset;

    private TripoliAboutWindow() {
    }

    public TripoliAboutWindow(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void loadAboutWindow() {
        try {
            Parent aboutPage = FXMLLoader.load(getClass().getResource("AboutTripoli.fxml"));
            Scene aboutScene = new Scene(aboutPage, 550, 700);
            Stage aboutWindow = new Stage(StageStyle.UNDECORATED);
            // center on app window
            aboutWindow.setX(primaryStage.getX() + (primaryStage.getWidth() - 550) / 2);
            aboutWindow.setY(primaryStage.getY() + (primaryStage.getHeight() - 700) / 2);
            aboutWindow.setResizable(false);
            aboutWindow.setScene(aboutScene);


            aboutWindow.requestFocus();
            aboutWindow.initOwner(primaryStage.getScene().getWindow());
            aboutWindow.initModality(Modality.NONE);

            aboutPage.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            aboutPage.setOnMouseDragged(event -> {
                aboutWindow.setX(event.getScreenX() - xOffset);
                aboutWindow.setY(event.getScreenY() - yOffset);
            });

            // Close window if main window gains focus.
            primaryStage.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    aboutWindow.close();
                }
            });

            aboutWindow.show();
        } catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }

}