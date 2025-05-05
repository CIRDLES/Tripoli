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

package org.cirdles.tripoli.expressions.operations;

import org.cirdles.tripoli.expressions.expressionTrees.ExpressionTreeInterface;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputBlockRecordLite;

import java.util.Map;
import java.util.stream.IntStream;

public class Sqrt extends Operation {

    public Sqrt(){
        super();
        name = "square root";
        precedence = 4;
        singleArg = true;
    }

    @Override
    public Double[][] eval(ExpressionTreeInterface leftChild, ExpressionTreeInterface rightChild, AnalysisInterface analysis){
        Double[][] leftCycle = leftChild.eval(analysis);
        Double[][] retVal = new Double[leftCycle.length][];

        for (int i = 0; i < leftCycle.length; i++){
            Double[] leftCycleRow = leftCycle[i];
            retVal[i] = new Double[leftCycleRow.length];

            for (int j = 0; j < leftCycleRow.length; j++){
                retVal[i][j] = StrictMath.sqrt(leftCycleRow[j]);
            }
        }

        return retVal;
    }

    @Override
    public Double[][] eval(ExpressionTreeInterface leftChildET, ExpressionTreeInterface rightChildET, String[] columnHeaders, Map<Integer, MassSpecOutputBlockRecordLite> blocksDataLite) {
        Double[][] retVal = new Double[blocksDataLite.size()][];

        int leftIndex = IntStream.range(0, columnHeaders.length)
                .filter(i -> columnHeaders[i].equals(leftChildET.getName()))
                .findFirst()
                .orElse(-1);
        leftIndex -= 2;

        for (Integer blockID : blocksDataLite.keySet()) {
            double[][] blockData = blocksDataLite.get(blockID).cycleData();
            retVal[blockID-1] = new Double[blockData.length];
            for (int i = 0; i < blockData.length; i++ ) {
                retVal[blockID-1][i] = StrictMath.sqrt(blockData[i][leftIndex]);
            }
        }
        return retVal;
    }
}
