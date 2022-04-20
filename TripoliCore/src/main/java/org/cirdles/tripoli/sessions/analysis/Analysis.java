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

import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;

import java.io.Serializable;

/**
 * @author James F. Bowring
 */
public class Analysis implements Serializable {

    private String analysisName;
    private AnalysisMethod analysisMethod;

    private Analysis() {
    }

    private Analysis(String analysisName, AnalysisMethod analysisMethod) {
        this.analysisName = analysisName;
        this.analysisMethod = analysisMethod;
    }

    public static Analysis initializeAnalysis(String analysisName, AnalysisMethod analysisMethod) {
        return new Analysis(analysisName, analysisMethod);
    }

    public String getAnalysisName() {
        return analysisName;
    }

    public void setAnalysisName(String analysisName) {
        this.analysisName = analysisName;
    }

    public AnalysisMethod getMethod() {
        return analysisMethod;
    }

    public void setMethod(AnalysisMethod analysisMethod) {
        this.analysisMethod = analysisMethod;
    }
}