package org.cirdles.tripoli.expressions.operations;

import org.cirdles.tripoli.expressions.expressionTrees.ExpressionTreeInterface;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;

import java.util.Map;
import java.util.TreeMap;

public abstract class Operation {

    protected String name;
    protected int precedence;
    public final static Map<String, Operation> OPERATIONS_MAP = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    static {

        OPERATIONS_MAP.put("+", add());
        OPERATIONS_MAP.put("-", subtract());
        OPERATIONS_MAP.put("/", divide());
        OPERATIONS_MAP.put("*", multiply());
    }

    public abstract Double eval(ExpressionTreeInterface leftChild, ExpressionTreeInterface rightChild, AnalysisInterface analysis);

    public int getPrecedence() {
        return precedence;
    }
    public String getName() {
        return name;
    }


    public static Operation add() {return new Add();}
    public static Operation subtract() {return new Subtract();}
    public static Operation divide() {return new Divide();}
    public static Operation multiply() {return new Multiply();}

}
