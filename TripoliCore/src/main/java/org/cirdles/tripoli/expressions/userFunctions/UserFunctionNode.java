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

package org.cirdles.tripoli.expressions.userFunctions;

import org.cirdles.tripoli.expressions.expressionTrees.ExpressionTree;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne.SingleBlockRawDataLiteSetRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne.initializers.AllBlockInitForDataLiteOne;

import java.util.List;
import java.util.Map;

public class UserFunctionNode extends ExpressionTree {
    private static final long serialVersionUID = -8842667140841645591L;

    String name;

    public UserFunctionNode(String name) {
        this.name = name;
    }
    
    public Double[][] eval(AnalysisInterface analysis) {
        AllBlockInitForDataLiteOne.initBlockModels(analysis);
        List<UserFunction> ufList = analysis.getUserFunctions();
        for (UserFunction uf : ufList) {
            if (uf.getName().equals(name)) {
                Map<Integer, SingleBlockRawDataLiteSetRecord> o = analysis.getMapOfBlockIdToRawDataLiteOne();
                Double[][] retVal = new Double[o.size()][];
                for (Integer blockID : o.keySet()){
                    retVal[blockID-1] = new Double[o.get(blockID).blockRawDataLiteArray().length];
                    for (int d = 0; d<o.get(blockID).blockRawDataLiteArray().length; d++) {
                        retVal[blockID-1][d] = o.get(blockID).blockRawDataLiteArray()[d][uf.getColumnIndex()];
                    }

                }
                return retVal;
                //return uf.getAnalysisStatsRecord().cycleModeMean();
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }
}
