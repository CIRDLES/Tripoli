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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.cirdles.tripoli.constants.TripoliConstants;
import org.cirdles.tripoli.expressions.species.SpeciesRecordInterface;
import org.cirdles.tripoli.gui.constants.ConstantsTripoliApp;
import org.cirdles.tripoli.settings.plots.species.SpeciesColors;
import org.cirdles.tripoli.utilities.DelegateActionSet;

import java.io.IOException;
import java.util.Map;

public class SpeciesIntensityColorSelectionPane extends Pane {

    private static final double POINTS_TO_PIXELS = 1.33;
    private static final double TITLE_TO_LABELS_RATIO = 1.05;
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
    private SpeciesRecordInterface speciesRecordInterface;
    private SpeciesColors speciesColors;
    private Map<SpeciesRecordInterface, SpeciesColors> colorMap;
    private DelegateActionSet delegateActionSet;


    public SpeciesIntensityColorSelectionPane(
            SpeciesRecordInterface speciesRecordInterface,
            Map<SpeciesRecordInterface, SpeciesColors> colorMap,
            DelegateActionSet delegateActionSet,
            double prefHeight
    ) {
        // TODO: Replace fxml with row construct
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SpeciesIntensityColorSelectionPane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setPrefHeight(prefHeight);
        this.getChildren().stream().forEach(child -> {
            if (child instanceof Region) {
                ((Region) child).prefHeightProperty().bind(prefHeightProperty().divide(getChildren().size()));
            }
            if (child instanceof Label) {
                ((Label) child).setFont(Font.font(
                        ((Label) child).getFont().getFamily(),
                        FontWeight.BOLD,
                        heightProperty().get() / POINTS_TO_PIXELS
                ));
            }
            if (child instanceof SpeciesColorSelectionRow) {
                SpeciesColorSelectionRow speciesColorSelectionRow = (SpeciesColorSelectionRow) child;
                speciesColorSelectionRow.fontProperty().setValue(
                        Font.font(
                                title.getFont().getFamily(),
                                FontWeight.BOLD,
                                heightProperty().get() / (POINTS_TO_PIXELS * TITLE_TO_LABELS_RATIO))
                );
            }
        });
        this.speciesRecordInterface = speciesRecordInterface;
        this.speciesColors = colorMap.get(speciesRecordInterface);
        this.colorMap = colorMap;
        this.delegateActionSet = delegateActionSet;
        this.title.textProperty().setValue(speciesRecordInterface.prettyPrintShortForm());
        this.faradayDataRow.colorObjectProperty().setValue(
                Color.web(speciesColors.faradayHexColor())
        );
        this.photomultiplierDataRow.colorObjectProperty().setValue(
                Color.web(speciesColors.pmHexColor())
        );
        this.faradayModelRow.colorObjectProperty().setValue(
                Color.web(speciesColors.faradayModelHexColor())
        );
        this.photomultiplierModelRow.colorObjectProperty().setValue(
                Color.web(speciesColors.pmModelHexColor())
        );
        // TODO: to avoid repeating ourselves, lets create one ChangeListener that takes a TripoliConstants.DetectorPlotFlavor
        this.faradayDataRow.colorObjectProperty().addListener((observable, oldValue, newValue) -> {
            colorMap.put(
                    speciesRecordInterface,
                    speciesColors.altered(TripoliConstants.DetectorPlotFlavor.FARADAY_DATA,
                            ConstantsTripoliApp.convertColorToHex(newValue)));
            delegateActionSet.executeDelegateActions();
        });
        this.photomultiplierDataRow.colorObjectProperty().addListener((observable, oldValue, newValue) -> {
            colorMap.put(
                    speciesRecordInterface,
                    speciesColors.altered(TripoliConstants.DetectorPlotFlavor.PM_DATA,
                            ConstantsTripoliApp.convertColorToHex(newValue))
            );
            delegateActionSet.executeDelegateActions();
        });
        this.photomultiplierModelRow.colorObjectProperty().addListener((observable, oldValue, newValue) -> {
            colorMap.put(
                    speciesRecordInterface,
                    speciesColors.altered(TripoliConstants.DetectorPlotFlavor.PM_MODEL,
                            ConstantsTripoliApp.convertColorToHex(newValue))
            );
            delegateActionSet.executeDelegateActions();
        });
        this.faradayModelRow.colorObjectProperty().addListener((observable, oldValue, newValue) -> {
            colorMap.put(
                    speciesRecordInterface,
                    speciesColors.altered(TripoliConstants.DetectorPlotFlavor.FARADAY_MODEL,
                            ConstantsTripoliApp.convertColorToHex(newValue))
            );
            delegateActionSet.executeDelegateActions();
        });
        setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1, 0, 1, 0))));
    }


    public SpeciesRecordInterface getSpeciesRecordInterface() {
        return speciesRecordInterface;
    }

    private void setSpeciesRecordInterface(SpeciesRecordInterface speciesRecordInterface) {
        this.speciesRecordInterface = speciesRecordInterface;
    }

    public SpeciesColors getSpeciesColors() {
        return speciesColors;
    }

    private void setSpeciesColors(SpeciesColors speciesColors) {
        this.speciesColors = speciesColors;
    }

    public Map<SpeciesRecordInterface, SpeciesColors> getColorMap() {
        return colorMap;
    }

    private void setColorMap(Map<SpeciesRecordInterface, SpeciesColors> colorMap) {
        this.colorMap = colorMap;
    }

    public DelegateActionSet getDelegateActionSet() {
        return delegateActionSet;
    }

    private void setDelegateActionSet(DelegateActionSet delegateActionSet) {
        this.delegateActionSet = delegateActionSet;
    }

    public void updateColorProperties() {
        faradayDataRow.colorObjectProperty().setValue(Color.web(colorMap.get(speciesRecordInterface).faradayHexColor()));
        photomultiplierDataRow.colorObjectProperty().setValue(Color.web(colorMap.get(speciesRecordInterface).pmHexColor()));
        faradayModelRow.colorObjectProperty().setValue(Color.web(colorMap.get(speciesRecordInterface).faradayModelHexColor()));
        photomultiplierModelRow.colorObjectProperty().setValue(Color.web(colorMap.get(speciesRecordInterface).pmModelHexColor()));
    }
}
