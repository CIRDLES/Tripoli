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

import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputDataRecord;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.sessions.analysis.samples.Sample;

import java.io.Serial;
import java.io.Serializable;

import static org.cirdles.tripoli.constants.ConstantsTripoliCore.MISSING_STRING_FIELD;
import static org.cirdles.tripoli.constants.ConstantsTripoliCore.SPACES_100;

/**
 * @author James F. Bowring
 */
public class Analysis implements Serializable, AnalysisInterface {
    @Serial
    private static final long serialVersionUID = 5737165372498262402L;

    private String analysisName;
    private AnalysisMethod analysisMethod;
    private Sample analysisSample;
    private String analysisSampleDescription;

    // note: path is not serializable
    private String dataFilePathString;
    private MassSpecOutputDataRecord massSpecOutputDataRecord;

    private Analysis() {
    }

    protected Analysis(String analysisName, AnalysisMethod analysisMethod, Sample analysisSample) {
        this.analysisName = analysisName;
        this.analysisMethod = analysisMethod;
        this.analysisSample = analysisSample;
        this.analysisSampleDescription = MISSING_STRING_FIELD;
        this.dataFilePathString = "";
        this.massSpecOutputDataRecord = null;
    }

    public final String prettyPrintAnalysisSummary() {
        return new StringBuilder().append(analysisName)
                .append(SPACES_100, 0, 40 - analysisName.length())
                .append(analysisMethod == null ? "NO Method" : analysisMethod.prettyPrintMethodSummary()).toString();
    }

    @Override
    public String getAnalysisName() {
        return analysisName;
    }

    @Override
    public void setAnalysisName(String analysisName) {
        this.analysisName = analysisName;
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
    public String getDataFilePath() {
        return dataFilePathString;
    }

    @Override
    public void setDataFilePath(String dataFilePathString) {
        this.dataFilePathString = dataFilePathString;
    }

    @Override
    public MassSpecOutputDataRecord getMassSpecOutputDataRecord() {
        return massSpecOutputDataRecord;
    }

    @Override
    public void setMassSpecOutputDataRecord(MassSpecOutputDataRecord massSpecOutputDataRecord) {
        this.massSpecOutputDataRecord = massSpecOutputDataRecord;
    }
}