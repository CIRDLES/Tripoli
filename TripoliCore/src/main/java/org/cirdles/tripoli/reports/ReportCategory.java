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

import org.cirdles.tripoli.expressions.species.IsotopicRatio;
import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReportCategory implements Serializable, Comparable<ReportCategory> {
    private static final long serialVersionUID = 6830475493400638448L;

    private String categoryName;
    private int positionIndex;
    private List<ReportDetails> columnDetails;
    public boolean visible;

    private final String FIXED_CATEGORY_NAME = "Analysis Info";

    // Handle blank category with no column data
    // todo: handle null position index (Append to end of treeset)
    public ReportCategory() {
        categoryName = "<Create a Category>";
        columnDetails = new ArrayList<>();
        columnDetails.add(new ReportDetails("<Add Column>", "<Add Data>"));
        visible = true;
    }
    // Handle importing existing category
    public ReportCategory(String categoryName, List<ReportDetails> columnDetailsList) {
        this.categoryName = categoryName;
        this.columnDetails = columnDetailsList;
        visible = true;
    }

    // Handle known name
    public ReportCategory(String categoryName) {
        this.categoryName = categoryName;
        columnDetails = new ArrayList<>();
        visible = true;
    }
    public int getPositionIndex() {
        return positionIndex;
    }

    public void setPositionIndex(int positionIndex) {
        this.positionIndex = positionIndex;
    }

    public void addColumn() {
        columnDetails.add(new ReportDetails("<Add Column>", "<Add Data>"));
    }
    public void addColumn(ReportDetails reportDetails) {
        columnDetails.add(reportDetails);
    }
    public List<ReportDetails> getColumns(){ return columnDetails; }
    public String getCategoryName() {return categoryName;}

    public static ReportCategory generateAnalysisInfo(Analysis analysis) throws TripoliException {
        if (analysis == null) {
            analysis = AnalysisInterface.initializeNewAnalysis(0);

        }
        List<ReportDetails> columnList = new ArrayList<>();
        columnList.add(new ReportDetails("Analysis Name", analysis.getAnalysisName()));
        columnList.add(new ReportDetails("Analyst", analysis.getAnalystName()));
        columnList.add(new ReportDetails("Lab Name", analysis.getLabName()));
        columnList.add(new ReportDetails("Sample Name", analysis.getAnalysisSampleName()));
        columnList.add(new ReportDetails("Sample Description", analysis.getAnalysisSampleDescription()));
        columnList.add(new ReportDetails("Fraction", analysis.getAnalysisFractionName()));
        columnList.add(new ReportDetails("Data File Name", Paths.get((analysis.getDataFilePathString())).getFileName().toString()));
        columnList.add(new ReportDetails("Data File Path", analysis.getDataFilePathString()));
        if (analysis.getMethod() != null) {
            columnList.add(new ReportDetails("Method Name", analysis.getMethod().getMethodName()));
        } else {
            columnList.add(new ReportDetails("Method Name", ""));
        }

        columnList.add(new ReportDetails("Start Time", analysis.getAnalysisStartTime()));

        return new ReportCategory("Analysis Info", columnList);
    }

    public static ReportCategory generateIsotopicRatios(Analysis analysis) throws TripoliException {
        if (analysis == null) {
            return new ReportCategory("Isotopic Ratio Analysis");
        }
        List<IsotopicRatio> ratioList = analysis.getAnalysisMethod().getIsotopicRatiosList();

        List<ReportDetails> columnList = new ArrayList<>();

        for (IsotopicRatio ratio : ratioList){
            //todo: theres no way this is the correct ratio value
            columnList.add(new ReportDetails(ratio.prettyPrint(), ratio.getRatioValuesForBlockEnsembles().toString()));
            //todo: missing other category options from report specification doc
        }
        return new ReportCategory("Isotopic Ratios", columnList);
    }

    public static ReportCategory generateUserFunctions(Analysis analysis) throws TripoliException {
        if (analysis == null) {
            analysis = AnalysisInterface.initializeNewAnalysis(0);
        }
        List<UserFunction> userFunctionList = analysis.getUserFunctions();
        List<ReportDetails> columnList = new ArrayList<>();
        for (UserFunction userFunction : userFunctionList){
            // todo: this is not complete to specification
            columnList.add(new ReportDetails(userFunction));
        }
        return new ReportCategory("User Functions", columnList);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ReportCategory that = (ReportCategory) o;
        return positionIndex == that.positionIndex && visible == that.visible && Objects.equals(categoryName, that.categoryName) && Objects.equals(columnDetails, that.columnDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryName, positionIndex, columnDetails, visible, FIXED_CATEGORY_NAME);
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
