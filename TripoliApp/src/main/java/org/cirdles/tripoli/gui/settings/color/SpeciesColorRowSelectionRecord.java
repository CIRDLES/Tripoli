package org.cirdles.tripoli.gui.settings.color;

import org.cirdles.tripoli.gui.settings.color.fxcomponents.SpeciesColorPane;
import org.cirdles.tripoli.gui.settings.color.fxcomponents.SpeciesColorRow;
import org.cirdles.tripoli.species.SpeciesColorSetting;

public record SpeciesColorRowSelectionRecord(SpeciesColorPane speciesColorPane,
                                             SpeciesColorRow speciesColorRow,
                                             SpeciesColorSetting speciesColorSetting) {
}