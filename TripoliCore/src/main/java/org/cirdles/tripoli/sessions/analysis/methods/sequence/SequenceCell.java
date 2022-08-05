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

import org.cirdles.tripoli.species.SpeciesRecordInterface;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author James F. Bowring
 */
public class SequenceCell implements Serializable {

    private String sequenceName;
    private double cellMass;
    private SpeciesRecordInterface targetSpecies;
    private List<SpeciesRecordInterface> includedSpecies;

    private SequenceCell(String sequenceName) {
        this.cellMass = 0.0;
        this.targetSpecies = null;
        this.includedSpecies = new ArrayList<>();
        this.sequenceName = sequenceName;
    }

    public static SequenceCell initializeSequenceCell(String sequenceName) {
        return new SequenceCell(sequenceName);
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
                retVal = this.getSequenceName().compareToIgnoreCase(otherSequenceCell.getSequenceName()) == 0;
            } else {
                retVal = false;
            }
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (sequenceName == null ? 0 : sequenceName.hashCode());
        hash = 31 * hash + (targetSpecies == null ? 0 : targetSpecies.hashCode());
        return hash;
    }


    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
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
}