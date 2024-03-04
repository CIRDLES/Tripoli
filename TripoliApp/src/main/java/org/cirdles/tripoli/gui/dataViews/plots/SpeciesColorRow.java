package org.cirdles.tripoli.gui.dataViews.plots;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.cirdles.tripoli.constants.TripoliConstants;
import org.cirdles.tripoli.species.SpeciesColors;

public class SpeciesColorRow extends HBox {

    private static final double COLUMN_WIDTH = 35.0;
    private TripoliConstants.DetectorPlotFlavor plotFlavor;
    private Color color;
    private Label colorSplotch;

    public SpeciesColorRow(TripoliConstants.DetectorPlotFlavor plotFlavor, Color color) {
        this.plotFlavor = plotFlavor;
        this.color = color;
        this.colorSplotch = new Label(" ");
        Background background = new Background(new BackgroundFill(color, new CornerRadii(0.05),new Insets(0.00)));
        this.colorSplotch.setPrefWidth(COLUMN_WIDTH);
        Label plotFlavorLabel = new Label(getPlotFlavor().getName());
        plotFlavorLabel.setPrefWidth(COLUMN_WIDTH);
        getChildren().add(plotFlavorLabel);
        getChildren().add(this.colorSplotch);
    }

    public TripoliConstants.DetectorPlotFlavor getPlotFlavor() {
        return plotFlavor;
    }

    public void setPlotFlavor(TripoliConstants.DetectorPlotFlavor plotFlavor) {
        this.plotFlavor = plotFlavor;
    }
}
