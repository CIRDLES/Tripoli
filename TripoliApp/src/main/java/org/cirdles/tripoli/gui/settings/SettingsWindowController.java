package org.cirdles.tripoli.gui.settings;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsWindowController implements Initializable {

    @FXML
    private VBox root;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public VBox getRoot() {
        return root;
    }
}
