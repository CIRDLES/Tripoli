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

import org.cirdles.tripoli.expressions.species.SpeciesRecordInterface;
import org.cirdles.tripoli.expressions.species.nuclides.NuclidesFactory;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.DetectorSetup;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.DetectorSetupBuiltinModelFactory;
import org.cirdles.tripoli.sessions.analysis.methods.baseline.BaselineCell;
import org.cirdles.tripoli.sessions.analysis.methods.baseline.BaselineTable;
import org.cirdles.tripoli.sessions.analysis.methods.sequence.SequenceCell;
import org.cirdles.tripoli.sessions.analysis.methods.sequence.SequenceTable;
import org.jetbrains.annotations.NonNls;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.cirdles.tripoli.constants.MassSpectrometerContextEnum.PHOENIX_FULL_SYNTHETIC;

/**
 * @author James F. Bowring
 */
public final class AnalysisMethodBuiltinFactory implements Serializable {

    public static final SpeciesRecordInterface pb204 = NuclidesFactory.retrieveSpecies("Pb", 204);
    public static final SpeciesRecordInterface pb205 = NuclidesFactory.retrieveSpecies("Pb", 205);
    public static final SpeciesRecordInterface pb206 = NuclidesFactory.retrieveSpecies("Pb", 206);
    public static final SpeciesRecordInterface pb207 = NuclidesFactory.retrieveSpecies("Pb", 207);
    public static final SpeciesRecordInterface pb208 = NuclidesFactory.retrieveSpecies("Pb", 208);
    @NonNls
    public static final String BURDICK_BL_SYNTHETIC_DATA = "BurdickBlSyntheticData";
    @NonNls
    public static final String KU_204_5_6_7_8_DALY_ALL_FARADAY_PB = "KU_204_5_6_7_8_Daly_AllFaradayPb";
    public static Map<String, AnalysisMethod> analysisMethodsBuiltinMap = new TreeMap<>();

    static {
        AnalysisMethod burdickBlSyntheticData = AnalysisMethod.initializeAnalysisMethod(BURDICK_BL_SYNTHETIC_DATA, PHOENIX_FULL_SYNTHETIC);
        analysisMethodsBuiltinMap.put(burdickBlSyntheticData.getMethodName(), burdickBlSyntheticData);

        burdickBlSyntheticData.addSpeciesToSpeciesList(pb206);
        burdickBlSyntheticData.addSpeciesToSpeciesList(pb208);

        burdickBlSyntheticData.createListsOfIsotopicRatios();

        DetectorSetup detectorSetup = DetectorSetupBuiltinModelFactory.detectorSetupBuiltinMap.get(PHOENIX_FULL_SYNTHETIC.getName());

        BaselineTable baselineTable = burdickBlSyntheticData.getBaselineTable();
        baselineTable.setSequenceCount(1);

        BaselineCell baselineCell = burdickBlSyntheticData.getBaselineTable().accessBaselineCellForDetector(detectorSetup.getMapOfDetectors().get("Ax"), "BL1", 1);
        baselineCell.setCellMass(203.5);

        baselineCell = burdickBlSyntheticData.getBaselineTable().accessBaselineCellForDetector(detectorSetup.getMapOfDetectors().get("PM"), "BL1", 1);
        baselineCell.setCellMass(205.5);

        baselineCell = burdickBlSyntheticData.getBaselineTable().accessBaselineCellForDetector(detectorSetup.getMapOfDetectors().get("H1"), "BL1", 1);
        baselineCell.setCellMass(207.5);

        SequenceTable sequenceTable = burdickBlSyntheticData.getSequenceTable();
        sequenceTable.setSequenceCount(2);

        SequenceCell sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Ax"), "S2", 2, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb206);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("PM"), "S1", 1, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb206);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("PM"), "S2", 2, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb208);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H1"), "S1", 1, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb208);
    }

    static {
        AnalysisMethod ku_204_5_6_7_8_Daly_AllFaradayPb = AnalysisMethod.initializeAnalysisMethod(KU_204_5_6_7_8_DALY_ALL_FARADAY_PB, PHOENIX_FULL_SYNTHETIC);
        analysisMethodsBuiltinMap.put(ku_204_5_6_7_8_Daly_AllFaradayPb.getMethodName(), ku_204_5_6_7_8_Daly_AllFaradayPb);

        ku_204_5_6_7_8_Daly_AllFaradayPb.addSpeciesToSpeciesList(pb204);
        ku_204_5_6_7_8_Daly_AllFaradayPb.addSpeciesToSpeciesList(pb205);
        ku_204_5_6_7_8_Daly_AllFaradayPb.addSpeciesToSpeciesList(pb206);
        ku_204_5_6_7_8_Daly_AllFaradayPb.addSpeciesToSpeciesList(pb207);
        ku_204_5_6_7_8_Daly_AllFaradayPb.addSpeciesToSpeciesList(pb208);

        ku_204_5_6_7_8_Daly_AllFaradayPb.createListsOfIsotopicRatios();

        DetectorSetup detectorSetup = DetectorSetupBuiltinModelFactory.detectorSetupBuiltinMap.get(PHOENIX_FULL_SYNTHETIC.getName());
        BaselineTable baselineTable = ku_204_5_6_7_8_Daly_AllFaradayPb.getBaselineTable();
        baselineTable.setSequenceCount(1);

        BaselineCell baselineCell = baselineTable.accessBaselineCellForDetector(detectorSetup.getMapOfDetectors().get("L4"), "BL1", 1);
        baselineCell.setCellMass(199.5);

        baselineCell = baselineTable.accessBaselineCellForDetector(detectorSetup.getMapOfDetectors().get("L3"), "BL1", 1);
        baselineCell.setCellMass(200.5);

        baselineCell = baselineTable.accessBaselineCellForDetector(detectorSetup.getMapOfDetectors().get("L2"), "BL1", 1);
        baselineCell.setCellMass(201.5);

        baselineCell = baselineTable.accessBaselineCellForDetector(detectorSetup.getMapOfDetectors().get("Ax"), "BL1", 1);
        baselineCell.setCellMass(202.5);


        baselineCell = baselineTable.accessBaselineCellForDetector(detectorSetup.getMapOfDetectors().get("PM"), "BL1", 1);
        baselineCell.setCellMass(203.5);

        baselineCell = baselineTable.accessBaselineCellForDetector(detectorSetup.getMapOfDetectors().get("H1"), "BL1", 1);
        baselineCell.setCellMass(204.5);

        baselineCell = baselineTable.accessBaselineCellForDetector(detectorSetup.getMapOfDetectors().get("H2"), "BL1", 1);
        baselineCell.setCellMass(205.5);

        baselineCell = baselineTable.accessBaselineCellForDetector(detectorSetup.getMapOfDetectors().get("H3"), "BL1", 1);
        baselineCell.setCellMass(206.5);

        baselineCell = baselineTable.accessBaselineCellForDetector(detectorSetup.getMapOfDetectors().get("H4"), "BL1", 1);
        baselineCell.setCellMass(207.5);

        SequenceTable sequenceTable = ku_204_5_6_7_8_Daly_AllFaradayPb.getSequenceTable();
        sequenceTable.setSequenceCount(5);

        SequenceCell sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("L4"), "S5", 5, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb204);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("L3"), "S4", 4, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb204);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("L3"), "S5", 5, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb205);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("L2"), "S3", 3, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb204);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("L2"), "S4", 4, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb205);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("L2"), "S5", 5, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb206);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Ax"), "S2", 2, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb204);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Ax"), "S3", 3, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb205);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Ax"), "S4", 4, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb206);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Ax"), "S5", 5, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb207);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("PM"), "S1", 1, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb204);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("PM"), "S2", 2, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb205);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("PM"), "S3", 3, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb206);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("PM"), "S4", 4, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb207);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("PM"), "S5", 5, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb208);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H1"), "S1", 1, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb205);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H1"), "S2", 2, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb206);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H1"), "S3", 3, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb207);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H1"), "S4", 4, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb208);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H2"), "S1", 1, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb206);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H2"), "S2", 2, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb207);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H2"), "S3", 3, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb208);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H3"), "S1", 1, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb207);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H3"), "S2", 2, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb208);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H4"), "S1", 1, new ArrayList<>());
        sequenceCell.addTargetSpecies(pb208);
        sequenceCell.setBaselineReferences(new ArrayList<>(List.of("BL1")));
    }
}