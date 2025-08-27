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

import org.cirdles.tripoli.sessions.Session;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;

import java.io.File;
import java.util.List;

public class ReportData {
    List<AnalysisInterface> analysisList;
    AnalysisInterface analysis;
    String analysisName;
    Session tripoliSession;
    String dataFilepath;
    File dataFile;

    public ReportData(List<AnalysisInterface> analysisList, AnalysisInterface analysis, String analysisName, Session tripoliSession, String dataFilepath, File dataFile) {
        this.analysisList = analysisList;
        this.analysis = analysis;
        this.analysisName = analysisName;
        this.tripoliSession = tripoliSession;
        this.dataFilepath = dataFilepath;
        this.dataFile = dataFile;
    }

    public List<AnalysisInterface> getAnalysisList() {
        return analysisList;
    }

    public AnalysisInterface getAnalysis() {
        return analysis;
    }

    public String getAnalysisName() {
        return analysisName;
    }

    public Session getTripoliSession() {
        return tripoliSession;
    }

    public String getDataFilepath() {
        return dataFilepath;
    }

    public File getDataFile() {
        return dataFile;
    }
}
