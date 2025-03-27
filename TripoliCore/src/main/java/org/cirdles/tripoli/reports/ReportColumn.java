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
import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.sessions.analysis.AnalysisStatsRecord;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class ReportColumn implements Serializable, Comparable<ReportColumn>{
    private static final long serialVersionUID = 3378567673921898881L;
    public String FIXED_COLUMN_NAME = "Analysis Name";

    private final String columnName;
    private int positionIndex;
    private boolean visible;
    private String methodName;
    private boolean isUserFunction;

    public ReportColumn(String title, int positionIndex, String methodName) {
        columnName = title;
        this.positionIndex = positionIndex;
        visible = true;
        this.methodName = methodName;
        isUserFunction = false;
    }
    public ReportColumn(String title, int positionIndex, boolean isUserFunction) {
        columnName = title;
        this.positionIndex = positionIndex;
        visible = true;
        this.isUserFunction = isUserFunction;
    }
    public ReportColumn(ReportColumn otherColumn) {
        columnName = otherColumn.columnName;
        positionIndex = otherColumn.positionIndex;
        visible = otherColumn.visible;
        FIXED_COLUMN_NAME = otherColumn.FIXED_COLUMN_NAME;
        methodName = otherColumn.methodName;
        isUserFunction = otherColumn.isUserFunction;
    }

    public String getColumnName() { return columnName; }

    public void setVisible(boolean visible) { this.visible = visible; }
    public boolean isVisible() { return visible; }

    public void setPositionIndex(int i) {
        positionIndex = i;
    }
    public int getPositionIndex() { return positionIndex; }

    public void setMethodName(String methodName) { this.methodName = methodName; }
    public String getMethodName() { return methodName; }

    public boolean isUserFunction() { return isUserFunction; }

    public String retrieveData(Analysis analysis) {
        if (methodName == null) {
            return "null";
        }

        if (methodName.equals("getUserFunctions")) {
            return retrieveUserFunctionData(analysis.getUserFunctions());
        }

        return invokeAnalysisMethod(analysis);
    }

    private String retrieveUserFunctionData(List<UserFunction> userFunctions) {
        return userFunctions.stream()
                .filter(f -> f.getName().equals(columnName))
                .findFirst()
                .map(userFunction -> {
                    AnalysisStatsRecord stats = userFunction.getAnalysisStatsRecord();
                    return String.format(
                            "Mean: %s, Variance: %s, Std Dev: %s",
                            stats.cycleModeMean(),
                            stats.cycleModeVariance(),
                            stats.cycleModeStandardDeviation()
                    );
                })
                .orElse("Error: Function not found");
    }

    private String invokeAnalysisMethod(Analysis analysis) {
        try {
            Method columnMethod = Analysis.class.getMethod(methodName);
            Object result = columnMethod.invoke(analysis);

            if (result instanceof AnalysisMethod analysisMethod) {
                return analysisMethod.getMethodName();
            } else if ("Data File Name".equals(columnName)) {
                return Path.of((String) result).getFileName().toString();
            } else {
                return result.toString();
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return "Error invoking method: " + methodName;
        }
    }

    @Override
    public int compareTo(@NotNull ReportColumn column) {
        if (this.columnName.equals(FIXED_COLUMN_NAME)){
            return -1;
        } else if (column.getColumnName().equals(FIXED_COLUMN_NAME)){
            return 1;
        }
        return Integer.compare(this.positionIndex, column.getPositionIndex());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ReportColumn that = (ReportColumn) o;

        return positionIndex == that.positionIndex && visible == that.visible && Objects.equals(columnName, that.columnName);
    }

    @Override
    public int hashCode() {
        return Objects.hash( positionIndex, visible, columnName);
    }
}
