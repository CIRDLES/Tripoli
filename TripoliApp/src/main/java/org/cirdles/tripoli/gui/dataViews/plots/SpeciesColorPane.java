package org.cirdles.tripoli.gui.dataViews.plots;

import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.cirdles.tripoli.species.SpeciesColors;

public class SpeciesColorPane extends Pane {

    private static final double TITLE_FONT_SIZE = 19;

    private String speciesName;
    private SpeciesColors speciesColors;

    public SpeciesColorPane(String speciesName, SpeciesColors speciesColors) {
        super();
        this.speciesName = speciesName;
        this.speciesColors = speciesColors;
        VBox vBox = initializeAndAddVbox();

    }

    private VBox initializeAndAddVbox() {
        VBox vBox = new VBox();
        Text title = new Text(speciesName);
        title.setFont(Font.font(TITLE_FONT_SIZE));
        vBox.getChildren().add(title);
        this.getChildren().add(vBox);
        return vBox;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public void setSpeciesName(String speciesName) {
        this.speciesName = speciesName;
    }

    public SpeciesColors getSpeciesColors() {
        return speciesColors;
    }

    public void setSpeciesColors(SpeciesColors speciesColors) {
        this.speciesColors = speciesColors;
    }
}
