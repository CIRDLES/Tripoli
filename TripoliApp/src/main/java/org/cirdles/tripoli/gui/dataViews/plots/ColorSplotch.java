package org.cirdles.tripoli.gui.dataViews.plots;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.cirdles.tripoli.constants.TripoliConstants.DetectorPlotFlavor;

public class ColorSplotch extends Label {

    private final DetectorPlotFlavor plotFlavor;
    private Color color;
    private final int index;

    public ColorSplotch(String text, DetectorPlotFlavor plotFlavor,Color color, int index) {
        super(text);
        this.plotFlavor = plotFlavor;
        setColor(color);
        this.index = index;
    }

    public DetectorPlotFlavor getPlotFlavor() {
        return plotFlavor;
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

    public int getIndex() {
        return index;
    }

}
