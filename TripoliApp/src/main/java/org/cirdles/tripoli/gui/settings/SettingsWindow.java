package org.cirdles.tripoli.gui.settings;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.utilities.DelegateActionSet;

import java.io.IOException;

public class SettingsWindow {
    private SettingsWindowController controller;
    private Stage stage;
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
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(fxmlLoader.load()));
            stage.initOwner(owner);
            stage.getScene().setOnMousePressed(mousePress -> {
                    offsetX = mousePress.getSceneX();
                    offsetY = mousePress.getSceneY();
            });
            stage.getScene().setOnMouseDragged(mouseDrag -> {
                stage.setX(mouseDrag.getScreenX() - offsetX);
                stage.setY(mouseDrag.getScreenY() - offsetY);
            });
            controller = fxmlLoader.getController();
            owner.xProperty().addListener((observable, oldValue, newValue) -> {
                stage.setX(stage.getX() + newValue.doubleValue()- oldValue.doubleValue());
            });
            owner.yProperty().addListener((observable, oldValue, newValue) -> {
                stage.setY(stage.getY() + newValue.doubleValue()- oldValue.doubleValue());
            });
            controller.getOneSigmaSplotch().getDelegateActionSet().addDelegateActions(delegateActionSet);
            controller.getOneSigmaSplotch().setHexColorSetter(analysis::setOneSigmaHexColorString);
            controller.getOneSigmaSplotch().getColorPicker().setValue(Color.web(analysis.getOneSigmaHexColorString()));
            controller.getTwoSigmaSplotch().getDelegateActionSet().addDelegateActions(delegateActionSet);
            controller.getTwoSigmaSplotch().setHexColorSetter(analysis::setTwoSigmaHexColorString);
            controller.getTwoSigmaSplotch().getColorPicker().setValue(Color.web(analysis.getTwoSigmaHexColorString()));
            controller.getStdErrorSplotch().getDelegateActionSet().addDelegateActions(delegateActionSet);
            controller.getStdErrorSplotch().setHexColorSetter(analysis::setTwoStandardErrorHexColorString);
            controller.getStdErrorSplotch().getColorPicker().setValue(Color.web(analysis.getTwoStandardErrorHexColorString()));
            controller.getMeanSplotch().getDelegateActionSet().addDelegateActions(delegateActionSet);
            controller.getMeanSplotch().setHexColorSetter(analysis::setMeanHexColorString);
            controller.getMeanSplotch().getColorPicker().setValue(Color.web(analysis.getMeanHexColorString()));
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

    public SettingsWindowController getController() {
        return controller;
    }

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

    public void close() {
        instance = null;
        stage.close();
    }

    public void show(){
        stage.show();
        centerOverOwner();
    }
}
