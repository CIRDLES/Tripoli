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
import org.cirdles.tripoli.species.SpeciesColors;
import org.cirdles.tripoli.utilities.DelegateActionSet;

import java.io.IOException;
import java.util.Map;

public class SpeciesIntensityColorSelectionPane extends Pane {

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

    private static final double POINTS_TO_PIXELS = 1.33;

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
        try{
            fxmlLoader.load();
        } catch (IOException e){
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
        setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2, 0, 0, 0))));
    }



    public SpeciesRecordInterface getSpeciesRecordInterface() {
        return speciesRecordInterface;
    }

    public SpeciesColors getSpeciesColors() {
        return speciesColors;
    }

    public Map<SpeciesRecordInterface, SpeciesColors> getColorMap() {
        return colorMap;
    }

    public DelegateActionSet getDelegateActionSet() {
        return delegateActionSet;
    }

    private void setSpeciesRecordInterface(SpeciesRecordInterface speciesRecordInterface) {
        this.speciesRecordInterface = speciesRecordInterface;
    }

    private void setSpeciesColors(SpeciesColors speciesColors) {
        this.speciesColors = speciesColors;
    }

    private void setColorMap(Map<SpeciesRecordInterface, SpeciesColors> colorMap) {
        this.colorMap = colorMap;
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
