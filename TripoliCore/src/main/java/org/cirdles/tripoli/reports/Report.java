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
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliSerializer;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.cirdles.tripoli.constants.TripoliConstants.TRIPOLI_USERS_DATA_FOLDER_NAME;


public class Report implements Serializable, Comparable<Report> {
    private static final long serialVersionUID = 1064098835718283672L;
    private static final String TRIPOLI_CUSTOM_REPORTS_FOLDER = "CustomReports";
    public final String FIXED_REPORT_NAME = "Full Report";
    private static File tripoliReportDirectoryLocal;

    private String reportName;
    private String methodName;

    private Set<ReportCategory> categorySet;

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
                .map(ReportCategory::new) // Call copy constructor of ReportCategory
                .collect(Collectors.toCollection(TreeSet::new));
    }

    // Assumes Report Names will never contain underscores naturally
    public String getReportName() { return this.reportName.replaceAll("_", " ").trim(); }
    public void setReportName(String reportName) { this.reportName = reportName.replaceAll("[\\\\/:*?\"<>| ]", "_").trim(); }

    public void setMethodName(String methodName) { this.methodName = methodName; }
    public String getMethodName() { return this.methodName; }

    public void addCategory(String categoryName) { this.categorySet.add(new ReportCategory(categoryName, categorySet.size())); }
    public void addCategory(ReportCategory category) {
        this.categorySet.add(category);
    }
    public void removeCategory(ReportCategory category) { this.categorySet.remove(category); }

    public Set<ReportCategory> getCategories() { return categorySet; }

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

    public File getTripoliReportFile() {
        if (tripoliReportDirectoryLocal == null) createReportDirectory();

        return tripoliReportDirectoryLocal.toPath().resolve(this.methodName + File.separator + this.getReportName()+".trf").toFile();
    }

    /**
     *  Walks local report directory to gather all report files pertaining to a specific analysis method. Also creates
     *  the full report for that method.
     * @param methodName Name of method to compare reports against
     * @param userFunctionList User functions must belong to the method of the methodName parameter
     * @return List of reports with method specific 'Full Report' initialized at the end
     * @throws IOException
     * @throws TripoliException
     */
    public static List<Report> generateReportList(String methodName, List<UserFunction> userFunctionList) throws IOException, TripoliException {
        if (tripoliReportDirectoryLocal == null) createReportDirectory();

        List<Report> reportList = new ArrayList<>();
        File methodReportDirectory = new File(tripoliReportDirectoryLocal, methodName);

        if (methodReportDirectory.exists()) {
            try (Stream<Path> pathStream = Files.walk(methodReportDirectory.toPath())) {
                reportList = pathStream.filter(Files::isRegularFile)
                        .map(path -> {
                            try {
                                return (Report) TripoliSerializer.getSerializedObjectFromFile(path.toString(), true);
                            } catch (TripoliException e) {
                                e.printStackTrace();
                            }
                            return null;
                        })
                        .collect(Collectors.toCollection(ArrayList::new));
            }
        }
        reportList.add(createFullReport("Full Report", methodName, userFunctionList));

        return reportList;
    }

    public static Report createBlankReport(String reportName, String methodName) {
        ReportColumn analysisName = new ReportColumn("Analysis Name", 0);
        ReportCategory analysisInfo = new ReportCategory("Analysis Info", 0);
        analysisInfo.addColumn(analysisName);
        Set<ReportCategory> categorySet = new HashSet<>();
        categorySet.add(analysisInfo);
        return new Report(reportName, methodName, categorySet);
    }

    public static Report createFullReport(String reportName, String methodName, List<UserFunction> ufList) {
        Set<ReportCategory> categories = new TreeSet<>();
        categories.add(ReportCategory.generateAnalysisInfo());
        categories.add(ReportCategory.generateIsotopicRatios(ufList));
        categories.add(ReportCategory.generateUserFunctions(ufList));

        return new Report(reportName, methodName, categories);
    }

    // Check if local report folder exists and create if not
    // Init the directory variable in non-static context
    private static void createReportDirectory() {
        String tripoliUserHomeDirectoryLocal = System.getProperty("user.home");

        tripoliReportDirectoryLocal = new File(
                File.separator + tripoliUserHomeDirectoryLocal + File.separator + TRIPOLI_USERS_DATA_FOLDER_NAME + File.separator + TRIPOLI_CUSTOM_REPORTS_FOLDER);
        if (!tripoliReportDirectoryLocal.exists()) {
            tripoliReportDirectoryLocal.mkdir();
        }
    }

    /**
     * Saves report structure to local file directory with method name file separation
     * @throws TripoliException
     */
    public File serializeReport() throws TripoliException {
        if (tripoliReportDirectoryLocal == null) createReportDirectory();
        File reportMethodDirectory = new File(tripoliReportDirectoryLocal.getAbsolutePath() + File.separator + methodName);

        if(!reportMethodDirectory.exists()){
            reportMethodDirectory.mkdir();
        }
        File reportFile = new File(reportMethodDirectory.getAbsolutePath() + File.separator + this.getReportName()+".trf");
        TripoliSerializer.serializeObjectToFile(this, reportFile.getAbsolutePath());
        return reportFile;
    }
    public boolean deleteReport() {
        return this.getTripoliReportFile().delete();
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

