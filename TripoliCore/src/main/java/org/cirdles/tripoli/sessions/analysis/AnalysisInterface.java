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

package org.cirdles.tripoli.sessions.analysis;

import jakarta.xml.bind.JAXBException;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;
import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.parameters.Parameters;
import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne.SingleBlockRawDataLiteSetRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.EnsemblesStore;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.SingleBlockModelRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.SingleBlockRawDataSetRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.initializers.AllBlockInitForMCMC;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecExtractedData;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.sessions.analysis.outputs.etRedux.ETReduxFraction;
import org.cirdles.tripoli.settings.plots.RatiosColors;
import org.cirdles.tripoli.utilities.callbacks.LoggingCallbackInterface;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.cirdles.tripoli.constants.TripoliConstants.MISSING_STRING_FIELD;

public interface AnalysisInterface {
    static Analysis initializeAnalysis(String analysisName,
                                       AnalysisMethod analysisMethod,
                                       String analysisSampleName) throws TripoliException {
        return new Analysis(analysisName, analysisMethod, analysisSampleName);
    }

    static Analysis initializeNewAnalysis(int suffix) throws TripoliException {
        return new Analysis("New Analysis" + "_" + (suffix), null, MISSING_STRING_FIELD);
    }

    static Analysis convertToAnalysis(AnalysisInterface analysis) throws TripoliException {
        Analysis result;
        if (analysis instanceof Analysis) {
            result = (Analysis) analysis;
        } else {
            result = new Analysis(analysis.getAnalysisName(), analysis.getAnalysisMethod(), analysis.getAnalysisSampleName());
        }
        return result;
    }

    static MassSpectrometerContextEnum determineMassSpectrometerContextFromDataFile(Path dataFilePath) throws IOException {
        MassSpectrometerContextEnum retVal = MassSpectrometerContextEnum.UNKNOWN;
        if (dataFilePath.toString().endsWith(".xls")) {

            Workbook workbook = null;
            try {
                WorkbookSettings workbookSettings = new WorkbookSettings();
                workbookSettings.setSuppressWarnings(true);
                workbook = Workbook.getWorkbook(dataFilePath.toFile(), workbookSettings);
                if (workbook.getSheet("SUMMARY").getCell(0, 0).getContents().compareToIgnoreCase("Summary Report") == 0) {
                    retVal = MassSpectrometerContextEnum.PHOENIX_IONVANTAGE_XLS;
                }
            } catch (BiffException e) {
                throw new RuntimeException(e);
            }

        } else {
            List<String> contentsByLine = new ArrayList<>();
            FileReader fileReader = new FileReader(dataFilePath.toFile());
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                // for infinity symbol
                line = line.replace("�", "");
                contentsByLine.add(line);
            }
            bufferedReader.close();

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
        }

        return retVal;
    }

    String extractMassSpecDataFromPath(Path dataFilePath) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, JAXBException, TripoliException;

    AnalysisMethod extractAnalysisMethodfromPath(Path phoenixAnalysisMethodDataFilePath) throws JAXBException;

    PlotBuilder[][] updatePlotsByBlock(int blockNumber, LoggingCallbackInterface loggingCallback) throws TripoliException;

    PlotBuilder[] updatePeakPlotsByBlock(int blockNumber) throws TripoliException;

    void updateRatiosPlotBuilderDisplayStatus(int indexOfIsotopicRatio, boolean displayed);

    String uppdateLogsByBlock(int blockNumber, String logEntry);

    AllBlockInitForMCMC.PlottingData assemblePostProcessPlottingData();

    String getAnalysisName();

    void setAnalysisName(String analysisName);

    String getAnalystName();

    void setAnalystName(String analystName);

    String getLabName();

    void setLabName(String labName);

    String getAnalysisSampleName();

    void setAnalysisSampleName(String analysisSampleName);

    public String getAnalysisFractionName();

    public void setAnalysisFractionName(String analysisFractionName);

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

    Map<Integer, SingleBlockRawDataLiteSetRecord> getMapOfBlockIdToRawDataLiteOne();

    Map<Integer, SingleBlockModelRecord> getMapOfBlockIdToFinalModel();

    Map<Integer, List<EnsemblesStore.EnsembleRecord>> getMapBlockIDToEnsembles();

    Map<Integer, Integer> getMapOfBlockIdToModelsBurnCount();

    String getTwoSigmaHexColorString();
    String getOneSigmaHexColorString();
    String getTwoStandardErrorHexColorString();
    String getMeanHexColorString();
    String getDataHexColorString();
    String getAntiDataHexColorString();
    RatiosColors getRatioColors();


    void setOneSigmaHexColorString(String hexColor);
    void setTwoSigmaHexColorString(String hexColor);
    void setTwoStandardErrorHexColorString(String hexColor);
    void setMeanHexColorString(String hexColor);
    void setDataHexColorString(String hexColor);
    void setAntiDataHexColorString(String hexColor);
    void setRatioColors(RatiosColors ratiosColors);

    Parameters getParameters();

    void resetAnalysis();

    int getAnalysisCaseNumber();

    ETReduxFraction prepareFractionForETReduxExport();

    String prepareFractionForClipboardExport();

    void setAnalysisStartTime(String s);

    List<UserFunction> getUserFunctions();

    void setUserFunctions(List<UserFunction> userFunctions);

}