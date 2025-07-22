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

package org.cirdles.tripoli.expressions.expressionTrees;

import org.cirdles.tripoli.expressions.constants.ConstantNode;
import org.cirdles.tripoli.expressions.operations.Operation;
import org.cirdles.tripoli.expressions.userFunctions.UserFunctionNode;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputBlockRecordLite;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static org.cirdles.tripoli.expressions.operations.Operation.OPERATIONS_MAP;


public class ExpressionTree implements ExpressionTreeInterface, Serializable {
    private static final long serialVersionUID = -2418173823255685906L;
    protected String name;
    protected ExpressionTreeInterface leftChildET;
    protected ExpressionTreeInterface rightChildET;
    protected Operation rootOperator;
    protected boolean isRatio = false;

    public ExpressionTree() {}

    public ExpressionTree(String name, ExpressionTreeInterface leftChildET, ExpressionTreeInterface rightChildET, Operation rootOperator) {
        this.name = name;
        this.leftChildET = leftChildET;
        this.rightChildET = rightChildET;
        this.rootOperator = rootOperator;
    }

    @Override
    public Double[][] eval(AnalysisInterface analysis){
        return rootOperator == null ? null : rootOperator.eval(leftChildET, rightChildET, analysis);
    }

    public ExpressionTree copy(){
        return new ExpressionTree(name, leftChildET, rightChildET, rootOperator);
    }

    public static ExpressionTreeInterface buildTree(List<String> parsedRPN) {
        Stack<ExpressionTreeInterface> stack = new Stack<>();

        for (String token : parsedRPN) {
            if (OPERATIONS_MAP.containsKey(token.trim())) {
                Operation operation = OPERATIONS_MAP.get(token);
                
                if (operation.isSingleArg()) {
                    if (stack.size() < 1) {
                        throw new IllegalArgumentException("Invalid RPN expression: insufficient operands for operator " + token);
                    }
                    ExpressionTreeInterface child = stack.pop();
                    
                    ExpressionTreeInterface node = new ExpressionTree(
                            "",
                            child,
                            null,
                            operation
                    );
                    
                    stack.push(node);
                } else {
                    if (stack.size() < 2) {
                        throw new IllegalArgumentException("Invalid RPN expression: insufficient operands for operator " + token);
                    }
                    ExpressionTreeInterface rightChild = stack.pop();
                    ExpressionTreeInterface leftChild = stack.pop();

                    ExpressionTreeInterface node = new ExpressionTree(
                            "",
                            leftChild,
                            rightChild,
                            operation
                    );

                    stack.push(node);
                }
            } else {
                try {
                    double value = Double.parseDouble(token);
                    ExpressionTreeInterface leaf = new ConstantNode(token, value);
                    stack.push(leaf);
                } catch (NumberFormatException e) {
                    ExpressionTreeInterface leaf = new UserFunctionNode(token);
                    stack.push(leaf);
                }
            }
        }

        if (stack.size() != 1) {
            throw new IllegalStateException("Invalid RPN expression: the final stack size is " + stack.size());
        }

        return stack.pop();
    }

    /**
     * Recursively generates string for the expression of the given tree in infix notation
     * @param node Tree root to be printed
     * @param analysis AnalysisInterface to be used for evaluation of UserFunctions
     * @param showValues if true, then the numerical values of the nodes are printed. Otherwise, the names of the nodes are printed.
     * @return String representing the expression tree in infix notation.
     */
    public static String prettyPrint(ExpressionTreeInterface node, AnalysisInterface analysis, boolean showValues) {
        if (node == null) {
            return "";
        }

        if (node instanceof UserFunctionNode || node instanceof ConstantNode) {
            if (showValues){
                return String.valueOf(node.eval(analysis)[0][0]);
            } else{
                if (node instanceof ConstantNode) {
                    return ((ConstantNode) node).getValue().toString();
                } else {
                    return node.getName();
                }
            }

        }

        if (node instanceof ExpressionTree tree) {

            String leftStr = prettyPrint(tree.getLeft(), analysis, showValues);
            String rightStr = prettyPrint(tree.getRight(), analysis, showValues);

            String operatorSymbol = OPERATIONS_MAP.entrySet().stream()
                    .filter(entry -> entry.getValue().getClass().equals(tree.getOperation().getClass()))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse("");

            return "(" + leftStr + " " + operatorSymbol + " " + rightStr + ")";
        }

        return "";
    }

    private ExpressionTreeInterface getLeft() {
        return this.leftChildET;
    }
    private ExpressionTreeInterface getRight() {
        return this.rightChildET;
    }
    private Operation getOperation() {return this.rootOperator;}

    @Override
    public int getOperationPrecedence() {
        int retVal = 100;

        if (rootOperator != null) {
            retVal = rootOperator.getPrecedence();
        }

        return retVal;
    }

    @Override
    public String getName() {
        return name;
    }
    @Override
    public void setName(String name) {this.name = name;}

    public boolean isRatio() {return isRatio;}
    public void setRatio(boolean isRatio) {this.isRatio = isRatio;}

    public Double[][] eval(String[] columnHeaders, Map<Integer, MassSpecOutputBlockRecordLite> blocksDataLite) {
        return rootOperator == null ? null : rootOperator.eval(leftChildET, rightChildET, columnHeaders, blocksDataLite);
    }
}