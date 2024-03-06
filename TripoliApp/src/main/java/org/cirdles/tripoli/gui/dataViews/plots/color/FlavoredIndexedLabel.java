package org.cirdles.tripoli.gui.dataViews.plots.color;

import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

import static org.cirdles.tripoli.constants.TripoliConstants.DetectorPlotFlavor;

public class FlavoredIndexedLabel extends Label {

    private final int index;
    private final DetectorPlotFlavor plotFlavor;

    public FlavoredIndexedLabel(String text, DetectorPlotFlavor plotFlavor, int index) {
        super(text);
        this.plotFlavor = plotFlavor;
        this.index = index;
        this.addEventFilter(MouseEvent.MOUSE_CLICKED, click ->{
            if(this.equals(click.getSource())) {
                fireEvent(click.copyFor(getScene(), this));
                click.consume();
            }
        });
    }

    public int getIndex(){
        return this.index;
    }

    public DetectorPlotFlavor getPlotFlavor(){
        return this.plotFlavor;
    }

}
