package org.cirdles.tripoli.sessions.analysis;

import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputDataRecord;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.sessions.analysis.samples.Sample;

public interface AnalysisInterface {
    static Analysis initializeAnalysis(String analysisName, AnalysisMethod analysisMethod, Sample analysisSample) {
        return new Analysis(analysisName, analysisMethod, analysisSample);
    }

    static Analysis initializeNewAnalysis() {
        return new Analysis("New Analysis", null, new Sample(""));
    }

    String getAnalysisName();

    void setAnalysisName(String analysisName);

    public Sample getAnalysisSample();

    public void setAnalysisSample(Sample analysisSample);

    public String getAnalysisSampleDescription();

    public void setAnalysisSampleDescription(String analysisSampleDescription);

    String prettyPrintAnalysisSummary();

    AnalysisMethod getMethod();

    void setMethod(AnalysisMethod analysisMethod);

    String getDataFilePath();

    void setDataFilePath(String dataFilePathString);

    MassSpecOutputDataRecord getMassSpecOutputDataRecord();

    void setMassSpecOutputDataRecord(MassSpecOutputDataRecord massSpecOutputDataRecord);
}