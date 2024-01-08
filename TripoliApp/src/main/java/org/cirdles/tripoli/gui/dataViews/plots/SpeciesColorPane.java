package org.cirdles.tripoli.gui.dataViews.plots;

import javafx.scene.layout.Pane;
import org.cirdles.tripoli.species.SpeciesColors;

public class SpeciesColorPane extends Pane {

    private String speciesName;
    private SpeciesColors speciesColors;

    public SpeciesColorPane(String speciesName, SpeciesColors speciesColors) {
        super();
        this.speciesName = speciesName;
        this.speciesColors = speciesColors;
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
