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
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

public class ReportCategory implements Serializable, Comparable<ReportCategory> {
    private static final long serialVersionUID = 6830475493400638448L;

    private String categoryName;
    private int positionIndex;
    private Set<ReportColumn> columnSet;
    private boolean visible;

    public final String FIXED_CATEGORY_NAME = "Analysis Info";

    // Handle blank category with no column data
    // todo: handle null position index (Append to end of treeset)
    public ReportCategory() {
        categoryName = "<Create a Category>";
        columnSet = new TreeSet<>();
        columnSet.add(new ReportColumn("<Add Column>", Integer.MAX_VALUE));
        visible = true;
    }
    // Handle importing existing category
    public ReportCategory(String categoryName, Set<ReportColumn> columnDetailsList, int positionIndex) {
        this.categoryName = categoryName;
        columnSet = columnDetailsList;
        this.positionIndex = positionIndex;
        visible = true;
    }

    // Handle known name
    public ReportCategory(String categoryName, int positionIndex) {
        this.categoryName = categoryName;
        columnSet = new TreeSet<>();
        this.positionIndex = positionIndex;
        visible = true;
    }
    public int getPositionIndex() {
        return positionIndex;
    }

    public void setPositionIndex(int positionIndex) {
        this.positionIndex = positionIndex;
    }

    public void setVisible(boolean visible) {this.visible = visible;}
    public boolean isVisible() {return visible;}

    public void addColumn() {
        columnSet.add(new ReportColumn("<Add Column>", Integer.MAX_VALUE));
    }
    public void addColumn(ReportColumn reportColumn) {
        columnSet.add(reportColumn);
    }
    public Set<ReportColumn> getColumns(){ return columnSet; }
    public String getCategoryName() {return categoryName;}

    public static ReportCategory generateAnalysisInfo() throws TripoliException {
        Set<ReportColumn> columnSet = new TreeSet<>();
        int i=0;
        columnSet.add(new ReportColumn("Analysis Name", i++));
        columnSet.add(new ReportColumn("Analyst", i++));
        columnSet.add(new ReportColumn("Lab Name",i++));
        columnSet.add(new ReportColumn("Sample Name",i++));
        columnSet.add(new ReportColumn("Sample Description",i++));
        columnSet.add(new ReportColumn("Fraction",i++));
        columnSet.add(new ReportColumn("Data File Name",i++));
        columnSet.add(new ReportColumn("Data File Path",i++));
        columnSet.add(new ReportColumn("Method Name",i++));
        columnSet.add(new ReportColumn("Start Time", i));

        return new ReportCategory("Analysis Info", columnSet, 0);
    }

    public static ReportCategory generateIsotopicRatios(List<UserFunction> userFunctionList) throws TripoliException {
        Set<ReportColumn> columnSet = new TreeSet<>();
        int i=0;
        for (UserFunction userFunction : userFunctionList) {
            if (userFunction.isTreatAsIsotopicRatio()) {
                columnSet.add(new ReportColumn(userFunction.getName(), i++));
            }
        }

        return new ReportCategory("Isotopic Ratios", columnSet,1);
    }

    public static ReportCategory generateUserFunctions(List<UserFunction> userFunctionList) throws TripoliException {
        Set<ReportColumn> columnSet = new TreeSet<>();
        int i=0;
        for (UserFunction userFunction : userFunctionList){
            columnSet.add(new ReportColumn(userFunction.getName(), i++));
        }
        return new ReportCategory("User Functions", columnSet,2);
    }
    public void updateColumnPosition(ReportColumn column, int newIndex) {
        int oldIndex = column.getPositionIndex();
        if (oldIndex == newIndex) {
            return;
        }

        columnSet.remove(column);

        if (oldIndex > newIndex) {
            for (ReportColumn c : columnSet) {
                if (c.getPositionIndex() >= newIndex && c.getPositionIndex() < oldIndex) {
                    c.setPositionIndex(c.getPositionIndex() + 1);
                }
            }
        } else {
            for (ReportColumn c : columnSet) {
                if (c.getPositionIndex() > oldIndex && c.getPositionIndex() <= newIndex) {
                    c.setPositionIndex(c.getPositionIndex() - 1);
                }
            }
        }

        column.setPositionIndex(newIndex);

        columnSet.add(column);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ReportCategory that = (ReportCategory) o;
        return positionIndex == that.positionIndex && visible == that.visible && Objects.equals(categoryName, that.categoryName) && Objects.equals(columnSet, that.columnSet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryName, positionIndex, columnSet, visible, FIXED_CATEGORY_NAME);
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
