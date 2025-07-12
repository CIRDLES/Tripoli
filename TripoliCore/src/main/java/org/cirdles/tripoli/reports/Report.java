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

import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne.initializers.AllBlockInitForDataLiteOne;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.stateUtilities.AnalysisMethodPersistance;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliPersistentState;
import org.jetbrains.annotations.NotNull;

import java.io.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;



public class Report implements Serializable, Comparable<Report> {
    private static final long serialVersionUID = 1064098835718283672L;
    public final String FIXED_REPORT_NAME = "Full Report";

    private String reportName;
    private String methodName;

    private Set<ReportCategory> categorySet;

    private String timeCreated;

    public Report(String reportName, String methodName, Set<ReportCategory> categorySet) {
        this.reportName = reportName;
        this.methodName = methodName;
        this.categorySet = categorySet;
    }

    /**
     * Creates a mutable copy from an existing Report
     * @param otherReport Report to be copied
     */
    public Report(Report otherReport) {
        this.reportName = otherReport.reportName;
        this.methodName = otherReport.methodName;
        this.categorySet = otherReport.categorySet.stream()
                .map(ReportCategory::new)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public String getReportName() { return this.reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }

    public String getMethodName() { return this.methodName; }

    public void addCategory(ReportCategory category) {
        this.categorySet.add(category);
    }
    public void removeCategory(ReportCategory category) { this.categorySet.remove(category); }

    public Set<ReportCategory> getCategories() { return categorySet; }

    /**
     * Inserts a category's new index position respecting the position of the category that already exists there.
     * If the moving category is moving up in the set then the categories residing between the old index and the new one
     * will be shifted DOWN to preserve order integrity. Likewise, categories moving down the set will force categories
     * to shift UP.
     * @param category The existing category that will have its index updated
     * @param newIndex New index for that category
     */
    public void updateCategoryPosition(ReportCategory category, int newIndex) {
        int oldIndex = category.getPositionIndex();
        if (oldIndex == newIndex) { return; }
        categorySet.remove(category);

        // Adjust indices for new spot
        for (ReportCategory c : categorySet) {
            int currentIndex = c.getPositionIndex();
            if (currentIndex >= newIndex && currentIndex < oldIndex) {
                c.setPositionIndex(currentIndex + 1);
            } else if (currentIndex > oldIndex && currentIndex <= newIndex) {
                c.setPositionIndex(currentIndex - 1);
            }
        }
        category.setPositionIndex(newIndex);
        categorySet.add(category);
    }

    public static List<Report> getReportList(String methodName) {
        TripoliPersistentState tripoliPersistentState;
        AnalysisMethodPersistance methodPersistence;
        try {
            tripoliPersistentState = TripoliPersistentState.getExistingPersistentState();
            methodPersistence = tripoliPersistentState.getMapMethodNamesToDefaults().get(methodName);
        } catch (TripoliException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        List<Report> methodReports;
        if (methodPersistence != null){
             methodReports = methodPersistence.getReportList();
        } else {
            methodReports = new ArrayList<>();
        }
        return methodReports;
    }

    /**
     * Creates a blank report class which only has the fixed category and column initialized. Since all reports are bound
     * to a method name, that is required to be passed
     * @param reportName Name of the report
     * @param methodName Analysis method relevant to this report
     * @return A report class object
     */
    public static Report createBlankReport(String reportName, String methodName) {
        ReportColumn analysisName = new ReportColumn("Analysis Name", 0, "getAnalysisName");
        ReportCategory analysisInfo = new ReportCategory("Analysis Info", 0);
        analysisInfo.addColumn(analysisName);
        Set<ReportCategory> categorySet = new HashSet<>();
        categorySet.add(analysisInfo);
        return new Report(reportName, methodName, categorySet);
    }

    public static Report createFullReport(String reportName, AnalysisInterface analysis) {
        List<UserFunction> ufList = analysis.getUserFunctions();

        Set<ReportCategory> categories = new TreeSet<>();
        categories.add(ReportCategory.generateAnalysisInfo());
        categories.add(ReportCategory.generateIsotopicRatios(ufList)); // TODO
        categories.add(ReportCategory.generateUserFunctions(ufList)); // TODO
        categories.add(ReportCategory.generateCustomExpressions(ufList));

        return new Report(reportName, analysis.getMethod().getMethodName(), categories);
    }

    public void serializeReport() {
        TripoliPersistentState tripoliPersistentState;
        try {
            tripoliPersistentState = TripoliPersistentState.getExistingPersistentState();
        } catch (TripoliException e) {
            e.printStackTrace();
            return;
        }
        AnalysisMethodPersistance methodPersistence =
                tripoliPersistentState.getMapMethodNamesToDefaults().get(methodName);

        List<Report> methodReports = methodPersistence.getReportList();
        methodReports.add(this);
    }

    public boolean deleteReport() {
        TripoliPersistentState tripoliPersistentState;
        try {
            tripoliPersistentState = TripoliPersistentState.getExistingPersistentState();
        } catch (TripoliException e) {
            e.printStackTrace();
            return false;
        }
        AnalysisMethodPersistance methodPersistence =
                tripoliPersistentState.getMapMethodNamesToDefaults().get(methodName);

        List<Report> methodReports = methodPersistence.getReportList();
        methodReports.remove(this);
        return true;
    }

    /**
     * Creates a class type of File for the location of a generated CSV report. Location is in the same directory as
     * the first analysis and the name is as follows: [SessionName]-[Each Analysis Name]-report.csc
     * <p>THIS METHOD DOES NOT GENERATE THE CSV</p>
     * @param listOfAnalyses analyses to be appended to file name
     * @param sessionName current session name
     * @return File class pointing to the expected output for a CSV file of the generated report
     */
    public static File getReportCSVFile(List<AnalysisInterface> listOfAnalyses, String sessionName){
        StringBuilder filePathString = new StringBuilder(listOfAnalyses.get(0).getDataFilePathString());
        filePathString.replace(filePathString.lastIndexOf(File.separator),filePathString.length(), File.separator);
        filePathString.append(sessionName).append("-");
        for (AnalysisInterface analysis : listOfAnalyses) {
            filePathString.append(analysis.getAnalysisName()).append("-");
        }
        filePathString.append("report.csv");

        return new File(filePathString.toString());
    }
    /**
     * Generates a CSV output and creates it at the report directory. Creates a row in the file for each analysis given.
     * Internally filters out analyses that don't match the report.
     * @param listOfAnalyses List of all loaded analyses
     * @return File of the created CSV. Null if process failed.
     */
    public File generateCSVFile(List<AnalysisInterface> listOfAnalyses, String sessionName) {
        File reportCSVFile = getReportCSVFile(listOfAnalyses, sessionName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportCSVFile))) {
            Set<ReportCategory> categories = this.getCategories().stream()
                    .filter(ReportCategory::isVisible)
                    .sorted(Comparator.comparingInt(ReportCategory::getPositionIndex))
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            // Collect all unique columns across categories
            List<ReportColumn> visibleColumns = categories.stream()
                    .flatMap(category -> category.getColumns().stream())
                    .filter(ReportColumn::isVisible)
                    .map(column -> {
                        String name = column.getColumnName();
                        if (name.contains(" = ( ")) {
                            name = name.split("\\( = ")[0];
                        }

                        // Create a new ReportColumn if needed, or mutate if allowed
                        ReportColumn updatedColumn = new ReportColumn(column);
                        updatedColumn.setColumnName(name);

                        return updatedColumn;
                    })
                    .toList();

            // Header row with proper naming for user function columns
            List<String> headers = new ArrayList<>();
            for (ReportColumn column : visibleColumns) {
                if (column.isUserFunction() && column.isRatio()) {
                    headers.add(column.getColumnName() + " Mean");
                    headers.add("%StdErr");
                    headers.add("%StdDev");
                } else if (column.isUserFunction()){
                    headers.add(column.getColumnName() + " Mean");
                    headers.add("StdErr");
                    headers.add("StdDev");
                } else {
                    headers.add(column.getColumnName());
                }
            }
            writer.write(String.join(",", headers));
            writer.newLine();

            // Write each analysis row
            for (AnalysisInterface analysis : listOfAnalyses) {
                if (analysis.getMethod().getMethodName().equals(methodName)) {
                    AllBlockInitForDataLiteOne.initBlockModels(analysis);
                    Analysis thisAnalysis = (Analysis) analysis;
                    List<String> rowValues = new ArrayList<>();

                    for (ReportColumn column : visibleColumns) {
                        String columnData = column.retrieveData(thisAnalysis);

                        if (column.isUserFunction()) {
                            String[] splitData = columnData.split(",");
                            rowValues.addAll(Arrays.asList(splitData));
                        } else {
                            rowValues.add(columnData);
                        }
                    }

                    writer.write(String.join(",", rowValues));
                    writer.newLine();
                }
            }

            for (int i = 0; i < 10; i++) {
                writer.newLine();
            }
            writer.write(reportCSVFile.getName()+ "  ");
            setTimeCreated(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.write("Created on: " + getTimeCreated() + "  ");
            writer.write("Tripoli  ");
            writer.write(Tripoli.VERSION);


            return reportCSVFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    @Override
    public int compareTo(@NotNull Report o) {
        return this.reportName.compareTo(o.reportName);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Report that = (Report) o;

        boolean categoriesEqual = true;
        Iterator<ReportCategory> categoriesIterator = this.categorySet.iterator();
        Iterator<ReportCategory> categoriesIterator2 = that.categorySet.iterator();

        if (this.categorySet.size() != that.categorySet.size()) { categoriesEqual = false; }
        while (categoriesIterator.hasNext() && categoriesIterator2.hasNext()) {
            ReportCategory category1 = categoriesIterator.next();
            ReportCategory category2 = categoriesIterator2.next();
            if (!category1.equals(category2)) {
                categoriesEqual = false;
            }
        }

        return Objects.equals(reportName, that.reportName) && Objects.equals(methodName, that.methodName) && categoriesEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportName, methodName, categorySet);
    }
}

