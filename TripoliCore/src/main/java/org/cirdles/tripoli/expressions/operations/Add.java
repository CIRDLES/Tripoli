package org.cirdles.tripoli.expressions.operations;

import org.cirdles.tripoli.expressions.expressionTrees.ExpressionTreeInterface;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;


public class Add extends Operation {
    public Add(){
        super();
        name = "add";
        precedence = 2;
    }

    @Override
    public Double eval(ExpressionTreeInterface leftChild, ExpressionTreeInterface rightChild, AnalysisInterface analysis){
        return leftChild.eval(analysis) + rightChild.eval(analysis);
    }
}
