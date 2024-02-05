package org.cirdles.tripoli.gui.dataViews.plots;

import javafx.geometry.Pos;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.cirdles.tripoli.constants.TripoliConstants;
import org.cirdles.tripoli.species.SpeciesColors;

public class SpeciesColorPane extends Pane {

    private static final double TITLE_FONT_SIZE = 19;

    private String speciesName;
    private SpeciesColors speciesColors;
    private ColorPicker colorPickerReference;

    public SpeciesColorPane(String speciesName, SpeciesColors speciesColors, ColorPicker colorPickerReference) {
        super();
        this.speciesName = speciesName;
        this.speciesColors = speciesColors;
        this.colorPickerReference = colorPickerReference;
        VBox vBox = initializeAndAddVbox();
        vBox.getChildren().add(initializeAndAddHbox());

    }

    private HBox initializeAndAddHbox() {
        HBox hBox = new HBox();
        VBox vBox = new VBox();
        hBox.getChildren().add(vBox);
        vBox.getChildren().add(new ColorRow(
                TripoliConstants.DetectorPlotFlavor.FARADAY_DATA,
                speciesColors.faradayHexColor(),
                this.colorPickerReference
        ));
        vBox.getChildren().add(new ColorRow(
                TripoliConstants.DetectorPlotFlavor.PM_DATA,
                speciesColors.pmHexColor(),
                this.colorPickerReference
        ));
        vBox.getChildren().add(new ColorRow(
                TripoliConstants.DetectorPlotFlavor.FARADAY_MODEL,
                speciesColors.faradayModelHexColor(),
                this.colorPickerReference
        ));
        vBox.getChildren().add(new ColorRow(
                TripoliConstants.DetectorPlotFlavor.PM_MODEL,
                speciesColors.pmModelHexColor(),
                this.colorPickerReference
        ));
        return hBox;
    }

    private VBox initializeAndAddVbox() {
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        Text title = new Text(speciesName);
        title.setFont(Font.font(TITLE_FONT_SIZE));
        vBox.getChildren().add(title);
        this.getChildren().add(vBox);
        return vBox;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public void setSpeciesName(String speciesName) {
        this.speciesName = speciesName;
    }

    public SpeciesColors getSpeciesColors() {
        return speciesColors;
    }

    public void setSpeciesColors(SpeciesColors speciesColors) {
        this.speciesColors = speciesColors;
    }
}
