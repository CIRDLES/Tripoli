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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne;

import org.cirdles.tripoli.expressions.userFunctions.UserFunction;

import java.io.Serializable;

/**
 * @author James F. Bowring
 */
public record SingleBlockRawDataLiteOneSetRecord(
        int blockID,
        int cycleCount,
        double[][] blockRawDataLiteArray
) implements Serializable {

    public double[] assembleCycleMeansForUserFunction(UserFunction userFunction) {
        double[] cycleMeans = new double[cycleCount];
        for (int i = 0; i < cycleCount; i++) {
            cycleMeans[i] = blockRawDataLiteArray()[i][userFunction.getColumnIndex()];
        }

        return cycleMeans;
    }

    public double[] assembleCycleStdDevForUserFunction(UserFunction userFunction) {
        return new double[cycleCount];
    }
}