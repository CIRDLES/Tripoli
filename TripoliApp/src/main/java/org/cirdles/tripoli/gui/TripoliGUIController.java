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

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.peakShapePlots.PeakShapePlotsWindow;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.RJMCMCPlots.RJMCMCPlotsWindow;
import org.cirdles.tripoli.gui.utilities.BrowserControl;

import java.net.URL;
import java.util.ResourceBundle;

import static org.cirdles.tripoli.gui.utilities.BrowserControl.urlEncode;

/**
 * @author James F. Bowring
 */
public class TripoliGUIController {

    public static String projectFileName;
    public static RJMCMCPlotsWindow RJMCMCPlotsWindow;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML
    private AnchorPane splashAnchor;

    @FXML // fx:id="versionBuildDate"
    private Label versionBuildDate; // Value injected by FXMLLoader

    @FXML // fx:id="versionLabel"
    private Label versionLabel; // Value injected by FXMLLoader

    @FXML
    void showTripoliAbout(ActionEvent event) {
        TripoliGUI.tripoliAboutWindow.loadAboutWindow();
    }

    @FXML
    private void quitAction(ActionEvent event) {
        // TODO: checks for save status etc.
        Platform.exit();
    }

    @FXML
    void showTripoliContributeIssue(ActionEvent event) {
        String version = "Tripoli Version: " + Tripoli.VERSION;
        String javaVersion = "Java Version: " + System.getProperties().getProperty("java.version");
        String javaFXVersion = "JavaFX Version: " + System.getProperties().getProperty("javafx.runtime.version");
        String operatingSystem = "OS: " + System.getProperties().getProperty("os.name") + " " + System.getProperties().getProperty("os.version");

        String issueBody = urlEncode(version + "\n") +
                urlEncode(javaVersion + "\n") +
                urlEncode(javaFXVersion + "\n") +
                urlEncode(operatingSystem + "\n") +
                urlEncode("\nIssue details:\n");

        BrowserControl.showURI("https://github.com/CIRDLES/Tripoli/issues/new?body=" + issueBody);
    }

    @FXML
    void showTripoliGitHubRepo(ActionEvent event) {
        BrowserControl.showURI("https://github.com/CIRDLES/Tripoli");
    }

    @FXML
    void showPeriodicTable(ActionEvent event) {
        (new PeriodicTableController()).launch();
    }

    @FXML
    void showDemo1(ActionEvent event) {
        RJMCMCPlotsWindow RJMCMCPlotsWindow = new RJMCMCPlotsWindow(TripoliGUI.primaryStage);
        RJMCMCPlotsWindow.loadPlotsWindow();
    }

    @FXML
    void showDemo2(ActionEvent event) {
        PeakShapePlotsWindow peakShapePlotsWindow = new PeakShapePlotsWindow(TripoliGUI.primaryStage);
        peakShapePlotsWindow.loadPlotsWindow();
    }


    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        versionLabel.setText("v" + Tripoli.VERSION);
        versionBuildDate.setText(Tripoli.RELEASE_DATE);

        RJMCMCPlotsWindow = new RJMCMCPlotsWindow(TripoliGUI.primaryStage);
    }


}