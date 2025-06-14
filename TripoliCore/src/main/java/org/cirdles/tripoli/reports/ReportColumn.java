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
import org.cirdles.tripoli.sessions.analysis.GeometricMeanStatsRecord;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.utilities.mathUtilities.MathUtilities;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.cirdles.tripoli.sessions.analysis.GeometricMeanStatsRecord.generateGeometricMeanStats;

public class ReportColumn implements Serializable, Comparable<ReportColumn>{
    private static final long serialVersionUID = 3378567673921898881L;
    public String FIXED_COLUMN_NAME = "Analysis Name";

    private String columnName;
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
    public void setColumnName(String columnName) { this.columnName = columnName; }

    public void setVisible(boolean visible) { this.visible = visible; }
    public boolean isVisible() { return visible; }

    public void setPositionIndex(int i) {
        positionIndex = i;
    }
    public int getPositionIndex() { return positionIndex; }

    public boolean isUserFunction() { return isUserFunction; }

    /**
     * Use the supplied analysis to extract data for the current column. Handles user function based columns as well as
     * method-defined non-uf columns.
     * @param analysis the analysis data to be extracted
     * @return The data based on the analysis, a null result represents a misconfigured column
     */
    public String retrieveData(Analysis analysis) {
        if (isUserFunction) {
            return retrieveUserFunctionData(analysis.getUserFunctions());
        }
        if (methodName == null) {
            return "null";
        }

        return invokeAnalysisMethod(analysis);
    }

    private String retrieveUserFunctionData(List<UserFunction> userFunctions) {
        String baseColumnName = columnName.contains(" ( = ")
                ? columnName.split(" \\( = ")[0]
                : columnName;

        return userFunctions.stream()
                .filter(f -> f.getName().equals(baseColumnName))
                .findFirst()
                .map(userFunction -> {
                    AnalysisStatsRecord stats = userFunction.getAnalysisStatsRecord();
                    GeometricMeanStatsRecord geoStats =
                            generateGeometricMeanStats(stats.cycleModeMean(), stats.cycleModeStandardDeviation(), stats.cycleModeStandardError());

                    if (userFunction.isTreatAsIsotopicRatio()) {

                        return String.format(
                                "%s,%s,%s",
                                MathUtilities.roundedToSize(geoStats.geoMean(), 4),
                                MathUtilities.roundedToSize(
                                        (geoStats.geoMeanPlusOneStdErr() - geoStats.geoMean()) / geoStats.geoMean() * 100.0,
                                        4) +
                                        "%",
                                MathUtilities.roundedToSize(
                                        (geoStats.geoMeanPlusOneStdDev() - geoStats.geoMean()) / geoStats.geoMean() * 100.0,
                                        4) +
                                        "%"
                        );
                    } else {
                        return String.format(
                                "%s,%s,%s",
                                MathUtilities.roundedToSize(stats.cycleModeMean(), 4),
                                MathUtilities.roundedToSize(stats.cycleModeStandardError(), 4),
                                MathUtilities.roundedToSize(stats.cycleModeStandardDeviation(), 4)
                        );
                    }

                })
                .orElse("Error,Error,Error");
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
