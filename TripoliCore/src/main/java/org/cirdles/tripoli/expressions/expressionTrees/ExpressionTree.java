package org.cirdles.tripoli.expressions.expressionTrees;

import org.cirdles.tripoli.expressions.operations.Operation;
import org.cirdles.tripoli.expressions.userFunctions.UserFunctionNode;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;

import java.util.List;
import java.util.Stack;


public class ExpressionTree implements ExpressionTreeInterface{
    //    private static final long serialVersionUID = 69881766695649050L;
    protected String name;
    protected ExpressionTreeInterface leftChildET;
    protected ExpressionTreeInterface rightChildET;
    public Operation rootOperator;
    protected boolean rootExpressionTree;


    public ExpressionTree(String name, ExpressionTreeInterface leftChildET, ExpressionTreeInterface rightChildET, Operation rootOperator) {
        this.name = name;
        this.leftChildET = leftChildET;
        this.rightChildET = rightChildET;
        this.rootOperator = rootOperator;
    }

    @Override
    public Double eval(AnalysisInterface analysis){
        return rootOperator == null ? 0.0 : rootOperator.eval(leftChildET, rightChildET, analysis);
    }

    public ExpressionTree copy(){
        return new ExpressionTree(name, leftChildET, rightChildET, rootOperator);
    }

    public static ExpressionTreeInterface buildTree(List<String> parsedRPN) {
        Stack<ExpressionTreeInterface> stack = new Stack<>();

        for (String token : parsedRPN) {
            // Check if the token is an operator (using your custom operations map)
            if (Operation.OPERATIONS_MAP.containsKey(token)) {
                // Verify there are at least two elements available on the stack
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Invalid RPN expression: insufficient operands for operator " + token);
                }
                ExpressionTreeInterface rightChild = stack.pop();
                ExpressionTreeInterface leftChild = stack.pop();

                // Create a new ExpressionTree node for the operator
                ExpressionTreeInterface node = new ExpressionTree(
                        "",
                        leftChild,
                        rightChild,
                        Operation.OPERATIONS_MAP.get(token)
                );

                // Push the new node back onto the stack
                stack.push(node);
            } else {
                // It's an operand: create a leaf node and push it onto the stack.
                ExpressionTreeInterface leaf = new UserFunctionNode(token);
                stack.push(leaf);
            }
        }

        // There should be exactly one element in the stack (the root of the tree).
        if (stack.size() != 1) {
            throw new IllegalStateException("Invalid RPN expression: the final stack size is " + stack.size());
        }

        return stack.pop();
    }

    public String prettyPrint(ExpressionTreeInterface node, AnalysisInterface analysis) {
        if (node == null) {
            return "";
        }

        // If the node is a leaf (operand/user function), simply return the token string.
        if (node instanceof UserFunctionNode) {
            // Assuming getToken() returns the operand or variable value
            return String.valueOf(node.eval(analysis));
        }

        // If the node is an operator node, assume it's an instance of ExpressionTree.
        if (node instanceof ExpressionTree) {
            ExpressionTree tree = (ExpressionTree) node;

            // Recursively prettyPrint the left and right subtrees.
            String leftStr = prettyPrint(tree.getLeft(), analysis);
            String rightStr = prettyPrint(tree.getRight(), analysis);
            // Assuming getOperation() returns an Operation instance that has a getSymbol() method.
            String operatorSymbol = tree.getOperation().getName();

            // Wrap the expression in parentheses to preserve order of operations.
            return "(" + leftStr + " " + operatorSymbol + " " + rightStr + ")";
        }

        // If you have other types of nodes, handle them here.
        return "";
    }

    private ExpressionTreeInterface getLeft() {
        return this.leftChildET;
    }
    private ExpressionTreeInterface getRight() {
        return this.rightChildET;
    }
    private Operation getOperation() {
        return this.rootOperator;
    }

    @Override
    public int getOperationPrecedence() {
        int retVal = 100;

        if (rootOperator != null) {
            retVal = rootOperator.getPrecedence();
        }

        return retVal;
    }
}
