package org.cirdles.tripoli.gui.settings.color.fxcomponents;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SpeciesColorSelectionScrollPane extends ScrollPane {

    @FXML
    private VBox paneVBox;
    @FXML
    private Label title;

    SpeciesIntensityColorSelectionPane[] speciesIntensityColorSelectionPanes;

    private SpeciesColorSelectionScrollPane() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SpeciesColorSelectionScrollPane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static SpeciesColorSelectionScrollPane buildSpeciesColorSelectionScrollPane() {
        SpeciesColorSelectionScrollPane speciesColorSelectionScrollPane = new SpeciesColorSelectionScrollPane();
        return speciesColorSelectionScrollPane;
    }
}
