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

package org.cirdles.tripoli.species.baselineMasses;

import org.cirdles.tripoli.species.SpeciesRecordInterface;

import java.io.Serializable;

/**
 * @author James F. Bowring
 */
public class BaselineMass implements SpeciesRecordInterface, Serializable {

    private final static String DEFAULT_NAME = "Placeholder";
    private final static double DEFAULT_ABUNDANCE = 0.0;
//    private double atomicMass;

//    public BaselineMass(double atomicMass) {
//        this.atomicMass = atomicMass;
//    }

    @Override
    public String getMolecularFormula() {
        return DEFAULT_NAME;
    }

    @Override
    public double getAtomicMass() {
        return 0;
    }

    @Override
    public double getNaturalAbundancePercent() {
        return DEFAULT_ABUNDANCE;
    }

    @Override
    public String prettyPrintShortForm() {
        return "OOPS";
    }
}