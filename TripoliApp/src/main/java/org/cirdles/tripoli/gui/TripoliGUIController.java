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
//import org.cirdles.MakeSqr;

import jakarta.xml.bind.JAXBException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots.MCMCPlotsWindow;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.peakShapePlots.PeakShapePlotsWindow;
import org.cirdles.tripoli.gui.dialogs.TripoliMessageDialog;
import org.cirdles.tripoli.gui.utilities.BrowserControl;
import org.cirdles.tripoli.gui.utilities.fileUtilities.FileHandlerUtil;
import org.cirdles.tripoli.sessions.Session;
import org.cirdles.tripoli.sessions.SessionBuiltinFactory;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliPersistentState;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliSerializer;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

import static org.cirdles.tripoli.gui.AnalysisManagerController.analysis;
import static org.cirdles.tripoli.gui.TripoliGUI.primaryStageWindow;
import static org.cirdles.tripoli.gui.utilities.BrowserControl.urlEncode;
import static org.cirdles.tripoli.gui.utilities.fileUtilities.FileHandlerUtil.*;
import static org.cirdles.tripoli.sessions.SessionBuiltinFactory.TRIPOLI_DEMONSTRATION_SESSION;
import static org.cirdles.tripoli.sessions.analysis.AnalysisInterface.initializeNewAnalysis;
import static org.cirdles.tripoli.utilities.stateUtilities.TripoliSerializer.serializeObjectToFile;

/**
 * @author James F. Bowring
 */
public class TripoliGUIController implements Initializable {

    public static TripoliPersistentState tripoliPersistentState = null;
    public static @Nullable Session tripoliSession;
    public static MCMCPlotsWindow MCMCPlotsWindow;
    public static String sessionFileName;
    @FXML
    private static GridPane sessionManagerUI;
    @FXML
    private static GridPane analysesManagerUI;

    static {
        try {
            tripoliPersistentState = TripoliPersistentState.getExistingPersistentState();
        } catch (TripoliException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public MenuItem openSessionMenuItem;
    @FXML
    public Menu openRecentSessionMenu;
    @FXML
    public HBox newVersionAnnounceHBox;
    @FXML
    public HBox latestVersionHBox;
    @FXML
    public Label newVersionLabel;
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
    private Menu analysisMenu;
    @FXML
    private Menu methodsMenu;
    @FXML
    private Menu parametersMenu;
    @FXML
    private AnchorPane splashAnchor;

    public static void quit() {
        try {
            TripoliPersistentState.getExistingPersistentState().updateTripoliPersistentState();
        } catch (TripoliException squidException) {
            TripoliMessageDialog.showWarningDialog(squidException.getMessage(), primaryStageWindow);
        }
        //  todo:      confirmSaveOnSessionClose();
        System.out.println("Tripoli quitting normally.");
        Platform.exit();
        System.exit(0);
    }

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

        MCMCPlotsWindow = new MCMCPlotsWindow(TripoliGUI.primaryStage, null);

        buildSessionMenuMRU();
        showStartingMenus();

        detectLatestVersion();

        // March 2024 implement drag n drop of files ===================================================================
        splashAnchor.setOnDragOver(event -> {
            event.acceptTransferModes(TransferMode.MOVE);
        });
        splashAnchor.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if ((tripoliSession == null) && event.getDragboard().hasFiles()) {
                File dataFile = db.getFiles().get(0);
                // new session
                MenuItem menuItemAnalysesNew = ((MenuBar) TripoliGUI.primaryStage.getScene()
                        .getRoot().getChildrenUnmodifiable().get(0)).getMenus().get(0).getItems().get(2);
                menuItemAnalysesNew.fire();

                AnalysisInterface analysisSelected = analysis;

                try {
                    analysisSelected.extractMassSpecDataFromPath(Path.of(dataFile.toURI()));
                } catch (JAXBException | IOException | InvocationTargetException | NoSuchMethodException e) {
//                    throw new RuntimeException(e);
                } catch (IllegalAccessException | TripoliException e) {
//                    throw new RuntimeException(e);
                }

                // manage analysis
                MenuItem menuItemAnalysesManager = ((MenuBar) TripoliGUI.primaryStage.getScene()
                        .getRoot().getChildrenUnmodifiable().get(0)).getMenus().get(1).getItems().get(0);
                menuItemAnalysesManager.fire();
            }
        });
        // end implement drag n drop of files ===================================================================

    }

    private void detectLatestVersion() {
        try {
            URL url = new URL("https://raw.githubusercontent.com/CIRDLES/Tripoli/main/buildSrc/src/main/kotlin/common-build.gradle.kts");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/txt");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.contains("val mavenVersion")) {
                    content.append(inputLine);
                }
            }
            con.disconnect();
            String[] contentString = content.toString().split("\"");
            String latestVersion = contentString[1];
            if (Tripoli.VERSION.compareToIgnoreCase(latestVersion) == -1) {
                latestVersionHBox.setVisible(true);
                newVersionLabel.setText("New Version v" + latestVersion + " at:");
            } else {
                latestVersionHBox.setVisible(false);
            }
        } catch (IOException e) {
            //throw new RuntimeException(e);
        }
    }

    private void showStartingMenus() {
        sessionManagerMenuItem.setDisable(true);
        newSessionMenuItem.setDisable(false);
        saveSessionMenuItem.setDisable(true);
        saveSessionAsMenuItem.setDisable(true);
        closeSessionMenuItem.setDisable(true);

//        analysisMenu.setDisable(true);

        methodsMenu.setDisable(true);

        parametersMenu.setDisable(true);
    }

    private void removeAllManagers() throws TripoliException {
        TripoliPersistentState.getExistingPersistentState().updateTripoliPersistentState();
        for (Node manager : splashAnchor.getChildren()) {
            manager.setVisible(false);
        }

        AnalysisManagerController.closePlotWindows();

        // prevent stacking of panes
        splashAnchor.getChildren().remove(sessionManagerUI);
        splashAnchor.getChildren().remove(analysesManagerUI);

        // logo
        splashAnchor.getChildren().get(0).setVisible(true);

    }

    //  ++++++++++++++++++++++++++++++++++++++++++++++++++++ sessions ++++++++++++++++++++++++++++++++++++++++++++++++
    private void launchSessionManager() throws IOException, TripoliException {
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
//        analysisMenu.setDisable(true);
    }

    private void buildSessionMenuMRU() {
        openRecentSessionMenu.setDisable(false);

        openRecentSessionMenu.getItems().clear();
        List<String> mruProjectList = tripoliPersistentState.getMRUSessionList();
        for (String aProjectFileName : mruProjectList) {
            MenuItem menuItem = new MenuItem(aProjectFileName);
            menuItem.setOnAction((ActionEvent t) -> {
                try {
                    openSession(menuItem.getText());
                } catch (IOException | TripoliException iOException) {
                    tripoliPersistentState.removeSessionFileNameFromMRU(menuItem.getText());
                    tripoliPersistentState.cleanSessionListMRU();
                    openRecentSessionMenu.getItems().remove(menuItem);
                }
            });
            openRecentSessionMenu.getItems().add(menuItem);
        }
    }

    @FXML
    void sessionManagerMenuItemAction() throws IOException, TripoliException {
        launchSessionManager();
    }

    public void newSessionMenuItemAction() throws IOException, JAXBException, TripoliException {
        tripoliSession = Session.initializeDefaultSession();
        launchSessionManager();
    }

    public void openSessionMenuItemAction() throws IOException, TripoliException {
        confirmSaveOnProjectClose();
        removeAllManagers();

        try {
            sessionFileName = FileHandlerUtil.selectSessionFile(primaryStageWindow);
            openSession(sessionFileName);
        } catch (IOException | TripoliException iOException) {
        }
    }

    private void openSession(String aSessionFileName) throws IOException, TripoliException {
        if (!"".equals(aSessionFileName)) {
            sessionFileName = aSessionFileName;
            File sessionFile = new File(sessionFileName);
            confirmSaveOnProjectClose();
            tripoliSession = (Session) TripoliSerializer.getSerializedObjectFromFile(sessionFileName, true);

            if (null != tripoliSession) {
                tripoliPersistentState.updateSessionListMRU(sessionFile);
                TripoliGUI.updateStageTitle(sessionFileName);
                buildSessionMenuMRU();
                tripoliPersistentState.setMRUSessionFolderPath(sessionFile.getParent());
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

    public void openDemonstrationSessionMenuItemAction() throws IOException, TripoliException {
        tripoliSession = SessionBuiltinFactory.sessionsBuiltinMap.get(TRIPOLI_DEMONSTRATION_SESSION);
        launchSessionManager();

    }

    public void saveSessionMenuItemAction() {
        if (null != tripoliSession) {
            try {
                serializeObjectToFile(tripoliSession, tripoliPersistentState.getMRUSessionFile().getAbsolutePath());
//                squidProjectOriginalHash = squidProject.hashCode();
            } catch (TripoliException ex) {
                TripoliMessageDialog.showWarningDialog(ex.getMessage(), null);
            }
        }
    }

    public void saveSessionAsMenuItemAction() throws TripoliException {
        if (null != tripoliSession) {
            saveAsSession();
        }
    }

    private void saveAsSession() throws TripoliException {
        try {
            File sessionFile = FileHandlerUtil.saveSessionFile(tripoliSession, primaryStageWindow);
            if (null != sessionFile) {
                sessionFileName = sessionFile.getPath();
                tripoliSession.setSessionFilePathAsString(sessionFileName);
                saveSessionMenuItem.setDisable(false);
                tripoliPersistentState.updateSessionListMRU(sessionFile);
                TripoliGUI.updateStageTitle(sessionFile.getAbsolutePath());
                buildSessionMenuMRU();
                launchSessionManager();
//                runSaveMenuDisableCheck = true;
//                squidProjectOriginalHash = squidProject.hashCode();
            }

        } catch (IOException ex) {
            saveSessionMenuItem.setDisable(false);
        }
    }

    @FXML
    void closeSessionMenuItemAction() throws TripoliException {
        //TODO:        confirmSaveOnProjectClose();
        removeAllManagers();
        TripoliGUI.updateStageTitle("");
        tripoliSession = null;
        //TODO:        menuHighlighter.deHighlight();
        showStartingMenus();
    }

    private void confirmSaveOnProjectClose() throws IOException, TripoliException {
        if (Session.isSessionChanged()) {

            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Do you want to save Tripoli Session changes?",
                    ButtonType.YES,
                    ButtonType.NO
            );
            alert.setX(primaryStageWindow.getX() + (primaryStageWindow.getWidth() - 200) / 2);
            alert.setY(primaryStageWindow.getY() + (primaryStageWindow.getHeight() - 150) / 2);
            alert.showAndWait().ifPresent((t) -> {
                if (t.equals(ButtonType.YES)) {
                    try {
                        FileHandlerUtil.saveSessionFile(tripoliSession, primaryStageWindow);
                    } catch (IOException iOException) {
                        TripoliMessageDialog.showWarningDialog("Tripoli cannot access the target file.\n",
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
        quit();
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++ end sessions ++++++++++++++++++++++++++++++++++++++++++++++++++

    // ++++++++++++++++++++++++++++++++++++++++++++++++++ analyses ++++++++++++++++++++++++++++++++++++++++++++++++++
    private void launchAnalysesManager() throws IOException, TripoliException {
        removeAllManagers();

        analysesManagerUI = FXMLLoader.load(getClass().getResource("AnalysesManager.fxml"));
        analysesManagerUI.setId("AnalysesManager");

        AnchorPane.setLeftAnchor(analysesManagerUI, 0.0);
        AnchorPane.setRightAnchor(analysesManagerUI, 0.0);
        AnchorPane.setTopAnchor(analysesManagerUI, 0.0);
        AnchorPane.setBottomAnchor(analysesManagerUI, 0.0);

        splashAnchor.getChildren().add(analysesManagerUI);
        analysesManagerUI.setVisible(true);
    }

    public void manageAnalysisMenuItemAction() throws IOException, TripoliException {
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
        MCMCPlotsWindow = new MCMCPlotsWindow(TripoliGUI.primaryStage, null);
        MCMCPlotsWindow.loadPlotsWindow();
    }

    @FXML
    private void showDemo2() {
        PeakShapePlotsWindow peakShapePlotsWindow = new PeakShapePlotsWindow(TripoliGUI.primaryStage);
        peakShapePlotsWindow.loadPlotsWindow();
    }


    public void newAnalysisMenuItemOnAction() {
        RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
        randomDataGenerator.reSeedSecure();
        int choice = randomDataGenerator.nextInt(1, 99);

        AnalysisInterface analysisSelected = initializeNewAnalysis(choice);
        if (tripoliSession.getMapOfAnalyses().containsKey(analysisSelected.getAnalysisName())) {
            analysisSelected = initializeNewAnalysis(choice * 10);
        }
        analysisSelected.resetAnalysis();
        tripoliSession.addAnalysis(analysisSelected);
        analysis = analysisSelected;
        // manage analysis
        MenuItem menuItemAnalysesManager = ((MenuBar) TripoliGUI.primaryStage.getScene()
                .getRoot().getChildrenUnmodifiable().get(0)).getMenus().get(1).getItems().get(0);
        menuItemAnalysesManager.fire();
    }

    public void generateMCMCDetailsPerBlockAction() throws IOException, TripoliException {
        reportEnsembleDataDetails(analysis, null);
    }

    public void generateMCMCVectorsPerBlockAction() throws TripoliException, IOException {
        reportMCMCDataVectors(analysis, null);
    }

    public void reportTemplateOneAction() throws TripoliException, IOException {
        saveAnalysisReport(analysis, null);
    }

    public void visitLatestVersionAction() {
        BrowserControl.showURI("https://github.com/CIRDLES/Tripoli/releases/latest");
    }


}