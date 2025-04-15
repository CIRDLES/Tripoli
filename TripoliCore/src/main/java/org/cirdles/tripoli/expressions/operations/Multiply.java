package org.cirdles.tripoli.expressions.operations;

import org.cirdles.tripoli.expressions.expressionTrees.ExpressionTreeInterface;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;

public class Multiply extends Operation {
    public Multiply() {
        name = "multiply";
        precedence = 3;
    }
    @Override
    public Double eval(ExpressionTreeInterface leftChild, ExpressionTreeInterface rightChild, AnalysisInterface analysis){
        return leftChild.eval(analysis) * rightChild.eval(analysis);
    }
}
