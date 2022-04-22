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

import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.DetectorSetup;

import java.io.Serial;
import java.io.Serializable;

public class MassSpectrometerModel implements Serializable {
    /* Notes see: https://docs.google.com/drawings/d/1sL-tRlgCEBHzIlv5-WZ47WKhJQ3lCADO_qcB64IUD6s/edit
    Detector setup (constrained list)
    Switchable amplifiers
    Magnet effective radius
    Collector width (mm)
    Beam width (mm)
     */

    @Serial
    private static final long serialVersionUID = 1402626964061990257L;

    private String massSpectrometerName;
    private DetectorSetup detectorSetup;

    private MassSpectrometerModel() {
        this("DefaultMassSpectrometer");
    }

    private MassSpectrometerModel(String massSpectrometerName) {
        this(massSpectrometerName, DetectorSetup.createEmptyDetectorSetup());
    }

    private MassSpectrometerModel(String massSpectrometerName, DetectorSetup detectorSetup) {
        this.massSpectrometerName = massSpectrometerName;
        this.detectorSetup = detectorSetup;
    }

    public static MassSpectrometerModel initializeMassSpectrometer(String name) {
        return new MassSpectrometerModel(name);
    }

    public String getMassSpectrometerName() {
        return massSpectrometerName;
    }

    public void setMassSpectrometerName(String massSpectrometerName) {
        this.massSpectrometerName = massSpectrometerName;
    }

    public DetectorSetup getDetectorSetup() {
        return detectorSetup;
    }

    public void setDetectorSetup(DetectorSetup detectorSetup) {
        this.detectorSetup = detectorSetup;
    }
}