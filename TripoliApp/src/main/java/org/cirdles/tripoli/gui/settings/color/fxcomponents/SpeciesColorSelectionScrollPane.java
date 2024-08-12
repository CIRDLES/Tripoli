package org.cirdles.tripoli.gui.settings.color.fxcomponents;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import org.cirdles.tripoli.expressions.species.SpeciesRecordInterface;
import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.utilities.DelegateActionSet;

import java.io.IOException;

public class SpeciesColorSelectionScrollPane extends ScrollPane {

    @FXML
    private VBox paneVBox;
    @FXML
    private Label title;

    Analysis analysis;
    DelegateActionSet delegateActionSet;

    private SpeciesColorSelectionScrollPane(Analysis analysis,
                                           DelegateActionSet delegateActionSet) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SpeciesColorSelectionScrollPane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        this.analysis = analysis;
        this.delegateActionSet = delegateActionSet;
        for (SpeciesRecordInterface speciesRecordInterface: analysis.getAnalysisMethod().getSpeciesList()) {
            paneVBox.getChildren().add(
              new SpeciesIntensityColorSelectionPane(
                      speciesRecordInterface,
                      analysis.getAnalysisMapOfSpeciesToColors(),
                      delegateActionSet
              )
            );
        }
    }

    public static SpeciesColorSelectionScrollPane buildSpeciesColorSelectionScrollPane(Analysis analysis, DelegateActionSet delegateActionSet) {
        return new SpeciesColorSelectionScrollPane(analysis, delegateActionSet);
    }
}
