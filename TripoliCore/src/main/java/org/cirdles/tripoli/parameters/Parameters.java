package org.cirdles.tripoli.parameters;

import java.io.Serializable;
import static org.cirdles.tripoli.constants.TripoliConstants.CHAUVENETS_DEFAULT_REJECT_PROBABILITY;
import static org.cirdles.tripoli.constants.TripoliConstants.CHAUVENETS_DEFAULT_MIN_DATUM_COUNT;

public class Parameters implements Serializable {


    // Chauvenet's parameters
    private double chauvenetRejectionProbability;
    private int requiredMinDatumCount;

    public Parameters() {
        this.chauvenetRejectionProbability = CHAUVENETS_DEFAULT_REJECT_PROBABILITY;
        this.requiredMinDatumCount = CHAUVENETS_DEFAULT_MIN_DATUM_COUNT;
    }

    public double getChauvenetRejectionProbability() {
        return chauvenetRejectionProbability;
    }

    public void setChauvenetRejectionProbability(double chauvenetRejectionProbability) {
        this.chauvenetRejectionProbability = chauvenetRejectionProbability;
    }

    public int getRequiredMinDatumCount() {
        return requiredMinDatumCount;
    }

    public void setRequiredMinDatumCount(int requiredMinDatumCount) {
        this.requiredMinDatumCount = requiredMinDatumCount;
    }
}
