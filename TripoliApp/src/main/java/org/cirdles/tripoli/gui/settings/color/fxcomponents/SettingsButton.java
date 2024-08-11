package org.cirdles.tripoli.gui.settings.color.fxcomponents;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;

import java.io.IOException;

public final class SettingsButton extends Button {

    public SettingsButton() {
        super();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SettingsButton.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
