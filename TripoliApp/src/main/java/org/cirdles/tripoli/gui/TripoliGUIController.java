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

import jakarta.xml.bind.JAXBException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcDemoPlots.MCMCPlotsWindow;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.peakShapePlots.PeakShapePlotsWindow;
import org.cirdles.tripoli.gui.utilities.BrowserControl;
import org.cirdles.tripoli.sessions.Session;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.SessionBuiltinFactory;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static org.cirdles.tripoli.gui.utilities.BrowserControl.urlEncode;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.SessionBuiltinFactory.TRIPOLI_DEMONSTRATION_SESSION;

/**
 * @author James F. Bowring
 */
public class TripoliGUIController implements Initializable {

    public static Session tripoliSession;
    public static MCMCPlotsWindow MCMCPlotsWindow;
    private static GridPane sessionManagerUI;
    public Menu analysisMenu;
    public Menu methodsMenu;
    public Menu parametersMenu;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;
    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;
    @FXML // fx:id="versionBuildDate"
    private Label versionBuildDate; // Value injected by FXMLLoader
    @FXML // fx:id="versionLabel"
    private Label versionLabel; // Value injected by FXMLLoader
    @FXML
    private MenuItem sessionManagerMenuItem;
    @FXML
    private MenuItem newSessionMenuItem;
    @FXML
    private MenuItem saveSessionMenuItem;
    @FXML
    private MenuItem saveSessionAsMenuItem;
    @FXML
    private MenuItem closeSessionMenuItem;
    @FXML
    private AnchorPane splashAnchor;

    /**
     * @param location  The location used to resolve relative paths for the root object, or
     *                  {@code null} if the location is not known.
     * @param resources The resources used to localize the root object, or {@code null} if
     *                  the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        versionLabel.setText("v" + Tripoli.VERSION);
        versionBuildDate.setText(Tripoli.RELEASE_DATE);

        MCMCPlotsWindow = new MCMCPlotsWindow(TripoliGUI.primaryStage);

        showStartingMenus();

    }


    private void showStartingMenus(){
        sessionManagerMenuItem.setDisable(true);
        newSessionMenuItem.setDisable(false);
        saveSessionMenuItem.setDisable(true);
        saveSessionAsMenuItem.setDisable(true);
        closeSessionMenuItem.setDisable(true);

        analysisMenu.setDisable(true);

        methodsMenu.setDisable(true);

        parametersMenu.setDisable(true);
    }


    private void removeAllManagers() {
        for (Node manager : splashAnchor.getChildren()) {
            manager.setVisible(false);
        }

        // prevent stacking of panes
        splashAnchor.getChildren().remove(sessionManagerUI);

        // logo
        splashAnchor.getChildren().get(0).setVisible(true);

    }

    //  ++++++++++++++++++++++++++++++++++++++++++++++++++++ sessions ++++++++++++++++++++++++++++++++++++++++++++++++
    private void launchSessionManager() throws IOException {
        removeAllManagers();

        sessionManagerUI = FXMLLoader.load(getClass().getResource("SessionManager.fxml"));
        sessionManagerUI.setId("SessionManager");

        AnchorPane.setLeftAnchor(sessionManagerUI, 0.0);
        AnchorPane.setRightAnchor(sessionManagerUI, 0.0);
        AnchorPane.setTopAnchor(sessionManagerUI, 0.0);
        AnchorPane.setBottomAnchor(sessionManagerUI, 0.0);

        splashAnchor.getChildren().add(sessionManagerUI);
        sessionManagerUI.setVisible(true);

        closeSessionMenuItem.setDisable(false);
        analysisMenu.setDisable(false);
    }

    @FXML
    void sessionManagerMenuItemAction(){

    }

    public void newSessionMenuItemAction() throws IOException, JAXBException {
        tripoliSession = Session.initializeDefaultSession();
        launchSessionManager();
        AnalysisMethod.TEST();
    }

    public void openSessionMenuItemAction() {
    }

    public void openRecentSessionMenuItemAction() {
    }

    public void openDemonstrationSessionMenuItemAction(ActionEvent actionEvent) throws IOException {
        tripoliSession = SessionBuiltinFactory.sessionsBuiltinMap.get(TRIPOLI_DEMONSTRATION_SESSION);
        launchSessionManager();

    }

    public void saveSessionMenuItemAction(ActionEvent actionEvent) {
    }

    public void saveSessionAsMenuItemAction(ActionEvent actionEvent) {
    }

    @FXML
    void closeSessionMenuItemAction(ActionEvent event) {
        //TODO:        confirmSaveOnProjectClose();
        removeAllManagers();
        TripoliGUI.updateStageTitle("");
        tripoliSession = null;
        //TODO:        menuHighlighter.deHighlight();
        showStartingMenus();
    }


    @FXML
    private void quitAction(ActionEvent event) {
        // TODO: checks for save status etc.
        Platform.exit();
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++ end sessions ++++++++++++++++++++++++++++++++++++++++++++++++++
    @FXML
    private void showTripoliAbout(ActionEvent event) {
        TripoliGUI.tripoliAboutWindow.loadAboutWindow();
    }

    @FXML
    private void showTripoliContributeIssue(ActionEvent event) {
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
    private void showTripoliGitHubRepo(ActionEvent event) {
        BrowserControl.showURI("https://github.com/CIRDLES/Tripoli");
    }

    @FXML
    private void showPeriodicTable(ActionEvent event) {
        var periodicTableController = new PeriodicTableController();
        periodicTableController.launch();
    }

    @FXML
    private void showDemo1(ActionEvent event) {
        MCMCPlotsWindow = new MCMCPlotsWindow(TripoliGUI.primaryStage);
        MCMCPlotsWindow.loadPlotsWindow();
    }

    @FXML
    private void showDemo2(ActionEvent event) {
        PeakShapePlotsWindow peakShapePlotsWindow = new PeakShapePlotsWindow(TripoliGUI.primaryStage);
        peakShapePlotsWindow.loadPlotsWindow();
    }


    @FXML
    private void TESTING(ActionEvent event) throws JAXBException {

    }


}