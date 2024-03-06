package org.cirdles.tripoli.gui.dataViews.plots;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static org.cirdles.tripoli.constants.TripoliConstants.DetectorPlotFlavor;
import static org.cirdles.tripoli.gui.constants.ConstantsTripoliApp.TRIPOLI_HIGHLIGHTED_HEX;

public class SpeciesColorRow extends HBox implements Selectable {

    private static final String TRIPOLI_HIGHLIGHTED_HEX = "#d3d3d3";
    private DetectorPlotFlavor plotFlavor;
    private Color color;
    private final ColorSplotch colorSplotch;
    private final Label plotFlavorLabel;



    public SpeciesColorRow(DetectorPlotFlavor plotFlavor, Color color, int index) {
        this.plotFlavor = plotFlavor;
        this.color = color;
        this.colorSplotch = new ColorSplotch(" ", plotFlavor, color, index);
        this.colorSplotch.prefWidthProperty().bind(widthProperty().divide(2));
        plotFlavorLabel = new Label(String.format("%s Color",getPlotFlavor().getName()));
        plotFlavorLabel.prefWidthProperty().bind(widthProperty().divide(2));
        plotFlavorLabel.setFont(new Font("Consolas", 14));
        getChildren().add(plotFlavorLabel);
        getChildren().add(this.colorSplotch);
    }

    @Override
    public void select() {
        // Implement highlighting for selection
        Color backgroundColor = Color.web(TRIPOLI_HIGHLIGHTED_HEX, 0.9);
        BackgroundFill fill = new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY);
        this.plotFlavorLabel.setBackground(new Background(fill));
    }

    @Override
    public void deselect() {
        this.plotFlavorLabel.setBackground(null);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        colorSplotch.getBackground().getFills().clear();
        colorSplotch.getBackground().getFills().add(new BackgroundFill(this.color,CornerRadii.EMPTY,Insets.EMPTY));
    }

    public ColorSplotch getColorSplotch() {
        return colorSplotch;
    }

    public DetectorPlotFlavor getPlotFlavor() {
        return plotFlavor;
    }

    public void setPlotFlavor(DetectorPlotFlavor plotFlavor) {
        this.plotFlavor = plotFlavor;
    }
}
