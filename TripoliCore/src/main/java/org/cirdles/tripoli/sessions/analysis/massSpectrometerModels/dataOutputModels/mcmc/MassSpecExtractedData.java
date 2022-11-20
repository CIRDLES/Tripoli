package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.mcmc;

import java.io.Serializable;

public class MassSpecExtractedData implements Serializable {

    private MassSpecExtractedHeader header;

    private MassSpecOutputDataRecord[] blocksData;


    public record MassSpecExtractedHeader() {

    }

}