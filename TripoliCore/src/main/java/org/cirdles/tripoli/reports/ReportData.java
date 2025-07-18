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
