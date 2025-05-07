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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors;

import org.cirdles.tripoli.expressions.expressionTrees.ExpressionTree;

import java.io.Serializable;

/**
 * @param blockID
 * @param cycleData
 */
public record MassSpecOutputBlockRecordLite(
        int blockID,
        double[][] cycleData
) implements Serializable {
    public MassSpecOutputBlockRecordLite expandForUraniumOxideCorrection(int r270_267ColumnIndex, int r265_267ColumnIndex, double r18O_16O) {
        double[][] cycleDataExpand = new double[cycleData.length][];
        for (int row = 0; row < cycleData.length; row++) {
            cycleDataExpand[row] = new double[cycleData[row].length + 3];
            System.arraycopy(cycleData[row], 0, cycleDataExpand[row], 0, cycleData[row].length);

            cycleDataExpand[row][cycleData[row].length + 0]
                    = cycleData[row][r265_267ColumnIndex] / (1.0 - 2.0 * r18O_16O * cycleData[row][r265_267ColumnIndex]);
            cycleDataExpand[row][cycleData[row].length + 1]
                    = cycleData[row][r270_267ColumnIndex] / (1.0 - 2.0 * r18O_16O * cycleData[row][r270_267ColumnIndex]);
            cycleDataExpand[row][cycleData[row].length + 2]
                    = cycleDataExpand[row][cycleData[row].length + 1] / cycleDataExpand[row][cycleData[row].length + 0];
        }
        return new MassSpecOutputBlockRecordLite(blockID, cycleDataExpand);
    }
    public MassSpecOutputBlockRecordLite expandForCustomExpression(Double[] expressionData){
        double[][] cycleDataExpand = new double[cycleData.length][];
        for (int row = 0; row < cycleData.length; row++) {
            cycleDataExpand[row] = new double[cycleData[row].length + 1];
            System.arraycopy(cycleData[row], 0, cycleDataExpand[row], 0, cycleData[row].length);

            cycleDataExpand[row][cycleData[row].length] =
                    expressionData[row];
        }
        return new MassSpecOutputBlockRecordLite(blockID, cycleDataExpand);
    }

    public MassSpecOutputBlockRecordLite replaceForCustomExpression(Double[] expressionData, int columnIndex){
        for (int row = 0; row < cycleData.length; row++) {
            cycleData[row][columnIndex-2] = expressionData[row];
        }
        return new MassSpecOutputBlockRecordLite(blockID, cycleData);
    }

    public MassSpecOutputBlockRecordLite copyWithNewBlockID(int blockIDNew) {
        return new MassSpecOutputBlockRecordLite(blockIDNew, cycleData);
    }

}