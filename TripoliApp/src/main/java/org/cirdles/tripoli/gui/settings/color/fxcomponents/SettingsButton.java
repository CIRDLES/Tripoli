package org.cirdles.tripoli.gui.settings.color.fxcomponents;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;

import java.io.IOException;

public class SettingsButton extends Button {

    public SettingsButton() throws IOException {
        super();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SettingsButton.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        loader.load();
        setOnAction(clickAction -> {});
    }
}
