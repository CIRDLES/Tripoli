package org.cirdles.tripoli.gui.settings;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.cirdles.tripoli.gui.constants.ConstantsTripoliApp;
import org.cirdles.tripoli.gui.settings.color.fxcomponents.RatioColorSelectionPane;
import org.cirdles.tripoli.sessions.Session;
import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.utilities.DelegateActionSet;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliPersistentState;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliSerializer;

import java.io.IOException;

public class SettingsWindow {
    private RatioColorSelectionPane ratioColorSelectionPane;
    private SettingsWindowController settingsWindowController;
    private Stage stage;
    private  AnalysisInterface analysis;
    private DelegateActionSet repaintDelegateActionSet;

    private String originalTwoSigmaHexColor;
    private String originalOneSigmaHexColor;
    private String originalStdErrHexColor;
    private String originalMeanHexColor;
    private static SettingsWindow instance;

    // Offset variables for dragging the window
    private double offsetX = 0;
    private double offsetY = 0;
    private boolean mousePressed = false;
    //   END OF offset variables

    private SettingsWindow(Window owner, DelegateActionSet delegateActionSet, AnalysisInterface analysis) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SettingsWindow.fxml"));
        try {
            stage = new Stage();
            this.analysis = analysis;
            this.originalTwoSigmaHexColor = analysis.getTwoSigmaHexColorString();
            this.originalOneSigmaHexColor = analysis.getOneSigmaHexColorString();
            this.originalStdErrHexColor = analysis.getTwoStandardErrorHexColorString();
            this.originalMeanHexColor = analysis.getMeanHexColorString();
//            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(fxmlLoader.load()));
            repaintDelegateActionSet = delegateActionSet;
//            stage.getScene().setOnMousePressed(mousePress -> {
//                offsetX = mousePress.getSceneX();
//                offsetY = mousePress.getSceneY();
//            });
//            stage.getScene().setOnMouseDragged(mouseDrag -> {
//                stage.setX(mouseDrag.getScreenX() - offsetX);
//                stage.setY(mouseDrag.getScreenY() - offsetY);
//            });
            settingsWindowController = fxmlLoader.getController();
            settingsWindowController.getRatioColorSelectionAnchorPane().prefWidthProperty().bind(stage.widthProperty());
            stage.initOwner(owner);
            stage.setOnCloseRequest(closeRequest -> {

            });
            owner.xProperty().addListener((observable, oldValue, newValue) -> {
                stage.setX(stage.getX() + newValue.doubleValue()- oldValue.doubleValue());
            });
            owner.yProperty().addListener((observable, oldValue, newValue) -> {
                stage.setY(stage.getY() + newValue.doubleValue()- oldValue.doubleValue());
            });
            ratioColorSelectionPane = new RatioColorSelectionPane(delegateActionSet, analysis);
            settingsWindowController.getRatioColorSelectionAnchorPane().getChildren().clear();
            settingsWindowController.getRatioColorSelectionAnchorPane().getChildren().add(
                    ratioColorSelectionPane
            );
            ratioColorSelectionPane.prefWidthProperty().bind(stage.widthProperty());
            initializeToolbarButtons();
            stage.setTitle("Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SettingsWindow requestSettingsWindow(
            Window owner,
            DelegateActionSet delegateActionSet,
            AnalysisInterface analysis) {
        if (instance == null) {
            instance = new SettingsWindow(owner, delegateActionSet, analysis);
        }
        else if (!instance.stage.getOwner().equals(owner)) {
            instance.close();
            instance = new SettingsWindow(owner, delegateActionSet, analysis);
        }
        return instance;
    }

    public Stage getStage() {return stage;}


    private void centerOverOwner() {
        Window owner = stage.getOwner();
        double ownerX = owner.getX();
        double ownerY = owner.getY();
        double ownerWidth = owner.getWidth();
        double ownerHeight = owner.getHeight();

        double stageWidth = stage.getWidth();
        double stageHeight = stage.getHeight();


        double centerX = ownerX + (ownerWidth - stageWidth) / 2;
        double centerY = ownerY + (ownerHeight - stageHeight) / 2;

        stage.setX(centerX);
        stage.setY(centerY);
    }

    public void initializeToolbarButtons() {
        settingsWindowController.getOkButton().setOnAction(e -> {
            try {
                Session currentSession = ((Analysis) analysis).getParentSession();
                TripoliSerializer.serializeObjectToFile(currentSession,
                    TripoliPersistentState.getExistingPersistentState().getMRUSessionFile().getAbsolutePath());
                close();
            } catch (TripoliException ex) {
                ex.printStackTrace();
            }
        });
        settingsWindowController.getRevertToSavedButton().setOnAction(e -> {
            Session currentSession = ((Analysis) analysis).getParentSession();
            analysis.setTwoSigmaHexColorString(currentSession.getTwoSigmaHexColorString());
            analysis.setOneSigmaHexColorString(currentSession.getOneSigmaHexColorString());
            analysis.setTwoStandardErrorHexColorString(currentSession.getTwoStdErrHexColorString());
            analysis.setMeanHexColorString(currentSession.getMeanHexColorString());
            repaintDelegateActionSet.executeDelegateActions();
            close();
        });
        settingsWindowController.getRestoreDefaultsButton().setOnAction(e -> {
            try{
                TripoliPersistentState tripoliPersistentState = TripoliPersistentState.getExistingPersistentState();
                analysis.setTwoSigmaHexColorString(tripoliPersistentState.getTwoSigmaHexColorString());
                analysis.setOneSigmaHexColorString(tripoliPersistentState.getOneSigmaHexColorString());
                analysis.setTwoStandardErrorHexColorString(tripoliPersistentState.getTwoStdErrHexColorString());
                analysis.setMeanHexColorString(tripoliPersistentState.getMeanHexColorString());
                repaintDelegateActionSet.executeDelegateActions();
                close();
            } catch (TripoliException ex) {
                ex.printStackTrace();
            }
        });
        settingsWindowController.getCancelButton().setOnAction(e -> {
            analysis.setTwoSigmaHexColorString(originalTwoSigmaHexColor);
            analysis.setOneSigmaHexColorString(originalOneSigmaHexColor);
            analysis.setTwoStandardErrorHexColorString(originalStdErrHexColor);
            analysis.setMeanHexColorString(originalMeanHexColor);
            repaintDelegateActionSet.executeDelegateActions();
            close();
        });
    }

    public void close() {
        instance = null;
        stage.close();
    }

    public void show(){
        stage.show();
        centerOverOwner();
    }
}
