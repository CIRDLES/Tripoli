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

import java.util.Map;
import java.util.TreeMap;

import static org.cirdles.tripoli.constants.MassSpectrometerContextEnum.PHOENIX_FULL_SYNTHETIC;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector.AmplifierTypeEnum.RESISTANCE;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector.AmplifierTypeEnum.VIRTUAL;

/**
 * @author James F. Bowring
 */
public enum DetectorSetupBuiltinModelFactory {
    ;

    public static Map<String, DetectorSetup> detectorSetupBuiltinMap = new TreeMap<>();

    static {

        DetectorSetup detectorSetup = DetectorSetup.initializeDetectorSetup();
        detectorSetupBuiltinMap.put(PHOENIX_FULL_SYNTHETIC.getName(), detectorSetup);

        detectorSetup.addDetector(Detector.initializeDetector(Detector.DetectorTypeEnum.FARADAY, "L5", 0,
                RESISTANCE, 1.0e11, 0.0, 1.0, 0.0));
        detectorSetup.addDetector(Detector.initializeDetector(Detector.DetectorTypeEnum.FARADAY, "L4", 1,
                RESISTANCE, 1.0e11, 0.0, 1.0, 0.0));
        detectorSetup.addDetector(Detector.initializeDetector(Detector.DetectorTypeEnum.FARADAY, "L3", 2,
                RESISTANCE, 1.0e11, 0.0, 1.0, 0.0));
        detectorSetup.addDetector(Detector.initializeDetector(Detector.DetectorTypeEnum.FARADAY, "L2", 3,
                RESISTANCE, 1.0e11, 0.0, 1.0, 0.0));
        detectorSetup.addDetector(Detector.initializeDetector(Detector.DetectorTypeEnum.FARADAY, "Ax", 4,
                RESISTANCE, 1.0e11, 0.0, 1.0, 0.0));
        detectorSetup.addDetector(Detector.initializeDetector(Detector.DetectorTypeEnum.DALYDETECTOR, "PM", 5,
                VIRTUAL, 1.0e11, 0.0, 1.0, 0.0));
        detectorSetup.addDetector(Detector.initializeDetector(Detector.DetectorTypeEnum.FARADAY, "H1", 6,
                RESISTANCE, 1.0e11, 0.0, 1.0, 0.0));
        detectorSetup.addDetector(Detector.initializeDetector(Detector.DetectorTypeEnum.FARADAY, "H2", 7,
                RESISTANCE, 1.0e11, 0.0, 1.0, 0.0));
        detectorSetup.addDetector(Detector.initializeDetector(Detector.DetectorTypeEnum.FARADAY, "H3", 8,
                RESISTANCE, 1.0e11, 0.0, 1.0, 0.0));
        detectorSetup.addDetector(Detector.initializeDetector(Detector.DetectorTypeEnum.FARADAY, "H4", 9,
                RESISTANCE, 1.0e11, 0.0, 1.0, 0.0));
    }
}