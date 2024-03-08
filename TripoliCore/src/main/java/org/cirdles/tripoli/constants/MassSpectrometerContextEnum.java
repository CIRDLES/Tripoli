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

package org.cirdles.tripoli.constants;

import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.nu.NuMassSpec;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.phoenix.PhoenixMassSpec;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.triton.TritonMassSpec;

import java.util.Arrays;
import java.util.List;

/**
 * @author James F. Bowring
 */
public enum MassSpectrometerContextEnum {
    PHOENIX_FULL("Phoenix Full", "Phoenix",
            "#HEADER, Analysis, Version, Filename, MethodName, MethodPath, IsoWorksMethod, FolderPath",
            PhoenixMassSpec.class, "extractMetaAndBlockDataFromFileVersion_1_2", 4),
    PHOENIX_TIMSDP_CASE1("Phoenix TIMSDP", "Phoenix",
            "#HEADER, Analysis, Version, Filename, MethodName, MethodPath, FolderPath, IsoWorksMethod, TFEMode, TFEMonitorPeak",
            PhoenixMassSpec.class, "extractDataFromFileVersion_2_TIMSDP", 1),
    PHOENIX_IONVANTAGE_XLS("Phoenix IonVantage", "Phoenix",
            "CYCLE Data",
            PhoenixMassSpec.class, "extractMetaDataAndBlockDataFromIonvantageXLS", 1),
    PHOENIX_FULL_SYNTHETIC("Phoenix Full_Synthetic", "Phoenix",
            "Version, Filename, Sample, Sample, Sample, Analysis, User, Method",
            PhoenixMassSpec.class, "extractMetaAndBlockDataFromFileVersion_1_0", 4),
    TRITON_EXP("Triton EXP", "Triton",
            "Triton Analysis Data Report",
            TritonMassSpec.class, "extractMetaAndBlockDataFromFileTriton", 1),
    NUTIMS_TXT_CASE1("Nu TIMS", "NU",
            "Version, Sample, Analysis, Number of measurements, Total, Number of answers, Caption",
            NuMassSpec.class, "extractMetaAndBlockDataFromFileNu", 1),
    UNKNOWN("UNKNOWN", "UNKNOWN", "",
            null, "", 0);

    private final String name;
    private final String massSpectrometerName;
    private final String keyWordsList;
    private final Class<?> clazz;
    private final String methodName;
    private int caseNumber;

    MassSpectrometerContextEnum(String name, String massSpectrometerName, String keyWordsList, Class<?> clazz, String methodName, int caseNumber) {
        this.name = name;
        this.massSpectrometerName = massSpectrometerName;
        this.keyWordsList = keyWordsList;
        this.clazz = clazz;
        this.methodName = methodName;
        this.caseNumber = caseNumber;
    }

    public String getName() {
        return name;
    }

    public String getMassSpectrometerName() {
        return massSpectrometerName;
    }

    public List<String> getKeyWordsList() {
        return Arrays.asList(keyWordsList.split(","));
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String getMethodName() {
        return methodName;
    }

    public int getCaseNumber() {
        return caseNumber;
    }
}