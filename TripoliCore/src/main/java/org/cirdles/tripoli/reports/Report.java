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
    private static File tripoliReportDirectoryLocal;

    private String reportName;
    private String methodName;

    Set<ReportCategory> categoryColumns;

    public Report(String reportName, String methodName, Set<ReportCategory> categoryColumns) {
        this.reportName = reportName;
        this.methodName = methodName;
        this.categoryColumns = categoryColumns;
    }

    // Assumes Report Names will never contain underscores naturally
    public String getReportName() {
        return this.reportName.replaceAll("_", " "); }

    public void setReportName(String reportName) {
        this.reportName = reportName.replaceAll("[\\\\/:*?\"<>| ]", "_").trim(); }

    public void setMethodName(String methodName) {
        this.methodName = methodName; }

    public void addCategory(ReportCategory category) {
        // If category doesn't have a defined index, append it to the end
        if (category.getColumns() == null) {
            category.setPositionIndex(categoryColumns.size());
        } else { categoryColumns.add(category); }
    }

    public Set<ReportCategory> getCategories() { return categoryColumns; }

    public File getTripoliReportFile() {
        return tripoliReportDirectoryLocal.toPath().resolve(this.methodName + File.separator + this.getReportName()+".tpr").toFile();
    }

    /**
     *  Walks local report directory to gather all report files pertaining to a specific analysis method. Also creates
     *  the default report for that method.
     * @param methodName Name of method to compare reports against
     * @param userFunctionList User functions must belong to the method of the methodName parameter
     * @return List of reports with method specific 'Default Report' initialized at the end
     * @throws IOException
     * @throws TripoliException
     */
    public static List<Report> generateReportList(String methodName, List<UserFunction> userFunctionList) throws IOException, TripoliException {
        createReportDirectory();
        List<Report> reportList = new ArrayList<>();
        File methodReportDirectory = new File(tripoliReportDirectoryLocal, methodName);

        if (methodReportDirectory.exists()) {
            try (Stream<Path> pathStream = Files.walk(methodReportDirectory.toPath())) {
                reportList = pathStream.filter(Files::isRegularFile)
                        .map(path -> {
                            try {
                                return (Report) TripoliSerializer.getSerializedObjectFromFile(path.toString(), true);
                            } catch (TripoliException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.toCollection(ArrayList::new));
            }
        }
        reportList.add(createDefaultReport(methodName, userFunctionList));

        return reportList;
    }

    private static Report createDefaultReport(String methodName, List<UserFunction> ufList) throws TripoliException {
        Set<ReportCategory> categories = new TreeSet<>();
        categories.add(ReportCategory.generateAnalysisInfo());
        categories.add(ReportCategory.generateIsotopicRatios(ufList));
        categories.add(ReportCategory.generateUserFunctions(ufList));

        return new Report("_Default Report", methodName, categories);
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
    public void serializeReport() throws TripoliException {
        File reportMethodDirectory = new File(tripoliReportDirectoryLocal.getAbsolutePath() + File.separator + methodName);

        if(!reportMethodDirectory.exists()){
            reportMethodDirectory.mkdir();
        }
        TripoliSerializer.serializeObjectToFile(this, reportMethodDirectory.getAbsolutePath()+reportName+".tpr");
    }

    @Override
    public int compareTo(@NotNull Report o) {
        return this.reportName.compareTo(o.reportName);
    }
}

