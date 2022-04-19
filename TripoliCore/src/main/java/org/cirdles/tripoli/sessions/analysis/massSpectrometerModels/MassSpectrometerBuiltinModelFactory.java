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