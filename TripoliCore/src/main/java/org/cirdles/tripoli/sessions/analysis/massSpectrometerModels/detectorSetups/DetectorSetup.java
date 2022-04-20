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

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class DetectorSetup implements Serializable {
    /* Notes
    List of detectors (order matters)
    Amplifier assignments
    Amplifier type (resistance, capacitance)
    Amplifier size (ohms, farads)
    Table connecting amplifiers to detectors
    Detector type for each detector (ion counter, Faraday, etc)

     */

    private Map<String, Detector> mapOfDetectors;

    private DetectorSetup() {
        mapOfDetectors = new LinkedHashMap<>();
    }

    public static DetectorSetup createEmptyDetectorSetup() {
        DetectorSetup detectorSetup = new DetectorSetup();

        return detectorSetup;
    }

    public Map<String, Detector> getMapOfDetectors() {
        return mapOfDetectors;
    }

    public void setMapOfDetectors(Map<String, Detector> mapOfDetectors) {
        this.mapOfDetectors = mapOfDetectors;
    }
}