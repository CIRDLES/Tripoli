package org.cirdles.tripoli.gui.dataViews.plots.color;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.cirdles.tripoli.constants.TripoliConstants.DetectorPlotFlavor;
import org.cirdles.tripoli.gui.dataViews.plots.color.FlavoredIndexedLabel;

public class ColorSplotch extends FlavoredIndexedLabel {

    private Color color;

    public ColorSplotch(String text, DetectorPlotFlavor plotFlavor,Color color, int index) {
        super(text, plotFlavor, index);
        setColor(color);
    }


    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        Background background = new Background(
                new BackgroundFill(color, CornerRadii.EMPTY,Insets.EMPTY));
        this.setBackground(background);
        this.color = color;
    }

}
