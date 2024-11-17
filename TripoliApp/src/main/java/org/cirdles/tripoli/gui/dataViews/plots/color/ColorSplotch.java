package org.cirdles.tripoli.gui.dataViews.plots.color;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.cirdles.tripoli.constants.TripoliConstants.DetectorPlotFlavor;

public class ColorSplotch extends FlavoredIndexedLabel {

    private Color color;

    public ColorSplotch(String text, DetectorPlotFlavor plotFlavor, Color color, int index) {
        super(text, plotFlavor, index);
        setColor(color);
        this.setStyle("-fx-border-color:black; -fx-border-width: 0px 1px 0px 1px;");
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        Background background = new Background(
                new BackgroundFill(color, CornerRadii.EMPTY, new Insets(0, 0, 0, 0)));
        this.setBackground(background);
        this.color = color;
    }

}
