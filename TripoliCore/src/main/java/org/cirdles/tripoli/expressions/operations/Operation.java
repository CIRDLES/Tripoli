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

import org.cirdles.tripoli.expressions.expressionTrees.ExpressionTree;
import org.cirdles.tripoli.expressions.expressionTrees.ExpressionTreeInterface;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputBlockRecordLite;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

public abstract class Operation extends ExpressionTree implements Serializable {

    protected String name;
    protected int precedence;
    public final static Map<String, Operation> OPERATIONS_MAP = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private static final long serialVersionUID = 8219110032598495439L;
    protected boolean singleArg = false;

    static {

        OPERATIONS_MAP.put("+", add());
        OPERATIONS_MAP.put("-", subtract());
        OPERATIONS_MAP.put("/", divide());
        OPERATIONS_MAP.put("*", multiply());
        OPERATIONS_MAP.put("^", power());
        OPERATIONS_MAP.put("log", log());
        OPERATIONS_MAP.put("exp", exp());
        OPERATIONS_MAP.put("sqrt", sqrt());

    }

    public abstract Double[][] eval(ExpressionTreeInterface leftChild, ExpressionTreeInterface rightChild, AnalysisInterface analysis);
    public abstract Double[][] eval(ExpressionTreeInterface leftChildET, ExpressionTreeInterface rightChildET, String[] columnHeaders, Map<Integer, MassSpecOutputBlockRecordLite> blocksDataLite);

    public int getPrecedence() { return precedence; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isSingleArg() { return singleArg; }
    public Operation copy() {return this;}



    public static Operation add() { return new Add(); }
    public static Operation subtract() { return new Subtract(); }
    public static Operation divide() { return new Divide(); }
    public static Operation multiply() { return new Multiply(); }
    public static Operation power() { return new Power(); }
    public static Operation log() { return new Log(); }
    public static Operation exp() { return new Exp(); }
    public static Operation sqrt() { return new Sqrt(); }

}