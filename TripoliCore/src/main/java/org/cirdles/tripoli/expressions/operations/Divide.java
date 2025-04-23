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

import org.cirdles.tripoli.expressions.expressionTrees.ExpressionTreeInterface;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;

public class Divide extends Operation {
    public Divide() {
        name = "divide";
        precedence = 3;
    }
    @Override
    public Double eval(ExpressionTreeInterface leftChild, ExpressionTreeInterface rightChild, AnalysisInterface analysis){
        return leftChild.eval(analysis) / rightChild.eval(analysis);
    }
}
