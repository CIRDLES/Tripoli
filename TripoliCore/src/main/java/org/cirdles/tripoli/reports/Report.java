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
import org.cirdles.tripoli.utilities.stateUtilities.TripoliSerializer;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    /**
     * Returns the current report as a file type with its Path set to the local report directory with the current name
     * @return Report File
     */
    public File getTripoliReportFile() {
        if (tripoliReportDirectoryLocal == null) createReportDirectory();

        return tripoliReportDirectoryLocal.toPath().resolve(this.methodName + File.separator + this.getReportName() + ".tripReport").toFile();
    }

    /**
     * Returns the current report as a file type with its Path set to the local report directory with the given name as
     * its to-be-saved-as name. This method is intended for Save-as prompts
     * @param newReportName Name to be saved as
     * @return Report File
     */
    public File getTripoliReportFile(String newReportName) {
        if (tripoliReportDirectoryLocal == null) createReportDirectory();

        return tripoliReportDirectoryLocal.toPath().resolve(this.methodName + File.separator + newReportName +".tripReport").toFile();
    }

    /**
     *  Walks local report directory to gather all report files pertaining to a specific analysis method. Also creates
     *  the full report for that method to be displayed in the report menu dropdown.
     * @param methodName Name of method to compare reports against
     * @param userFunctionList User functions must belong to the method of the methodName parameter
     * @return List of reports with method specific 'Full Report' initialized at the end
     * @throws IOException
     * @throws TripoliException
     */
    public static List<Report> generateReportList(String methodName, List<UserFunction> userFunctionList) throws IOException {
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

    /**
     * Creates a full report which contains every possible (excluding custom) category and column for a given method
     * @param reportName Name of the report
     * @param methodName
     * @param ufList
     * @return
     */
    public static Report createFullReport(String reportName, String methodName, List<UserFunction> ufList) {
        Set<ReportCategory> categories = new TreeSet<>();
        categories.add(ReportCategory.generateAnalysisInfo());
        categories.add(ReportCategory.generateIsotopicRatios(ufList));
        categories.add(ReportCategory.generateUserFunctions(ufList));
        categories.add(ReportCategory.generateCustomExpressions(ufList));

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
    public void serializeReport() throws TripoliException {
        if (tripoliReportDirectoryLocal == null) createReportDirectory();
        File reportMethodDirectory = new File(tripoliReportDirectoryLocal.getAbsolutePath() + File.separator + methodName);

        if(!reportMethodDirectory.exists()){
            reportMethodDirectory.mkdir();
        }
        File reportFile = new File(reportMethodDirectory.getAbsolutePath() + File.separator + this.getReportName()+".tripReport");
        TripoliSerializer.serializeObjectToFile(this, reportFile.getAbsolutePath());

    }
    public boolean deleteReport() {
        return this.getTripoliReportFile().delete();
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
            Set<ReportCategory> visibleCategories = this.getCategories().stream()
                    .filter(ReportCategory::isVisible)
                    .collect(Collectors.toSet());

            // Collect all unique columns across categories
            List<ReportColumn> visibleColumns = visibleCategories.stream()
                    .flatMap(category -> category.getColumns().stream())
                    .filter(ReportColumn::isVisible)
                    .toList();

            // Header row with proper naming for user function columns
            List<String> headers = new ArrayList<>();
            for (ReportColumn column : visibleColumns) {
                if (column.isUserFunction()) {
                    headers.add(column.getColumnName() + " Mean");
                    headers.add("StdDev");
                    headers.add("Variance");
                } else {
                    headers.add(column.getColumnName());
                }
            }
            writer.write(String.join(",", headers));
            writer.newLine();

            // Write each analysis row
            for (AnalysisInterface analysis : listOfAnalyses) {
                if (analysis.getMethod().getMethodName().equals(methodName)) { // Only process analyses of the same method
                    AllBlockInitForDataLiteOne.initBlockModels(analysis); // Init values
                    Analysis thisAnalysis = (Analysis) analysis;
                    List<String> rowValues = new ArrayList<>();

                    for (ReportColumn column : visibleColumns) {
                        String columnData = column.retrieveData(thisAnalysis);

                        // If user function, split the comma-separated values
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
            writer.write("Created on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "  ");
            writer.write("Tripoli  "); // Replace with actual program name
            writer.write(Tripoli.VERSION); // Replace with actual version number


            return reportCSVFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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

