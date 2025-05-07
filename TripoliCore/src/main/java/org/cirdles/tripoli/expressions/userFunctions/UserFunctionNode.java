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
import org.cirdles.tripoli.plots.compoundPlotBuilders.PlotBlockCyclesRecord;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne.initializers.AllBlockInitForDataLiteOne;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputBlockRecordLite;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserFunctionNode extends ExpressionTree {
    private static final long serialVersionUID = -8842667140841645591L;

    String name;

    public UserFunctionNode(String name) {
        this.name = name;
    }

    public Double[][] eval(AnalysisInterface analysis) {
        AllBlockInitForDataLiteOne.initBlockModels(analysis);
        List<UserFunction> ufList = analysis.getUserFunctions();

        Optional<UserFunction> maybeUserFunction = ufList.stream()
                .filter(uf -> uf.getName().equals(name))
                .findFirst();

        if (maybeUserFunction.isEmpty()) {
            return null;
        }

        UserFunction targetFunction = maybeUserFunction.get();
        Map<Integer, PlotBlockCyclesRecord> cycleRecordMap = targetFunction.getMapBlockIdToBlockCyclesRecord();

        Double[][] retVal = new Double[cycleRecordMap.size()][];
        cycleRecordMap.forEach((k, v) ->
                retVal[k-1] = Arrays.stream(v.cycleMeansData())
                        .boxed()
                        .toArray(Double[]::new)
        );

        return retVal;
    }

    @Override
    public Double[][] eval(String[] columnHeaders, Map<Integer, MassSpecOutputBlockRecordLite> blocksDataLite) {

        if (name.contains("[")){
            this.name = name.substring(1, name.length() - 1);
        }

        int columnIndex = -1;
        for (int i = 0; i < columnHeaders.length; i++) {
            if (columnHeaders[i].equals(name)) {
                columnIndex = i - 2;
                break;
            }
        }
        
        Double[][] retVal = new Double[blocksDataLite.size()][];
        
        for (Integer blockID : blocksDataLite.keySet()) {
            double[][] blockData = blocksDataLite.get(blockID).cycleData();
            retVal[blockID-1] = new Double[blockData.length];
            
            for (int i = 0; i < blockData.length; i++) {
                if (columnIndex < blockData[i].length) {
                    retVal[blockID-1][i] = blockData[i][columnIndex];
                } else {
                    retVal[blockID-1][i] = 0.0;
                }
            }
        }
        
        return retVal;
    }


    public String getName() {
        if (!name.contains("[")){
            return "[" + name + "]";
        } else {
            return name;
        }
    }
}