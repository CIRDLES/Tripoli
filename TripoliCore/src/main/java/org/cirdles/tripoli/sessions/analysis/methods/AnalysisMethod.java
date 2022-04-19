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

package org.cirdles.tripoli.sessions.analysis.methods;

import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.MassSpectrometerModel;
import org.cirdles.tripoli.sessions.analysis.methods.baselineTables.BaselineTable;
import org.cirdles.tripoli.sessions.analysis.methods.sequenceTables.SequenceTable;

import java.io.Serializable;

/**
 * @author James F. Bowring
 */
public class AnalysisMethod implements Serializable {
    /* Notes
    MassSpec Model
    Isotopes of Interest
    Baseline Table
    Sequence Table
     */

    protected String methodName;
    protected MassSpectrometerModel massSpectrometer;
    protected BaselineTable baselineTable;
    protected SequenceTable sequenceTable;

    private AnalysisMethod(String methodName, MassSpectrometerModel massSpectrometer) {
        this(methodName, massSpectrometer, BaselineTable.createEmptyBaselineTable(), SequenceTable.createEmptySequenceTable());
    }

    private AnalysisMethod(String methodName, MassSpectrometerModel massSpectrometer, BaselineTable baselineTable, SequenceTable sequenceTable) {
        this.methodName = methodName;
        this.massSpectrometer = massSpectrometer;
        this.baselineTable = baselineTable;
        this.sequenceTable = sequenceTable;
    }

    public static AnalysisMethod initializeMethod(String methodName, MassSpectrometerModel massSpectrometer) {
        return new AnalysisMethod(methodName, massSpectrometer);
    }

    @Override
    public boolean equals(Object otherObject) {
        boolean retVal = true;
        if (otherObject != this) {
            if (otherObject instanceof AnalysisMethod) {
                AnalysisMethod otherAnalysisMethod = (AnalysisMethod) otherObject;
                retVal = this.getMethodName().compareToIgnoreCase(otherAnalysisMethod.getMethodName()) == 0;
            } else {
                retVal = false;
            }
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (methodName == null ? 0 : methodName.hashCode());
        hash = 31 * hash + (massSpectrometer == null ? 0 : massSpectrometer.hashCode());
        return hash;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public MassSpectrometerModel getMassSpectrometer() {
        return massSpectrometer;
    }

    public void setMassSpectrometer(MassSpectrometerModel massSpectrometer) {
        this.massSpectrometer = massSpectrometer;
    }

    public BaselineTable getBaselineTable() {
        return baselineTable;
    }

    public void setBaselineTable(BaselineTable baselineTable) {
        this.baselineTable = baselineTable;
    }

    public SequenceTable getSequenceTable() {
        return sequenceTable;
    }

    public void setSequenceTable(SequenceTable sequenceTable) {
        this.sequenceTable = sequenceTable;
    }
}