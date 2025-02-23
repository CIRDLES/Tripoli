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

import org.cirdles.tripoli.utilities.stateUtilities.TripoliPersistentState;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.cirdles.tripoli.constants.TripoliConstants.TRIPOLI_USERS_DATA_FOLDER_NAME;


public class Report implements Serializable{
    private static final long serialVersionUID = 1064098835718283672L;
    private static final String TRIPOLI_CUSTOM_REPORTS_FOLDER = "CustomReports";
    private static File tripoliReportDirectoryLocal;

    private String reportName;


    Set<ReportCategory> categoryColumns;

    public Report() {
        createReportDirectory();
        categoryColumns = new TreeSet<>();
    }

    public String getReportName() {
        return reportName;
    }

    // todo: ensure report name is file-safe
    public File getTripoliReportFile() {
        return tripoliReportDirectoryLocal.toPath().resolve(this.getReportName()+".tpr").toFile();
    }

    // Generate list of saved reports for menu building
    public static List<Path> generateReportList() throws IOException {
        createReportDirectory();
        List<Path> reportList;
        try (Stream<Path> pathStream = Files.walk(tripoliReportDirectoryLocal.toPath())) {
            reportList = pathStream.filter(Files::isRegularFile)
                    .toList();
        }
        return reportList;
    }

    // Check if local report folder exists and create if it does not
    // also init the directory variable in non-static context
    private static void createReportDirectory() {
        String tripoliUserHomeDirectoryLocal = System.getProperty("user.home");

        tripoliReportDirectoryLocal = new File(
                File.separator + tripoliUserHomeDirectoryLocal + File.separator + TRIPOLI_USERS_DATA_FOLDER_NAME + File.separator + TRIPOLI_CUSTOM_REPORTS_FOLDER);
        if (!tripoliReportDirectoryLocal.exists()) {
            tripoliReportDirectoryLocal.mkdir();
        }
    }

}

