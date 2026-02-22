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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.sessions.analysis.imports.OgTripoliImporter;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.phoenix.PhoenixLiveData;
import org.cirdles.tripoli.utilities.file.FileWatcher;
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;
import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots.MCMCPlotsWindow;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.OGTripoliPlotsWindow;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.OGTripoliViewController;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.peakShapePlots.PeakShapePlotsWindow;
import org.cirdles.tripoli.gui.dialogs.TripoliMessageDialog;
import org.cirdles.tripoli.gui.reports.ReportBuilderController;
import org.cirdles.tripoli.gui.settings.SettingsRequestType;
import org.cirdles.tripoli.gui.settings.SettingsWindow;
import org.cirdles.tripoli.gui.utilities.BrowserControl;
import org.cirdles.tripoli.gui.utilities.events.SaveCurrentSessionEvent;
import org.cirdles.tripoli.gui.utilities.events.SaveSessionAsEvent;
import org.cirdles.tripoli.sessions.Session;
import org.cirdles.tripoli.sessions.SessionBuiltinFactory;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne.initializers.AllBlockInitForDataLiteOne;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.initializers.AllBlockInitForMCMC;
import org.cirdles.tripoli.sessions.analysis.outputs.etRedux.ETReduxFraction;
import org.cirdles.tripoli.utilities.DelegateActionSet;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.stateUtilities.AnalysisMethodPersistance;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliPersistentState;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliSerializer;
import org.jetbrains.annotations.Nullable;
import org.cirdles.tripoli.reports.Report;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.cirdles.tripoli.gui.AnalysisManagerController.analysis;
import static org.cirdles.tripoli.gui.AnalysisManagerController.ogTripoliPreviewPlotsWindow;
import static org.cirdles.tripoli.gui.TripoliGUI.primaryStage;
import static org.cirdles.tripoli.gui.TripoliGUI.primaryStageWindow;
import static org.cirdles.tripoli.gui.utilities.BrowserControl.urlEncode;
import static org.cirdles.tripoli.gui.utilities.fileUtilities.FileHandlerUtil.*;
import static org.cirdles.tripoli.sessions.SessionBuiltinFactory.TRIPOLI_DEMONSTRATION_SESSION;
import static org.cirdles.tripoli.sessions.analysis.AnalysisInterface.initializeNewAnalysis;
import static org.cirdles.tripoli.utilities.comparators.LiveDataEntryComparator.blockCycleComparator;
import static org.cirdles.tripoli.utilities.stateUtilities.TripoliSerializer.serializeObjectToFile;
import static org.cirdles.tripoli.gui.SessionManagerController.listOfSelectedAnalyses;

/**
 * @author James F. Bowring
 */
public class TripoliGUIController implements Initializable {

    public static TripoliPersistentState tripoliPersistentState = null;
    public static @Nullable Session tripoliSession;
    public static MCMCPlotsWindow MCMCPlotsWindow;
    public static SettingsWindow settingsWindow;
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
    @FXML
    public Menu reportsMenu;
    public MenuItem manageAnalysisMenuItem;
    @FXML
    public Menu customReportMenu;
    @FXML
    public MenuItem processLiveDataMenuItem;
    @FXML
    public MenuItem importAnalysisMenuItem;
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
    private Menu settingsMenu;
    @FXML
    private MenuItem settingsMenuMenuItem;
    @FXML
    private AnchorPane splashAnchor;
    Thread liveDataLogThread;
    FileWatcher liveDataLogWatcher;
    Thread liveDataFinishFileThread;
    FileWatcher liveDataFinishFileWatcher;
    Thread liveDataStatusThread;
    FileWatcher liveDataStatusWatcher;
    PhoenixLiveData phoenixLiveData;


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
        primaryStage.getScene().addEventFilter(SaveSessionAsEvent.SAVE_SESSION_AS_EVENT_EVENT_TYPE,
                saveSessionAsEvent -> {
                    try{
                        saveSessionAsMenuItemAction();
                    } catch ( TripoliException ex) {
                        ex.printStackTrace();
                    } finally {
                        saveSessionAsEvent.consume();
                    }
        });
        primaryStage.getScene().addEventFilter(SaveCurrentSessionEvent.SAVE_CURRENT_SESSION_EVENT,
                saveCurrentSessionEvent -> {
                saveSessionMenuItemAction();
                saveCurrentSessionEvent.consume();
        });
        versionLabel.setText("v" + Tripoli.VERSION);
        versionBuildDate.setText(Tripoli.RELEASE_DATE);
        MCMCPlotsWindow = new MCMCPlotsWindow(primaryStage, null);

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
                if (dataFile.getName().endsWith(".tripoli")) {
                    // existing session
                    try {
                        openSession(dataFile.getAbsolutePath());
                    } catch (IOException | TripoliException e) {
//                        throw new RuntimeException(e);
                    }
                } else {
                    // new session
                    MenuItem menuItemSessionNew = ((MenuBar) primaryStage.getScene()
                            .getRoot().getChildrenUnmodifiable().get(0)).getMenus().get(0).getItems().get(2);
                    menuItemSessionNew.fire();
                    for (int i = 0; i < db.getFiles().size(); i++) {
                        dataFile = db.getFiles().get(i);

                        AnalysisInterface analysisProposed;
                        try {
                            analysisProposed = initializeNewAnalysis(0);
                            String analysisName = analysisProposed.extractMassSpecDataFromPath(Path.of(dataFile.toURI()));
                            if (analysisProposed.getMassSpecExtractedData().getMassSpectrometerContext().compareTo(MassSpectrometerContextEnum.UNKNOWN) != 0) {

                                tripoliPersistentState.setMRUDataFileFolderPath(dataFile.getParent());
                                analysisProposed.setAnalysisName(analysisName);
                                analysisProposed.setAnalysisStartTime(analysisProposed.getMassSpecExtractedData().getHeader().analysisStartTime());
                                tripoliSession.addAnalysis(analysisProposed);
                                analysis = analysisProposed;
                                AllBlockInitForDataLiteOne.initBlockModels(analysis);
                                reportsMenu.setDisable(false);
                                AnalysisManagerController.readingFile = true;
                                analysis.getParameters().setMassSpectrometerContext(analysis.getMassSpecExtractedData().getMassSpectrometerContext());
                                detectMassSpecContext();
                            } else {
                                analysis = null;
                                reportsMenu.setDisable(true);
                                TripoliMessageDialog.showWarningDialog("Tripoli does not recognize this file format.", primaryStageWindow);
                            }
                        } catch (JAXBException | IOException | InvocationTargetException | NoSuchMethodException |
                                 IllegalAccessException | TripoliException e) {
//                    throw new RuntimeException(e);
                        }
                    }
                    MenuItem menuItemSessionManager = ((MenuBar) primaryStage.getScene()
                            .getRoot().getChildrenUnmodifiable().get(0)).getMenus().get(0).getItems().get(0);
                    menuItemSessionManager.fire();
                }
            }
        });
        // end implement drag n drop of files ===================================================================
        reportsMenu.setOnShowing(event -> {
            try {
                if (analysis.getAnalysisMethod() != null) {
                    buildCustomReportMenu();
                } else {
                    customReportMenu.getItems().clear();
                }

            } catch (TripoliException | IOException ignored) {
            }
        });
        Platform.runLater(this::detectMassSpecContext);
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

    public void detectMassSpecContext() {
        MassSpectrometerContextEnum currentMassSpec;
        if (tripoliPersistentState == null
                || tripoliPersistentState.getTripoliPersistentParameters().getMassSpectrometerContext() == null
                || tripoliPersistentState.getTripoliPersistentParameters().getMassSpectrometerContext() == MassSpectrometerContextEnum.UNKNOWN) {
            currentMassSpec = TripoliMessageDialog.showMassSpecChoiceDialog(
                    "Please choose a supported mass spectrometer type:", primaryStageWindow);
            tripoliPersistentState.getTripoliPersistentParameters().setMassSpectrometerContext(currentMassSpec);
            tripoliPersistentState.updateTripoliPersistentState();
        }

        currentMassSpec = tripoliPersistentState.getTripoliPersistentParameters().getMassSpectrometerContext();
        if (analysis != null) {
            currentMassSpec = analysis.getParameters().getMassSpectrometerContext();
        }
        processLiveDataMenuItem.setVisible(false);
        if (currentMassSpec != null) {
            TripoliGUI.updateStageTitle(currentMassSpec);
            processLiveDataMenuItem.setVisible(currentMassSpec.getMassSpectrometerName().equals("Phoenix"));
        }
    }

    private void showStartingMenus() {
        sessionManagerMenuItem.setDisable(true);
        newSessionMenuItem.setDisable(false);
        saveSessionMenuItem.setDisable(true);
        saveSessionAsMenuItem.setDisable(true);
        closeSessionMenuItem.setDisable(true);

        analysisMenu.setDisable(false);
        manageAnalysisMenuItem.setDisable(true);

        reportsMenu.setDisable(true);
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
        analysisMenu.setDisable(false);
        manageAnalysisMenuItem.setDisable(false);
        if (analysis != null) {
            reportsMenu.setDisable(false);
        }
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
    public void buildCustomReportMenu() throws TripoliException, IOException {
        List<Report> reportTreeList = analysis.getMethod().getReports();
        customReportMenu.getItems().clear();

        Report fullReport = Report.createFullReport("Full Report", analysis);
        MenuItem menuItem = new MenuItem(fullReport.getReportName());
        menuItem.setOnAction((ActionEvent t) -> {openCustomReport(fullReport);});
        customReportMenu.getItems().add(0, menuItem);

        for (Report report : reportTreeList) {
            customReportMenu.getItems().add(1, new SeparatorMenuItem());
            menuItem = new MenuItem(report.getReportName());
            menuItem.setOnAction((ActionEvent t) -> {openCustomReport(report);});
            customReportMenu.getItems().add(menuItem);
        }
    }

    private void openCustomReport(Report report) {
        if (listOfSelectedAnalyses == null || listOfSelectedAnalyses.isEmpty()) {
            return;
        }

        String referenceMethodName = listOfSelectedAnalyses.get(0).getMethod().getMethodName();

        if (listOfSelectedAnalyses.stream()
                .allMatch(analysis ->
                        analysis.getMethod().getMethodName().equals(referenceMethodName))){
            ReportBuilderController.loadReportBuilder(report, listOfSelectedAnalyses);
        } else {
            TripoliMessageDialog.showWarningDialog("All selected analyses must have the same method name.", primaryStage);
        }
    }

    @FXML
    void sessionManagerMenuItemAction() throws IOException, TripoliException {
        launchSessionManager();
    }

    public void newSessionMenuItemAction() throws IOException, JAXBException, TripoliException {
        tripoliSession = Session.initializeDefaultSession();
        SessionManagerController.tripoliSession = tripoliSession;
        launchSessionManager();
    }

    public void openSessionMenuItemAction() throws IOException, TripoliException {
        confirmSaveOnProjectClose();
        removeAllManagers();
//        launchSessionManager();

        try {
            sessionFileName = selectSessionFile(primaryStageWindow);
            openSession(sessionFileName);
        } catch (IOException | TripoliException ignored) {
        }
    }

    private void openSession(String aSessionFileName) throws IOException, TripoliException {
        if (!"".equals(aSessionFileName)) {
            sessionFileName = aSessionFileName;
            File sessionFile = new File(sessionFileName);
//            Session.setSessionChanged(true);
            confirmSaveOnProjectClose();
            tripoliSession = (Session) TripoliSerializer.getSerializedObjectFromFile(sessionFileName, true);

            if (null != tripoliSession) {
                SessionManagerController.tripoliSession = tripoliSession;
                tripoliPersistentState.updateSessionListMRU(sessionFile);

                handleExpressionsInSavedSession();

                detectMassSpecContext();
                buildSessionMenuMRU();
                tripoliPersistentState.setMRUSessionFolderPath(sessionFile.getParent());
                launchSessionManager();
                saveSessionMenuItem.setDisable(false);

//                squidProjectOriginalHash = squidProject.hashCode();
//                runSaveMenuDisableCheck = true;
            } else {
                saveSessionMenuItem.setDisable(true);
                detectMassSpecContext();
                throw new IOException();
            }
        }
    }

    public static void handleExpressionsInSavedSession() {
        List<AnalysisInterface> listOfAnalyses = tripoliSession.getMapOfAnalyses().values().stream().toList();

        StringBuilder expressionDiffReport = new StringBuilder();
        String headerLeft = "[" + tripoliSession.getSessionName() + "]";
        String headerRight = "Method Defaults";
        expressionDiffReport.append(String.format("%-" + 60 + "s%s%n", headerLeft, headerRight));

        boolean expressionMismatch = false;
        boolean proceed = true;

        for (AnalysisInterface analysisSingleton : listOfAnalyses) {
            List<UserFunction> customExpressionsInAnalysis = analysisSingleton.getUserFunctions().stream()
                    .filter(UserFunction::isTreatAsCustomExpression)
                    .toList();

            String methodName = analysisSingleton.getMassSpecExtractedData().getHeader().methodName();

            AnalysisMethodPersistance analysisMethodPersistance =
                    tripoliPersistentState.getMapMethodNamesToDefaults().get(methodName);

            if (analysisMethodPersistance == null) {
                proceed = false;
                expressionDiffReport.append("No method definition found for method: ")
                        .append(methodName)
                        .append("\n\n");
                continue;
            }

            List<UserFunction> customExpressionsInMethod = analysisMethodPersistance.getExpressionUserFunctionList();

            boolean foundMismatch = false;

            // Mismatch: in analysis but not in method
            for (UserFunction customExpression : customExpressionsInAnalysis) {
                boolean foundInMethod = customExpressionsInMethod.stream()
                        .anyMatch(methodExpr -> methodExpr.getName().equals(customExpression.getName()));
                if (!foundInMethod) {
                    foundMismatch = true;
                    break;
                }
            }

            // Mismatch: in method but not in analysis
            for (UserFunction methodExpression : customExpressionsInMethod) {
                boolean foundInAnalysis = customExpressionsInAnalysis.stream()
                        .anyMatch(analysisExpr -> analysisExpr.getName().equals(methodExpression.getName()));
                if (!foundInAnalysis) {
                    foundMismatch = true;
                    break;
                }
            }

            if (foundMismatch) {
                expressionMismatch = true;

                String secondLineLeft = "[" + analysisSingleton.getAnalysisName() + "] Custom Expressions";

                expressionDiffReport.append(String.format("%-" + 60 + "s%n", secondLineLeft));

                int maxRows = Math.max(customExpressionsInMethod.size(), customExpressionsInAnalysis.size());
                for (int i = 0; i < maxRows; i++) {
                    String sessionExpr = i < customExpressionsInAnalysis.size()
                            ? customExpressionsInAnalysis.get(i).getName()
                            : "";
                    String methodExpr = i < customExpressionsInMethod.size()
                            ? customExpressionsInMethod.get(i).getName()
                            : "";


                    expressionDiffReport.append(String.format("%-" + 60 + "s%s%n", sessionExpr, methodExpr));
                }

                expressionDiffReport.append("\n\n");
            }
        }

        if (expressionMismatch){
            proceed = TripoliMessageDialog.showSessionDiffDialog(
                    String.valueOf(expressionDiffReport), primaryStageWindow);
        }

        if (proceed) {
            for (AnalysisInterface analysisSingleton : listOfAnalyses) {
                List<UserFunction> functionsToRemove = analysisSingleton.getUserFunctions().stream()
                        .filter(UserFunction::isTreatAsCustomExpression)
                        .toList();

                analysisSingleton.getUserFunctions().removeAll(functionsToRemove);

                AnalysisMethodPersistance analysisMethodPersistance =
                        tripoliPersistentState.getMapMethodNamesToDefaults().get(analysisSingleton.getMassSpecExtractedData().getHeader().methodName());

                List<UserFunction> customExpressionInAnalysisMethod = analysisMethodPersistance.getExpressionUserFunctionList();

                analysisSingleton.getUserFunctions().addAll(customExpressionInAnalysisMethod);

                tripoliSession.setExpressionRefreshed(true);
            }
        } else {
            tripoliSession.setExpressionRefreshed(false);
        }

    }

    public void openDemonstrationSessionMenuItemAction() throws IOException, TripoliException {
        tripoliSession = SessionBuiltinFactory.sessionsBuiltinMap.get(TRIPOLI_DEMONSTRATION_SESSION);
        SessionManagerController.tripoliSession = tripoliSession;
        launchSessionManager();

    }

    public void saveSessionMenuItemAction() {
        if (null != tripoliSession) {
            try {
                serializeObjectToFile(tripoliSession, tripoliPersistentState.getMRUSessionFile().getAbsolutePath());
                Session.setSessionChanged(false);
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
            tripoliSession.setExpressionRefreshed(false);
            File sessionFile = saveSessionFile(tripoliSession, primaryStageWindow);
            if (null != sessionFile) {
                sessionFileName = sessionFile.getPath();
                tripoliSession.setSessionFilePathAsString(sessionFileName);
                saveSessionMenuItem.setDisable(false);
                tripoliPersistentState.updateSessionListMRU(sessionFile);
                detectMassSpecContext();
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
    void closeSessionMenuItemAction() throws TripoliException, IOException {
        confirmSaveOnProjectClose();
        removeAllManagers();
        //       launchSessionManager();
        analysis = null;
        tripoliSession = null;
        detectMassSpecContext();
        SessionManagerController.tripoliSession = tripoliSession;
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
                        saveSessionFile(tripoliSession, primaryStageWindow);
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
        if (analysis != null) {
            removeAllManagers();

            if(((Analysis)analysis).hasMemberAnalyses()) {
                ((Analysis) analysis).updateConcatenatedAnalysis();
            }

            analysesManagerUI = FXMLLoader.load(getClass().getResource("AnalysesManager.fxml"));
            analysesManagerUI.setId("AnalysesManager");

            AnchorPane.setLeftAnchor(analysesManagerUI, 0.0);
            AnchorPane.setRightAnchor(analysesManagerUI, 0.0);
            AnchorPane.setTopAnchor(analysesManagerUI, 0.0);
            AnchorPane.setBottomAnchor(analysesManagerUI, 0.0);

            splashAnchor.getChildren().add(analysesManagerUI);
            analysesManagerUI.setVisible(true);
        }
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
        MCMCPlotsWindow = new MCMCPlotsWindow(primaryStage, null);
        MCMCPlotsWindow.loadPlotsWindow();
    }

    @FXML
    private void showDemo2() {
        PeakShapePlotsWindow peakShapePlotsWindow = new PeakShapePlotsWindow(primaryStage);
        peakShapePlotsWindow.loadPlotsWindow();
    }

    public void settingsMenuMenuItemOnAction() throws TripoliException {
        if (settingsWindow == null) {
            settingsWindow =
                    SettingsWindow.requestSettingsWindow(
                            primaryStage,
                            new DelegateActionSet(),
                            analysis != null ? analysis : initializeNewAnalysis(0),
                            SettingsRequestType.MENU_ITEM);
        }
        settingsWindow.show();
    }


    public void newAnalysisMenuItemOnAction() throws TripoliException {
        if (tripoliSession == null) {
            MenuItem menuItemSessionNew = ((MenuBar) primaryStage.getScene()
                    .getRoot().getChildrenUnmodifiable().get(0)).getMenus().get(0).getItems().get(2);
            menuItemSessionNew.fire();
        }
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
        reportsMenu.setDisable(false);
        manageAnalysisMenuItem.setDisable(false);
        // manage analysis
        MenuItem menuItemAnalysesManager = ((MenuBar) primaryStage.getScene()
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


    public void etReduxExportAction() {
        ETReduxFraction etReduxFraction = analysis.prepareFractionForETReduxExport();
        String fileName = etReduxFraction.getSampleName() + "_" + etReduxFraction.getFractionID() + "_" + etReduxFraction.getEtReduxExportType() + ".xml";
        etReduxFraction.serializeXMLObject(fileName);
        try {
            saveExportFile(etReduxFraction, null);
        } catch (IOException e) {
//TODO:            throw new RuntimeException(e);
        } catch (TripoliException e) {
// TODO:           throw new RuntimeException(e);
        }
    }

    public void clipboardExportAction() {
        String clipBoardString = analysis.prepareFractionForClipboardExport();
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(clipBoardString);
        clipboard.setContent(content);
    }

    public void showTripoliDiscussionsAction() {
        BrowserControl.showURI("https://github.com/CIRDLES/Tripoli/discussions");
    }

    public void showTripoliUserManual() {
        BrowserControl.showURI("https://cirdles.org/tripoli-manual");
    }

    // ------------------ LiveData Methods ------------------------------------------------

    public void processLiveData() throws IOException, TripoliException {
        // Handles halting the processing. Two active cases are either:
        // Logs & Finish watchers are running OR Status watcher is running
        if (liveDataLogThread != null && liveDataLogThread.isAlive()){
            liveDataLogWatcher.stop();
            liveDataFinishFileWatcher.stop();
            processLiveDataMenuItem.textProperty().set("Start LiveData");
            return;
        } else if (liveDataStatusThread != null && liveDataStatusThread.isAlive()){
            liveDataStatusWatcher.stop();
            processLiveDataMenuItem.textProperty().set("Start LiveData");
            return;
        }

        Path liveDataFolderPath = null;
        // Check for MRU Folder
        // Prompt if MRU doesnt exist
        while (liveDataFolderPath == null) {
            if (tripoliPersistentState == null || tripoliPersistentState.getMRUDataFileFolderPath() == null) {
                File methodFolder = selectMethodFolder(primaryStageWindow);
                if (methodFolder == null) return; // User cancelled, bail
                liveDataFolderPath = PhoenixLiveData.getLiveDataFolderPath(methodFolder);
                // Handle data file folder
            } else if (new File (Path.of(tripoliPersistentState.getMRUDataFileFolderPath()).getParent() + File.separator + "LiveDataStatus.txt").exists()) {
                Path mruDataFolderPath = Path.of(tripoliPersistentState.getMRUDataFileFolderPath()).getParent();
                liveDataFolderPath = PhoenixLiveData.getLiveDataFolderPath(mruDataFolderPath.toFile());
                // Handle root folder
            } else if (new File (Path.of(tripoliPersistentState.getMRUDataFileFolderPath()) + File.separator + "LiveDataStatus.txt").exists()){
                Path mruDataFolderPath = Path.of(tripoliPersistentState.getMRUDataFileFolderPath());
                liveDataFolderPath = PhoenixLiveData.getLiveDataFolderPath(mruDataFolderPath.toFile());
            } else {
                tripoliPersistentState.setMRUDataFileFolderPath(null);
            }
        }
        tripoliPersistentState.setMRUDataFileFolderPath(liveDataFolderPath.getParent().toString());

        Path methodFolderPath = liveDataFolderPath.getParent().getParent();
        boolean finalFileExists = PhoenixLiveData.getFinishedFile(methodFolderPath.toFile()).exists();
        if (finalFileExists) {
            waitForLiveDataStatusUpdate(methodFolderPath);
            TripoliMessageDialog.showInfoDialog("Finished analysis was found in current directory. Waiting for new analysis to start...", primaryStageWindow);
        } else {
            processLiveDataOnNewFolder(liveDataFolderPath);
        }
        processLiveDataMenuItem.textProperty().set("Stop LiveData");
    }

    private void attachAnalysisToSession(AnalysisInterface newAnalysis) {
        // New session
        if (tripoliSession == null) {
            MenuItem menuItemSessionNew = ((MenuBar) primaryStage.getScene()
                    .getRoot().getChildrenUnmodifiable().get(0)).getMenus().get(0).getItems().get(2);
            menuItemSessionNew.fire();
        }
        tripoliSession.addAnalysis(newAnalysis);
        analysis = newAnalysis;

        MenuItem menuItemSessionManager = ((MenuBar) primaryStage.getScene()
                .getRoot().getChildrenUnmodifiable().get(0)).getMenus().get(0).getItems().get(0);
        menuItemSessionManager.fire();
    }
    private void removeAnalysisFromSession(AnalysisInterface analysisToRemove){
        if (tripoliSession == null) {
            return;
        }
        tripoliSession.getMapOfAnalyses().remove(analysisToRemove.getAnalysisName());
        MenuItem menuItemSessionManager = ((MenuBar) primaryStage.getScene()
                .getRoot().getChildrenUnmodifiable().get(0)).getMenus().get(0).getItems().get(0);
        menuItemSessionManager.fire();
    }

    /**
     * Resets the analysis data used in live data to allow the plots data to recalculate with every update. Calls the plots window
     * on the first update.
     * @param liveDataAnalysis The analysis that holds the livedata points
     */
    public void onLiveDataUpdated(AnalysisInterface liveDataAnalysis) {
        if (tripoliSession == null || !tripoliSession.getMapOfAnalyses().containsKey(liveDataAnalysis.getAnalysisName())) {
            attachAnalysisToSession(liveDataAnalysis);
        }

        liveDataAnalysis.getMapOfBlockIdToRawDataLiteOne().clear();
        AllBlockInitForMCMC.PlottingData plottingData = AllBlockInitForDataLiteOne.initBlockModels(liveDataAnalysis);

        if (plottingData != null) {
            OGTripoliViewController.analysis = liveDataAnalysis;
            if (ogTripoliPreviewPlotsWindow != null) {
                ogTripoliPreviewPlotsWindow.setPlottingData(plottingData);
                ogTripoliPreviewPlotsWindow.getOgTripoliViewController().replotAllPlots();
            } else {
                ogTripoliPreviewPlotsWindow = new OGTripoliPlotsWindow(primaryStage, null, plottingData);
                ogTripoliPreviewPlotsWindow.loadPlotsWindow();
            }
        }
    }

    private void handleFinalFileProcessing(Path newFilePath) {
        if (newFilePath == null) {
            return;
        }
        String finishedFileName = newFilePath.getFileName().toString();
        if (finishedFileName.endsWith(".TIMSDP")) {
            AnalysisInterface analysisProposed;
            try {
                // Make finished analysis
                analysisProposed = initializeNewAnalysis(0);
                String analysisName = analysisProposed.extractMassSpecDataFromPath(newFilePath);
                analysisProposed.setAnalysisName(analysisName);
                analysisProposed.setAnalysisStartTime(analysisProposed.getMassSpecExtractedData().getHeader().analysisStartTime());

                // init in UI
                attachAnalysisToSession(analysisProposed);
                AllBlockInitForDataLiteOne.initBlockModels(analysisProposed);

                // Merge with LiveData changes
                phoenixLiveData.mergeFinalFile(analysisProposed);

                // Cleanup
                removeAnalysisFromSession(phoenixLiveData.getLiveDataAnalysis());

                waitForLiveDataStatusUpdate(newFilePath.getParent().getParent());
                TripoliMessageDialog.showInfoDialog("Analysis has finished and was loaded. Waiting for new analysis to start...", primaryStageWindow);

            } catch (TripoliException | JAXBException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException | IOException e) {
                e.printStackTrace();
            }

        }
    }
    private void waitForLiveDataStatusUpdate(Path parentFolder) throws IOException {
        liveDataStatusWatcher = new FileWatcher(parentFolder, (filePath, kind) -> {
            if (kind == StandardWatchEventKinds.ENTRY_MODIFY &&
                    filePath.getFileName().toString().equals("LiveDataStatus.txt")) {

                // Stop watching once we detect change
                liveDataStatusWatcher.stop();

                // Read new folder path from status file
                Path newLiveDataFolder = PhoenixLiveData.getLiveDataFolderPath(parentFolder.toFile());

                // Update MRU path
                tripoliPersistentState.setMRUDataFileFolderPath(newLiveDataFolder.getParent().toString());

                // Resume normal live data processing on new folder
                Platform.runLater(() -> {
                    try {
                        processLiveDataOnNewFolder(newLiveDataFolder);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });

        liveDataStatusThread = new Thread(liveDataStatusWatcher);
        liveDataStatusThread.setDaemon(true);
        liveDataStatusThread.start();
    }
    private void processLiveDataOnNewFolder(Path liveDataFolderPath) throws TripoliException {
        phoenixLiveData = new PhoenixLiveData();
        ogTripoliPreviewPlotsWindow = null;
        AtomicReference<AnalysisInterface> liveDataAnalysis = new AtomicReference<>(phoenixLiveData.getLiveDataAnalysis());
        liveDataAnalysis.get().setDataFilePathString(liveDataFolderPath.toString());

        Path analysisFolderPath = liveDataFolderPath.getParent();

        liveDataFinishFileWatcher = new FileWatcher(analysisFolderPath, (filePath, kind) -> {
            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                Platform.runLater(() -> handleFinalFileProcessing(filePath));
            }
        });

        liveDataLogWatcher = new FileWatcher(liveDataFolderPath, (filePath, kind) -> {
            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                liveDataAnalysis.set(phoenixLiveData.readLiveDataFile(filePath));
                if (liveDataAnalysis.get() != null) {
                    Platform.runLater(() -> onLiveDataUpdated(liveDataAnalysis.get()));
                }
            }
        });
        liveDataLogWatcher.processExistingFiles(blockCycleComparator);

        liveDataLogThread = new Thread(liveDataLogWatcher);
        liveDataLogThread.setDaemon(true);
        liveDataLogThread.start();

        liveDataFinishFileThread = new Thread(liveDataFinishFileWatcher);
        liveDataFinishFileThread.setDaemon(true);
        liveDataFinishFileThread.start();
    }

    // ------------------ End LiveData Methods ------------------------------------------------

    // ------------------ Import from ogTripoli -----------------------------------------------

    public void importAnalysisAction() throws TripoliException {
        File ogTripoliFile = selectImportFile(primaryStageWindow);
        if (ogTripoliFile == null) {
            return;
        }

        AnalysisInterface proposedAnalysis = OgTripoliImporter.importTripolizedData(ogTripoliFile);

        if (proposedAnalysis == null) {
            TripoliMessageDialog.showWarningDialog("Could not process Tripolized data file.", primaryStageWindow);
            return;
        }
        analysis = proposedAnalysis;
        attachAnalysisToSession(analysis);

    }

    // ------------------ End Import from ogTripoli -------------------------------------------

    public void showTripoliTutorialYoutube() {
        BrowserControl.showURI("https://www.youtube.com/playlist?list=PLfF8bcNRe2WTSMU4sOvDqciajlYi1-CZI");
    }
}