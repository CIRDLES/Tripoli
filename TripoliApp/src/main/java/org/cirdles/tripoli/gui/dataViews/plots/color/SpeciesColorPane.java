package org.cirdles.tripoli.gui.dataViews.plots.color;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.cirdles.tripoli.gui.dataViews.plots.Highlightable;
import org.cirdles.tripoli.species.SpeciesColors;

import java.util.Map;
import java.util.TreeMap;

import static org.cirdles.tripoli.constants.TripoliConstants.DetectorPlotFlavor;
import static org.cirdles.tripoli.gui.constants.ConstantsTripoliApp.TRIPOLI_HIGHLIGHTED_HEX;

public class SpeciesColorPane extends Pane implements Highlightable {


    private final Map<DetectorPlotFlavor, SpeciesColorRow> mapOfPlotFlavorsToSpeciesColorRows;
    private final Label title;

    public SpeciesColorPane(int speciesIndex, String speciesName, SpeciesColors speciesColors) {
        VBox root = new VBox();
        getChildren().add(root);
        root.prefWidthProperty().bind(prefWidthProperty());
        this.mapOfPlotFlavorsToSpeciesColorRows = new TreeMap<>();
        for (DetectorPlotFlavor plotFlavor : DetectorPlotFlavor.values()) {
            mapOfPlotFlavorsToSpeciesColorRows.put(
                    plotFlavor, new SpeciesColorRow(plotFlavor,
                            Color.web(speciesColors.get(plotFlavor)),
                            speciesIndex));
            mapOfPlotFlavorsToSpeciesColorRows.get(plotFlavor).prefWidthProperty().bind(widthProperty());
        }
        this.title = new Label(speciesName);
        this.title.setAlignment(Pos.CENTER);
        this.title.setTextAlignment(TextAlignment.CENTER);
        this.title.prefWidthProperty().bind(prefWidthProperty());
        this.title.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        this.title.setStyle(this.title.getStyle() + ";;-fx-font-weight: bolder;");
        root.getChildren().add(title);
        root.getChildren().addAll(mapOfPlotFlavorsToSpeciesColorRows.values());
        for (Node node : root.getChildren()) {
            node.setStyle(node.getStyle() + ";;-fx-border-color: black; -fx-border-width: .5px .25px .5px .25px;");

        }
//        title.setStyle(title.getStyle() + ";;-fx-border-color: black; -fx-border-width: .5px 15.75px .5px .25px;");
//        title.setBorder(new Border(new BorderStroke(null,null,null,null)));
    }

    @Override
    public void highlight() {
        Color backgroundColor = Color.web(TRIPOLI_HIGHLIGHTED_HEX, 0.9);
        BackgroundFill fill = new BackgroundFill(backgroundColor, CornerRadii.EMPTY, new Insets(0, -5, 0, 0));
        this.title.setBackground(new Background(fill));
    }

    @Override
    public void removeHighlight() {
        this.title.setBackground(null);
    }

    public Map<DetectorPlotFlavor, SpeciesColorRow> getMapOfPlotFlavorsToSpeciesColorRows() {
        return mapOfPlotFlavorsToSpeciesColorRows;
    }

    public Label getTitle() {
        return title;
    }
}
