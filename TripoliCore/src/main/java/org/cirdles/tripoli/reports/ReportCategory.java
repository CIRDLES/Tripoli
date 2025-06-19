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
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ReportCategory implements Serializable, Comparable<ReportCategory> {
    private static final long serialVersionUID = 6830475493400638448L;

    private String categoryName;
    private int positionIndex;
    private Set<ReportColumn> columnSet;
    private boolean visible;

    public final String FIXED_CATEGORY_NAME = "Analysis Info";

    // Handle importing existing category
    public ReportCategory(String categoryName, Set<ReportColumn> reportColumnSet, int positionIndex) {
        this.categoryName = categoryName;
        columnSet = reportColumnSet;
        this.positionIndex = positionIndex;
        visible = true;
    }
    // Handle known name, no columns
    public ReportCategory(String categoryName, int positionIndex) {
        this.categoryName = categoryName;
        columnSet = new TreeSet<>();
        this.positionIndex = positionIndex;
        visible = true;
    }
    public ReportCategory(ReportCategory otherCategory) {
        this.categoryName = otherCategory.categoryName;
        this.positionIndex = otherCategory.positionIndex;
        this.visible = otherCategory.visible;
        this.columnSet = otherCategory.columnSet.stream()
                .map(ReportColumn::new)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public int getPositionIndex() {
        return positionIndex;
    }

    public void setPositionIndex(int positionIndex) {
        this.positionIndex = positionIndex;
    }

    public void setVisible(boolean visible) {this.visible = visible;}
    public boolean isVisible() {return visible;}

    public void addColumn(ReportColumn reportColumn) {
        columnSet.add(reportColumn);
    }
    public void removeColumn(ReportColumn reportColumn) { columnSet.remove(reportColumn); }

    public Set<ReportColumn> getColumns(){ return columnSet; }
    public String getCategoryName() {return categoryName;}

    /**
     * Generates the default columns for the Analysis Info category used in the full report
     * @return Default ReportCategory for Analysis Info
     */
    public static ReportCategory generateAnalysisInfo() {
        Set<ReportColumn> columnSet = new TreeSet<>();
        int i=0;
        columnSet.add(new ReportColumn("Analysis Name", i++, "getAnalysisName"));
        columnSet.add(new ReportColumn("Analyst", i++, "getAnalystName"));
        columnSet.add(new ReportColumn("Lab Name",i++, "getLabName"));
        columnSet.add(new ReportColumn("Sample Name",i++, "getAnalysisSampleName"));
        columnSet.add(new ReportColumn("Sample Description",i++, "getAnalysisSampleDescription"));
        columnSet.add(new ReportColumn("Fraction",i++, "getAnalysisFractionName"));
        columnSet.add(new ReportColumn("Data File Name",i++, "getDataFilePathString"));
        columnSet.add(new ReportColumn("Data File Path",i++, "getDataFilePathString"));
        columnSet.add(new ReportColumn("Method Name",i++, "getMethod"));
        columnSet.add(new ReportColumn("Start Time", i, "getAnalysisStartTime"));

        return new ReportCategory("Analysis Info", columnSet, 0);
    }

    /**
     * Generates the default columns for the Isotopic Ratios category used in the full report based on a list of UserFunctions
     * @return Default ReportCategory for Isotopic Ratios
     */
    public static ReportCategory generateIsotopicRatios(List<UserFunction> userFunctionList){
        Set<ReportColumn> columnSet = new TreeSet<>();
        int i=0;
        for (UserFunction userFunction : userFunctionList) {
            if (userFunction.isTreatAsIsotopicRatio() && !userFunction.isTreatAsCustomExpression()) {
                columnSet.add(new ReportColumn(userFunction.getName(), i++, true, true));
            }
        }

        return new ReportCategory("Isotopic Ratios", columnSet,1);
    }

    /**
     * Generates the default columns for the User Function category used in the full report based on a list of UserFunctions
     * @return Default ReportCategory for User Functions
     */
    public static ReportCategory generateUserFunctions(List<UserFunction> userFunctionList) {
        Set<ReportColumn> columnSet = new TreeSet<>();
        int i=0;
        for (UserFunction userFunction : userFunctionList){
            if (!userFunction.isTreatAsIsotopicRatio() && !userFunction.isTreatAsCustomExpression()) {
                columnSet.add(new ReportColumn(userFunction.getName(), i++, true, false));
            }
        }
        return new ReportCategory("User Functions", columnSet,2);
    }

    public static ReportCategory generateCustomExpressions(List<UserFunction> userFunctionList){
        Set<ReportColumn> columnSet = new TreeSet<>();
        int i=0;
        for (UserFunction userFunction : userFunctionList){
            if (userFunction.isTreatAsCustomExpression() && userFunction.isTreatAsIsotopicRatio()){
                columnSet.add(new ReportColumn(userFunction.getCustomExpression().getName(), i++, true, true));
            } else if (userFunction.isTreatAsCustomExpression()){
                columnSet.add(new ReportColumn(userFunction.getName(), i++, true, false));
            }
        }
        return new ReportCategory("Custom Expressions", columnSet,3);
    }

    /**
     * Updates a column move within the set. Pushes column indices up or down based on the placement of the index
     * @param column column to be moved
     * @param newIndex new index for the column in the set
     */
    public void updateColumnPosition(ReportColumn column, int newIndex) {
        int oldIndex = column.getPositionIndex();
        if (oldIndex == newIndex) {return;}

        columnSet.remove(column);

        for (ReportColumn c : columnSet) {
            int currentIndex = c.getPositionIndex();
            if (currentIndex >= newIndex && currentIndex < oldIndex) {
                c.setPositionIndex(currentIndex + 1);
            } else if (currentIndex > oldIndex && currentIndex <= newIndex) {
                c.setPositionIndex(currentIndex - 1);
            }
        }

        column.setPositionIndex(newIndex);
        columnSet.add(column);
    }

    /**
     * Placing a new column at a specific index in the set. Makes a mutable copy so that the entry can be a duplicate.
     * @param column ReportColumn instance to be copied
     * @param index Index to be inserted at. All greater indices will be shifted up.
     */
    public void insertColumnAtPosition(ReportColumn column, int index) {
        for (ReportColumn c : columnSet) {
            if (c.getPositionIndex() >= index) {
                c.setPositionIndex(c.getPositionIndex() + 1);
            }
        }
        column.setPositionIndex(index);
        columnSet.add(column);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ReportCategory that = (ReportCategory) o;
        boolean columnsEqual = true;
        Iterator<ReportColumn> columnIterator1 = this.columnSet.iterator();
        Iterator<ReportColumn> columnIterator2 = that.columnSet.iterator();

        if (this.columnSet.size() != that.columnSet.size()) { columnsEqual = false; }
        while (columnIterator1.hasNext() && columnIterator2.hasNext()) {
            ReportColumn column1 = columnIterator1.next();
            ReportColumn column2 = columnIterator2.next();
            if (!column1.equals(column2)) {
                columnsEqual = false;
            }
        }
        return positionIndex == that.positionIndex && visible == that.visible && Objects.equals(categoryName, that.categoryName) && columnsEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(positionIndex, visible, categoryName, columnSet);
    }

    @Override
    public int compareTo(@NotNull ReportCategory category) {
        // Holds the fixed category name in its assigned index
        if (this.categoryName.equals(FIXED_CATEGORY_NAME)){
            return -1;
        } else if (category.getCategoryName().equals(FIXED_CATEGORY_NAME)){
            return 1;
        }
        return Integer.compare(this.positionIndex, category.getPositionIndex());
    }
}
