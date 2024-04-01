package org.cirdles.tripoli.gui.dataViews.plots.color;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.cirdles.tripoli.gui.dataViews.plots.Highlightable;

import static org.cirdles.tripoli.constants.TripoliConstants.DetectorPlotFlavor;
import static org.cirdles.tripoli.gui.constants.ConstantsTripoliApp.TRIPOLI_HIGHLIGHTED_HEX;

public class SpeciesColorRow extends HBox implements Highlightable {

    private Color color;
    private final DetectorPlotFlavor plotFlavor;
    private final ColorSplotch colorSplotch;
    private final ColorFlavoredIndexedLabel plotFlavorLabel;



    public SpeciesColorRow(DetectorPlotFlavor plotFlavor, Color color, int index) {
        this.plotFlavor = plotFlavor;
        this.color = color;
        this.colorSplotch = new ColorSplotch(" ", plotFlavor, color, index);
        this.colorSplotch.prefWidthProperty().bind(widthProperty().divide(1.5));
        plotFlavorLabel = new ColorFlavoredIndexedLabel(
                String.format("%s",getPlotFlavor().getName()),
                plotFlavor,
                index);
        plotFlavorLabel.prefWidthProperty().bind(widthProperty().divide(3));
        plotFlavorLabel.setFont(new Font("Consolas", 14));
        plotFlavorLabel.setAlignment(Pos.CENTER_LEFT);
        getChildren().add(plotFlavorLabel);
        getChildren().add(this.colorSplotch);
    }

    @Override
    public void highlight() {
        Color backgroundColor = Color.web(TRIPOLI_HIGHLIGHTED_HEX, 0.9);
        BackgroundFill fill = new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY);
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
        this.colorSplotch.setColor(color);
    }

    public ColorSplotch getColorSplotch() {
        return colorSplotch;
    }

    public DetectorPlotFlavor getPlotFlavor() {
        return plotFlavor;
    }

}
