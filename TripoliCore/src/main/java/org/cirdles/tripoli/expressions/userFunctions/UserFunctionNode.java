package org.cirdles.tripoli.expressions.userFunctions;

import org.cirdles.tripoli.expressions.expressionTrees.ExpressionTreeInterface;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne.initializers.AllBlockInitForDataLiteOne;

import java.util.List;

public class UserFunctionNode implements ExpressionTreeInterface {
    String name;

    public UserFunctionNode(String name) {
        this.name = name;
    }


    @Override
    public Double eval(AnalysisInterface analysis) {
        AllBlockInitForDataLiteOne.initBlockModels(analysis);
        List<UserFunction> ufList = analysis.getUserFunctions();
        for (UserFunction uf : ufList) {
            if (uf.getName().equals(name)) {
                return uf.getAnalysisStatsRecord().cycleModeMean();
            }
        }
        return 0.0;
    }

    @Override
    public int getOperationPrecedence() {
        return 0;
    }
}
