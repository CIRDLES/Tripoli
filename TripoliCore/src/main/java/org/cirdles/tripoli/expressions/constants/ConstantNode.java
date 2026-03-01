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

package org.cirdles.tripoli.expressions.constants;

import org.cirdles.tripoli.expressions.expressionTrees.ExpressionTree;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputBlockRecordLite;

import java.util.Map;

public class ConstantNode extends ExpressionTree {
    private static final long serialVersionUID = 750641824380081476L;

    String name;
    Double value;

    public ConstantNode(String name, Double value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public Double[][] eval(AnalysisInterface analysis) {
        return new Double[][]{new Double[]{value}};
    }

    @Override
    public Double[][] eval(String[] columnHeaders, Map<Integer, MassSpecOutputBlockRecordLite> blocksDataLite) {

        Double[][] retVal = new Double[blocksDataLite.size()][];

        for (Integer blockID : blocksDataLite.keySet()) {
            double[][] blockData = blocksDataLite.get(blockID).cycleData();
            retVal[blockID - 1] = new Double[blockData.length];

            for (int i = 0; i < blockData.length; i++) {
                retVal[blockID - 1][i] = value;
            }
        }

        return retVal;
    }


    @Override
    public int getOperationPrecedence() {
        return 0;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String newName) {
        name = newName;
    }

    public Double getValue() {
        return value;
    }
}
