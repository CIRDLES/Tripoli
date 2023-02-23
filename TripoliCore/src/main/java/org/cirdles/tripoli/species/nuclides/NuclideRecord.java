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

package org.cirdles.tripoli.species.nuclides;

import org.cirdles.tripoli.species.SpeciesRecordInterface;

import java.io.Serializable;
import java.text.DecimalFormat;

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
) implements SpeciesRecordInterface, Serializable {

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
}