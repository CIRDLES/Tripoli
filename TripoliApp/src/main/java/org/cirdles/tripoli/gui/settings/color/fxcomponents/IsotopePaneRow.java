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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.cirdles.tripoli.constants.TripoliConstants;
import org.cirdles.tripoli.expressions.species.SpeciesRecordInterface;
import org.cirdles.tripoli.gui.constants.ConstantsTripoliApp;
import org.cirdles.tripoli.settings.plots.species.SpeciesColors;
import org.cirdles.tripoli.utilities.DelegateActionSet;

import java.util.Map;

public class IsotopePaneRow extends HBox {

    private static final double TITLE_WIDTH = 124;
    private static final double PADDING = 10;
    private static final double COLOR_WIDTH = 90;

    private final Label title;
    private final ColorPickerSplotch faradayData;
    private final ColorPickerSplotch pmData;
    private final ColorPickerSplotch faradayModel;
    private final ColorPickerSplotch pmModel;
    private final Map<SpeciesRecordInterface, SpeciesColors> colorMap;
    private SpeciesRecordInterface speciesRecord;
    private ObjectProperty<SpeciesColors> speciesColorsProperty;
    private boolean speciesColorsSet = false;
    private DelegateActionSet delegateActionSet;


    public IsotopePaneRow(SpeciesRecordInterface speciesRecordInterface, Map<SpeciesRecordInterface, SpeciesColors> colorMap, DelegateActionSet delegateActionSet, double height) {
        this(speciesRecordInterface, colorMap, delegateActionSet);
        this.setPrefHeight(height);
    }

    public IsotopePaneRow(SpeciesRecordInterface speciesRecordInterface,
                          Map<SpeciesRecordInterface, SpeciesColors> colorMap,
                          DelegateActionSet delegateActionSet) {
        super();
        this.speciesRecord = speciesRecordInterface;
        this.speciesColorsProperty = new SimpleObjectProperty<>(colorMap.get(speciesRecordInterface));
        this.delegateActionSet = delegateActionSet;
        this.colorMap = colorMap;

        // Initialize all components
        this.title = new Label(speciesRecordInterface.prettyPrintShortForm());
        this.title.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        this.title.setPrefWidth(TITLE_WIDTH);
        this.faradayData = new ColorPickerSplotch();
        this.faradayData.prefWidthProperty().set(COLOR_WIDTH);
        this.faradayData.colorProperty().addListener(((observable, oldValue, newValue) -> {
            if (!speciesColorsSet) {
                speciesColorsProperty.set(speciesColorsProperty.get().altered(
                        TripoliConstants.DetectorPlotFlavor.FARADAY_DATA,
                        ConstantsTripoliApp.convertColorToHex(newValue)
                ));
            }
            // Change listener only activated when new value is not equal to old value
        }));
        this.faradayData.colorProperty().setValue(
                Color.web(speciesColorsProperty.get().faradayHexColor())
        );
        this.pmData = new ColorPickerSplotch();
        this.pmData.prefWidthProperty().set(COLOR_WIDTH);
        this.pmData.colorProperty().addListener(((observable, oldValue, newValue) -> {
            if (!speciesColorsSet) {
                speciesColorsProperty.set(speciesColorsProperty.get().altered(
                        TripoliConstants.DetectorPlotFlavor.PM_DATA,
                        ConstantsTripoliApp.convertColorToHex(newValue)
                ));
            }
        }));
        this.pmData.colorProperty().setValue(
                Color.web(speciesColorsProperty.get().pmHexColor())
        );
        this.faradayModel = new ColorPickerSplotch();
        this.faradayModel.prefWidthProperty().set(COLOR_WIDTH);
        this.faradayModel.colorProperty().addListener(((observable, oldValue, newValue) -> {
            if (!speciesColorsSet) {
                speciesColorsProperty.set(speciesColorsProperty.get().altered(
                        TripoliConstants.DetectorPlotFlavor.FARADAY_MODEL,
                        ConstantsTripoliApp.convertColorToHex(newValue)
                ));
            }
        }));
        this.faradayModel.colorProperty().setValue(
                Color.web(speciesColorsProperty.get().faradayModelHexColor())
        );
        this.pmModel = new ColorPickerSplotch();
        this.pmModel.prefWidthProperty().set(COLOR_WIDTH);
        this.pmModel.colorProperty().addListener(((observable, oldValue, newValue) -> {
            if (!speciesColorsSet) {
                speciesColorsProperty.set(speciesColorsProperty.get().altered(
                        TripoliConstants.DetectorPlotFlavor.PM_MODEL,
                        ConstantsTripoliApp.convertColorToHex(newValue)
                ));
            }
        }));
        this.pmModel.colorProperty().setValue(
                Color.web(speciesColorsProperty.get().pmModelHexColor())
        );

        this.speciesColorsProperty.addListener((observable, oldValue, newValue) -> {
            speciesColorsSet = true;
            colorMap.put(speciesRecord, newValue);
            delegateActionSet.executeDelegateActions();
            faradayData.colorProperty().set(Color.web(newValue.faradayHexColor()));
            pmData.colorProperty().set(Color.web(newValue.pmHexColor()));
            faradayModel.colorProperty().set(Color.web(newValue.faradayModelHexColor()));
            pmModel.colorProperty().set(Color.web(newValue.pmModelHexColor()));
            speciesColorsSet = false;
        });
        Region spacer1 = new Region();
        Region spacer2 = new Region();
        spacer1.setPrefWidth(PADDING);
        spacer2.setPrefWidth(PADDING * 2);

        // Add components and spacers to the HBox
        this.getChildren().addAll(
                title,
                spacer1,
                faradayData,
                pmData,
                spacer2,
                faradayModel,
                pmModel
        );
        this.getChildren().forEach(child -> {
            if (child instanceof Region region) {
                region.prefHeightProperty().bind(this.heightProperty());
            }
            if (child instanceof ColorPickerSplotch splotch) {
                splotch.textObjectProperty().setValue("Click Me");
            }
        });
        HBox.setMargin(title, new Insets(0, 0, 0, PADDING));// provides a left margin around the species name

    }


    public ObjectProperty<SpeciesColors> speciesColorsProperty() {
        return speciesColorsProperty;
    }

    public void setSpeciesColors(SpeciesColors speciesColors) {
        this.speciesColorsProperty.set(speciesColors);
    }

    public SpeciesRecordInterface getSpeciesRecord() {
        return speciesRecord;
    }
}
