package org.cirdles.tripoli.gui.dataViews.plots.color;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import static org.cirdles.tripoli.constants.TripoliConstants.DetectorPlotFlavor;
import static org.cirdles.tripoli.gui.constants.ConstantsTripoliApp.TRIPOLI_HIGHLIGHTED_HEX;

import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.cirdles.tripoli.gui.dataViews.plots.Selectable;
import org.cirdles.tripoli.species.SpeciesColors;

import java.util.Map;
import java.util.TreeMap;

public class SpeciesColorPane extends Pane implements Selectable {


    private final Map<DetectorPlotFlavor, SpeciesColorRow> mapOfPlotFlavorsToSpeciesColorRows;
    private SpeciesColors speciesColors;
    private int speciesIndex;
    private String speciesName;
    private Label title;
    private VBox root;

    public SpeciesColorPane(int speciesIndex, String speciesName, SpeciesColors speciesColors) {
        this.root = new VBox();
        getChildren().add(root);
        this.speciesIndex = speciesIndex;
        this.speciesName = speciesName;
        this.speciesColors = speciesColors;
        this.mapOfPlotFlavorsToSpeciesColorRows = new TreeMap<>();
        for (DetectorPlotFlavor plotFlavor: DetectorPlotFlavor.values()) {
            mapOfPlotFlavorsToSpeciesColorRows.put(
                    plotFlavor,new SpeciesColorRow(plotFlavor,
                            Color.web(speciesColors.get(plotFlavor)),
                            speciesIndex));
            mapOfPlotFlavorsToSpeciesColorRows.get(plotFlavor).prefWidthProperty().bind(widthProperty());
        }
        this.title = new Label(speciesName);
        this.title.setAlignment(Pos.CENTER);
        this.title.prefWidthProperty().bind(prefWidthProperty());
        this.title.setFont(new Font( 20.0));
        this.title.setTextAlignment(TextAlignment.CENTER);
        root.getChildren().add(title);
        root.getChildren().addAll(mapOfPlotFlavorsToSpeciesColorRows.values());
        for(Node node : root.getChildren()) {
            node.setStyle("-fx-border-color: black; -fx-border-bottom: 1px");
        }
    }

    @Override
    public void select() {
        Color backgroundColor = Color.web(TRIPOLI_HIGHLIGHTED_HEX, 0.9);
        BackgroundFill fill = new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY);
        this.title.setBackground(new Background(fill));
    }

    @Override
    public void deselect() {
        this.title.setBackground(null);
    }

    public Map<DetectorPlotFlavor, SpeciesColorRow> getMapOfPlotFlavorsToSpeciesColorRows() {
        return mapOfPlotFlavorsToSpeciesColorRows;
    }

    public SpeciesColors getSpeciesColors() {
        return speciesColors;
    }

    public void setSpeciesColors(SpeciesColors speciesColors) {
        this.speciesColors = speciesColors;
    }

    public int getSpeciesIndex() {
        return speciesIndex;
    }

    public void setSpeciesIndex(int speciesIndex) {
        this.speciesIndex = speciesIndex;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public void setSpeciesName(String speciesName) {
        this.speciesName = speciesName;
    }
}
