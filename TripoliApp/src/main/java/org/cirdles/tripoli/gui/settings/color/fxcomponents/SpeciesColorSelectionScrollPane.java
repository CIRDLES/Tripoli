/*
 * Copyright 2022 James Bowring, Noah McLean, Scott Burdick, and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    Analysis analysis;
    DelegateActionSet delegateActionSet;
    ArrayList<SpeciesIntensityColorSelectionPane> speciesIntensityColorSelectionPanes;
    @FXML
    private VBox paneVBox;
    @FXML
    private Label title;

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
        if (analysis.getAnalysisMethod() != null && analysis.getAnalysisMethod().getSpeciesList() != null) {
            for (SpeciesRecordInterface speciesRecordInterface : analysis.getAnalysisMethod().getSpeciesList()) {
                SpeciesIntensityColorSelectionPane pane = new SpeciesIntensityColorSelectionPane(speciesRecordInterface,
                        analysis.getAnalysisMapOfSpeciesToColors(),
                        delegateActionSet,
                        140); // TODO: make this more robust to deal with minimum sized fonts
                paneVBox.getChildren().add(pane);
                speciesIntensityColorSelectionPanes.add(pane);
            }
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
