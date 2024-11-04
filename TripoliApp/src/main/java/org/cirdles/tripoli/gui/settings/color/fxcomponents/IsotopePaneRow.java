package org.cirdles.tripoli.gui.settings.color.fxcomponents;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.cirdles.tripoli.constants.TripoliConstants;
import org.cirdles.tripoli.expressions.species.SpeciesRecordInterface;
import org.cirdles.tripoli.gui.constants.ConstantsTripoliApp;
import org.cirdles.tripoli.species.SpeciesColors;
import org.cirdles.tripoli.utilities.DelegateActionSet;

import java.util.Map;

public class IsotopePaneRow extends HBox {

    private static final double TITLE_WIDTH = 124;
    private static final double PADDING = 10;
    private static final double COLOR_WIDTH = 90;
    // The padding is
    private final Label title;
    private final ColorPickerSplotch faradayData;
    private final ColorPickerSplotch pmData;
    private final ColorPickerSplotch faradayModel;
    private final ColorPickerSplotch pmModel;
    private final Map<SpeciesRecordInterface, SpeciesColors> colorMap;
    private SpeciesRecordInterface speciesRecord;
    private ObjectProperty<SpeciesColors> speciesColorsObjectProperty;
    private DelegateActionSet delegateActionSet;



    public IsotopePaneRow(SpeciesRecordInterface speciesRecordInterface, Map<SpeciesRecordInterface, SpeciesColors> colorMap, DelegateActionSet delegateActionSet, double height) {
        this(speciesRecordInterface, colorMap, delegateActionSet);
        this.setPrefHeight(height);
    }

    public IsotopePaneRow(SpeciesRecordInterface speciesRecordInterface,
                          Map<SpeciesRecordInterface, SpeciesColors> colorMap,
                          DelegateActionSet delegateActionSet) {
        this.speciesRecord = speciesRecordInterface;
        this.speciesColorsObjectProperty = new SimpleObjectProperty<>(colorMap.get(speciesRecordInterface));
        this.delegateActionSet = delegateActionSet;
        this.colorMap = colorMap;
        // Initialize all components
        this.title = new Label(speciesRecordInterface.prettyPrintShortForm());
        this.title.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        this.title.setPrefWidth(TITLE_WIDTH);
        this.faradayData = new ColorPickerSplotch();
        this.faradayData.prefWidthProperty().set(COLOR_WIDTH);
        this.faradayData.colorProperty().addListener(((observable, oldValue, newValue) -> {
            // Change listener only activated when new value is not equal to old value
            speciesColorsObjectProperty.set(speciesColorsObjectProperty.get().altered(
                    TripoliConstants.DetectorPlotFlavor.FARADAY_DATA,
                    ConstantsTripoliApp.convertColorToHex(newValue)
            ));
        }));
        this.faradayData.colorProperty().setValue(
                Color.web(speciesColorsObjectProperty.get().faradayHexColor())
        );
        this.pmData = new ColorPickerSplotch();
        this.pmData.prefWidthProperty().set(COLOR_WIDTH);
        this.pmData.colorProperty().addListener(((observable, oldValue, newValue) -> {
            speciesColorsObjectProperty.set(speciesColorsObjectProperty.get().altered(
                    TripoliConstants.DetectorPlotFlavor.PM_DATA,
                    ConstantsTripoliApp.convertColorToHex(newValue)
            ));
        }));
        this.pmData.colorProperty().setValue(
                Color.web(speciesColorsObjectProperty.get().pmHexColor())
        );
        this.faradayModel = new ColorPickerSplotch();
        this.faradayModel.prefWidthProperty().set(COLOR_WIDTH);
        this.faradayModel.colorProperty().addListener(((observable, oldValue, newValue) -> {
            speciesColorsObjectProperty.set(speciesColorsObjectProperty.get().altered(
                    TripoliConstants.DetectorPlotFlavor.FARADAY_MODEL,
                    ConstantsTripoliApp.convertColorToHex(newValue)
            ));
        }));
        this.faradayModel.colorProperty().setValue(
                Color.web(speciesColorsObjectProperty.get().faradayModelHexColor())
        );
        this.pmModel = new ColorPickerSplotch();
        this.pmModel.prefWidthProperty().set(COLOR_WIDTH);
        this.pmModel.colorProperty().addListener(((observable, oldValue, newValue) -> {
            speciesColorsObjectProperty.set(speciesColorsObjectProperty.get().altered(
                    TripoliConstants.DetectorPlotFlavor.PM_MODEL,
                    ConstantsTripoliApp.convertColorToHex(newValue)
            ));
        }));
        this.pmModel.colorProperty().setValue(
                Color.web(speciesColorsObjectProperty.get().pmModelHexColor())
        );

        this.speciesColorsObjectProperty.addListener((observable, oldValue, newValue) -> {
            colorMap.put(speciesRecord, newValue);
            delegateActionSet.executeDelegateActions();
            faradayData.colorProperty().set(Color.web(newValue.faradayHexColor()));
            pmData.colorProperty().set(Color.web(newValue.pmHexColor()));
            faradayModel.colorProperty().set(Color.web(newValue.faradayModelHexColor()));
            pmModel.colorProperty().set(Color.web(newValue.pmModelHexColor()));
        });
        Region spacer1 = new Region();
        Region spacer2 = new Region();
        spacer1.setPrefWidth(PADDING);
        spacer2.setPrefWidth(PADDING*2);

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
            if (child instanceof Region) {
                Region region = (Region) child;
                region.prefHeightProperty().bind(this.heightProperty());
            }
            if (child instanceof ColorPickerSplotch) {
                ColorPickerSplotch splotch = (ColorPickerSplotch) child;
                splotch.textObjectProperty().setValue("Click Me");
            }
        });

    }
}
