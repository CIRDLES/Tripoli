package org.cirdles.tripoli.gui.settings;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SettingsWindow {
    private SettingsWindowController controller;
    private Stage stage;

    private SettingsWindow() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SettingsWindow.fxml"));
        try {
            stage = new Stage();
            stage.setScene(new Scene(fxmlLoader.load()));
            stage.setTitle("Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SettingsWindow requestSettingsWindow() {
        return new SettingsWindow();
    }

    public void show(){
        stage.show();
    }
}
