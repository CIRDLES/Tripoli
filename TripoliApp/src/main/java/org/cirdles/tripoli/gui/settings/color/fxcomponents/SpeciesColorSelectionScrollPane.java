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
import java.util.ArrayList;

public class SpeciesColorSelectionScrollPane extends ScrollPane {

    @FXML
    private VBox paneVBox;
    @FXML
    private Label title;

    Analysis analysis;
    DelegateActionSet delegateActionSet;
    ArrayList<SpeciesIntensityColorSelectionPane> speciesIntensityColorSelectionPanes;

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
        speciesIntensityColorSelectionPanes = new ArrayList<>();
        for (SpeciesRecordInterface speciesRecordInterface: analysis.getAnalysisMethod().getSpeciesList()) {
            SpeciesIntensityColorSelectionPane pane = new SpeciesIntensityColorSelectionPane(speciesRecordInterface,
                    analysis.getAnalysisMapOfSpeciesToColors(),
                    delegateActionSet);
            paneVBox.getChildren().add(pane);
            speciesIntensityColorSelectionPanes.add(pane);
        }
    }

    public static SpeciesColorSelectionScrollPane buildSpeciesColorSelectionScrollPane(Analysis analysis, DelegateActionSet delegateActionSet) {
        return new SpeciesColorSelectionScrollPane(analysis, delegateActionSet);
    }

    public VBox getPaneVBox() {
        return paneVBox;
    }

    public Label getTitle() {
        return title;
    }

    public ArrayList<SpeciesIntensityColorSelectionPane> getSpeciesIntensityColorSelectionPanes() {
        return speciesIntensityColorSelectionPanes;
    }

    public DelegateActionSet getDelegateActionSet() {
        return delegateActionSet;
    }
}
