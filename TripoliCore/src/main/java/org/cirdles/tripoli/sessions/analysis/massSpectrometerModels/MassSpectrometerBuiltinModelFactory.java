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

import java.util.Map;
import java.util.TreeMap;

import static org.cirdles.tripoli.constants.MassSpectrometerContextEnum.PHOENIX_FULL;
import static org.cirdles.tripoli.constants.MassSpectrometerContextEnum.UNKNOWN;

/**
 * @author James F. Bowring
 */
public enum MassSpectrometerBuiltinModelFactory {
    ;

    public static Map<String, MassSpectrometerModel> massSpectrometerModelBuiltinMap = new TreeMap<>();

    static {
        MassSpectrometerModel massSpectrometerModelPhoenix = MassSpectrometerModel.initializeMassSpectrometer(PHOENIX_FULL);
        massSpectrometerModelPhoenix.setCollectorWidthMM(0.95135);
        massSpectrometerModelPhoenix.setEffectiveRadiusMagnetMM(540.0);
        massSpectrometerModelPhoenix.setTheoreticalBeamWidthMM(0.35);
        massSpectrometerModelBuiltinMap.put(PHOENIX_FULL.getMassSpectrometerName(), massSpectrometerModelPhoenix);

        MassSpectrometerModel massSpectrometerModelUnknown = MassSpectrometerModel.initializeMassSpectrometer(UNKNOWN);
        massSpectrometerModelBuiltinMap.put(UNKNOWN.getMassSpectrometerName(), massSpectrometerModelUnknown);
    }
}