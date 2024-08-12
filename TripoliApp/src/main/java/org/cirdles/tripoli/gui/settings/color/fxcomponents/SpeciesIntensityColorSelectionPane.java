package org.cirdles.tripoli.gui.settings.color.fxcomponents;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
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

    private SpeciesRecordInterface speciesRecordInterface;
    private SpeciesColors speciesColors;
    private Map<SpeciesRecordInterface, SpeciesColors> colorMap;
    private DelegateActionSet delegateActionSet;


    public SpeciesIntensityColorSelectionPane(
                                               SpeciesRecordInterface speciesRecordInterface,
                                               Map<SpeciesRecordInterface, SpeciesColors> colorMap,
                                               DelegateActionSet delegateActionSet) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SpeciesIntensityColorSelectionPane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try{
            fxmlLoader.load();
        } catch (IOException e){
            e.printStackTrace();
        }
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
