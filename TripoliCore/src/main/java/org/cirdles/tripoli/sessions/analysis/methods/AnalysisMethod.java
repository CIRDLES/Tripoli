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

import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.DetectorSetup;
import org.cirdles.tripoli.sessions.analysis.methods.baseline.BaselineTable;
import org.cirdles.tripoli.sessions.analysis.methods.machineMethods.phoenixMassSpec.PhoenixAnalysisMethod;
import org.cirdles.tripoli.sessions.analysis.methods.sequence.SequenceCell;
import org.cirdles.tripoli.sessions.analysis.methods.sequence.SequenceTable;
import org.cirdles.tripoli.species.IsotopicRatio;
import org.cirdles.tripoli.species.SpeciesRecordInterface;
import org.cirdles.tripoli.species.nuclides.NuclidesFactory;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

import static org.cirdles.tripoli.constants.ConstantsTripoliCore.SPACES_100;

/**
 * @author James F. Bowring
 */
public class AnalysisMethod implements Serializable {
    @Serial
    private static final long serialVersionUID = -642166785514147638L;

    private String methodName;
    private BaselineTable baselineTable;
    private SequenceTable sequenceTable;
    private List<SpeciesRecordInterface> speciesList;
    private List<IsotopicRatio> isotopicRatiosList;


    private AnalysisMethod(String methodName) {
        this(methodName, BaselineTable.createEmptyBaselineTable(), SequenceTable.createEmptySequenceTable());
    }

    private AnalysisMethod(String methodName,  BaselineTable baselineTable, SequenceTable sequenceTable) {
        this.methodName = methodName;
        speciesList = new ArrayList<>();
        this.baselineTable = baselineTable;
        this.sequenceTable = sequenceTable;
        isotopicRatiosList = new ArrayList<>();
    }

    public static AnalysisMethod initializeAnalysisMethod(String methodName) {
        return new AnalysisMethod(methodName);
    }

    public static AnalysisMethod createAnalysisMethodFromPhoenixAnalysisMethod(PhoenixAnalysisMethod phoenixAnalysisMethod, AnalysisInterface analysis) {
        AnalysisMethod analysisMethod = new AnalysisMethod(
                phoenixAnalysisMethod.getHEADER().getFilename());

        List<PhoenixAnalysisMethod.ONPEAK> onPeakSequences = phoenixAnalysisMethod.getONPEAK();
        analysisMethod.sequenceTable.setSequenceCount(onPeakSequences.size());
        DetectorSetup detectorSetup = analysis.getMassSpecExtractedData().getDetectorSetup();
        for (PhoenixAnalysisMethod.ONPEAK onpeakSequence : onPeakSequences) {
            String sequenceNumber = onpeakSequence.getSequence();
            // <CollectorArray>147Sm:H1S1,148Sm:H2S1,149Sm:H3S1,150Sm:H4S1</CollectorArray>
            String[] collectorArray = onpeakSequence.getCollectorArray()
                    .trim()
                    .replace("<CollectorArray>", "")
                    .replace("</CollectorArray>", "")
                    .split(",");
            for (String cellSpec : collectorArray) {
                String[] cellSpecs = cellSpec.split(":");

                int indexOfElementNameStart = cellSpecs[0].split("\\d\\D\\D")[0].length() + 1;
                int massNumber = Integer.parseInt(cellSpecs[0].substring(0, indexOfElementNameStart));
                String elementName = cellSpecs[0].substring(indexOfElementNameStart);
                SpeciesRecordInterface species = NuclidesFactory.retrieveSpecies(elementName, massNumber);
                analysisMethod.addSpeciesToSpeciesList(species);
                analysisMethod.sortSpeciesListByAbundance();
                analysisMethod.createBaseListOfRatios();

                String collectorName = cellSpecs[1].split("S")[0];
                Detector detector = detectorSetup.getMapOfDetectors().get(collectorName);
                SequenceCell sequenceCell = analysisMethod.sequenceTable.accessSequenceCellForDetector(detector, "OP" + sequenceNumber, Integer.parseInt(sequenceNumber));
                sequenceCell.addTargetSpecies(species);

                // TODO: baselines

            }

        }


        return analysisMethod;
    }

    private String prettyPrintSequenceTable() {
        StringBuilder retVal = new StringBuilder();
        Map<Detector, List<SequenceCell>> detectorToSequenceCell = sequenceTable.getMapOfDetectorsToSequenceCells();
        detectorToSequenceCell.entrySet().stream()
                .forEach(e -> {
                    retVal.append(e.getKey().getDetectorName()).append(" ");
                    boolean offset = false;
                    for (SequenceCell sequenceCell : e.getValue()) {
                        int sequenceNumber = sequenceCell.getSequenceIndex();
                        if (!offset) {
                            retVal.append(SPACES_100, 0, (sequenceNumber - 1) * 10);
                            offset = true;
                        }
                        retVal.append(sequenceCell.getSequenceName()).append(":").append(sequenceCell.getTargetSpecies().prettyPrintShortForm()).append(" ");
                    }

                    retVal.append("\n");
                });

        return retVal.toString();
    }

    public String prettyPrintMethodSummary() {
        StringBuilder retVal = new StringBuilder();
        retVal.append("Method: ").append(methodName).append(SPACES_100, 0, 40 - methodName.length()).append("  Species: ");
        for (SpeciesRecordInterface species : speciesList) {
            retVal.append(species.prettyPrintShortForm() + " ");
        }

        return retVal.toString();
    }

    @Override
    public boolean equals(Object otherObject) {
        boolean retVal = true;
        if (otherObject != this) {
            if (otherObject instanceof AnalysisMethod otherAnalysisMethod) {
                retVal = 0 == methodName.compareToIgnoreCase(otherAnalysisMethod.methodName);
            } else {
                retVal = false;
            }
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (null == methodName ? 0 : methodName.hashCode());
        return hash;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<SpeciesRecordInterface> getSpeciesList() {
        return speciesList;
    }

    public void setSpeciesList(List<SpeciesRecordInterface> speciesList) {
        this.speciesList = speciesList;
    }

    public void addSpeciesToSpeciesList(SpeciesRecordInterface species) {
        if (null == speciesList) {
            speciesList = new ArrayList<>();
        }
        if (!speciesList.contains(species)) {
            speciesList.add(species);
        }
    }

    public void sortSpeciesListByAbundance() {
        Collections.sort(speciesList, Comparator.comparing(s -> s.getNaturalAbundancePercent()));
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

    public List<IsotopicRatio> getTripoliRatiosList() {
        return isotopicRatiosList;
    }

    public void setTripoliRatiosList(List<IsotopicRatio> isotopicRatiosList) {
        this.isotopicRatiosList = isotopicRatiosList;
    }

    public void addRatioToIsotopicRatiosList(IsotopicRatio isotopicRatio) {
        if (null == isotopicRatiosList) {
            isotopicRatiosList = new ArrayList<>();
        }
        if (!isotopicRatiosList.contains(isotopicRatio)) {
            isotopicRatiosList.add(isotopicRatio);
        }
    }

    /**
     * Creates a ratio of each species except the last one divided by the last one in specieslist
     */
    public void createBaseListOfRatios() {
        isotopicRatiosList = new ArrayList<>();
        SpeciesRecordInterface highestAbundanceSpecies = speciesList.get(speciesList.size() - 1);
        for (int speciesIndex = 0; speciesIndex < speciesList.size() - 1; speciesIndex++) {
            addRatioToIsotopicRatiosList(new IsotopicRatio(speciesList.get(speciesIndex), highestAbundanceSpecies));
        }
    }

}