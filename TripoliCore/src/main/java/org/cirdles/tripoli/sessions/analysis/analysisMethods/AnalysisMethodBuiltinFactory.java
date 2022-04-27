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

package org.cirdles.tripoli.sessions.analysis.analysisMethods;

import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.MassSpectrometerBuiltinModelFactory;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.DetectorSetup;
import org.cirdles.tripoli.sessions.analysis.analysisMethods.baselineTables.BaselineCell;
import org.cirdles.tripoli.sessions.analysis.analysisMethods.baselineTables.BaselineTable;
import org.cirdles.tripoli.sessions.analysis.analysisMethods.sequenceTables.SequenceCell;
import org.cirdles.tripoli.sessions.analysis.analysisMethods.sequenceTables.SequenceTable;
import org.cirdles.tripoli.species.SpeciesRecordInterface;
import org.cirdles.tripoli.species.nuclides.NuclidesFactory;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author James F. Bowring
 */
public final class AnalysisMethodBuiltinFactory implements Serializable {

    public static final Map<String, AnalysisMethod> analysisMethodsBuiltinMap = new LinkedHashMap<>();

    static {
        AnalysisMethod burdickBlSyntheticData = AnalysisMethod.initializeAnalysisMethod(
                "BurdickBlSyntheticData",
                MassSpectrometerBuiltinModelFactory.massSpectrometersBuiltinMap.get("OP_Phoenix"));
        analysisMethodsBuiltinMap.put(burdickBlSyntheticData.getMethodName(), burdickBlSyntheticData);

        burdickBlSyntheticData.getSpeciesList().add(NuclidesFactory.retrieveSpecies("Pb", 206));
        burdickBlSyntheticData.getSpeciesList().add(NuclidesFactory.retrieveSpecies("Pb", 208));

        DetectorSetup detectorSetup = burdickBlSyntheticData.getMassSpectrometer().getDetectorSetup();

        BaselineCell baselineCell = burdickBlSyntheticData.getBaselineTable().accessBaselineCellForDetector(detectorSetup.getMapOfDetectors().get("Ax_Fara"), "Bl1");
        baselineCell.setCellMass(203.5);

        baselineCell = burdickBlSyntheticData.getBaselineTable().accessBaselineCellForDetector(detectorSetup.getMapOfDetectors().get("Axial"), "Bl1");
        baselineCell.setCellMass(205.5);

        baselineCell = burdickBlSyntheticData.getBaselineTable().accessBaselineCellForDetector(detectorSetup.getMapOfDetectors().get("H1"), "Bl1");
        baselineCell.setCellMass(207.5);

        SequenceCell sequenceCell = burdickBlSyntheticData.getSequenceTable().accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Ax_Fara"), "S2");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 206));

        sequenceCell = burdickBlSyntheticData.getSequenceTable().accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Axial"), "S1");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 206));
        sequenceCell = burdickBlSyntheticData.getSequenceTable().accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Axial"), "S2");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 208));

        sequenceCell = burdickBlSyntheticData.getSequenceTable().accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H1"), "S1");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 208));
    }

    static {
        AnalysisMethod ku_204_5_6_7_8_Daly_AllFaradayPb = AnalysisMethod.initializeAnalysisMethod(
                "KU_204_5_6_7_8_Daly_AllFaradayPb",
                MassSpectrometerBuiltinModelFactory.massSpectrometersBuiltinMap.get("OP_Phoenix"));
        analysisMethodsBuiltinMap.put(ku_204_5_6_7_8_Daly_AllFaradayPb.getMethodName(), ku_204_5_6_7_8_Daly_AllFaradayPb);

        SpeciesRecordInterface pb204 = NuclidesFactory.retrieveSpecies("Pb", 204);
        SpeciesRecordInterface pb205 = NuclidesFactory.retrieveSpecies("Pb", 205);
        SpeciesRecordInterface pb206 = NuclidesFactory.retrieveSpecies("Pb", 206);

        ku_204_5_6_7_8_Daly_AllFaradayPb.getSpeciesList().add(pb204);
        ku_204_5_6_7_8_Daly_AllFaradayPb.getSpeciesList().add(pb205);
        ku_204_5_6_7_8_Daly_AllFaradayPb.getSpeciesList().add(pb206);
        ku_204_5_6_7_8_Daly_AllFaradayPb.getSpeciesList().add(NuclidesFactory.retrieveSpecies("Pb", 207));
        ku_204_5_6_7_8_Daly_AllFaradayPb.getSpeciesList().add(NuclidesFactory.retrieveSpecies("Pb", 208));

        DetectorSetup detectorSetup = ku_204_5_6_7_8_Daly_AllFaradayPb.getMassSpectrometer().getDetectorSetup();
        BaselineTable baselineTable = ku_204_5_6_7_8_Daly_AllFaradayPb.getBaselineTable();

        BaselineCell baselineCell = baselineTable.accessBaselineCellForDetector(detectorSetup.getMapOfDetectors().get("Axial"), "Bl1");
        baselineCell.setCellMass(203.5);

        baselineCell = baselineTable.accessBaselineCellForDetector(detectorSetup.getMapOfDetectors().get("H1"), "Bl1");
        baselineCell.setCellMass(204.5);

        baselineCell = baselineTable.accessBaselineCellForDetector(detectorSetup.getMapOfDetectors().get("H2"), "Bl1");
        baselineCell.setCellMass(205.5);

        baselineCell = baselineTable.accessBaselineCellForDetector(detectorSetup.getMapOfDetectors().get("H3"), "Bl1");
        baselineCell.setCellMass(206.5);

        baselineCell = baselineTable.accessBaselineCellForDetector(detectorSetup.getMapOfDetectors().get("H4"), "Bl1");
        baselineCell.setCellMass(207.5);

        SequenceTable sequenceTable = ku_204_5_6_7_8_Daly_AllFaradayPb.getSequenceTable();

        SequenceCell sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("L4"), "S5");
        sequenceCell.addTargetSpecies(pb204);

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("L3"), "S4");
        sequenceCell.addTargetSpecies(pb204);
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("L3"), "S5");
        sequenceCell.addTargetSpecies(pb205);

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("L2"), "S3");
        sequenceCell.addTargetSpecies(pb204);
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("L2"), "S4");
        sequenceCell.addTargetSpecies(pb205);
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("L2"), "S5");
        sequenceCell.addTargetSpecies(pb206);

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Ax_Fara"), "S2");
        sequenceCell.addTargetSpecies(pb204);
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Ax_Fara"), "S3");
        sequenceCell.addTargetSpecies(pb205);
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Ax_Fara"), "S4");
        sequenceCell.addTargetSpecies(pb206);
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Ax_Fara"), "S5");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 207));

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Axial"), "S1");
        sequenceCell.addTargetSpecies(pb204);
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Axial"), "S2");
        sequenceCell.addTargetSpecies(pb205);
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Axial"), "S3");
        sequenceCell.addTargetSpecies(pb206);
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Axial"), "S4");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 207));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Axial"), "S5");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 208));

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H1"), "S1");
        sequenceCell.addTargetSpecies(pb205);
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H1"), "S2");
        sequenceCell.addTargetSpecies(pb206);
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H1"), "S3");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 207));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H1"), "S4");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 208));

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H2"), "S1");
        sequenceCell.addTargetSpecies(pb206);
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H2"), "S2");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 207));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H2"), "S3");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 208));

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H3"), "S1");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 207));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H3"), "S2");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 208));

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H4"), "S1");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 208));
    }

    static {
        AnalysisMethod ku_PbDaly204_5_6_7_8 = AnalysisMethod.initializeAnalysisMethod(
                "KU_PbDaly204_5_6_7_8",
                MassSpectrometerBuiltinModelFactory.massSpectrometersBuiltinMap.get("OP_Phoenix"));
        analysisMethodsBuiltinMap.put(ku_PbDaly204_5_6_7_8.getMethodName(), ku_PbDaly204_5_6_7_8);
    }


    static {
        AnalysisMethod ku_UoxideStaticFaraday = AnalysisMethod.initializeAnalysisMethod(
                "KU_UoxideStaticFaraday",
                MassSpectrometerBuiltinModelFactory.massSpectrometersBuiltinMap.get("OP_Phoenix"));
        analysisMethodsBuiltinMap.put(ku_UoxideStaticFaraday.getMethodName(), ku_UoxideStaticFaraday);
    }

    static {
        AnalysisMethod garconDynNd1 = AnalysisMethod.initializeAnalysisMethod(
                "GarconDynNd1",
                MassSpectrometerBuiltinModelFactory.massSpectrometersBuiltinMap.get("OP_Triton"));
        analysisMethodsBuiltinMap.put(garconDynNd1.getMethodName(), garconDynNd1);
    }

    static {
        AnalysisMethod garconDynNd2 = AnalysisMethod.initializeAnalysisMethod(
                "GarconDynNd2",
                MassSpectrometerBuiltinModelFactory.massSpectrometersBuiltinMap.get("OP_Triton"));
        analysisMethodsBuiltinMap.put(garconDynNd2.getMethodName(), garconDynNd2);
    }
}