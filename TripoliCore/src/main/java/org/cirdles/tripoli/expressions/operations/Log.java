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

public class Log extends Operation {
    public Log(){
        super();
        name = "logarithmic";
        precedence = 4;
        singleArg = true;
    }

    @Override
    public Double[][] eval(ExpressionTreeInterface leftChild, ExpressionTreeInterface rightChild, AnalysisInterface analysis){
        Double[][] leftCycle = leftChild.eval(analysis);
        //Double[][] rightCycle = rightChild.eval(analysis);
        Double[][] retVal = new Double[leftCycle.length][];

        for (int i = 0; i < leftCycle.length; i++){
            Double[] leftCycleRow = leftCycle[i];
            //Double[] rightCycleRow = rightCycle[i];
            retVal[i] = new Double[leftCycleRow.length];

            for (int j = 0; j < leftCycleRow.length; j++){
                retVal[i][j] = StrictMath.log10(leftCycleRow[j]);
            }
        }

        return retVal;
    }
}
