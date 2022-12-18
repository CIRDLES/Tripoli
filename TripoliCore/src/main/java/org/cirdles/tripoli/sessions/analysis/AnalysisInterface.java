package org.cirdles.tripoli.sessions.analysis;

import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecExtractedData;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputDataRecord;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.sessions.analysis.samples.Sample;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.cirdles.tripoli.constants.ConstantsTripoliCore.MISSING_STRING_FIELD;

public interface AnalysisInterface {
    static Analysis initializeAnalysis(String analysisName, AnalysisMethod analysisMethod, Sample analysisSample) {
        return new Analysis(analysisName, analysisMethod, analysisSample);
    }

    static Analysis initializeNewAnalysis() {
        return new Analysis("New Analysis", null, new Sample(MISSING_STRING_FIELD));
    }

    static MassSpectrometerContextEnum determineMassSpectrometerContextFromDataFile(Path dataFilePath) throws IOException {
        MassSpectrometerContextEnum retVal = MassSpectrometerContextEnum.UNKNOWN;
        List<String> contentsByLine = new ArrayList<>(Files.readAllLines(dataFilePath, Charset.defaultCharset()));
        for (MassSpectrometerContextEnum massSpecContext : MassSpectrometerContextEnum.values()) {
            List<String> keyWordList = massSpecContext.getKeyWordsList();
            boolean keywordsMatch = true;
            for (int keyWordIndex = 0; keyWordIndex < keyWordList.size(); keyWordIndex++) {
                keywordsMatch = keywordsMatch && (contentsByLine.get(keyWordIndex).startsWith(keyWordList.get(keyWordIndex).trim()));
            }
            if (keywordsMatch) {
                retVal = massSpecContext;
                break;
            }
        }

        return retVal;
    }

    public void extractMassSpecDataFromPath(Path dataFilePath) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException;

    String getAnalysisName();

    void setAnalysisName(String analysisName);

    String getAnalystName();

    void setAnalystName(String analystName);

    String getLabName();

    void setLabName(String labName);

    Sample getAnalysisSample();

    void setAnalysisSample(Sample analysisSample);

    String getAnalysisSampleDescription();

    void setAnalysisSampleDescription(String analysisSampleDescription);

    String prettyPrintAnalysisSummary();
    public String prettyPrintAnalysisMetaData();
    public String prettyPrintAnalysisDataSummary();

    AnalysisMethod getMethod();

    void setMethod(AnalysisMethod analysisMethod);

    MassSpecOutputDataRecord getMassSpecOutputDataRecord();

    void setMassSpecOutputDataRecord(MassSpecOutputDataRecord massSpecOutputDataRecord);

    public MassSpecExtractedData getMassSpecExtractedData();

    public void setMassSpecExtractedData(MassSpecExtractedData massSpecExtractedData);

    AnalysisMethod getAnalysisMethod();

    void setAnalysisMethod(AnalysisMethod analysisMethod);

    String getDataFilePathString();

    void setDataFilePathString(String dataFilePathString);
    public MassSpectrometerContextEnum getMassSpectrometerContext() ;
    public void setMassSpectrometerContext(MassSpectrometerContextEnum massSpectrometerContext) ;
    boolean isMutable();

    void setMutable(boolean mutable);
}