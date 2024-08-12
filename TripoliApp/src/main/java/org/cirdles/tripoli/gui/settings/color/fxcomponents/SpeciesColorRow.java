package org.cirdles.tripoli.gui.settings.color.fxcomponents;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.cirdles.tripoli.gui.dataViews.plots.Highlightable;

import static org.cirdles.tripoli.constants.TripoliConstants.DetectorPlotFlavor;
import static org.cirdles.tripoli.gui.constants.ConstantsTripoliApp.TRIPOLI_HIGHLIGHTED_HEX;

public class SpeciesColorRow extends HBox implements Highlightable {

    private Color color;
    private final DetectorPlotFlavor plotFlavor;
    private final FlavoredIndexedColorSplotch flavoredIndexedColorSplotch;
    private final ColorFlavoredIndexedLabel plotFlavorLabel;



    public SpeciesColorRow(DetectorPlotFlavor plotFlavor, Color color, int index) {
        this.plotFlavor = plotFlavor;
        this.color = color;
        this.flavoredIndexedColorSplotch = new FlavoredIndexedColorSplotch(" ", plotFlavor, color, index);
        this.flavoredIndexedColorSplotch.prefWidthProperty().bind(widthProperty().divide(1.5));
        plotFlavorLabel = new ColorFlavoredIndexedLabel(
                String.format("%s",getPlotFlavor().getName()),
                plotFlavor,
                index);
        plotFlavorLabel.prefWidthProperty().bind(widthProperty().divide(3));
        plotFlavorLabel.setFont(new Font("Consolas", 14));
        plotFlavorLabel.setAlignment(Pos.CENTER_LEFT);
        getChildren().add(plotFlavorLabel);
        getChildren().add(this.flavoredIndexedColorSplotch);
    }

    @Override
    public void highlight() {
        Color backgroundColor = Color.web(TRIPOLI_HIGHLIGHTED_HEX, 0.9);
        BackgroundFill fill = new BackgroundFill(backgroundColor, CornerRadii.EMPTY, new Insets(0, 0, 0,0));
        this.plotFlavorLabel.setBackground(new Background(fill));
    }

    @Override
    public void removeHighlight() {
        this.plotFlavorLabel.setBackground(null);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        this.flavoredIndexedColorSplotch.setColor(color);
    }

    public FlavoredIndexedColorSplotch getColorSplotch() {
        return flavoredIndexedColorSplotch;
    }

    public DetectorPlotFlavor getPlotFlavor() {
        return plotFlavor;
    }

}
