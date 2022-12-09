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
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcDemoPlots.MCMCPlotsWindow;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.peakShapePlots.PeakShapePlotsWindow;
import org.cirdles.tripoli.gui.dialogs.TripoliMessageDialog;
import org.cirdles.tripoli.gui.utilities.BrowserControl;
import org.cirdles.tripoli.gui.utilities.fileUtilities.FileHandlerUtil;
import org.cirdles.tripoli.sessions.Session;
import org.cirdles.tripoli.sessions.SessionBuiltinFactory;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliSerializer;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static org.cirdles.tripoli.gui.utilities.BrowserControl.urlEncode;
import static org.cirdles.tripoli.sessions.SessionBuiltinFactory.TRIPOLI_DEMONSTRATION_SESSION;
import static org.cirdles.tripoli.utilities.stateUtilities.TripoliSerializer.serializeObjectToFile;

/**
 * @author James F. Bowring
 */
public class TripoliGUIController implements Initializable {

    public static @Nullable Session tripoliSession;
    public static MCMCPlotsWindow MCMCPlotsWindow;
    public static String sessionFileName;
    @FXML
    private static GridPane sessionManagerUI;
    @FXML
    private static GridPane analysesManagerUI;
    @FXML
    public MenuItem openSessionMenuItem;
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;
    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;
    @FXML // fx:id="versionBuildDate"
    private Label versionBuildDate; // Value injected by FXMLLoader
    @FXML // fx:id="versionLabel"
    private Label versionLabel; // Value injected by FXMLLoader
    @FXML
    private MenuItem openRecentSessionMenuItem;
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
    private Menu analysisMenu;
    @FXML
    private Menu methodsMenu;
    @FXML
    private Menu parametersMenu;
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


    private void showStartingMenus() {
        sessionManagerMenuItem.setDisable(true);
        newSessionMenuItem.setDisable(false);
        openRecentSessionMenuItem.setDisable(true);
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
        splashAnchor.getChildren().remove(analysesManagerUI);

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

        sessionManagerMenuItem.setDisable(false);
        saveSessionAsMenuItem.setDisable(false);
        closeSessionMenuItem.setDisable(false);
        analysisMenu.setDisable(false);
    }

    @FXML
    void sessionManagerMenuItemAction() throws IOException {
        launchSessionManager();
    }

    public void newSessionMenuItemAction() throws IOException, JAXBException {
        tripoliSession = Session.initializeDefaultSession();
        launchSessionManager();
        AnalysisMethod.test();
    }

    public void openSessionMenuItemAction() throws IOException {
        confirmSaveOnProjectClose();
        removeAllManagers();

        try {
            sessionFileName = FileHandlerUtil.selectSessionFile(TripoliGUI.primaryStageWindow);
            openSession(sessionFileName);
        } catch (IOException | TripoliException iOException) {
        }
    }

    private void openSession(String aSessionFileName) throws IOException, TripoliException {
        if (!"".equals(aSessionFileName)) {
            sessionFileName = aSessionFileName;
            confirmSaveOnProjectClose();
            tripoliSession = (Session) TripoliSerializer.getSerializedObjectFromFile(sessionFileName, true);

            if (tripoliSession != null) {
//                squidPersistentState.updateProjectListMRU(new File(projectFileName));
                TripoliGUI.updateStageTitle(sessionFileName);
//                buildSessionMenuMRU();
                launchSessionManager();
                saveSessionMenuItem.setDisable(false);

//                squidProjectOriginalHash = squidProject.hashCode();
//                runSaveMenuDisableCheck = true;
            } else {
                saveSessionMenuItem.setDisable(true);
                TripoliGUI.updateStageTitle("");
                throw new IOException();
            }
        }
    }

    public void openRecentSessionMenuItemAction() {
    }

    public void openDemonstrationSessionMenuItemAction() throws IOException {
        tripoliSession = SessionBuiltinFactory.sessionsBuiltinMap.get(TRIPOLI_DEMONSTRATION_SESSION);
        launchSessionManager();

    }

    public void saveSessionMenuItemAction() {
        if (tripoliSession != null) {
            try {
//                serializeObjectToFile(tripoliSession, squidPersistentState.getMRUProjectFile().getCanonicalPath());
                serializeObjectToFile(tripoliSession, sessionFileName);
//                squidProjectOriginalHash = squidProject.hashCode();
            } catch (TripoliException ex) {
                TripoliMessageDialog.showWarningDialog(ex.getMessage(), null);
            }
        }
    }

    public void saveSessionAsMenuItemAction() {
        if (tripoliSession != null) {
            saveAsSession();
        }
    }

    private void saveAsSession() {
        try {
            File sessionFile = FileHandlerUtil.saveSessionFile(tripoliSession, TripoliGUI.primaryStageWindow);
            if (sessionFile != null) {
                saveSessionMenuItem.setDisable(false);
//                squidPersistentState.updateProjectListMRU(projectFile);
                TripoliGUI.updateStageTitle(sessionFile.getAbsolutePath());
//                buildSessionMenuMRU();
                launchSessionManager();
//                runSaveMenuDisableCheck = true;
//                squidProjectOriginalHash = squidProject.hashCode();
            }

        } catch (IOException ex) {
            saveSessionMenuItem.setDisable(false);
        }
    }

    @FXML
    void closeSessionMenuItemAction() {
        //TODO:        confirmSaveOnProjectClose();
        removeAllManagers();
        TripoliGUI.updateStageTitle("");
        tripoliSession = null;
        //TODO:        menuHighlighter.deHighlight();
        showStartingMenus();
    }

    private void confirmSaveOnProjectClose() throws IOException {
        if (Session.isSessionChanged()) {

            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Do you want to save Tripoli Session changes?",
                    ButtonType.YES,
                    ButtonType.NO
            );
            alert.setX(TripoliGUI.primaryStageWindow.getX() + (TripoliGUI.primaryStageWindow.getWidth() - 200) / 2);
            alert.setY(TripoliGUI.primaryStageWindow.getY() + (TripoliGUI.primaryStageWindow.getHeight() - 150) / 2);
            alert.showAndWait().ifPresent((t) -> {
                if (t.equals(ButtonType.YES)) {
                    try {
                        File projectFile = FileHandlerUtil.saveSessionFile(tripoliSession, TripoliGUI.primaryStageWindow);
                    } catch (IOException iOException) {
                        TripoliMessageDialog.showWarningDialog("Squid3 cannot access the target file.\n",
                                null);
                    }
                }
            });
            Session.setSessionChanged(false);
            launchSessionManager();
//            squidProjectOriginalHash = squidProject.hashCode();
        }
    }


    @FXML
    private void quitAction() {
        // TODO: checks for save status etc.
        Platform.exit();
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++ end sessions ++++++++++++++++++++++++++++++++++++++++++++++++++

    // ++++++++++++++++++++++++++++++++++++++++++++++++++ analyses ++++++++++++++++++++++++++++++++++++++++++++++++++
    private void launchAnalysesManager() throws IOException {
        removeAllManagers();

        analysesManagerUI = FXMLLoader.load(getClass().getResource("AnalysesManager.fxml"));
        analysesManagerUI.setId("AnalysesManager");

        AnchorPane.setLeftAnchor(analysesManagerUI, 0.0);
        AnchorPane.setRightAnchor(analysesManagerUI, 0.0);
        AnchorPane.setTopAnchor(analysesManagerUI, 0.0);
        AnchorPane.setBottomAnchor(analysesManagerUI, 0.0);

        splashAnchor.getChildren().add(analysesManagerUI);
        analysesManagerUI.setVisible(true);

        analysisMenu.setDisable(false);
    }

    public void manageAnalysesMenuItemAction(ActionEvent actionEvent) throws IOException {
        launchAnalysesManager();
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++ end analyses ++++++++++++++++++++++++++++++++++++++++++++++++++
    @FXML
    private void showTripoliAbout() {
        TripoliGUI.tripoliAboutWindow.loadAboutWindow();
    }

    @FXML
    private void showTripoliContributeIssue() {
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
    private void showTripoliGitHubRepo() {
        BrowserControl.showURI("https://github.com/CIRDLES/Tripoli");
    }

    @FXML
    private void showPeriodicTable() {
        var periodicTableController = new PeriodicTableController();
        periodicTableController.launch();
    }

    @FXML
    private void showDemo1() {
        MCMCPlotsWindow = new MCMCPlotsWindow(TripoliGUI.primaryStage);
        MCMCPlotsWindow.loadPlotsWindow();
    }

    @FXML
    private void showDemo2() {
        PeakShapePlotsWindow peakShapePlotsWindow = new PeakShapePlotsWindow(TripoliGUI.primaryStage);
        peakShapePlotsWindow.loadPlotsWindow();
    }


}