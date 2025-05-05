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

package org.cirdles.tripoli.expressions.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.regex.Pattern;

public class ShuntingYard {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");
    private static final Pattern NUMBER_SCIENTIFIC_PATTERN = Pattern.compile("([+-]?(?:0|[1-9]\\d*)(?:\\.\\d*)?(?:[eE][+\\-]?\\d+))");

    public static List<String> infixToPostfix(List<String> infix) {
        Stack<String> operatorStack = new Stack<>();
        List<String> outputQueue = new ArrayList<>();
        Stack<Boolean> wereValues = new Stack<>();
        Stack<Integer> argCount = new Stack<>();
        boolean lastWasOperationOrFunction = true;

        for (String token : infix) {
            // classify token
            TokenTypes tokenType = TokenTypes.getType(token);
            switch (tokenType) {
                case OPERATOR_A:
                    /* while there is an operator token o2, at the top of the operator stack and either
                    o1 is left-associative and its precedence is less than or equal to that of o2, or
                    o1 is right associative, and has precedence less than that of o2,
                    pop o2 off the operator stack, onto the output queue;
                    at the end of iteration push o1 onto the operator stack.
                     */

                    boolean keepLooking = true;

                    // allow for negative expressions by inserting -1 *
                    if (lastWasOperationOrFunction && token.compareTo("-") == 0) {
                        outputQueue.add("-1");
                        operatorStack.push("*");
                        lastWasOperationOrFunction = true;
                    } else {
                        while (!operatorStack.empty() && keepLooking) {
                            TokenTypes peek = TokenTypes.getType(operatorStack.peek());
                            if ((peek.compareTo(TokenTypes.OPERATOR_A) == 0)
                                    || (peek.compareTo(TokenTypes.OPERATOR_M) == 0)
                                    || (peek.compareTo(TokenTypes.OPERATOR_E) == 0)) {
                                outputQueue.add(operatorStack.pop());
                            } else {
                                keepLooking = false;
                            }
                        }
                        operatorStack.push(token);
                        lastWasOperationOrFunction = true;
                    }
                    break;
                case OPERATOR_M:
                    /* while there is an operator token o2, at the top of the operator stack and either
                    o1 is left-associative and its precedence is less than or equal to that of o2, or
                    o1 is right associative, and has precedence less than that of o2,
                    pop o2 off the operator stack, onto the output queue;
                    at the end of iteration push o1 onto the operator stack.
                     */
                    keepLooking = true;
                    while (!operatorStack.empty() && keepLooking) {
                        TokenTypes peek = TokenTypes.getType(operatorStack.peek());
                        if ((peek.compareTo(TokenTypes.OPERATOR_M) == 0)
                                || (peek.compareTo(TokenTypes.OPERATOR_E) == 0)) {
                            outputQueue.add(operatorStack.pop());
                        } else {
                            keepLooking = false;
                        }
                    }
                    operatorStack.push(token);
                    lastWasOperationOrFunction = true;
                    break;
                case OPERATOR_E:
                /* while there is an operator token o2, at the top of the operator stack and either
                    o1 is left-associative and its precedence is less than or equal to that of o2, or
                    o1 is right associative, and has precedence less than that of o2,
                    pop o2 off the operator stack, onto the output queue;
                    at the end of iteration push o1 onto the operator stack.
                 */
//                    operatorStack.push(token);
//                    lastWasOperationOrFunction = true;
//                    break;
                case LEFT_PAREN:
                    if (!lastWasOperationOrFunction) {
                        // If the last token was a value or right parenthesis, insert a multiplication operator
                        operatorStack.push("*");
                    }
                    operatorStack.push(token);
                    lastWasOperationOrFunction = true;
                    break;

                case RIGHT_PAREN:
                    /* Until the token at the top of the stack is a left parenthesis, pop operators off the stack onto the output queue.
                    Pop the left parenthesis from the stack, but not onto the output queue.
                    If the token at the top of the stack is a function token, pop it onto the output queue.
                    If the stack runs out without finding a left parenthesis, then there are mismatched parentheses.
                     */
                    keepLooking = true;
                    while (!operatorStack.empty() && keepLooking) {
                        TokenTypes peek = TokenTypes.getType(operatorStack.peek());
                        if ((peek.compareTo(TokenTypes.OPERATOR_A) == 0)
                                || (peek.compareTo(TokenTypes.OPERATOR_M) == 0)
                                || (peek.compareTo(TokenTypes.OPERATOR_E) == 0)) {
                            outputQueue.add(operatorStack.pop());
                        } else {
                            keepLooking = false;
                            if (peek.compareTo(TokenTypes.LEFT_PAREN) == 0) {
                                operatorStack.pop();
                                if (!operatorStack.empty()) {
                                    String func;
                                    peek = TokenTypes.getType(operatorStack.peek());
                                    if (peek.compareTo(TokenTypes.FUNCTION) == 0) {
                                        func = operatorStack.pop();
//                                        int a = argCount.pop();
                                        boolean w = wereValues.pop();
                                        if (w) {
                                            // opted to specify count of arguments in function definition
//                                            a++;
//                                            String funcWithArgCount = func + ":" + String.valueOf(a);
                                            outputQueue.add(func);// temp simplify(funcWithArgCount);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    lastWasOperationOrFunction = false;
                    break;
                case NUMBER:
                case NAMED_CONSTANT:
                    outputQueue.add(token);
                    if (!wereValues.empty()) {
                        wereValues.pop();
                        wereValues.push(true);
                    }
                    lastWasOperationOrFunction = false;
                    break;
                case FUNCTION:
                    operatorStack.push(token);
                    if (!wereValues.empty()) {
                        wereValues.pop();
                        wereValues.push(true);
                    }
                    wereValues.push(false);
                    argCount.push(0);
                    lastWasOperationOrFunction = true;
                    break;
                case COMMA:
                    /*If the token is a function argument separator (e.g., a comma):
                        Until the token at the top of the stack is a left parenthesis,
                    pop operators off the stack onto the output queue. If no left parentheses
                    are encountered, either the separator was misplaced or parentheses were mismatched.
                     */
                    keepLooking = true;
                    while (!operatorStack.empty() && keepLooking) {
                        TokenTypes peek = TokenTypes.getType(operatorStack.peek());
                        if (peek.compareTo(TokenTypes.LEFT_PAREN) == 0) {
                            keepLooking = false;
                        } else {
                            outputQueue.add(operatorStack.pop());
                        }
                    }

                    boolean w = false;
                    try {
                        w = wereValues.pop();
                    } catch (Exception e) {
                        // ignore
                    }
                    if (w) {
                        int a = 0;
                        try {
                            a = argCount.pop();
                        } catch (Exception e) {
                            //ignore
                        }
                        argCount.push(a + 1);
                    }
                    wereValues.push(false);
                    lastWasOperationOrFunction = false;
                    break;
                case FORMATTER:
                case NAMED_EXPRESSION_INDEXED:
                case NAMED_EXPRESSION:
                    outputQueue.add(token);
                    if (!wereValues.empty()) {
                        wereValues.pop();
                        wereValues.push(true);
                    }
                    lastWasOperationOrFunction = false;
                    break;
                default:
                    break;
            }
        }

        while (!operatorStack.empty()) {
            outputQueue.add(operatorStack.pop());
        }

        return outputQueue;
    }

    public static boolean isNumber(String string) {
        return (string != null) && (NUMBER_PATTERN.matcher(string).matches() || NUMBER_SCIENTIFIC_PATTERN.matcher(string).matches());
    }

    public enum TokenTypes {

        /**
         *
         */
        OPERATOR_A,
        /**
         *
         */
        OPERATOR_M,
        /**
         *
         */
        OPERATOR_E,
        /**
         *
         */
        LEFT_PAREN,
        /**
         *
         */
        RIGHT_PAREN,
        /**
         *
         */
        NUMBER,
        /**
         *
         */
        NAMED_CONSTANT,
        /**
         *
         */
        FUNCTION,
        /**
         *
         */
        NAMED_EXPRESSION,
        /**
         *
         */
        NAMED_EXPRESSION_INDEXED,
        /**
         *
         */
        COMMA,
        FORMATTER;

        TokenTypes() {}
        public static TokenTypes getType(String token) {
            TokenTypes retVal = NAMED_CONSTANT;

            if ("|+|-|==|<|<=|>|>=|<>|".contains("|" + token + "|")) {
                retVal = OPERATOR_A;
            } else if ("*/".contains(token)) {
                retVal = OPERATOR_M;
            } else if ("|^|$$|".contains("|" + token + "|")) {
                // '$$' is hidden Value operation for top level singleton expressions
                retVal = OPERATOR_E;
            } else if ("(".contains(token)) {
                retVal = LEFT_PAREN;
            } else if (")".contains(token)) {
                retVal = RIGHT_PAREN;
            } else if (token.equals(",")) {
                retVal = COMMA;
            //} else if (FUNCTIONS_MAP.containsKey(token)) {
            //    retVal = FUNCTION;
            } else if (token.matches("\\[(±?)(%?)\"(.*?)\"\\]( )*\\[\\d\\]( )*")) {
                retVal = NAMED_EXPRESSION_INDEXED;
            } else if (token.matches("\\w+\\[\\d\\]") && !token.matches("\\d+\\[\\d\\]")
                    && !token.toUpperCase(Locale.ENGLISH).matches("TRUE\\[\\d\\]")
                    && !token.toUpperCase(Locale.ENGLISH).matches("FALSE\\[\\d\\]")) {
                retVal = NAMED_EXPRESSION_INDEXED;
            } else if (token.matches("\\[(±?)(%?)\"(.*?)\"\\]")) {
                retVal = NAMED_EXPRESSION;
            } else if (token.matches("\\w+") && !token.matches("\\d+") && !token.toUpperCase().matches("TRUE") && !token.toUpperCase().matches("FALSE")) {
                retVal = NAMED_EXPRESSION;
            } else if ("| |\t|\n|\r|".contains("|" + token + "|")) {
                retVal = FORMATTER;
            } else if (isNumber(token)) {
                retVal = NUMBER;
            }

            return retVal;
        }
        }
}
