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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.gui.utilities.BrowserControl;

import java.net.URL;
import java.util.ResourceBundle;

public class AboutTripoliController {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="aboutDetailsLabel"
    private Label aboutDetailsLabel; // Value injected by FXMLLoader

    @FXML // fx:id="buildDate"
    private Label buildDate; // Value injected by FXMLLoader

    @FXML // fx:id="contributorsLabel"
    private Label contributorsLabel; // Value injected by FXMLLoader

    @FXML // fx:id="supportersLabel"
    private Label supportersLabel; // Value injected by FXMLLoader

    @FXML // fx:id="versionText"
    private Label versionText; // Value injected by FXMLLoader

    @FXML
    void visitTripoliOnCirdlesAction(ActionEvent event) {
        BrowserControl.showURI("http://cirdles.org/");
    }

    @FXML
    void visitUsOnGithubAction(ActionEvent event) {
        BrowserControl.showURI("https://github.com/CIRDLES/Tripoli");
    }

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        versionText.setText("Tripoli v" + Tripoli.VERSION);
        buildDate.setText("Release Date: " + Tripoli.RELEASE_DATE);
        aboutDetailsLabel.setText(Tripoli.ABOUT_WINDOW_CONTENT.toString());
        contributorsLabel.setText(Tripoli.CONTRIBUTORS_CONTENT.toString());
        supportersLabel.setText(Tripoli.SUPPORTERS_CONTENT.toString());
    }

}