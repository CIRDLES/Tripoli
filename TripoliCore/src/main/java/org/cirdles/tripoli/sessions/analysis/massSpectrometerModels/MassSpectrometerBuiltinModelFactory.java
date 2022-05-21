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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels;

import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.DetectorSetup;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author James F. Bowring
 */
public final class MassSpectrometerBuiltinModelFactory {

    public static Map<String, MassSpectrometerModel> massSpectrometersBuiltinMap = new LinkedHashMap<>();

    static {
        MassSpectrometerModel op_Phoenix = MassSpectrometerModel.initializeMassSpectrometer("OP_Phoenix");
        massSpectrometersBuiltinMap.put(op_Phoenix.getMassSpectrometerName(), op_Phoenix);

        DetectorSetup detectorSetup = DetectorSetup.initializeDetectorSetup();
        detectorSetup.addDetector(Detector.initializeDetector(Detector.DetectorTypeEnum.FARADAY, "L5", 0));
        detectorSetup.addDetector(Detector.initializeDetector(Detector.DetectorTypeEnum.FARADAY, "L4", 1));
        detectorSetup.addDetector(Detector.initializeDetector(Detector.DetectorTypeEnum.FARADAY, "L3", 2));
        detectorSetup.addDetector(Detector.initializeDetector(Detector.DetectorTypeEnum.FARADAY, "L2", 3));
        detectorSetup.addDetector(Detector.initializeDetector(Detector.DetectorTypeEnum.FARADAY, "Ax_Fara", 4));
        detectorSetup.addDetector(Detector.initializeDetector(Detector.DetectorTypeEnum.DALY, "Axial", 5));
        detectorSetup.addDetector(Detector.initializeDetector(Detector.DetectorTypeEnum.FARADAY, "H1", 6));
        detectorSetup.addDetector(Detector.initializeDetector(Detector.DetectorTypeEnum.FARADAY, "H2", 7));
        detectorSetup.addDetector(Detector.initializeDetector(Detector.DetectorTypeEnum.FARADAY, "H3", 8));
        detectorSetup.addDetector(Detector.initializeDetector(Detector.DetectorTypeEnum.FARADAY, "H4", 9));
        op_Phoenix.setDetectorSetup(detectorSetup);

    }

    static {
        MassSpectrometerModel bl_Phoenix = MassSpectrometerModel.initializeMassSpectrometer("BL_Phoenix");
        massSpectrometersBuiltinMap.put(bl_Phoenix.getMassSpectrometerName(), bl_Phoenix);
    }

    static {
        MassSpectrometerModel op_Triton = MassSpectrometerModel.initializeMassSpectrometer("OP_Triton");
        massSpectrometersBuiltinMap.put(op_Triton.getMassSpectrometerName(), op_Triton);
    }
}