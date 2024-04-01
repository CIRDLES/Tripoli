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
public record SingleBlockRawDataLiteSetRecord(
        int blockID,
        boolean isIncluded,
        double[][] blockRawDataLiteArray,
        boolean[][] blockRawDataLiteIncludedArray)
        implements Serializable {

    public boolean[] assembleCyclesIncludedForUserFunction(UserFunction userFunction) {
        boolean[] cyclesIncluded = new boolean[blockRawDataLiteArray.length];
        for (int i = 0; i < blockRawDataLiteArray.length; i++) {
            cyclesIncluded[i] = blockRawDataLiteIncludedArray()[i][userFunction.getColumnIndex()];
        }

        return cyclesIncluded;
    }

    public double[] assembleCycleMeansForUserFunction(UserFunction userFunction) {
        double[] cycleMeans = new double[blockRawDataLiteArray.length];
        for (int i = 0; i < blockRawDataLiteArray.length; i++) {
            cycleMeans[i] = blockRawDataLiteArray()[i][userFunction.getColumnIndex()];
        }

        return cycleMeans;
    }

    public double[] assembleCycleStdDevForUserFunction(UserFunction userFunction) {
        return new double[blockRawDataLiteArray.length];
    }

    public SingleBlockRawDataLiteSetRecord resetAllDataIncluded() {
        for (int row = 0; row < blockRawDataLiteIncludedArray.length; row++) {
            for (int col = 0; col < blockRawDataLiteIncludedArray[row].length; col++) {
                blockRawDataLiteIncludedArray[row][col] = true;
            }
        }
        return new SingleBlockRawDataLiteSetRecord(
                blockID,
                true,
                blockRawDataLiteArray,
                blockRawDataLiteIncludedArray);
    }

    public SingleBlockRawDataLiteSetRecord toggleAllDataIncludedUserFunction(UserFunction userFunction) {
        for (int row = 0; row < blockRawDataLiteIncludedArray.length; row++) {
            int col = userFunction.getColumnIndex();
            blockRawDataLiteIncludedArray[row][col] = !blockRawDataLiteIncludedArray[row][col];
        }
        return new SingleBlockRawDataLiteSetRecord(
                blockID,
                isIncluded,
                blockRawDataLiteArray,
                blockRawDataLiteIncludedArray);
    }

    public SingleBlockRawDataLiteSetRecord updateIncludedCycles(UserFunction userFunction, boolean[] includedCycles) {
        for (int row = 0; row < blockRawDataLiteIncludedArray.length; row++) {
            int col = userFunction.getColumnIndex();
            blockRawDataLiteIncludedArray[row][col] = includedCycles[row];
        }
        return new SingleBlockRawDataLiteSetRecord(
                blockID,
                isIncluded,
                blockRawDataLiteArray,
                blockRawDataLiteIncludedArray);
    }

//    public boolean calcBlockIncludedForUserFunc(UserFunction userFunction) {
//        boolean retVal = false;
//        for (int row = 0; row < blockRawDataLiteIncludedArray.length; row++) {
//            retVal = retVal || blockRawDataLiteIncludedArray[row][userFunction.getColumnIndex()];
//        }
//        return retVal;
//    }

    public SingleBlockRawDataLiteSetRecord synchronizeIncludedToUserFunc(UserFunction userFunction) {
        for (int row = 0; row < blockRawDataLiteIncludedArray.length; row++) {
            for (int col = 0; col < blockRawDataLiteIncludedArray[row].length; col++) {
                blockRawDataLiteIncludedArray[row][col] = blockRawDataLiteIncludedArray[row][userFunction.getColumnIndex()];
            }
        }
        return new SingleBlockRawDataLiteSetRecord(
                blockID,
                isIncluded,
                blockRawDataLiteArray,
                blockRawDataLiteIncludedArray);
    }
}