package org.cirdles.tripoli.sessions.analysis;

import jakarta.xml.bind.JAXBException;
import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;
import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.AllBlockInitForOGTripoli;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.EnsemblesStore;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.SingleBlockModelRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.SingleBlockRawDataSetRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecExtractedData;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.utilities.callbacks.LoggingCallbackInterface;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.cirdles.tripoli.constants.TripoliConstants.MISSING_STRING_FIELD;

public interface AnalysisInterface {
    static Analysis initializeAnalysis(String analysisName, AnalysisMethod analysisMethod, String analysisSampleName) {
        return new Analysis(analysisName, analysisMethod, analysisSampleName);
    }

    static Analysis initializeNewAnalysis() {
        return new Analysis("New Analysis", null, MISSING_STRING_FIELD);
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

    void extractMassSpecDataFromPath(Path dataFilePath) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, JAXBException, TripoliException;

    AnalysisMethod extractAnalysisMethodfromPath(Path phoenixAnalysisMethodDataFilePath) throws JAXBException;

    PlotBuilder[][] updatePlotsByBlock(int blockNumber, LoggingCallbackInterface loggingCallback) throws TripoliException;

    PlotBuilder[] updatePeakPlotsByBlock(int blockNumber) throws TripoliException;

    void updateRatiosPlotBuilderDisplayStatus(int indexOfIsotopicRatio, boolean displayed);

    String uppdateLogsByBlock(int blockNumber, String logEntry);

    public AllBlockInitForOGTripoli.PlottingData assemblePostProcessPlottingData();

    String getAnalysisName();

    void setAnalysisName(String analysisName);

    String getAnalystName();

    void setAnalystName(String analystName);

    String getLabName();

    void setLabName(String labName);

    String getAnalysisSampleName();

    void setAnalysisSampleName(String analysisSampleName);

    String getAnalysisSampleDescription();

    void setAnalysisSampleDescription(String analysisSampleDescription);

    String prettyPrintAnalysisSummary();

    String prettyPrintAnalysisMetaData();

    String prettyPrintAnalysisDataSummary();

    AnalysisMethod getMethod();

    void setMethod(AnalysisMethod analysisMethod);

    MassSpecExtractedData getMassSpecExtractedData();

    void setMassSpecExtractedData(MassSpecExtractedData massSpecExtractedData);

    AnalysisMethod getAnalysisMethod();

    void setAnalysisMethod(AnalysisMethod analysisMethod);

    String getDataFilePathString();

    void setDataFilePathString(String dataFilePathString);

    boolean isMutable();

    void setMutable(boolean mutable);

    Map<Integer, Integer> getMapOfBlockIdToProcessStatus();

    Map<Integer, PlotBuilder[][]> getMapOfBlockIdToPlots();

    Map<Integer, PlotBuilder[]> getMapOfBlockIdToPeakPlots();

    Map<Integer, SingleBlockRawDataSetRecord> getMapOfBlockIdToRawData();

    Map<Integer, SingleBlockModelRecord> getMapOfBlockIdToFinalModel();

    Map<Integer, List<EnsemblesStore.EnsembleRecord>> getMapBlockIDToEnsembles();

    Map<Integer, Integer> getMapOfBlockIdToModelsBurnCount();

    void resetAnalysis();
}