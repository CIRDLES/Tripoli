package org.cirdles.tripoli.gui.settings;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
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


    private SettingsWindow(Window owner, DelegateActionSet delegateActionSet, AnalysisInterface analysis) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SettingsWindow.fxml"));
        try {
            stage = new Stage();
            this.analysis = analysis;
            this.originalTwoSigmaHexColor = analysis.getTwoSigmaHexColorString();
            this.originalOneSigmaHexColor = analysis.getOneSigmaHexColorString();
            this.originalStdErrHexColor = analysis.getTwoStandardErrorHexColorString();
            this.originalMeanHexColor = analysis.getMeanHexColorString();
            stage.setScene(new Scene(fxmlLoader.load()));
            repaintDelegateActionSet = delegateActionSet;
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
        settingsWindowController.getSaveAnalysisSettingsButton().setOnAction(e -> {
            try {
                Session currentSession = ((Analysis) analysis).getParentSession();
                TripoliSerializer.serializeObjectToFile(currentSession,
                    TripoliPersistentState.getExistingPersistentState().getMRUSessionFile().getAbsolutePath());
                close();
            } catch (TripoliException ex) {
                ex.printStackTrace();
            }
        });
        settingsWindowController.getSaveAsSessionDefaultsButton().setOnAction(e -> {
            Session currentSession = ((Analysis) analysis).getParentSession();
            currentSession.setTwoSigmaHexColorString(analysis.getTwoSigmaHexColorString());
            currentSession.setOneSigmaHexColorString(analysis.getOneSigmaHexColorString());
            currentSession.setTwoStdErrHexColorString(analysis.getTwoStandardErrorHexColorString());
            currentSession.setMeanHexColorString(analysis.getMeanHexColorString());
            try {
                TripoliSerializer.serializeObjectToFile(currentSession,
                        TripoliPersistentState.getExistingPersistentState().getMRUSessionFile().getAbsolutePath());
            } catch (TripoliException ex) {
                ex.printStackTrace();
            }
        });
        settingsWindowController.getSaveAsUserDefaultsButton().setOnAction(e -> {
            try{
                TripoliPersistentState tripoliPersistentState = TripoliPersistentState.getExistingPersistentState();
                tripoliPersistentState.setTwoSigmaHexColorString(analysis.getTwoSigmaHexColorString());
                tripoliPersistentState.setOneSigmaHexColorString(analysis.getOneSigmaHexColorString());
                tripoliPersistentState.setTwoStdErrHexColorString(analysis.getTwoStandardErrorHexColorString());
                tripoliPersistentState.setMeanHexColorString(analysis.getMeanHexColorString());
                tripoliPersistentState.updateTripoliPersistentState();
            } catch (TripoliException ex) {
                ex.printStackTrace();
            }
        });
        settingsWindowController.getRestoreSessionDefaultsButton().setOnAction(e -> {
            Session currentSession = ((Analysis) analysis).getParentSession();
            analysis.setTwoSigmaHexColorString(currentSession.getTwoSigmaHexColorString());
            analysis.setTwoStandardErrorHexColorString(currentSession.getTwoStdErrHexColorString());
            analysis.setMeanHexColorString(currentSession.getMeanHexColorString());
            analysis.setOneSigmaHexColorString(currentSession.getOneSigmaHexColorString());
            repaintDelegateActionSet.executeDelegateActions();
            updateRatioColorSelectionPane();
//            close();
        });
        settingsWindowController.getRestoreUserDefaultsButton().setOnAction(e -> {
            try{
                TripoliPersistentState tripoliPersistentState = TripoliPersistentState.getExistingPersistentState();
                analysis.setTwoSigmaHexColorString(tripoliPersistentState.getTwoSigmaHexColorString());
                analysis.setOneSigmaHexColorString(tripoliPersistentState.getOneSigmaHexColorString());
                analysis.setTwoStandardErrorHexColorString(tripoliPersistentState.getTwoStdErrHexColorString());
                analysis.setMeanHexColorString(tripoliPersistentState.getMeanHexColorString());
                repaintDelegateActionSet.executeDelegateActions();
                updateRatioColorSelectionPane();
//                close();
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

    public void updateRatioColorSelectionPane() {
        ratioColorSelectionPane.getTwoSigmaSplotch().valueProperty().setValue(
                Color.web(analysis.getTwoSigmaHexColorString()));
        ratioColorSelectionPane.getOneSigmaSplotch().valueProperty().setValue(Color.web(
                analysis.getOneSigmaHexColorString()));
        ratioColorSelectionPane.getStdErrorSplotch().valueProperty().setValue(Color.web(
                analysis.getTwoStandardErrorHexColorString()));
        ratioColorSelectionPane.getMeanSplotch().valueProperty().setValue(Color.web(
                analysis.getMeanHexColorString()));
    };

    public void close() {
        instance = null;
        stage.close();
    }

    public void show(){
        stage.show();
        centerOverOwner();
    }
}
