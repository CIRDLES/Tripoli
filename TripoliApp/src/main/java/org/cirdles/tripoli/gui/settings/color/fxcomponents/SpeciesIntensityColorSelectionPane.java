package org.cirdles.tripoli.gui.settings.color.fxcomponents;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.cirdles.tripoli.species.SpeciesColors;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SpeciesIntensityColorSelectionPane extends Pane implements Initializable {

    @FXML
    private Label title;
    @FXML
    private SpeciesColorSelectionRow faradayDataRow;
    @FXML
    private SpeciesColorSelectionRow photomultiplierDataRow;
    @FXML
    private SpeciesColorSelectionRow faradayModelRow;
    @FXML
    private SpeciesColorSelectionRow photomultiplierModelRow;

    private SpeciesColors speciesColors;


    private SpeciesIntensityColorSelectionPane() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SpeciesIntensityColorSelectionPane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try{
            fxmlLoader.load();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static SpeciesIntensityColorSelectionPane buildSpeciesIntensityColorSelectionPane(
            String speciesName,
            SpeciesColors speciesColors ) {
        SpeciesIntensityColorSelectionPane pane = new SpeciesIntensityColorSelectionPane();
        pane.title.textProperty().setValue(speciesName);
        pane.faradayModelRow.colorObjectProperty().setValue(
                Color.web(speciesColors.faradayHexColor())
        );
        pane.photomultiplierDataRow.colorObjectProperty().setValue(
                Color.web(speciesColors.pmHexColor())
        );
        pane.faradayModelRow.colorObjectProperty().setValue(
                Color.web(speciesColors.faradayModelHexColor())
        );
        pane.photomultiplierModelRow.colorObjectProperty().setValue(
                Color.web(speciesColors.pmModelHexColor())
        );
        pane.speciesColors = speciesColors;
        return pane;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
