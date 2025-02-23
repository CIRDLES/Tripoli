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

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.cirdles.tripoli.constants.TripoliConstants.TRIPOLI_USERS_DATA_FOLDER_NAME;


public class Report implements Serializable, Comparable<ReportCategory>{
    private static final String TRIPOLI_CUSTOM_REPORTS_FOLDER = "CustomReports";
    private static File tripoliReportDirectoryLocal;

    private String reportName;


    Set<ReportCategory> categoryColumns;

    public Report() {

        categoryColumns = new TreeSet<>();
    }
    @Override
    public int compareTo(@NotNull ReportCategory o) {
        return o.getPositionIndex();
    }
    public String getReportName() {
        return reportName;
    }

    // Generate list of saved reports
    public static List<Path> generateReportList() throws IOException {
        createReportDirectory();
        List<Path> reportList;
        try (Stream<Path> pathStream = Files.walk(tripoliReportDirectoryLocal.toPath())) {
            reportList = pathStream.filter(Files::isRegularFile)
                    .toList();
        }
        return reportList;
    }

    private static void createReportDirectory() {
        String tripoliUserHomeDirectoryLocal = System.getProperty("user.home");

        // check if local report folder exists and create if it does not
        tripoliReportDirectoryLocal = new File(
                File.separator + tripoliUserHomeDirectoryLocal + File.separator + TRIPOLI_USERS_DATA_FOLDER_NAME + File.separator + TRIPOLI_CUSTOM_REPORTS_FOLDER);
        if (!tripoliReportDirectoryLocal.exists()) {
            tripoliReportDirectoryLocal.mkdir();
        }
    }
}

