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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups;

/**
 * @author James F. Bowring
 */
public enum DetectorEnumTypeA {

    L5("L5", 1),
    L4("L4", 2),
    L3("L3", 3),
    L2("L2", 4),
    AX_FARA("Ax", 5),
    AXIAL("PM", -1),
    H1("H1", 6),
    H2("H2", 7),
    H3("H3", 8),
    H4("H4", 9);

    private final String name;
    private final int amplifierIndex;

    DetectorEnumTypeA(String name, int amplifierIndex) {
        this.name = name;
        this.amplifierIndex = amplifierIndex;
    }

    public static DetectorEnumTypeA getByName(String name) {
        for (DetectorEnumTypeA detector : values()) {
            if (0 == detector.name.compareTo(name)) {
                return detector;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public int getAmplifierIndex() {
        return amplifierIndex;
    }

    public boolean isFaraday() {
        return -1 < amplifierIndex;
    }
}