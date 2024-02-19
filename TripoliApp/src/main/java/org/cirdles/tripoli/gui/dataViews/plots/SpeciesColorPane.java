package org.cirdles.tripoli.gui.dataViews.plots;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.cirdles.tripoli.constants.TripoliConstants;
import org.cirdles.tripoli.species.SpeciesColors;

import java.util.Map;
import java.util.TreeMap;

public class SpeciesColorPane extends Pane {

    private static final double TITLE_FONT_SIZE = 19;

    private int speciesIndex;
    private String speciesName;
    private SpeciesColors speciesColors;
    private ColorPicker colorPickerReference;
    private PlotWallPaneIntensities plotWallPaneReference;
    private Map<Integer, SpeciesColors> mapOfSpeciesToColors;
    private VBox colorRowContainer;

    public SpeciesColorPane(int speciesIndex,
                            Map<Integer, SpeciesColors> mapOfSpeciesToColors,
                            String speciesName,
                            SpeciesColors speciesColors,
                            ColorPicker colorPickerReference,
                            PlotWallPaneIntensities plotWallPaneReference) {
        super();
        this.speciesIndex = speciesIndex;
        this.mapOfSpeciesToColors = mapOfSpeciesToColors;
        this.speciesName = speciesName;
        this.speciesColors = speciesColors;
        this.colorPickerReference = colorPickerReference;
        this.plotWallPaneReference = plotWallPaneReference;
        VBox vBox = initializeAndAddVbox();
        vBox.getChildren().add(initializeAndAddHbox());

    }

    private HBox initializeAndAddHbox() {
        HBox hBox = new HBox();
        colorRowContainer = new VBox();
        hBox.getChildren().add(colorRowContainer);
        colorRowContainer.getChildren().add(new ColorRow(
                TripoliConstants.DetectorPlotFlavor.FARADAY_DATA,
                speciesColors.faradayHexColor(),
                this.colorPickerReference,
                this
        ));
        colorRowContainer.getChildren().add(new ColorRow(
                TripoliConstants.DetectorPlotFlavor.PM_DATA,
                speciesColors.pmHexColor(),
                this.colorPickerReference,
                this
        ));
        colorRowContainer.getChildren().add(new ColorRow(
                TripoliConstants.DetectorPlotFlavor.FARADAY_MODEL,
                speciesColors.faradayModelHexColor(),
                this.colorPickerReference,
                this
        ));
        colorRowContainer.getChildren().add(new ColorRow(
                TripoliConstants.DetectorPlotFlavor.PM_MODEL,
                speciesColors.pmModelHexColor(),
                this.colorPickerReference,
                this
        ));
        this.colorPickerReference.getCustomColors().addAll(
                Color.web(speciesColors.faradayHexColor()),
                Color.web(speciesColors.pmHexColor()),
                Color.web(speciesColors.faradayModelHexColor()),
                Color.web(speciesColors.pmModelHexColor())
        );
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

    public void requestRebuildPlot() {
        mapOfSpeciesToColors.put(speciesIndex, reportNewSpeciesColors());
        plotWallPaneReference.rebuildPlot(false,false);
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

    public SpeciesColors reportNewSpeciesColors() {
        Map<TripoliConstants.DetectorPlotFlavor, String> mapOfPlotFlavorToHexColor = new TreeMap<>();
        for(Node node : colorRowContainer.getChildren()) {
            if(node instanceof ColorRow) {
                ColorRow row = (ColorRow) node;
                mapOfPlotFlavorToHexColor.put(row.getPlotFlavor(), row.getHexColor());
            }
        }
        return new SpeciesColors(
                mapOfPlotFlavorToHexColor.get(TripoliConstants.DetectorPlotFlavor.FARADAY_DATA),
                mapOfPlotFlavorToHexColor.get(TripoliConstants.DetectorPlotFlavor.PM_DATA),
                mapOfPlotFlavorToHexColor.get(TripoliConstants.DetectorPlotFlavor.FARADAY_MODEL),
                mapOfPlotFlavorToHexColor.get(TripoliConstants.DetectorPlotFlavor.PM_MODEL)
        );
    }
}
