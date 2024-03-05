package org.cirdles.tripoli.gui.dataViews.plots;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import static org.cirdles.tripoli.constants.TripoliConstants.DetectorPlotFlavor;

import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.cirdles.tripoli.species.SpeciesColors;

public class SpeciesColorPane extends Pane {

    private SpeciesColorRow[] speciesColorRows;
    private SpeciesColors speciesColors;
    private int speciesIndex;
    private String speciesName;
    private VBox root;

    public SpeciesColorPane(int speciesIndex, String speciesName, SpeciesColors speciesColors) {
        this.root = new VBox();
        getChildren().add(root);
        this.speciesIndex = speciesIndex;
        this.speciesName = speciesName;
        this.speciesColors = speciesColors;
        this.speciesColorRows = new SpeciesColorRow[4];
        this.speciesColorRows[0] = new SpeciesColorRow(
                DetectorPlotFlavor.FARADAY_DATA, Color.web(speciesColors.faradayHexColor()));
        this.speciesColorRows[0].prefWidthProperty().bind(widthProperty());
        this.speciesColorRows[1] = new SpeciesColorRow(
                DetectorPlotFlavor.PM_DATA, Color.web(speciesColors.pmHexColor()));
        this.speciesColorRows[1].prefWidthProperty().bind(widthProperty());
        this.speciesColorRows[2] = new SpeciesColorRow(
                DetectorPlotFlavor.FARADAY_MODEL, Color.web(speciesColors.faradayModelHexColor()));
        this.speciesColorRows[2].prefWidthProperty().bind(widthProperty());
        this.speciesColorRows[3] = new SpeciesColorRow(
                DetectorPlotFlavor.PM_MODEL, Color.web(speciesColors.pmModelHexColor()));
        this.speciesColorRows[3].prefWidthProperty().bind(widthProperty());
        Label title = new Label(speciesName);
        title.setAlignment(Pos.CENTER);
        title.prefWidthProperty().bind(prefWidthProperty());
        title.setFont(new Font( 20.0));
        title.setTextAlignment(TextAlignment.CENTER);
        root.getChildren().add(title);
        root.getChildren().addAll(speciesColorRows);
        for(Node node : root.getChildren()) {
            node.setStyle("-fx-border-color: black; -fx-border-bottom: 1px");
        }
    }

    public SpeciesColorRow[] getSpeciesColorRows() {
        return speciesColorRows;
    }

    public void setSpeciesColorRows(SpeciesColorRow[] speciesColorRows) {
        this.speciesColorRows = speciesColorRows;
    }

    public SpeciesColors getSpeciesColors() {
        return speciesColors;
    }

    public void setSpeciesColors(SpeciesColors speciesColors) {
        this.speciesColors = speciesColors;
    }

    public int getSpeciesIndex() {
        return speciesIndex;
    }

    public void setSpeciesIndex(int speciesIndex) {
        this.speciesIndex = speciesIndex;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public void setSpeciesName(String speciesName) {
        this.speciesName = speciesName;
    }
}
