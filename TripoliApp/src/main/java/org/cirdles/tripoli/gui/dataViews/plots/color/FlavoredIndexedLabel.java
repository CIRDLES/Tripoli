package org.cirdles.tripoli.gui.dataViews.plots.color;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

import static org.cirdles.tripoli.constants.TripoliConstants.DetectorPlotFlavor;

public class FlavoredIndexedLabel extends Label {

    private static final double ROW_HEIGHT = 12.0;
    private final int index;
    private final DetectorPlotFlavor plotFlavor;

    public FlavoredIndexedLabel(String text, DetectorPlotFlavor plotFlavor, int index) {
        super(text);
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(2));
        this.setPrefHeight(ROW_HEIGHT);
        this.plotFlavor = plotFlavor;
        this.index = index;
        this.addEventFilter(MouseEvent.MOUSE_CLICKED, click -> {
            if (this.equals(click.getSource())) {
                fireEvent(click.copyFor(getScene(), this));
                click.consume();
            }
        });
    }

    public int getIndex() {
        return this.index;
    }

    public DetectorPlotFlavor getPlotFlavor() {
        return this.plotFlavor;
    }

}
