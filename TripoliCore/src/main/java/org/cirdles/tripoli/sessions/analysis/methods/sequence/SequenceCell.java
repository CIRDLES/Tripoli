/*
 * Copyright 2022 James Bowring, Noah McLean, Scott Burdick, and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cirdles.tripoli.sessions.analysis.methods.sequence;

import org.cirdles.tripoli.expressions.species.SpeciesRecordInterface;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author James F. Bowring
 */
public class SequenceCell implements Serializable {

    @Serial
    private static final long serialVersionUID = -8315387553980986168L;
    private String sequenceId;
    private int onPeakSequence;
    private double cellMass;
    private SpeciesRecordInterface targetSpecies;
    private List<SpeciesRecordInterface> includedSpecies;
    private List<String> baselineReferences;

    private SequenceCell(String sequenceId, int onPeakSequence) {
        cellMass = 0.0;
        targetSpecies = null;
        includedSpecies = new ArrayList<>();
        this.sequenceId = sequenceId;
        this.onPeakSequence = onPeakSequence;
        baselineReferences = new ArrayList<>();
    }

    public static SequenceCell initializeSequenceCell(String sequenceId, int onPeakSequence) {
        return new SequenceCell(sequenceId, onPeakSequence);
    }

    public void addTargetSpecies(SpeciesRecordInterface species) {
        targetSpecies = species;
        cellMass = species.getAtomicMass();
        if (!includedSpecies.contains(species)) {
            includedSpecies.add(species);
        }
    }

    @Override
    public boolean equals(Object otherObject) {
        boolean retVal = true;
        if (otherObject != this) {
            if (otherObject instanceof SequenceCell otherSequenceCell) {
                retVal = 0 == sequenceId.compareToIgnoreCase(otherSequenceCell.sequenceId);
            } else {
                retVal = false;
            }
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (null == sequenceId ? 0 : sequenceId.hashCode());
        hash = 31 * hash + (null == targetSpecies ? 0 : targetSpecies.hashCode());
        return hash;
    }


    public String getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(String sequenceId) {
        this.sequenceId = sequenceId;
    }

    public int getOnPeakSequence() {
        return onPeakSequence;
    }

    public void setOnPeakSequence(int onPeakSequence) {
        this.onPeakSequence = onPeakSequence;
    }

    public double getCellMass() {
        return cellMass;
    }

    public void setCellMass(double cellMass) {
        this.cellMass = cellMass;
    }

    public SpeciesRecordInterface getTargetSpecies() {
        return targetSpecies;
    }

    public void setTargetSpecies(SpeciesRecordInterface targetSpecies) {
        this.targetSpecies = targetSpecies;
    }

    public List<SpeciesRecordInterface> getIncludedSpecies() {
        return includedSpecies;
    }

    public void setIncludedSpecies(List<SpeciesRecordInterface> includedSpecies) {
        this.includedSpecies = includedSpecies;
    }

    public List<String> getBaselineReferences() {
        return baselineReferences;
    }

    public void setBaselineReferences(List<String> baselineReferences) {
        this.baselineReferences = baselineReferences;
    }

    public String prettyPrintBaseLineRefs() {
        String retVal = "";
        for (String blref : baselineReferences) {
            retVal += blref + " ";
        }
        return retVal;
    }
}