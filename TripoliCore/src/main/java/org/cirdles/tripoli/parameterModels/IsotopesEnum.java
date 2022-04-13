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

package org.cirdles.tripoli.parameterModels;

/**
 * @author James F. Bowring
 */
public enum IsotopesEnum {

    INDEX_ZERO_DUMMY("Baseline dummy", 0, 0),
    PB204("204Pb", 203.9730435, 204),
    PB205("205Pb", 204.9744817, 205),
    PB206("206Pb", 205.9744653, 206),
    PB207("207Pb", 206.9758968, 207),
    PB208("208Pb", 207.9766521, 208),
    ;

    private final String name;
    private final double isotopicMass;
    private final int massSpecMass;

    IsotopesEnum(String name, double isotopicMass, int massSpecMass) {
        this.name = name;
        this.isotopicMass = isotopicMass;
        this.massSpecMass = massSpecMass;
    }

    public static IsotopesEnum getByName(String name) {
        for (IsotopesEnum detector : IsotopesEnum.values()) {
            if (detector.name.compareTo(name) == 0) {
                return detector;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public double getIsotopicMass() {
        return isotopicMass;
    }

    public int getMassSpecMass() {
        return massSpecMass;
    }
}