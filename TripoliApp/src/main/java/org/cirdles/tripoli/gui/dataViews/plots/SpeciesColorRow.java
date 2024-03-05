package org.cirdles.tripoli.gui.dataViews.plots;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static org.cirdles.tripoli.constants.TripoliConstants.DetectorPlotFlavor;

public class SpeciesColorRow extends HBox {

    private static final double COLUMN_WIDTH = 100;
    private DetectorPlotFlavor plotFlavor;
    private Color color;
    private Label colorSplotch;


    public SpeciesColorRow(DetectorPlotFlavor plotFlavor, Color color) {
        this.plotFlavor = plotFlavor;
        this.color = color;
        this.colorSplotch = new Label(" ");
        Background background = new Background(new BackgroundFill(color, new CornerRadii(0.05),new Insets(0.00)));
        this.colorSplotch.setBackground(background);
        this.colorSplotch.prefWidthProperty().bind(widthProperty().divide(2));
        Label plotFlavorLabel = new Label(String.format("%s Color",getPlotFlavor().getName()));
        plotFlavorLabel.prefWidthProperty().bind(widthProperty().divide(2));
        plotFlavorLabel.setFont(new Font("Consolas", 14));
        getChildren().add(plotFlavorLabel);
        getChildren().add(this.colorSplotch);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        colorSplotch.getBackground().getFills().clear();
        colorSplotch.getBackground().getFills().add(new BackgroundFill(this.color,CornerRadii.EMPTY,Insets.EMPTY));
    }

    public Label getColorSplotch() {
        return colorSplotch;
    }

    public void setColorSplotch(Label colorSplotch) {
        this.colorSplotch = colorSplotch;
    }

    public DetectorPlotFlavor getPlotFlavor() {
        return plotFlavor;
    }

    public void setPlotFlavor(DetectorPlotFlavor plotFlavor) {
        this.plotFlavor = plotFlavor;
    }
}
