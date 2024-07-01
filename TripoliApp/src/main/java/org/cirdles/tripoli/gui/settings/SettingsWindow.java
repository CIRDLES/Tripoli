package org.cirdles.tripoli.gui.settings;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

public class SettingsWindow {
    private SettingsWindowController controller;
    private Stage stage;
    private static SettingsWindow instance;

    private SettingsWindow(Window owner) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SettingsWindow.fxml"));
        try {
            stage = new Stage();
            stage.setScene(new Scene(fxmlLoader.load()));
            stage.initOwner(owner);
            owner.xProperty().addListener((observable, oldValue, newValue) -> {
                stage.setX(stage.getX() + newValue.doubleValue()- oldValue.doubleValue());
            });
            owner.yProperty().addListener((observable, oldValue, newValue) -> {
                stage.setY(stage.getY() + newValue.doubleValue()- oldValue.doubleValue());
            });

            stage.setTitle("Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SettingsWindow requestSettingsWindow(Window owner) {
        if (instance == null) {
            instance = new SettingsWindow(owner);
        }
        else if (!instance.stage.getOwner().equals(owner)) {
            instance.close();
            instance = new SettingsWindow(owner);
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
