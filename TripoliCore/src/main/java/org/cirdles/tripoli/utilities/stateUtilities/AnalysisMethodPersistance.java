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

package org.cirdles.tripoli.utilities.stateUtilities;

import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.expressions.userFunctions.UserFunctionDisplay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AnalysisMethodPersistance implements Serializable {
    private static final long serialVersionUID = -3839100871560617989L;
    //        private void readObject(ObjectInputStream stream) throws IOException,
//            ClassNotFoundException {
//        stream.defaultReadObject();
//
//        ObjectStreamClass myObject = ObjectStreamClass.lookup(
//                Class.forName(AnalysisMethodPersistance.class.getCanonicalName()));
//        long theSUID = myObject.getSerialVersionUID();
//
//        System.out.println("Customized De-serialization of AnalysisMethodPersistance "
//                + theSUID);
//    }
    private int cyclesPerBlock;
    private Map<String, UserFunctionDisplay> userFunctionDisplay;
    private List<UserFunction> expressionUserFunctionList;

    public AnalysisMethodPersistance(int cyclesPerBlock) {
        this.cyclesPerBlock = cyclesPerBlock;
    }

    public int getCyclesPerBlock() {
        return cyclesPerBlock;
    }

    public void setCyclesPerBlock(int cyclesPerBlock) {
        this.cyclesPerBlock = cyclesPerBlock;
    }

    public Map<String, UserFunctionDisplay> getUserFunctionDisplayMap() {
        if (userFunctionDisplay == null) {
            userFunctionDisplay = new TreeMap<>();
        }
        return userFunctionDisplay;
    }

    public void setUserFunctionDisplayMap(Map<String, UserFunctionDisplay> userFunctionDisplay) {
        this.userFunctionDisplay = userFunctionDisplay;
    }

    public List<UserFunction> getExpressionUserFunctionList() {
        if (expressionUserFunctionList == null) {
            expressionUserFunctionList = new ArrayList<>();
        }
        return expressionUserFunctionList;
    }
    public void setExpressionUserFunctionList(List<UserFunction> expressionUserFunctionList) {
        this.expressionUserFunctionList = expressionUserFunctionList;
    }
}
