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

package org.cirdles.tripoli.reports;

import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.sessions.analysis.outputs.etRedux.MeasuredUserFunction;

import java.io.Serializable;
import java.util.List;

public class ReportDetails implements Serializable {
    private static final long serialVersionUID = 3378567673921898881L;

    private List<UserFunction> reportFunctions;
    private final String columnName;
    private final String columnValue;

    public ReportDetails(String title, String data) {
        columnName = title;
        columnValue = data;
    }
    public ReportDetails(UserFunction function) {
        columnName = function.getName();
        MeasuredUserFunction measuredUserFunctionModel = new MeasuredUserFunction(function.showCorrectName());
        //measuredUserFunctionModel.refreshStats(function); todo: function is blank, must be initialized
        columnValue = String.valueOf(measuredUserFunctionModel.getValue());

    }

    public String getColumnName() { return columnName; }
    public String getColumnValue() { return columnValue; }

    public void moveFunctionPosition(UserFunction function, int newPosition) {
        reportFunctions.remove(function);
        reportFunctions.add(newPosition, function);
    }

}
