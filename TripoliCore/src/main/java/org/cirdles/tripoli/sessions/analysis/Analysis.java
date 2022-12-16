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

import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecExtractedData;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputDataRecord;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.sessions.analysis.samples.Sample;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Formatter;

import static org.cirdles.tripoli.constants.ConstantsTripoliCore.MISSING_STRING_FIELD;
import static org.cirdles.tripoli.constants.ConstantsTripoliCore.SPACES_100;

/**
 * @author James F. Bowring
 */
public class Analysis implements Serializable, AnalysisInterface {
    @Serial
    private static final long serialVersionUID = 5737165372498262402L;

    private String analysisName;
    private String analystName;
    private String labName;
    private AnalysisMethod analysisMethod;
    private Sample analysisSample;
    private String analysisSampleDescription;

    // note: path is not serializable
    private String dataFilePathString;
    private MassSpectrometerContextEnum massSpectrometerContext;
    private MassSpecOutputDataRecord massSpecOutputDataRecord;
    private MassSpecExtractedData massSpecExtractedData;
    private boolean mutable;

    private Analysis() {
    }

    protected Analysis(String analysisName, AnalysisMethod analysisMethod, Sample analysisSample) {
        this.analysisName = analysisName;
        this.analysisMethod = analysisMethod;
        this.analysisSample = analysisSample;
        analystName = MISSING_STRING_FIELD;
        labName = MISSING_STRING_FIELD;
        analysisSampleDescription = MISSING_STRING_FIELD;
        dataFilePathString = MISSING_STRING_FIELD;
        massSpectrometerContext = MassSpectrometerContextEnum.UNKNOWN;
        massSpecOutputDataRecord = null;
        massSpecExtractedData = new MassSpecExtractedData();
        mutable = true;
    }

    public void extractMassSpecDataFromPath(Path dataFilePath)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        dataFilePathString = dataFilePath.toString();
        massSpectrometerContext = AnalysisInterface.determineMassSpectrometerContextFromDataFile(dataFilePath);
        if (0 != massSpectrometerContext.compareTo(MassSpectrometerContextEnum.UNKNOWN)) {
            Class<?> clazz = massSpectrometerContext.getClazz();
            Method method = clazz.getMethod(massSpectrometerContext.getMethodName(), Path.class);
            massSpecExtractedData = (MassSpecExtractedData) method.invoke(null, dataFilePath);

            System.out.println(massSpecExtractedData.getBlocksData().size());
        } else {
            massSpecExtractedData = new MassSpecExtractedData();
        }
    }

    public final String prettyPrintAnalysisSummary() {
        return analysisName +
                SPACES_100.substring(0, 40 - analysisName.length()) +
                (null == analysisMethod ? "NO Method" : analysisMethod.prettyPrintMethodSummary());
    }

    public final String prettyPrintAnalysisMetaData(){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%30s", "Mass Spectrometer: ")).append(String.format("%-40s", massSpectrometerContext.getName()));
        if (massSpectrometerContext.compareTo(MassSpectrometerContextEnum.UNKNOWN) != 0) {
            sb.append(String.format("%30s", "Software Version: ")).append(massSpecExtractedData.getHeader().softwareVersion())
                    .append("\n").append(String.format("%30s", "File Name: ")).append(String.format("%-40s", massSpecExtractedData.getHeader().filename()))
                    .append(String.format("%30s", "Corrected?: ")).append(massSpecExtractedData.getHeader().isCorrected())
                    .append("\n").append(String.format("%30s", "Method Name: ")).append(String.format("%-40s", massSpecExtractedData.getHeader().methodName()))
                    .append(String.format("%30s", "BChannels?: ")).append(massSpecExtractedData.getHeader().hasBChannels())
                    .append("\n").append(String.format("%30s", "Time Zero: ")).append(String.format("%-40s", massSpecExtractedData.getHeader().localDateTimeZero()));
        } else {
            sb.append("\n\n\n\t\t\t\t   >>>  Unable to parse data file.  <<<");
        }

        return sb.toString();
    }


    @Override
    public String getAnalysisName() {
        return analysisName;
    }

    @Override
    public void setAnalysisName(String analysisName) {
        this.analysisName = analysisName;
    }

    public String getAnalystName() {
        return analystName;
    }

    public void setAnalystName(String analystName) {
        this.analystName = analystName;
    }

    public String getLabName() {
        return labName;
    }

    public void setLabName(String labName) {
        this.labName = labName;
    }

    public Sample getAnalysisSample() {
        return analysisSample;
    }

    public void setAnalysisSample(Sample analysisSample) {
        this.analysisSample = analysisSample;
    }

    public String getAnalysisSampleDescription() {
        return analysisSampleDescription;
    }

    public void setAnalysisSampleDescription(String analysisSampleDescription) {
        this.analysisSampleDescription = analysisSampleDescription;
    }

    @Override
    public AnalysisMethod getMethod() {
        return analysisMethod;
    }

    @Override
    public void setMethod(AnalysisMethod analysisMethod) {
        this.analysisMethod = analysisMethod;
    }

    @Override
    public MassSpecOutputDataRecord getMassSpecOutputDataRecord() {
        return massSpecOutputDataRecord;
    }

    @Override
    public void setMassSpecOutputDataRecord(MassSpecOutputDataRecord massSpecOutputDataRecord) {
        this.massSpecOutputDataRecord = massSpecOutputDataRecord;
    }

    public MassSpecExtractedData getMassSpecExtractedData() {
        return massSpecExtractedData;
    }

    public void setMassSpecExtractedData(MassSpecExtractedData massSpecExtractedData) {
        this.massSpecExtractedData = massSpecExtractedData;
    }

    public AnalysisMethod getAnalysisMethod() {
        return analysisMethod;
    }

    public void setAnalysisMethod(AnalysisMethod analysisMethod) {
        this.analysisMethod = analysisMethod;
    }

    public String getDataFilePathString() {
        return dataFilePathString;
    }

    public void setDataFilePathString(String dataFilePathString) {
        this.dataFilePathString = dataFilePathString;
    }

    public MassSpectrometerContextEnum getMassSpectrometerContext() {
        return massSpectrometerContext;
    }

    public void setMassSpectrometerContext(MassSpectrometerContextEnum massSpectrometerContext) {
        this.massSpectrometerContext = massSpectrometerContext;
    }

    public boolean isMutable() {
        return mutable;
    }

    public void setMutable(boolean mutable) {
        this.mutable = mutable;
    }
}