package org.cirdles.tripoli.expressions.expressionTrees;

import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;


public interface ExpressionTreeInterface {

    Double eval(AnalysisInterface analysis);
    int getOperationPrecedence();
}
