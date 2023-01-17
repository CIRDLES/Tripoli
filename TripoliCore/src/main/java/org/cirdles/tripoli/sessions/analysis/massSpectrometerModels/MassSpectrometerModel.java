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

import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;

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
    private final String massSpectrometerName;
    private double collectorWidthMM;        //% collector aperture width (mm)
    private double theoreticalBeamWidthMM;  //% a priori estimate of beam width (mm)
    private double effectiveRadiusMagnetMM;

    private MassSpectrometerModel() {
        this("DefaultMassSpectrometer");
    }

    private MassSpectrometerModel(String massSpectrometerName) {
        this.massSpectrometerName = massSpectrometerName;
        collectorWidthMM = 0.0;
        theoreticalBeamWidthMM = 0.0;
        effectiveRadiusMagnetMM = 0.0;
    }

    public static MassSpectrometerModel initializeMassSpectrometer(MassSpectrometerContextEnum massSpectrometerContext) {
        MassSpectrometerModel massSpectrometerModel = new MassSpectrometerModel(massSpectrometerContext.getMassSpectrometerName());
        return massSpectrometerModel;
    }

    public String getMassSpectrometerName() {
        return massSpectrometerName;
    }

    public double getCollectorWidthMM() {
        return collectorWidthMM;
    }

    public void setCollectorWidthMM(double collectorWidthMM) {
        this.collectorWidthMM = collectorWidthMM;
    }

    public double getTheoreticalBeamWidthMM() {
        return theoreticalBeamWidthMM;
    }

    public void setTheoreticalBeamWidthMM(double theoreticalBeamWidthMM) {
        this.theoreticalBeamWidthMM = theoreticalBeamWidthMM;
    }

    public double getEffectiveRadiusMagnetMM() {
        return effectiveRadiusMagnetMM;
    }

    public void setEffectiveRadiusMagnetMM(double effectiveRadiusMagnetMM) {
        this.effectiveRadiusMagnetMM = effectiveRadiusMagnetMM;
    }
}