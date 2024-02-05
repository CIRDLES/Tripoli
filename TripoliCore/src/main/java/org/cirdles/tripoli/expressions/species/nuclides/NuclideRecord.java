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

package org.cirdles.tripoli.expressions.species.nuclides;

import org.cirdles.tripoli.expressions.species.SpeciesRecordInterface;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Objects;

/**
 * @author James F. Bowring
 */
public record NuclideRecord(
        String elementSymbol,
        int protonsZ,
        int neutronsN,
        // mass number = Z + N
        double atomicMass,
        double halfLifeAnnum,
        double naturalAbundancePercent
) implements SpeciesRecordInterface, Serializable, Comparable {

    public String prettyPrintLongForm() {
        DecimalFormat df = new DecimalFormat("###0.0000000#####           ");
        return prettyPrintShortForm() +
                ": " + df.format(atomicMass) +
                "\t %abundance: " +
                naturalAbundancePercent +
                "\t 1/2Life: " +
                (0 < halfLifeAnnum ? halfLifeAnnum : (0 > halfLifeAnnum) ? "Stable" : "n/a");
    }

    public String prettyPrintShortForm() {
        return (protonsZ + neutronsN) + elementSymbol;
    }

    public int getMassNumber() {
        return protonsZ + neutronsN;
    }

    @Override
    public String getMolecularFormula() {
        return elementSymbol;
    }

    @Override
    public double getAtomicMass() {
        return atomicMass;
    }

    @Override
    public double getNaturalAbundancePercent() {
        return naturalAbundancePercent;
    }

    /**
     * @param o the object to be compared.
     * @return
     */
    @Override
    public int compareTo(@NotNull Object o) {
        return Integer.compare(protonsZ + neutronsN, ((NuclideRecord) o).neutronsN + ((NuclideRecord) o).protonsZ);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;
        NuclideRecord that = (NuclideRecord) o;
        return protonsZ == that.protonsZ && neutronsN == that.neutronsN && elementSymbol.equals(that.elementSymbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementSymbol, protonsZ, neutronsN);
    }
}